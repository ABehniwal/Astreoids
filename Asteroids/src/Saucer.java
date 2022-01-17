import java.awt.*;
import java.util.Random;
import java.awt.geom.Rectangle2D;

class Saucer {

    // Magnitudes of the big and small saucers
    public static final double SMALL = 17.45;
    public static final double BIG = 30.09;

    private static final int LEFT = -1;
    private static final int RIGHT = 1;

    private static final double SPEED = 1.92;

    // Points for hitting a big or small saucer
    public static final int BIGHIT = 200;
    public static final int SMALLHIT = 1000;

    private int bulletTime;
    private double targetX;
    private double targetY;

    private GamePanel game;
    public Polygon enemyShip;
    private double cx;
    private double cy;
    // The height from (cx, cy) to the top of the enemyShip is around 4/5 * mag
    // The height from (cx, cy) to the bottom of the enemyShip is around 2/5 * mag
    private double mag;
    private int dir;

    private double [] magnitudes;
    private double [] angles = new double [] {0, 60, 80, 100, 120, 60, 120, 180, 0, 180, 240, 300};
    private Random rand = new Random();

    public Saucer(GamePanel game, double size, int time) {

        this.game = game;
        mag = size;
        bulletTime = time;

        if (mag == BIG) {
            // Generating a random coordinate that we want the big saucer to move towards
            targetX = cx + dir * 200;
            targetY = rand.nextInt(600);
        }

        magnitudes = new double [] {mag, mag/2, mag * 5/6, mag * 5/6, mag/2, mag/2, mag/2, mag, mag, mag, mag/2, mag/2};

        // Initializing the saucer in a random location

        // We let side = 0 represent the left and side = 1 represent the right side of the screen
        int side = rand.nextInt(2);

        // If the saucer starts from the left part of the screen
        if (side == 0) {
            cx = 0;
            // Generating a random y coordinate
            cy = rand.nextInt(600);
            dir = RIGHT;
        }

        // If the saucer starts from the right of the screen
        else {
            dir = LEFT;
            cx = 800;
            // Generating a random y coordinate
            cy = rand.nextInt(600);
        }

        int [] xCoords = new int[12];
        int [] yCoords = new int[12];

        enemyShip = new Polygon(xCoords, yCoords, 12);
        adjustPosition();
    }

    public void adjustPosition() {
        for (int i = 0; i < 12; i++) {
            double [] newCoords = Util.xy(magnitudes[i], angles[i]);
            enemyShip.xpoints[i] = (int) (newCoords[0] + cx);
            enemyShip.ypoints[i] = (int) (newCoords[1] + cy);

        }
    }

    public void move() {

        // If it is a big saucer, it is not very accurate in its movement
        if (mag == BIG) {

            // We change the direction of motion if the big saucer has reaching its target coordinates and hasn't left the screen
            if (cx >= targetX) {
                targetX = cx + dir * 200;
                targetY = rand.nextInt(600);
            }

            double angle = Util.angle(cx, cy, targetX, targetY);

            // Updating the center of the saucer 
            double [] updatedCenter = Util.xy(SPEED, angle);
            cx += updatedCenter[0];
            cy += updatedCenter[1];
        }

        // Otherwise if the saucer is a small saucer, it moves cleverly
        else {

            Rectangle2D borderSpace = new Rectangle2D.Float();
            borderSpace.setFrame( (int) (cx - 2 * mag), (int) (cy - 2 * mag), (int) (4 * mag), (int) (4 * mag));
            for (Asteroid asteroid: game.asteroids) {
                if (asteroid.asteroidObject.intersects(borderSpace)) {

                    // If the asteroid is above the enemyShip, we move the enemyShip down a bit
                    if (cy >= asteroid.centerY) {
                        cy += 5;
                    }

                    // If the asteroid is below the enemyShip, we move the enemyShip up a bit
                    else {
                        cy -= 5;
                    }
                }
            }
        }

        // If the enemyShip has gone off the top of the screen
        // For the entire ship to go off the top of the screen, the middle plus the height from
        // the middle to the bottom of the ship must be less than 0. The height from the middle (cx, cy) to the bottom
        // of the ship is (mag * 2) / 5
        if (cy + (mag * 2) / 5 < 0) {
            cy = 599;
        }

        // If the enemyShip has gone off the bottom of the screen
        // For the entire ship to go off the bottom of the screen, the middle minus the height from
        // the middle to the top of the ship must be greater than 600; The height from the middle (cx, cy) to the top
        // of the ship is (mag * 4) / 5
        else if (cy - (mag*4) / 5 > 600) {
            cy = 1;
        }

        cx += dir * SPEED;

        // Adjusting the rest of the points in terms of the center
        adjustPosition();
        
        
    }

    // Checks if the saucer is on screen
    public boolean onScreen() {

        // If the saucer was moving right and has gone off the right side of the screen
        if (dir == RIGHT) {
            if (cx > 800) {
                return false;
            }
            return true;
        }

        // Otherwise the saucer was moving left and checking if it has gone off the left side of the screen
        else {
            if (cx < 0) {
                return false;
            }
            return true;
        }
    }

    // Verifies that they correct amount of time has passed before a saucer can shoot (again perhaps)
    public boolean canShoot(int time) {

        // Returns true if the time interval between shooting bullets has passed and if the saucer is on the screen
        if ((time - bulletTime) % 70 == 0 && onScreen()) {
            return true;
        }

        return false;
    }

    // Determines the angle at which a bullet released from the saucer should go at
    public double bulletAngle(double playerX, double playerY) {

        // If it is a big saucer, it will shoot randomly
        if (mag == BIG) {
            double angle = rand.nextInt(360);
            return angle;
        }

        // The small saucer shoots directly at the player
        else {
            double angle = Util.angle(cx, cy, playerX, playerY);
            return angle;
        }
    }

    // Returns a Rectangle2D object to help check if the saucer was hit
    public Rectangle2D hitArea() {
        Rectangle2D borderSpace = new Rectangle2D.Float();
        // Setting the frame to approximate the borders of the saucer
        borderSpace.setFrame(cx - mag, cy - (4 * mag)/5, 2 * mag, (6 * mag)/5);
        return borderSpace;
    }
    
    // Returns true if the borderSpace of the saucer contains the bullet at (xCoord, yCoord), otherwise will return false
    public boolean containsBullet(double xCoord, double yCoord) {
        Rectangle2D borderSpace = hitArea();
        if (borderSpace.contains(xCoord, yCoord)) {
            return true;
        }
        return false;
    }

    // Returns the x coordinate of the center of the saucer
    public double centerX() {
        return cx;
    }

    // Returns the y coordinate of the center of the saucer
    public double centerY() {
        return cy;
    }

    public double magnitude() {
        return mag;
    }
}