import java.awt.*;
import java.util.Random;

class Asteroid {
    
    public Polygon asteroidObject;
    private double speed;

    // 3 sizes of asteroids
    public static final int BIG = 35;
    public static final int MEDIUM = 25;
    public static final int SMALL = 15;

    // 3 speeds of asteroids
    public static final double BIGSPEED = 2.18;
    public static final double MEDIUMSPEED = 2.56;
    public static final double SMALLSPEED = 2.94;

    // Points for hitting an asteroids
    public static final int BIGHIT = 20;
    public static final int MEDIUMHIT = 50;
    public static final int SMALLHIT = 100;


    public double centerX;
    public double centerY;
    public double magnitude = 0;
    public int angle = 90;

    private double largestMagnitude;
    private int points;

    private double [] magnitudes;
    private int [] angles;
    private Random rand = new Random();


    public Asteroid(int points, double magnitude, double speed) {

        this.points = points;
        this.magnitude = magnitude;
        this.speed = speed;

        // We let side = 0 represent the top, side = 1 represent the left side of the screen, etc.
        int side = rand.nextInt(4);
        int angle;

        // If the asteroid starts from the top
        if (side == 0) {
            centerY = 0;
            // Generating a random x coordinate
            centerX = rand.nextInt(800);
            // We want the angle to be between 180 and 360 so that the asteroid moves downwards
            angle = rand.nextInt(180) + 180;
        }

        // If the asteroid starts from the left part of the screen
        else if (side == 1) {
            centerX = 0;
            // Generating a random y coordinate
            centerY = rand.nextInt(600);
            // We want the angle to be between 270 and 90 (also 450)
            angle = rand.nextInt(180) + 270;
        }

        // If the asteroid starts from the bottom of the screen
        else if (side == 2) {
            centerY = 600;
            // Generating a random x coordinate
            centerX = rand.nextInt(800);
            // We want the angle to be bewteen 0 and 180
            angle = rand.nextInt(180);
        }

        // If the asteroid starts from the right of the screen
        else {
            centerX = 800;
            // Generating a random y coordinate
            centerY = rand.nextInt(600);
            // We want the angle to be between 90 and 270
            angle = rand.nextInt(180) + 90;
        }

        int [] xCoords = new int[points];
        int [] yCoords = new int[points];

        magnitudes = new double [points];
        angles = new int[points];

        // Choosing the coordinates of each point of the polygon
        for (int i = 0; i < points; i++) {

            // We create a variable dir so that we can either make the magnitude of the current point from the center smaller or larger than the 'magnitude' given
            int dir = rand.nextInt(2);
            if (dir == 0) dir = -1;
            else dir =  1;
 
            // Choosing a random magnitude
            double mag = dir * rand.nextDouble()*(magnitude/2) + magnitude;
            magnitudes[i] = mag;

            // Keeping track of the largest magnitude so that we know when an asteroid goes off the screen
            if (mag > largestMagnitude) {
                largestMagnitude = mag;
            }

            // Setting the current angle
            this.angle += rand.nextInt((int) (60/points)) + (int)(300/points);

            angles[i] = this.angle;
            double [] coords = Util.xy(mag, this.angle);
            xCoords[i] = (int) (coords[0] + centerX);
            yCoords[i] = (int) (coords[1] + centerY);
        }

        this.angle = angle;

        asteroidObject = new Polygon(xCoords, yCoords, points);
    }

    public void move() {

        // Updating the center of the asteroid
        double [] updatedCenter = Util.xy(speed, angle);
        centerX += updatedCenter[0];
        centerY += updatedCenter[1];


        // If the asteroid has gone off to the left of the screen
        if (centerX + largestMagnitude < 0) {
            centerX = 799;
        }

        // If the asteroid has gone off to the right of the screen
        else if (centerX - largestMagnitude > 800) {
            centerX = 1;
        }

        // If the asteroid has gone off the top of the screen
        else if (centerY + largestMagnitude < 0) {
            centerY = 599;
        }

        // If the asteroid has gone off the bottom of the screen
        else if (centerY - largestMagnitude > 600) {
            centerY = 1;
        }

        // Updating all the points of the asteroid based off of the new center
        for (int i = 0; i < asteroidObject.npoints; i++) {
            angles[i] += 1;

            double [] coords = Util.xy(magnitudes[i], angles[i]);
            asteroidObject.xpoints[i] = (int) (coords[0] + centerX);
            asteroidObject.ypoints[i] = (int) (coords[1] + centerY);
        }

        // Recreating the object for collision purposes (if we don't recreate,
        // sometimes checks for collisions in previous location that the asteroid was location)
        asteroidObject = new Polygon(asteroidObject.xpoints,asteroidObject.ypoints,asteroidObject.npoints);
    }
} 
