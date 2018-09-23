package com.dekagames.blot.algorithm;

import com.dekagames.blot.Picture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class VBezierCurve {
    private VPoint p1;
    private VPoint p2;



    public VBezierCurve(VPoint p1, VPoint p2){
        this.p1 = p1;
        this.p2 = p2;
    }


    /**
     * Возвращает векторные координаты произвольной точки на кривой по параметру t
     *
     * @param t - параметр t - [0, 1]
     * @return
     */
    public CoordsXY getCurvePoint(double t){
        if (t < 0) t = 0;
        if (t > 1) t = 1;

        double x = (1-t)*(1-t)*(1-t)*p1.x +
                     3*(1-t)*(1-t)*t*p1.p2x +
                       3*(1-t)*t*t*p2.p1x +
                          t*t*t*p2.x;

        double y = (1-t)*(1-t)*(1-t)*p1.y +
                     3*(1-t)*(1-t)*t*p1.p2y +
                       3*(1-t)*t*t*p2.p1y +
                          t*t*t*p2.y;

        return new CoordsXY(x,y);

    }


    /**
     * Рисует кривую на BufferedImage
     *
     * @param img - BufferedImage на который нужно нарисовать (DrawPanel)
     * @param fscale - масштаб изображения
     * @param left - векторная координата x верхнего левого угла
     * @param top - векторная координата y верхнего левого угла
     */
    public void draw(BufferedImage img, float fscale, double left, double top){
//        Graphics2D gr = img.createGraphics();

        // преобразуем изображение в массив пикселей
        final int[] data = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        int w = img.getWidth();
        int h = img.getHeight();
        double factor = Picture.getInstance().getPixelSize();

//        gr.setColor(Color.BLACK);

        for(double t = 0; t <= 1; t+=0.01){
            CoordsXY cxy = getCurvePoint(t);

            int x = (int)Math.round((cxy.getX() * fscale - left) / factor);
            int y = (int)Math.round((cxy.getY() * fscale - top) / factor);

            if (x >= 0 && x < w && y >= 0 && y < h)
                data[y*w+x] = 0xFF000000;
        }
    }
}
