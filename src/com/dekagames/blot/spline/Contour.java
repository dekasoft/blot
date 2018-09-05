package com.dekagames.blot.spline;


// класс замкнутого контура, состоящего из точек и соединяющих их сегментов кривых Безье

import java.util.ArrayList;

public class Contour {

    private ArrayList<Point> points;


    public Contour(){
        points = new ArrayList<>();
    }


    public void addPoint(Point p){
        if (!points.contains(p))
            points.add(p);
    }


    // производит перерасчет всех опорных точек всех сегментов контура, основываясь на коородинатах
    // граничных точек
    public void recalculate(){
    }
}
