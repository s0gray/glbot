package com.ogray.glc.math;

public class Matrix2 {
    /*
   (0,0) (0,1)
   (1,0) (1,1)
*/
    public double a[][] = new double[2][2];
    public Matrix2() {
        a[0][0]= 0.;
        a[0][1]= 0.;
        a[1][0]= 0.;
        a[1][1]= 0.;
    }

    /**
     *  C = A - B
     * @param b
     * @return
     */
    public Matrix2 minus(Matrix2 b) {
        Matrix2 c = new Matrix2();
        c.a[0][0] = a[0][0]-b.a[0][0];
        c.a[0][1] = a[0][1]-b.a[0][1];
        c.a[1][0] = a[1][0]-b.a[1][0];
        c.a[1][1] = a[1][1]-b.a[1][1];
        return c;
    }

    public Point mul(Point z)
    {
        Point b = new Point();
        b.x = a[0][0]*z.x + a[0][1]*z.y;
        b.y = a[1][0]*z.x + a[1][1]*z.y;
        return b;
    }

    public Matrix2 mul(Matrix2 b)
    {
        Matrix2 c = new Matrix2();
        c.a[0][0] = a[0][0]*b.a[0][0]+a[0][1]*b.a[1][0];
        c.a[0][1] = a[0][0]*b.a[0][1]+a[0][1]*b.a[1][1];
        c.a[1][0] = a[1][0]*b.a[0][0]+a[1][1]*b.a[1][0];
        c.a[1][1] = a[1][0]*b.a[0][1]+a[1][1]*b.a[1][1];
        return c;
    }

    public Matrix2 plus(Matrix2 b)
    {
        Matrix2 c = new Matrix2();

        c.a[0][0] = a[0][0]+b.a[0][0];
        c.a[0][1] = a[0][1]+b.a[0][1];
        c.a[1][0] = a[1][0]+b.a[1][0];
        c.a[1][1] = a[1][1]+b.a[1][1];
        return c;
    }

    public double det() {
        return a[0][0]*a[1][1]-a[0][1]*a[1][0];
    }

    public Matrix2 trans()
    {
        Matrix2 b = new Matrix2();

        b.a[1][0] = a[0][1];
        b.a[0][1] = a[1][0];
        b.a[0][0] = a[0][0];
        b.a[1][1] = a[1][1];
        return b;
    }

    public Matrix2 back()
    {
        Matrix2 b = new Matrix2();
        if(det()==0)
            return b;

        if(a[1][0]!=0 && a[0][0]!=0)
        {
            b.a[1][0] = 1/(a[0][1]-a[1][1]*a[0][0]/a[1][0]);
            b.a[0][0] = -a[1][1]*b.a[1][0]/a[1][0];

            b.a[1][1] = 1/(a[1][1]-a[0][1]*a[1][0]/a[0][0]);
            b.a[0][1] = -a[0][1]*b.a[1][1]/a[0][0];
            return b;
        }

        if(a[0][0]!=0 && a[1][1]!=0 && a[1][0]==0)
        {       //a[1][0]=0

            b.a[1][0] = 0;
            b.a[0][0] = 1/a[0][0];

            b.a[1][1] = 1/a[1][1];
            b.a[0][1] = -a[0][1]*b.a[1][1]/a[0][0];
            return b;
        }

        if(a[0][0]==0 && a[0][1]!=0 && a[1][0]!=0)
        { // a[0][0] =0
            b.a[1][1] = 0;
            b.a[1][0] = 1/a[0][1];

            b.a[0][1] = 1/a[1][0];
            b.a[0][0] = -a[1][1]*b.a[1][0]/a[1][0];
            return b;
        }
        return b;
    }
}
