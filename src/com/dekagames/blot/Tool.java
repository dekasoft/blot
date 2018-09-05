package com.dekagames.blot;

import javax.swing.*;
import java.awt.event.MouseEvent;


/**
 * Ancestor class for all tools
 */
public class Tool {
    public ImageIcon    icon;           // button image
    public String       hint;           // button hint
    protected ToolPanel   toolPanel;


    public Tool(ToolPanel parent, ImageIcon icon, String hint){
        toolPanel = parent;
        this.icon = icon;
        this.hint = hint;
    }

    // инструмент окончил рисование, требуется перерисовка Picture на DrawPanel
    public void finish(){
        toolPanel.mainWindow.drawPanel.repaint();
    }


    public void mousePress(MouseEvent e){}

    public void mouseMove(MouseEvent e){}

    public void mouseRelease(MouseEvent e){}

    public void mouseDragged(MouseEvent e){}
 }
