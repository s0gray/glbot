package com.ogray.glc.math;

public class Pixel {
    public Point field = new Point();     // Field Value
    public boolean fieldOk = false;
    public Pix i = new Pix();          // brightness value

    public boolean ok = false;          // done, no subdividing required
    public boolean light = false;       // brightness not calculated yet, but light is present here
    public double psi = 0.;       // value of znamennyk, store to not compute twice

    public float det = 0.f;        // determinant of mapping matrix
    public boolean detOk = false;


}
