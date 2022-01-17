class Util{
    public static double[] xy(double mag, double ang){
        double []ans = new double[2];
        ans[0] = Math.cos(Math.toRadians(ang))*mag;
        ans[1] = -Math.sin(Math.toRadians(ang))*mag;
        return ans;
        
    }
    
    public static double vec(double x, double y){
        double ans = Math.sqrt(x * x + y * y);
        return ans;
    
    }

    public static double angle(double startX, double startY, double finishX, double finishY) {
        double ans = Math.toDegrees(Math.atan2(-(finishY - startY), finishX - startX));
        return ans;
    }
    
}