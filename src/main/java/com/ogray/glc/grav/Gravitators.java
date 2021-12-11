package com.ogray.glc.grav;

import com.ogray.glc.math.Point;
import com.ogray.glc.math.Star;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Gravitators distribution
 */
@Slf4j
public class Gravitators {
    @Setter @Getter
    int count;

    @Setter @Getter
    Star[] data;

    public Gravitators(int ng) {
        this.count = ng;
        data = new Star[ng];
    }

    public void setStar(int i, Star st) {
        data[i] = st;
    }

    public Point calcGc() {
        return new Point();
    }

    public void log() {
        log.info("gravitators count = "+ this.count);
        for(int i=0; i<this.count; i++) {
            log.info("#"+ (i+1)+". " + data[i]);
        }
    }
}
