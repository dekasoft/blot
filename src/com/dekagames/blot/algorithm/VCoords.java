package com.dekagames.blot.algorithm;


/**
 * класс координат в системе координат picture:
 * x, y - типа double
 *
 * используется для получения/передачи координат при преобразовании
 * и в других подобных случаях.
 */
public class VCoords {
    double x,y;

    public VCoords(double x, double y){
        this.x = x;
        this.y = y;
    }


    public void scale(double scale){
        x *= scale;
        y *= scale;
    }

    public void translate(double fx, double fy){
        x += fx;
        y += fy;
    }
}
