package com.dekagames.blot.algorithm;

import com.dekagames.blot.Picture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class VBezierCurve {
    private VPoint startPoint;
    private VPoint endPoint;



    public VBezierCurve(VPoint startPoint, VPoint endPoint){
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }


    /**
     * Возвращает векторные координаты произвольной точки на кривой по параметру t
     *
     * @param t - параметр t - [0, 1]
     * @return
     */
    public VCoords getCurvePoint(double t){
        if (t < 0) t = 0;
        if (t > 1) t = 1;

        double x = (1-t)*(1-t)*(1-t)* startPoint.x +
                     3*(1-t)*(1-t)*t* startPoint.p2.x +
                       3*(1-t)*t*t* endPoint.p1.x +
                          t*t*t* endPoint.x;

        double y = (1-t)*(1-t)*(1-t)* startPoint.y +
                     3*(1-t)*(1-t)*t* startPoint.p2.y +
                       3*(1-t)*t*t* endPoint.p1.y +
                          t*t*t* endPoint.y;

        return new VCoords(x,y);

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
            VCoords cxy = getCurvePoint(t);

            int x = (int)Math.round((cxy.x * fscale - left) / factor);
            int y = (int)Math.round((cxy.y * fscale - top) / factor);

            if (x >= 0 && x < w && y >= 0 && y < h)
                data[y*w+x] = 0xFF000000;
        }
    }
}
