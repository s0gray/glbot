package com.ogray.glc.grav;

import lombok.Getter;
import lombok.Setter;

public class Foot {
    @Setter @Getter
    int size;
    @Setter @Getter
    int items;
    @Setter @Getter
    int increment;

    int []a;

    public Foot(int sz, int inc)
    {
        size = sz;
        increment = inc;

       clear();
    }

    public void clear() {
        a = new int[size];
        items = 0;
    }

    public boolean add(int x) {
        if(items<size){
            a[items++] = x;
            return true;
        }
        int[] b = new int[size+increment];
        System.arraycopy(a,0,b,0,a.length);
        a = b;
        //if(realloc(a,sizeof(long)*(size+increment))==NULL) { // error
        //    return false;
        //}
        items++;
        size += increment;
        return true;
    }

    public int get(int n) {
        if(n<items)
            return a[n];
        return 0;
    }
}
