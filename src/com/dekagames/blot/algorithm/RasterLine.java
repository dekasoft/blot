package com.dekagames.blot.algorithm;


/**
 * класс растровой линии, заданной уравнением y = kx + b
 * Содержит всякие полезные методы
 */
public class RasterLine {
    double k, b;


    public RasterLine(double k, double b){
        this.k = k;
        this.b = b;
    }


    public RasterLine(RasterCoords p1, RasterCoords p2){
        int x1 = p1.x;
        int y1 = p1.y;

        int x2 = p2.x;
        int y2 = p2.y;

        int dx = x2 - x1;
        int dy = y2 - y1;

        if (dx == 0)
            k = 10000.0;
        else
            k = (double)dy/dx;

        b = y1 - k * x1;
    }


    /**
     * возвращает прямую параллельную данной и проходящую через точку
     * @param p
     * @return
     */
    public RasterLine getParallel(RasterCoords p){
        double b_par = p.y - k * p.x;
        return new RasterLine(k, b_par);

    }


    /**
     * возвращает прямую, перпендикулярную данной и проходящую через точку p
     * @param p
     * @return
     */
    public RasterLine getNormal(RasterCoords p){
        // у перпендикулярных  прямых  k1 * k2 = -1
        double k_n = -1/k;
        double b_n = p.y - k_n * p.x;

        return new RasterLine(k_n, b_n);
    }


    /**
     * Возвращает координаты точки пересечения с другой прямой или null если прямые параллельны
     * @param line
     * @return
     */
    public RasterCoords getIntercept(RasterLine line){
        if (Math.abs(k - line.k) < 0.1 )   // у параллельных прямых k одинаковый
            return null;

        double x = (line.b - b)/(k - line.k);
        double y = k * x + b;

        return new RasterCoords((int)x,(int)y);

    }


    /**
     * Возвращает точку, лежащую на прямой, на расстоянии distance от точки pFrom.
     * distance может быть отрицательным или положительным - при этом возвращаются
     * две разные точки (по разные стороны от pFrom)
     * @param pFrom
     * @param distance
     * @return
     */
    public RasterCoords getPointOnDistanceFrom(RasterCoords pFrom, double distance){
        double xTo = pFrom.x + distance/Math.sqrt(k*k+1);
        double yTo = k * xTo + b;
        return new RasterCoords((int)xTo,(int)yTo);
    }

}
