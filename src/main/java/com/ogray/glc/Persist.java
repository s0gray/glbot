package com.ogray.glc;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Persist {
    private static Persist instance = null;
    public static Persist getInstance() {
        if(instance==null) {
            instance = new Persist();
        }
        return instance;
    }

    @Getter
    private int sourceType = 1;

    @Setter @Getter
    private float sourceSize = 3;

    @Setter @Getter
    private int calcMode = 0;

    @Setter @Getter
    private float sigmaC = 0.1f;

    @Setter @Getter
    private float gamma = 0.1f;

    @Setter @Getter
    private float sizeRE = 15f;

    @Setter @Getter
    private int sizePX = 1024;

    @Setter @Getter
    private int ng = 100;

    @Setter @Getter
    private float starM0 = 1f;

}
