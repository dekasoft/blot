package com.dekagames.blot.algorithm;


import java.util.ArrayList;

/*
Класс обьекта пятна.

BlotObject - это обьект полученный рисованием за один нажим кисти, или
преобразованием обьекта каким-либо образом. Нажал кисть, поводил, нарисовал
какое-то пятно, отпустил - создался обьект BlotObj. Состоит из нескольких контуров
(может и из одного), имеет определенный цвет. Объекты этого типа содержатся на
слое.
 */
public class BlotObject {
    private int color;                          // ARGB
    private ArrayList<VContour> contours;       // контуры


    private BlotObject() {
        contours = new ArrayList<>();
    }




}
