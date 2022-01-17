class Bullets {
    
    private static double speed = 10;
    // The player can have a max of 6 bullets at a time on the screen
    public static int MAXBULLETS = 6;
    public static final int RADIUS = 5;
    private double distTravelled = 0;
    private double x, y, angle;
    

    public Bullets(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }
 
    // Returning the x coordinate of the center of the bullet
    public double bulletX() {
        return x;
    }

    // Returning the y coordinate of the center of the bullet
    public double bulletY() {
        return y;
    }

    public void move() {

        // Calculating the updates in x and y in terms of the speed and angle of the bullet
        x += (int) (speed * Math.cos(Math.toRadians(angle)));
        y -= (int) (speed * Math.sin(Math.toRadians(angle)));

        distTravelled += Util.vec(speed * Math.cos(Math.toRadians(angle)), speed * Math.sin(Math.toRadians(angle)));

        // If the bullet has gone off to the left of the screen
        if (x < 0) {
            x = 800;
        }

        // If the bullet has gone off to the right of the screen
        else if (x >= 800) {
            x = 0;
        }

        // If the bullet has gone off the top of the screen
        else if (y < 0) {
            y = 600;
        }

        // If the bullet has gone off the bottom of the screen
        else if (y > 600) {
            y = 0;
        }

    }

    public boolean maxDist() {

        // If the bullet has travelled 590 units or more, then we return true
        if (distTravelled >= 590) {
            return true;
        }

        return false;
    }
}
