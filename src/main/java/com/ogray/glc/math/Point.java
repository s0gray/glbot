package com.ogray.glc.math;

import lombok.Getter;
import lombok.Setter;

public class Point {
    @Setter @Getter
    public double x;

    @Setter @Getter
    public double y;
    /*
        double mul(_point b);       // z-component of vector multiplying
        double mul_sc(_point b);    // scalaar mult
        double L(_point b);  // length of difference
        double l(); // length

        _point operator +(_point);
        _point operator -(_point);

        _point operator +=(_point);
        _point operator -=(_point);

        _point operator *(_point);
        _point operator /(_point);

        _point operator *=(_point);
        _point operator /=(_point);

        _point operator*(double );
        _point operator/(double );

        _point operator*=(double );
        _point operator/=(double );

         double phi();
     */
    public Point() {
        this.x = 0;
        this.y = 0;
    }
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public Point(Point p) {
        this.x = p.x;
        this.y = p.y;
    }

    public Point(double r, double phi, int type) { // polar s.k init
        switch(type)
        {
            case 1:
                x = r * Math.cos(phi* Math.PI/180);
                y = r * Math.sin(phi * Math.PI/180);
                break;
            default:
                x = 0;
                y = 0;
        }
    }
    public double mod() {
        return x*x + y*y;
    }

    public Point div(Point a) {
        Point c = new Point(this);
        if(a.x!=0) c.x /=  a.x;
        if(a.y!=0) c.y /=  a.y;
        return c;
    }
    public Point div(double m)
    {
        Point c = new Point(this);
        if(m!=0)
        {
            c.x /= m;
            c.y /= m;
        }
        return c;
    }
    /**
     * /= operator
     * @param a
     * @return
     */
    public Point divide(Point a) {
        if(a.x!=0)
            x = (x/a.getX());
        if(a.y!=0)
            y = (y/a.getY());
        return this;
    }

    /**
     * /=
     * @param m
     * @return
     */
    public Point divide(double m) {
        if(m!=0)
        {
            x /=  m;
            y /=  m;
        }
        return this;
    }

    /**
     * operator-=
     * @param a
     * @return
     */
    public void minusMe(Point a) {
        x = x - a.x;
        y = y - a.y;
    }

    /**
     * operator-
     * @param a
     * @return this-a
     */
    public Point minus(Point a) {
        Point p = new Point(this);
        p.x = x - a.x;
        p.y = y - a.y;
        return p;
    }

    public double mul_sc(Point b) {
        return x*b.x+y*b.y;
    }

    /**
     * +=
     * @param a
     */
    public void plusMe(Point a) {
        x += a.x ;
        y += a.y ;
    }

    public String toString() {
        return "[" + this.x + ", "+y+"]";
    }

    /**
     * operator *=
     * @param a
     */
    public void mulMe(Point a) {
        x *= a.x;
        y *= a.y;
    }

    public double l() {
        return Math.sqrt(x*x + y*y);
    }

    public Point mulPoint(Point a) {
        Point c = new Point(this);
        c.x *=  a.x;
        c.y *=  a.y;
        return c;
    }

    public double mul(Point b)
    {
        return x*b.y-y*b.x;
    }


    public Point mul(double m) {
        Point c = new Point(this);
        c.x*= m;
        c.y *= m;
        return c;
    }

}
