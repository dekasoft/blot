package com.dekagames.blot.algorithm;

/** Класс векторной точки.
 *
 * Из таких точек состоит векторный контур. Являются фактически
 * крайними точками сегментов кривой Безье, из которых состоит векторный контур
 *
 */

public class VPoint extends VCoords {
    boolean isCorner;    // является ли точка острым углом.
                                // если true, то p1x, p2x, p1y, p2y не учитываются (равны x и y)

    // координаты опорных точек. Опорные точки для неугловых точек контура лежат на одной прямой
    // касательной к контуру. Для угловых точек координаты опорных точек совпадают с координатами
    // x,y
    VCoords p1;                 // координаты первой опорной точки (второй опорной предыдущего сегмента
                                //  кривой Безье)

    VCoords p2;                 // координаты второй опорной точки (первой опорной точки начинающегося
                                // сегмента кривой Безье)


    // конструктор создания обычной точки
    public VPoint(double x, double y, double p1x, double p1y, double p2x, double p2y){
        super(x, y);
        p1 = new VCoords(p1x, p1y);
        p2 = new VCoords(p2x, p2y);
    }



    // конструктор создания угла
    public VPoint(double x, double y){
        super(x, y);
        this.isCorner = true;

        p1 = new VCoords(x, y);
        p2 = new VCoords(x, y);
    }


    public void scale(double scale){
        super.scale(scale);

        p1.scale(scale);
        p2.scale(scale);
    }


    public void translate(double fx, double fy){
        super.translate(fx, fy);
        p1.translate(fx, fy);
        p2.translate(fx, fy);
    }

}
