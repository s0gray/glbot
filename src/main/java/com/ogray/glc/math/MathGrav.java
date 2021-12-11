package com.ogray.glc.math;

public class MathGrav {
    /**
     * Solve square equation ax^2 +bx+c =0
     * @param a
     * @param b
     * @param c
     * @return (x1,x2) or (0,0) if no solution
     */
    public static Point SqEq(double a, double b, double c) {
        Point x = new Point();
        double D = b*b - 4*a*c;
        if(D<0)
            return x;
        x.x = (-b + Math.sqrt(D))/(2*a);
        x.y = (-b - Math.sqrt(D))/(2*a);
        return x;
    }
}
