package com.dekagames.blot.algorithm;

/**
 * класс используется при обработке растровых изображений
 */
public class RasterPoint extends RasterCoords {

    double cosfi;         // косинус угла с соседними точками
    boolean isCorner;
    float fratio;        // коэффициент закрашенности окна с центром в данном пикселе (поиск углов SUSAN)

    // уравнение касательной y = k*x + b в этой точке
    //double k, b;
    RasterLine tangent;

    public RasterPoint(int x, int y) {
        super(x,y);
    }


}
