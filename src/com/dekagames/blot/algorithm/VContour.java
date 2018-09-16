package com.dekagames.blot.algorithm;


/**
 * Класс замкнутого контура, состоящего из векторных точек и соединяющих их сегментов
 * кривых Безье.
 *
 */


import java.util.ArrayList;

public class VContour {

    private ArrayList<VPoint> points;


    public VContour(){
        points = new ArrayList<>();
    }


    public void addPoint(VPoint p){
        if (!points.contains(p))
            points.add(p);
    }


    // производит перерасчет всех опорных точек всех сегментов контура, основываясь на коородинатах
    // граничных точек
    public void recalculate(){
    }
}
