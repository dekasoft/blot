package com.dekagames.blot;

import com.dekagames.blot.ToolPanel;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;


/**
 * Ancestor class for all tools
 */
public class Tool {
    protected ImageIcon    icon;           // button image
    protected String       hint;           // button hint

    // в начале использования инструмента сюда будем сохранять текущий на тот момент
    // масштаб DrawPanel
    protected float fScale;

    // в начале использования инструмента сюда будем сохранять текущие на тот момент
    // координаты левого верхнего угла DrawPanel
    protected double leftDrawPanel, topDrawPanel;


    protected ToolPanel toolPanel;


    public Tool(ToolPanel parent, ImageIcon icon, String hint){
        toolPanel = parent;
        this.icon = icon;
        this.hint = hint;
    }

    // инструмент окончил рисование, требуется перерисовка Picture на DrawPanel
    // imgTmp - временное изображение инструмента: кисть во время рисования
    // до векторизации, рамка выделения и т.д.
    public void finish(BufferedImage tmpImage){
        toolPanel.mainWindow.drawPanel.drawTmpImg(tmpImage);
    }


    public void mousePress(MouseEvent e){}

    public void mouseMove(MouseEvent e){}

    public void mouseRelease(MouseEvent e){}

    public void mouseDragged(MouseEvent e){}
 }
