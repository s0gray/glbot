package com.ogray.glc.math;

public class Pix {
    public int r = 0;
    public int g = 0;
    public int b = 0;
    public double val = 0.;

    public Pix() {
    }

    public Pix(double val1) {
        r=(byte)val1;
        g=(byte)val1;
        b=(byte)val1;
        val = val1;
    }


    public Pix(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }


    public byte getGrey() {
        return (byte)(r*0.3 + g*0.3 + b*0.3);
    }

    public int i() {
        return r+g+b;
    }

    public Pix minus(Pix B) {
        Pix C = new Pix();
        C.r = r - B.r;
        C.g = g - B.g;
        C.b = b - B.b;
        return C;
    }

    public Pix mul(double k)
    {
        Pix C = new Pix();
        C.r = (byte)(r * k);
        C.g = (byte)(g * k);
        C.b = (byte)(b * k);
        return C;
    }
}
