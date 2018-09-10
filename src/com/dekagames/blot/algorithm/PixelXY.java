package com.dekagames.blot.algorithm;

/**
 * класс используется при обработке растровых изображений
 */
public class PixelXY {

    public int x;
    public int y;
    // при упрощении растрового контура считается отклонение delta точки от прямой проведенной через ее соседей
    // фактически - векторное произведение в координатной форме
    public int delta;


    public PixelXY(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
