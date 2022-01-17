import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.awt.geom.Rectangle2D;

public class Player {

    public Polygon person;
    public Polygon fire;
    public GamePanel game;

    public double vx = 0;
    public double vy = 0;
    private static final double ACCEL = 0.57;
    private static final double DECCEL = 0.95;
    private static final double RECOIL = 3.92;
    private static final double MAXSPEED = 7.67;
    private static final int ROTATEANGLE = 6;
    private double largestMagnitude = 30.21023;

    // We set the original angle to 90 degrees so that at the start, the player is facing upwards
    public int angle = 90;
    private double cx = 400;
    private double cy = 300;
    public boolean hyperSpace = true;

    private Random rand = new Random();
    
    public Player(GamePanel game) {

        this.game = game;

        // Creating the player in the center of the screen (400,300)

        int [] xCoords = new int [5];
        int [] yCoords = new int [5];

        person = new Polygon(xCoords, yCoords, 5);

        // Creating the fire behind the player that is visible only when the player ship is accelerating
        int [] fireX = new int [3];
        int [] fireY = new int [3];

        fire = new Polygon(fireX, fireY, 3);

        changeCoords();
    }

    public void move(boolean [] keys) {

        // Changing the position of the player based on the key that was pressed

        angle = angle % 360;

        // For negative angles
        angle = angle + 360;
        angle = angle % 360;

        if (keys[KeyEvent.VK_LEFT]) {
            angle += ROTATEANGLE;
        }

        else if (keys[KeyEvent.VK_RIGHT]) {
            angle -= ROTATEANGLE;
        }

        if (keys[KeyEvent.VK_LEFT] || keys[KeyEvent.VK_RIGHT]) {
            changeCoords();
        }

        if(keys[KeyEvent.VK_UP]){

            // Increasing the velocity (accelerating) until a maximum velocity
            if (Math.abs(vx) <= MAXSPEED) {
                vx += ACCEL * Math.cos(Math.toRadians(angle));
            }
            if (Math.abs(vy) <= MAXSPEED) {
                vy -= ACCEL * Math.sin(Math.toRadians(angle));
            }
            game.shipFire = true;
        }	

        // Teleports to a random location
        if(keys[KeyEvent.VK_DOWN] && hyperSpace){
            int newCX = rand.nextInt((int) (800 - 2 * largestMagnitude)) + 30;
            int newCY = rand.nextInt((int) (600 - 2 * largestMagnitude)) + 30;
   
            cx = newCX;
            cy = newCY;
            changeCoords();

            hyperSpace = false;

        }

        // Gradually slowing vx until it comes to a stop if the user stops thrusting
        vx *= DECCEL;
        vy *= DECCEL;

        // Updating the center
        cx += Math.round(vx);
        cy += Math.round(vy);

        // Updating the location of the player if they have gone off the screen


        // If the player has gone off to the left of the screen
        if (cx + largestMagnitude < 0) {
            cx = 799;
            changeCoords();
        }

        // If the player has gone off to the right of the screen
        else if (cx - largestMagnitude > 800) {
            cx = 1;
            changeCoords();
        }

        // If the player has gone off the top of the screen
        else if (cy + largestMagnitude < 0) {
            cy = 599;
            changeCoords();
        }

        // If the player has gone off the bottom of the screen
        else if (cy - largestMagnitude > 600) {
            cy = 1;
            changeCoords();
        }

        // Moving the points according to the players current velocity

        for (int i = 0; i < person.npoints; i++) {
            person.xpoints[i] += Math.round(vx);
            person.ypoints[i] += Math.round(vy);

        }

        for (int i = 0; i < fire.npoints; i++) {
            fire.xpoints[i] += Math.round(vx);
            fire.ypoints[i] += Math.round(vy);
        }

        person = new Polygon(person.xpoints, person.ypoints, person.npoints);
    }

    // Whenever the player shoots, they are pushed a certain distance backwards
    public void recoil() {
        double [] coords = Util.xy(RECOIL, angle);
        cx -= coords[0];
        cy -= coords[1];

        changeCoords();
    }

    public void changeCoords() {

        double [] firstCoords = Util.xy(14.954343, angle);
        double [] secondCoords = Util.xy(29.54343, angle + 150);
        double [] thirdCoords = Util.xy(20.454343, angle + 145);
        double [] fourthCoords = Util.xy(20.454343, angle - 145);
        double [] fifthCoords = Util.xy(29.54343, angle - 150);

        person.xpoints[0] = (int) (firstCoords[0] + cx);
        person.xpoints[1] = (int) (secondCoords[0] + cx);
        person.xpoints[2] = (int) (thirdCoords[0] + cx);
        person.xpoints[3] = (int) (fourthCoords[0] + cx);
        person.xpoints[4] = (int) (fifthCoords[0] + cx);

        person.ypoints[0] = (int) (firstCoords[1] + cy); 
        person.ypoints[1] = (int) (secondCoords[1] + cy);
        person.ypoints[2] = (int) (thirdCoords[1] + cy);
        person.ypoints[3] = (int) (fourthCoords[1] + cy);
        person.ypoints[4] = (int) (fifthCoords[1] + cy);


        // Changing the location of the fire
        // We want the bottom coordinates of the fire to be in line with the center and the top of the player ship
        double [] bottomCoords = Util.xy(30.2, angle + 180);

        fire.xpoints[0] = person.xpoints[2];
        fire.xpoints[1] = person.xpoints[3];
        fire.xpoints[2] = (int) (bottomCoords[0] + cx);

        fire.ypoints[0] = person.ypoints[2];
        fire.ypoints[1] = person.ypoints[3];
        fire.ypoints[2] = (int) (bottomCoords[1] + cy);
    }

    // We use the method initiateLocation to check if it is safe for the player to begin the game at a location (centerX, centerY). 
    // This is done by checking if there are any asteroids in a square made around this point.
    public boolean initiateLocation(int centerX, int centerY) {
        Rectangle2D borderSpace = new Rectangle2D.Float();
        borderSpace.setFrame( (int) (centerX - 2 * largestMagnitude), (int) (centerY - 2 * largestMagnitude), (int) (4 * largestMagnitude), (int) (4 * largestMagnitude));
        for (Asteroid asteroid : game.asteroids) {
            if (asteroid.asteroidObject.intersects(borderSpace)) {
                return false;
            }
        }
        return true;
    }

    // Returns the x coordinate of the center of the player
    public double centerX() {
        return cx;
    }

    // Returns the y coordinate of the center of the player
    public double centerY() {
        return cy;
    }
}

