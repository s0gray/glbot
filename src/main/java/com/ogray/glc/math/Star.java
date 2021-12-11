package com.ogray.glc.math;

import lombok.Getter;
import lombok.Setter;

public class Star {
    // coordinates
    @Setter @Getter
    public Point r = new Point();
    // velocity
    @Setter @Getter
   public  Point v = new Point();
    // mass
    @Setter @Getter
    public double mass;

    public Star() {
    }

    public String toString() {
        return "[ r=" + r +"], v=[" + v +"] m="+mass;
    }
}
