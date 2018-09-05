package com.dekagames.blot;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * Панель инструментов.
 *
 * Передает текущему иснтрументу события о передвижении мыши над DrawPanel
 * Получает события от инструментов о необходимости перерисовки DrawPanel
 *
 *
 */

public class ToolPanel extends JPanel implements DrawPanelEventListener {
    public enum BlotTool {
        PENCIL,
        BRUSH
    }

    private ToolButton btnPencil, btnBrush;
    public MainWindow mainWindow;


    // available tools
    public PencilTool   pencilTool;
    public BrushTool    brushTool;

    // selected tool
    public Tool currentTool;


    public ToolPanel(MainWindow parent){
        super();

        mainWindow = parent;

        // create tools
        pencilTool = new PencilTool(this, new ImageIcon(getClass().getResource("media/pencil.png")),
                "Pencil");
        brushTool = new BrushTool(this, new ImageIcon(getClass().getResource("media/brush.png")),
                "Brush");


        // create buttons
        btnPencil = new ToolButton(pencilTool);     add(btnPencil);
        btnBrush = new ToolButton(brushTool);       add(btnBrush);


        currentTool = brushTool;        // TEST
    }

    //------------------- DrawPanelEventListener methods ----------------------------
    @Override
    public void onMousePress(MouseEvent e) {
        currentTool.mousePress(e);
    }

    @Override
    public void onMouseMove(MouseEvent e) {
        currentTool.mouseMove(e);
    }

    @Override
    public void onMouseRelease(MouseEvent e) {
        currentTool.mouseRelease(e);
    }

    @Override
    public void onMouseDragged(MouseEvent e) {
        currentTool.mouseDragged(e);
    }

}
