package com.dekagames.blot;


import java.awt.event.MouseEvent;

// интерефейс слушателя событий в DrawPanel.
// как правило им является ToolPanel
public interface DrawPanelEventListener {

    public void onMousePress(MouseEvent e);
    public void onMouseMove(MouseEvent e);
    public void onMouseRelease(MouseEvent e);
    public void onMouseDragged(MouseEvent e);

}
