package com.dekagames.blot;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Класс изображения. Singleton
 *
 * Координаты точек на изображении задаются значениями типа double. Координата (0,0) - центр
 * картинки. Ось Х - вправо, осьY - вниз. Верхняя граница изображения имеет Y = -1, нижняя -
 * Y = 1. Координата X рассчитывается пропорционально высоте изображения.
 *
 * Например: изображение имеет размер 800 пикселей в ширину и 400 пикселей в высоту.
 * Координата левого верхнего угла (-2.0d; -1.0d), координата правого нижнего угла
 * соответственно (2.0d; 1.0d).
 *
 */
public class Picture {

    private static Picture instance;

    public int width, height;
    public Tool currentTool;

//    private BufferedImage image;
//    private Graphics2D graphics2D;


    public ArrayList<Layer> layers;                 // массив слоев



    private Picture(){
        width = 600;
        height = 600;

//        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//        graphics2D = image.createGraphics();

        layers = new ArrayList<>();
    }

    public static Picture getInstance(){
        if (instance == null){
            instance = new Picture();
        }

        return instance;
    }

    /**
     * Возвращает размер одного пикселя в векторных координатах.
     * Применяется для пересчета временных растровых координат в векторные
     * @return
     */
    public double getPixelSize(){
        return 2.0/height;             // Y [-1;1] приходится на height пикселей
    }



//    public Graphics2D getGraphics(){
//        return graphics2D;
//    }
//
//    public BufferedImage getImage(){
//        return image;
//    }


    // рисует картинку на img (т.е. на DrawPanel)
    public void draw(BufferedImage img){
        for (Layer layer:layers)
            layer.draw(img);

    }

}
