package com.dekagames.blot.algorithm;

/** Класс векторной точки.
 *
 * Из таких точек состоит векторный контур. Являются фактически
 * крайними точками сегментов кривой Безье, из которых состоит векторный контур
 *
 */

public class VPoint {

    public boolean isCorner;    // является ли точка острым углом.
                                // если true, то p1x, p2x, p1y, p2y не учитываются (равны x и y)
    public double x,y;          // координаты самой точки

    // координаты опорных точек. Опорные точки для неугловых точек контура лежат на одной прямой
    // касательной к контуру. Для угловых точек координаты опорных точек совпадают с координатами
    // x,y
    public double p1x, p1y;     // координаты первой опорной точки (второй опорной предыдущего сегмента
                                //  кривой Безье)
    public double p2x, p2y;     // координаты второй опорной точки (первой опорной точки начинающегося
                                // сегмента кривой Безье)


    // конструктор создания обычной точки
    public VPoint(double x, double y, double p1x, double p1y, double p2x, double p2y){
        this.x = x;
        this.y = y;
        this.p1x = p1x;
        this.p1y = p1y;
        this.p2x = p2x;
        this.p2y = p2y;
    }

    // конструктор создания угла
    public VPoint(double x, double y, boolean isCorner){
        this.isCorner = isCorner;
        this.x = p1x = p2x = x;
        this.y = p1y = p2y = y;
    }

    // конструктор копирования
    public VPoint(VPoint p){
        x = p.x;
        y = p.y;
        p1x = p.p1x;
        p1y = p.p1y;
        p2x = p.p2x;
        p2y = p.p2y;
        isCorner = p.isCorner;
    }

}
