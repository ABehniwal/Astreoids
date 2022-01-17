import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.io.*;
import java.util.Scanner;
import java.util.Random;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioSystem;

class GamePanel extends JPanel implements KeyListener, ActionListener, MouseListener{

	public static final int INTRO=0, GAME=1;
	private int level = -1;
	private int highScore;
	private int score = 0;
	private int respawningPlayerTime = 0;
	private static final int PLAYERSPAWNTIME = 50;

	// alienSpawnTime keeps track of how long it has been since a saucer came on screen 
	// so that there is a delay between the appearance of saucers
	private int alienSpawnTime = 0;
	// alienBulletTime helps keep track of how long ago an alien saucer shot a bullet (so that there is a delay between bullets)
	private int alienBulletTime = 0;
	// Helps us randomly determine which size saucer will appear on the screen
	private static final int SAUCERCHANCE = 6;

	// Arraylists containing all the objects that move (player bullets, enemy bullets, saucers, asteroids)
    private ArrayList<Bullets> bullets = new ArrayList<Bullets>(0);
	private ArrayList<Bullets> enemyBullets = new ArrayList<Bullets>(0);
	private ArrayList<Saucer> saucers = new ArrayList<Saucer>(0);
	public ArrayList<Asteroid> asteroids = new ArrayList<Asteroid>(0);

	private int screen = INTRO;
    private boolean shootBullet = true;
	public boolean shipFire = false;
    public static int width;
    public static int height;
	private int lives = 3;
	// If the player dies and has enough lives/ships to restart, 
	// we do not display or count any collisions against the player until its safe to display him in the middle
	private boolean restarting = false;
	
	private boolean []keys;
	Timer timer;
    
    
	private Image back, startButtonImage, title;
	private SoundEffect smallExplosion, mediumExplosion, largeExplosion, thrustSound, fireSound, smallSaucer, bigSaucer;
	private Font hyperspaceFont = new Font("Hyperspace", Font.PLAIN, 18);
	private Random rand = new Random();
	Player player;
	
	public GamePanel() {

		// Loads the highScore from the highScore.txt file
		loadScore();

		// Initializing Images
		back = new ImageIcon("OuterSpace.jpg").getImage();
        startButtonImage = new ImageIcon("StartButtonImage.png").getImage();
        title = new ImageIcon("asteroidsTitle.png").getImage();

		// Initializing sounds
		smallExplosion = new SoundEffect("bangSmall.wav");
		mediumExplosion = new SoundEffect("bangMedium.wav");
		largeExplosion = new SoundEffect("bangLarge.wav");
		thrustSound = new SoundEffect("thrust.wav");
		fireSound = new SoundEffect("fire.wav");
		smallSaucer = new SoundEffect("saucerSmall.wav");
		bigSaucer = new SoundEffect("saucerBig.wav");

		keys = new boolean[KeyEvent.KEY_LAST+1];
		player = new Player(this);

		setPreferredSize(new Dimension(800, 600));
        width = 800;
        height = 600;
		timer = new Timer(20, this);
		timer.start();
		setFocusable(true);
		requestFocus();
		addKeyListener(this);
		addMouseListener(this);
	}

    @Override
	public void actionPerformed(ActionEvent e){

		// If all the asteroids have been destroyed, we start the next level
		if (asteroids.size() == 0) {
			level += 1;

			// Generating the number of asteroids corresponding to the level
			for (int i = 0; i < level + 5; i++) {
				asteroids.add(new Asteroid(8, Asteroid.BIG, Asteroid.BIGSPEED));
			}
		}

		if (keys[KeyEvent.VK_UP] && screen == GAME) {
			// Playing the thrust sound if the player moves forward
			thrustSound.loop();
		}

		else {
			thrustSound.stop();
		}
		
		// If the player is not in restarting mode and we are in the game screen, we move the player
		if (! restarting && screen == GAME) {
			player.move(keys);
		}

		// At a certain point, the saucer will keep appearing every time the alienSpawnTime hits 600 or above 
		// only the small saucer will appear at this point
		if (1000 - 100 * level <= 700 && alienSpawnTime >= 600) {
			saucers.add(new Saucer(this, Saucer.SMALL, alienBulletTime));
			smallSaucer.loop();
			alienSpawnTime = 0;
		}

		// Otherwise we keep reducing the time it takes for it to appear based on level
		else if (alienSpawnTime >= 1000 - 100 * level && (1000 - 100 * level) > 700) {
			int num1 = rand.nextInt(SAUCERCHANCE - level);
			int num2 = rand.nextInt(SAUCERCHANCE - level);

			// If the two numbers are equal (lower change) then draw a small saucer
			if (num1 == num2) {
				saucers.add(new Saucer(this, Saucer.SMALL, alienBulletTime));
				smallSaucer.loop();
			}
			else {
				saucers.add(new Saucer(this, Saucer.BIG, alienBulletTime));
				bigSaucer.loop();
			}

			alienSpawnTime = 0;
		}

		// Moving saucers and removing them if they go off the screen from the left or right side
		for (int i = saucers.size() - 1; i >= 0; i--) {

			// If the saucer is still on screen, move it
			if (saucers.get(i).onScreen()) {
				saucers.get(i).move();
			}
			
			// Otherwise remove it
			else {
				saucers.remove(i);
				// There can only be one saucer on the screen so we can just stop the sound for both
				smallSaucer.stop();
				bigSaucer.stop();
			}
		}

		// Firing bullets from the saucer if it can
		for (Saucer saucer : saucers) {
			if (saucer.canShoot(alienBulletTime)) {
				enemyBullets.add(new Bullets(saucer.centerX(), saucer.centerY(), saucer.bulletAngle(player.centerX(), player.centerY())));
			}
		}

		// Moving bullets
		for (Bullets bullet : bullets) {
			bullet.move();
		}

		// Moving enemy bullets
		for (Bullets enemyBullet: enemyBullets) {
			enemyBullet.move();
		}
	
		// Moving asteroids
		for (Asteroid asteroid : asteroids) {
			asteroid.move();
		}

		// Checking for collisions between bullets and asteroids

		for (int i = bullets.size() - 1; i >= 0; i--) {

			for (int j = asteroids.size() - 1; j >= 0; j--) {
				if (asteroids.get(j).asteroidObject.contains((int) bullets.get(i).bulletX(), (int) bullets.get(i).bulletY())) {

					splitAsteroid(asteroids.get(j));

					asteroids.remove(j);
					bullets.remove(i);
					break;
				}
			}
		}

		// Checking for collisions between bullets and saucers

		for (int i = bullets.size() - 1; i >= 0; i--) {
			for (int k = saucers.size() - 1; k >= 0; k--) {

				if (saucers.get(k).containsBullet(bullets.get(i).bulletX(), bullets.get(i).bulletY())) {

					// Playing the saucer explosion sound based off of its size
					if (saucers.get(k).magnitude() == Saucer.SMALL) {
						smallSaucer.stop();
						score += Saucer.SMALLHIT;
					}

					else {
						bigSaucer.stop();
						score += Saucer.BIGHIT;
					}
					
					saucers.remove(k);
					bullets.remove(i);
					break;
				}
			}
		}

		// We make the bullet dissapear if it collides with the player or if it has reached its maximum distance of the width of the screen
		for (int i = bullets.size() - 1; i >= 0; i--) {
			if (player.person.contains(bullets.get(i).bulletX(), bullets.get(i).bulletY())) {
				bullets.remove(i);
			}

			else if (bullets.get(i).maxDist()) {
				bullets.remove(i);
			}
		}

		// We check for collisions between enemy bullets and asteroids/player or if it has travelled its maximum distance
		for (int i = enemyBullets.size() - 1; i >= 0; i--) {

			// Checking if the bullet has reached maximum distance
			if (enemyBullets.get(i).maxDist()) {
				enemyBullets.remove(i);
			}

			// Checking if the bullet hit the player
			else if (player.person.contains(enemyBullets.get(i).bulletX(), enemyBullets.get(i).bulletY())) {
				if (screen == GAME && ! restarting) {
					enemyBullets.remove(i);

					// Set the mode of player to restarting
					restarting = true;
					lives -= 1;
				}
			}

			// Checking if the bullet hit any of the asteroids
			else {
				for (int j = asteroids.size() - 1; j >= 0; j--) {
					if (asteroids.get(j).asteroidObject.contains(enemyBullets.get(i).bulletX(), enemyBullets.get(i).bulletY())) {
						enemyBullets.remove(i);
						splitAsteroid(asteroids.get(j));
						asteroids.remove(j);
						break;
					}
				}
			}
		}

		// Checking for collisions between asteroids and the player
		for (int i = asteroids.size() - 1; i >= 0; i--) {
			// Player
			for (int j = 0; j < player.person.npoints; j++) {
				// If any point of the player is contained inside the asteroid object, this means that they two objects are overlapping
				if (asteroids.get(i).asteroidObject.contains(player.person.xpoints[j], player.person.ypoints[j]) && ! restarting) {
					// We split the asteroid that was hit
					splitAsteroid(asteroids.get(i));
					asteroids.remove(i);
					restarting = true;
					lives -= 1;
					break;
				}
			}
		}

		if (screen == GAME) {

			// Checking for collisions between saucers and the player

			for (int i = saucers.size() - 1; i >= 0; i--) {
				if (player.person.intersects(saucers.get(i).hitArea()) && ! restarting) {
					// Playing the saucer explosion sound based off of its size and adding the points
					if (saucers.get(i).magnitude() == Saucer.SMALL) {
						smallSaucer.stop();
						score += Saucer.SMALLHIT;
					}

					else {
						bigSaucer.stop();
						score += Saucer.BIGHIT;
					}

					saucers.remove(i);
					restarting = true;
					lives -= 1;

				}
			}

			// Checking for collisions between asteroids and saucers
			for (int i = asteroids.size() - 1; i >= 0; i--) {
				for (int j = saucers.size() - 1; j >= 0; j--) {
					if (asteroids.get(i).asteroidObject.intersects(saucers.get(j).hitArea())) {
						// Splitting the asteroid into two smaler ones and removing the original
						splitAsteroid(asteroids.get(i));
						asteroids.remove(i);
						// Playing the saucer explosion sound based off of its size
						if (saucers.get(j).magnitude() == Saucer.SMALL) {
							smallSaucer.stop();
						}

						else {
							bigSaucer.stop();
						}

						saucers.remove(j);
						break;
					}
				}
			}

			// If the player is in restarting mode, we keep adding 1 to their spawning time
			if (restarting) {
				respawningPlayerTime += 1;
				// Stopping the thrust sound if the player died while thrusting
				thrustSound.stop();
			}

			// Spawning the player in the middle of the screen when it is safe to do so 
			// and the minimum amount of time before respawning has been reached
			if (lives > 0 && restarting && respawningPlayerTime >= PLAYERSPAWNTIME) {
				if (player.initiateLocation(400,300)) {
					// Taking away all saucers and their bullets
					saucers.clear();
					// Stopping the saucer noises in case they are still going
					smallSaucer.stop();
					bigSaucer.stop();
					enemyBullets.clear();
					// Putting the player back in the middle
					player = new Player(this);
					restarting = false;
					respawningPlayerTime = 0;
					// We reset the keys
					keys = new boolean[KeyEvent.KEY_LAST+1];
				}
			}
		}

		if (lives <= 0) {
			screen = INTRO;
			// We clear the asteroids and bullets arraylists
			asteroids.clear();
			bullets.clear();
			enemyBullets.clear();
			saucers.clear();
			smallSaucer.stop();
			bigSaucer.stop();
			// We reset the keys
			keys = new boolean[KeyEvent.KEY_LAST+1];
			// Initializing the player in the middle of the screen
			player = new Player(this);
			respawningPlayerTime = PLAYERSPAWNTIME;
			level = -1;
			lives = 3;
			// updating the high score
			updateScore();
			score = 0;
		}

		alienBulletTime += 1;

		// We only start alienSpawnTime when there are no saucers on the screen
		// so that there are not multiple saucers on the screen
		if (saucers.size() == 0) {
			alienSpawnTime += 1;
		}

		repaint();
		
	}

	private void splitAsteroid(Asteroid asteroid) {

		// If a small asteroid was hit, it does not split into more asteroids
		if (asteroid.magnitude == Asteroid.SMALL) {

			score += Asteroid.SMALLHIT;
			// Playing the explosion sound of a small asteroid
			smallExplosion.play();
			return;
		}

		// If a big or medium sized asteroid was hit, we split the asteroid into two smaller asteroids.
		if (asteroid.magnitude == Asteroid.BIG) {

			score += Asteroid.BIGHIT;

			// Playing the explosion sound of a large asteroid splitting
			largeExplosion.play();

			// Creating two new medium sized asteroids
			asteroids.add(new Asteroid(8, Asteroid.MEDIUM, Asteroid.MEDIUMSPEED));
			asteroids.add(new Asteroid(8, Asteroid.MEDIUM, Asteroid.MEDIUMSPEED));
		}

		else if (asteroid.magnitude == Asteroid.MEDIUM) {

			score += Asteroid.MEDIUMHIT;

			// Playing the explosion sound of a medium asteroid splitting
			mediumExplosion.play();

			// Creating two new small sized asteroids
			asteroids.add(new Asteroid(8, Asteroid.SMALL, Asteroid.SMALLSPEED));
			asteroids.add(new Asteroid(8, Asteroid.SMALL, Asteroid.SMALLSPEED));
		}

		// Setting the centers of the two new asteroids to the center of the asteroid that they split from
		asteroids.get(asteroids.size() - 1).centerX = asteroid.centerX;
		asteroids.get(asteroids.size() - 1).centerY = asteroid.centerY;
		asteroids.get(asteroids.size() - 2).centerX = asteroid.centerX;
		asteroids.get(asteroids.size() - 2).centerY = asteroid.centerY;

		// Sending the split asteroids in separate directions by changing ones angle
		asteroids.get(asteroids.size() - 1).angle = asteroids.get(asteroids.size() - 2).angle + 180;

	}


    @Override
	public void keyReleased(KeyEvent ke){

		if (screen == GAME && ! restarting) {

			int key = ke.getKeyCode();
			keys[key] = false;
			
			// Since we don't want the player to be able to hold the space key and continuously shoot we only let them shoot
			// again after they have released the space key
			if (key == KeyEvent.VK_SPACE) {
				shootBullet = true;
			}

			if (key == KeyEvent.VK_UP) {
				// If the player is no longer accelerating, we dont show the fire behind the player ship
				shipFire = false;
			}

			if (key == KeyEvent.VK_DOWN) {
				// The player can only hyperspace once at a time
				player.hyperSpace = true;
			}
		}

	}	
	
	@Override
	public void keyPressed(KeyEvent ke){

		if (screen == GAME && ! restarting) {

			int key = ke.getKeyCode();
			keys[key] = true;

			// Shoot if the space key was pressed, shootBullet is true, and we have less than the max number of bullets on the screen
			if(key == KeyEvent.VK_SPACE && shootBullet && bullets.size() < Bullets.MAXBULLETS) {

				// Playing the sound of a bullet firing
				fireSound.play();

				Bullets bullet = new Bullets(player.person.xpoints[0], player.person.ypoints[0], player.angle);
				bullets.add(bullet);
				// The player recoils a bit whenever they shoot
				player.recoil();
				shootBullet = false;
			}
		}
	}
	
	@Override
	public void keyTyped(KeyEvent ke){}

	@Override
	public void	mouseClicked(MouseEvent e){}

	@Override
	public void	mouseEntered(MouseEvent e){}

	@Override
	public void	mouseExited(MouseEvent e){}

	@Override
	public void	mousePressed(MouseEvent e){

		if(screen == INTRO){
			if(e.getX() > getWidth() * 5 / 16 && e.getX() < getWidth() * 5 / 16 + startButtonImage.getWidth(this) &&
			   e.getY() > getHeight() * 7 / 12 && e.getY() < getHeight() * 7 / 12 + startButtonImage.getHeight(this)){
				   // We clear the asteroids and bullets arraylists in case the player is restarting the game.
				   asteroids.clear();
				   bullets.clear();
				   enemyBullets.clear();
				   saucers.clear();
				   // We reset the keys
				   keys = new boolean[KeyEvent.KEY_LAST+1];
				   // Initializing the player in the middle of the screen
				   player = new Player(this);
				   alienSpawnTime = 0;
				   // Resetting the lives
				   lives = 3;
				   level = 0;
                   screen = GAME;
			   }				
		}
	}


	@Override
	public void	mouseReleased(MouseEvent e){}


	public void loadScore() {
		try {
			Scanner inFile = new Scanner(new File("highScore.txt"));
			highScore = inFile.nextInt();
		}

		catch(IOException ex) {
			System.out.println(ex);
		}
	}

	public void updateScore() {

		try {
			PrintWriter outFile = new PrintWriter(new File("highScore.txt"));
			outFile.println(highScore);
			outFile.flush();
		}

		catch(IOException ex) {
			System.out.println(ex);
		}
		
	}

	@Override
	public void paint(Graphics g){
		if(screen == INTRO){
			g.setColor(Color.BLACK);
			g.fillRect(0,0,getWidth(), getHeight());

			// Drawing the bullets
			g.setColor(Color.WHITE);
			for (int i = 0; i < bullets.size(); i++) {
				g.fillOval((int) bullets.get(i).bulletX(), (int) bullets.get(i).bulletY(), Bullets.RADIUS, Bullets.RADIUS);
			}

			// Drawing the enemy bullets
			g.setColor(Color.ORANGE);
			for (int i = 0; i < enemyBullets.size(); i++) {
				g.fillOval((int) enemyBullets.get(i).bulletX(), (int) enemyBullets.get(i).bulletY(), Bullets.RADIUS, Bullets.RADIUS);
			}

			// Drawing Asteroids
			g.setColor(Color.WHITE);
			for (int i = 0; i < asteroids.size(); i++) {
				g.drawPolygon(asteroids.get(i).asteroidObject);
			}

			// Drawing saucers
			for (Saucer saucer : saucers) {
				g.setColor(Color.RED);
				g.drawPolygon(saucer.enemyShip);
			}
            g.drawImage(title, getWidth() * 8 / 25, getHeight() * 3 / 12, this);
            g.drawImage(startButtonImage, getWidth() * 5 / 16, getHeight() * 7 / 12, this);				
		}
        
		else if (screen == GAME) {
			g.drawImage(back,0,0,this);

            // Drawing the player if the player is not in restarting mode
			if (! restarting) {
				g.setColor(Color.WHITE);
				g.drawPolygon(player.person);

				// Drawing the fire if it should be visible
				if (shipFire) {
					g.setColor(Color.RED);
					g.fillPolygon(player.fire);
				}
			}

			// Drawing the bullets
			g.setColor(Color.WHITE);
			for (int i = 0; i < bullets.size(); i++) {
				g.fillOval((int) bullets.get(i).bulletX(), (int) bullets.get(i).bulletY(), Bullets.RADIUS, Bullets.RADIUS);
			}

			// Drawing the enemy bullets
			g.setColor(Color.ORANGE);
			for (int i = 0; i < enemyBullets.size(); i++) {
				g.fillOval((int) enemyBullets.get(i).bulletX(), (int) enemyBullets.get(i).bulletY(), Bullets.RADIUS, Bullets.RADIUS);
			}

			// Drawing Asteroids
			g.setColor(Color.WHITE);
			for (int i = 0; i < asteroids.size(); i++) {
				g.drawPolygon(asteroids.get(i).asteroidObject);
			}

			// Drawing saucers
			for (Saucer saucer : saucers) {
				g.setColor(Color.RED);
				g.drawPolygon(saucer.enemyShip);
			}

			// Drawing all the information (high score, current score, lives left, current level)
			g.setColor(Color.WHITE);
			g.setFont(hyperspaceFont);
			g.drawString("" + highScore, 100, 25);
			g.drawString("" + score, 400, 25);
			g.drawString("Lives: " + lives, 700, 25);
			g.drawString("Level: " + level, 700, 50);

			// Changing the high score if it has been beat
			if (score > highScore) {
				highScore = score;
			}
		}
    }
}

class SoundEffect{
    private Clip c;
    public SoundEffect(String filename){
        setClip(filename);
    }
    public void setClip(String filename){
        try{
            File f = new File(filename);
            c = AudioSystem.getClip();
            c.open(AudioSystem.getAudioInputStream(f));
        } catch(Exception e){ System.out.println("error"); }
    }
    public void play(){
        c.setFramePosition(0);
        c.start();
    }
    public void stop(){
        c.stop();
    }

	public void loop(){
		c.loop(Clip.LOOP_CONTINUOUSLY);
	}
}