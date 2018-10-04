package com.dekagames.blot.algorithm;


/**
 * Класс замкнутого контура, состоящего из векторных точек и соединяющих их сегментов
 * кривых Безье.
 *
 */


import com.dekagames.blot.Picture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;

public class VContour {
    public int color;

    private ArrayList<VPoint> points;


    public VContour(){
        points = new ArrayList<>();
    }


    public void addPoint(VPoint p){
        if (!points.contains(p))
            points.add(p);
    }

    /**
     * Рисует контур на img, в соответствии с left, top и fscale DrawPanel
     * @param img
     */
    public void drawContour(BufferedImage img, float fscale, double left, double top){
        Graphics2D gr = img.createGraphics();

        // преобразуем изображение в массив пикселей
        final int[] data = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        int w = img.getWidth();
        int h = img.getHeight();
        double factor = Picture.getInstance().getPixelSize();

        gr.setColor(Color.BLACK);
        for (VPoint p :points){
            // пересчитаем координаты в растр
            long x0 = Math.round((p.x * fscale - left) / factor);
            long y0 = Math.round((p.y * fscale - top) / factor);

            // опорные точки
            long x1 = Math.round((p.p1.x * fscale - left) / factor);
            long y1 = Math.round((p.p1.y * fscale - top) / factor);

            long x2 = Math.round((p.p2.x * fscale - left) / factor);
            long y2 = Math.round((p.p2.y * fscale - top) / factor);

            // опорные точки
            gr.setColor(Color.MAGENTA);
            gr.drawRect((int)(x1-2), (int)(y1-2), 4, 4);
            gr.drawRect((int)(x2-2), (int)(y2-2), 4, 4);

            gr.setColor(Color.DARK_GRAY);
            gr.drawLine((int) x1, (int) y1, (int)x0, (int)y0);
            gr.drawLine((int) x0, (int) y0, (int)x2, (int)y2);

            // точка
            gr.setColor(Color.GREEN);
            gr.fillOval((int)(x0-2), (int)(y0-2), 4, 4);

//            data[p.y*w+p.x] = col;
        }


        // нарисуем кривые сегменты между точками
        for (int i = 0; i < points.size(); i++){
            VPoint p1 = points.get(i);

            // следующая точка
            int j = i+1;
            while (j>=points.size())
                j-=points.size();

            VPoint p2 = points.get(j);

            new VBezierCurve(p1,p2).draw(img, fscale, left, top);
        }
    }



    // перерассчитывает точки в соответствии с масштабом
    public void scale(double scale){
        for (VPoint p:points){
            p.scale(scale);
        }
    }


    public void translate(double fx, double fy){
        for (VPoint p:points){
            p.translate(fx, fy);
        }
    }



}
