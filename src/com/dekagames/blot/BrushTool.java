package com.dekagames.blot;

import com.dekagames.blot.algorithm.RasterContour;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;



public class BrushTool extends Tool {
    public static int  SIZE;                // реально - половина размера (для ускорения)

    private BufferedImage imgBrush;       // изображение кисти которым будем рисовать
    private BufferedImage imgTmp;         // временное изображение, которое будем обрабатывать

    private DrawPanel drawPanel;          // ссылка на DrawPanel
                                            // оно существует только когда кисть рисует
    private Graphics2D    grTmp;

    private Color color;

    private int x0, y0;                     // предыдущие координаты
    private int x, y;                       // текущие координаты кисти

    private int img_h, img_w;               // размеры imgTmp




    public BrushTool(ToolPanel parent, ImageIcon icon, String hint){
        super(parent, icon, hint);

        SIZE = 20;

        imgBrush = new BufferedImage(2 * SIZE, 2 * SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = imgBrush.createGraphics();

        color = new Color(255, 0,0);
        gr.setColor(color);
        gr.fillOval(0,0, 2 * SIZE, 2 * SIZE);
    }


    public void mousePress(MouseEvent e){
        // создадим временное изображение по размеру рисовательной панели, на которое будем рисовать
        drawPanel = toolPanel.mainWindow.drawPanel;

        // сохраним текущие характеристики DrawPanel, чтобы потом корректно пересчитать
        // все в векторной форме
        fScale = drawPanel.getScale();
        leftDrawPanel = drawPanel.getLeft();
        topDrawPanel = drawPanel.getTop();


        img_h = drawPanel.getHeight();
        img_w = drawPanel.getWidth();
        imgTmp = new BufferedImage(img_w, img_h, BufferedImage.TYPE_INT_ARGB);
        grTmp = imgTmp.createGraphics();

        x = e.getX();
        y = e.getY();

        x0 = x;
        y0 = y;

        grTmp.drawImage(imgBrush, x - SIZE, y - SIZE,null);
        finish(imgTmp);
    }



    public void mouseRelease(MouseEvent e){
        // найдем все контуры
        ArrayList<RasterContour> contours = RasterContour.get_raster_contours(imgTmp, color);//contour_points);

        // тест: нарисуем все контуры
        int col = 0xFF0000FF;
        for (RasterContour c:contours){
            c.toVContour(imgTmp, SIZE);
            c.testDraw(imgTmp, col);
            col = 0xFF000000 | (col * 100);
        }

        finish(imgTmp);
        imgTmp = null;
        grTmp = null;
    }



    public void mouseDragged(MouseEvent e){
        if (grTmp == null)
            return;

        x = e.getX();
        y = e.getY();

        // рисуем кисть в текущем положении
        grTmp.drawImage(imgBrush, x - SIZE, y - SIZE, null);

        //рассчитаем координаты полигона между предыдущей и нынешней точкой
        double distance = Math.hypot(x-x0, y-y0);
        double cosa = (y-y0) / distance;
        double sina = (x-x0) / distance;
        int rcosa = (int)Math.round(SIZE * cosa);
        int rsina = (int)Math.round(SIZE * sina);

        int[] xpoints = {x0-rcosa, x0+rcosa, x+rcosa, x-rcosa};
        int[] ypoints = {y0+rsina, y0-rsina, y-rsina, y+rsina};

        // рисуем полигон по посчитанным четырем точкам
        grTmp.setColor(color);
        grTmp.fillPolygon(xpoints, ypoints, 4);

        // перепишем координаты
        x0 = x;
        y0 = y;

        finish(imgTmp);
    }

}
