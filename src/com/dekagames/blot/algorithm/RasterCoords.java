package com.dekagames.blot.algorithm;


/**
 *  Класс растровой точки.
 *  Содержит всякие полезные методы и нужен для приема/передачи между
 *  другими классами/методами
 */
public class RasterCoords {
    // координаты точки
    int x, y;


    public RasterCoords(int x, int y){
        this.x = x;
        this.y = y;
    }


    /**
     * расстояние до другой точки
     */
    public double distanceTo(int toX, int toY){
        return Math.hypot(x - toX, y - toY);
    }

    /**
     * Косинус угла с вершиной в данной точке и лучами в две другие точки
     */
    public double getCos(RasterCoords p1, RasterCoords p2){
        // косинус угла будем рассчитывать по формуле скалярного произведения в координатной форме
        // cosfi = (x1*x2 + y1*y2)/(|x1,y1|*|x2,y2|) , где (x1,y1) - координаты вектора
        // в точку p1, (x2,y2) - координаты вектора в точку p2
        int x1 = p1.x - x;
        int y1 = p1.y - y;
        int x2 = p2.x - x;
        int y2 = p2.y - y;
        double len1 = Math.hypot(x1, y1);
        double len2 = Math.hypot(x2, y2);

        return (x1*x2 + y1*y2)/(len1*len2);
    }

}

