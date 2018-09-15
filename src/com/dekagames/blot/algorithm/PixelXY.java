package com.dekagames.blot.algorithm;

/**
 * класс используется при обработке растровых изображений
 */
public class PixelXY {

    public int x;
    public int y;

    public float cosfi;         // косинус угла с соседними точками
    public boolean isCorner;
    public float fratio;        // коэффициент закрашенности окна с центром в данном пикселе (поиск углов SUSAN)

    public PixelXY(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
