package com.ogray.glc.math;

public class Complex {
    // z = a+ib
    public double a;
    public double b;

    public Complex() {
        a = 0;
        b = 0;
    }
    public Complex(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public Complex(Point c) {
        this.a = c.getX();
        this.b = c.getY();
    }


    /**
     * ~z
     * @return
     */
    public Complex conjugate() {
        return new Complex(a, -b);
    }

    public Complex minus(Complex z) {
        Complex d = new Complex( a - z.a, b - z.b);
        return d;
    }

    public Complex divide(Complex z) {
        Complex d = new Complex();
        if(z.a==0 && z.b==0)
        {
            //printf("Div to 0 in complex!!\n");
            d.a = a; d.b = b;
            return d;
        }

        d.b = (b*z.a-z.b*a)/(z.a*z.a+z.b*z.b);

        if(z.a!=0)
            d.a = (a+d.b*z.b)/z.a;
        else
            d.a = (b-d.b*z.a)/z.b;

        return d;
    }

    public Complex plus(Complex z) {
        Complex d = new Complex();
        d.a = a + z.a;
        d.b = b + z.b;
        return d;
    }

    public Complex mul(double d) {
        Complex z = new Complex();
        z.a = a*d;
        z.b = b*d;
        return z;
    }

    public Complex mul(Complex z) {
        Complex d = new Complex();
        d.a = a * z.a - b * z.b;
        d.b = b * z.a + a * z.b;
        return d;
    }

    public Point point() {
        return new Point(a,b);
    }
}
