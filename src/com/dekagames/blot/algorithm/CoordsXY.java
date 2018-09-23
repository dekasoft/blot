package com.dekagames.blot.algorithm;


/**
 * класс координат в системе координат picture:
 * x, y - типа double
 *
 * используется для получения/передачи координат при преобразовании
 * и в других подобных случаях.
 */
public class CoordsXY {
    private double x,y;

    public CoordsXY(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
