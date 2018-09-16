package com.dekagames.blot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;


/**
 * Чтобы не создавать отдельный класс viewport-а, все его функции выполняет
 * DrawPanel
 */
public class DrawPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
    public static final float MAX_SCALE = 100.0f;
    public static final float MIN_SCALE = 0.01f;


    private double left,top;   // координаты Picture, отображаемые в верхнем левом углу.

    private MainWindow mainWindow;

    private DrawPanelEventListener listener;

    private BufferedImage img;      // изображение на которое рисуется все

    private float fScale;           // масштаб панели



    public DrawPanel(MainWindow parent){
        super();
        mainWindow = parent;
        fScale = 1.0f;             // 100%

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);

        // при изменении размеров панели будем вызывать соответствующий метод
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);

                panelResizedTo(e.getComponent().getWidth(), e.getComponent().getHeight());

            }
        });
    }


    private void panelResizedTo(int width, int height){
        System.out.println("Panel resized to: w=" + width + ", h= " + height);
    }


    public void setEventListener(DrawPanelEventListener listener){
        this.listener = listener;
    }

    public float getScale() {
        return fScale;
    }

    public void setScale(float fscale){
        if (fscale < MIN_SCALE)
            fscale = MIN_SCALE;
        if (fscale > MAX_SCALE)
            fscale = MAX_SCALE;
        fScale = fscale;
    }

    // возвращаем координаты левого верхнего угла панели в векторных координатах Picture
    public double getTop() {
        return top;
    }

    public double getLeft() {
        return left;
    }


    @Override
    public void paint(Graphics gr){
        super.paint(gr);
//        gr.clearRect(0,0, 1000, 1000);
        Picture.getInstance().draw(img);
        gr.drawImage(img, 0,0, null);
    }



    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (listener != null)
            listener.onMousePress(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (listener != null)
            listener.onMouseRelease(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (listener != null)
            listener.onMouseDragged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (listener != null)
            listener.onMouseMove(e);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

    }


}
