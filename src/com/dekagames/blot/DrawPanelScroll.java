package com.dekagames.blot;


import javax.swing.*;
import java.awt.*;

// собственный скролл для DrawPanel
public class DrawPanelScroll extends JPanel {
    private DrawPanel drawPanel;
    private JScrollBar hscroll, vscroll;



    public DrawPanelScroll(DrawPanel drawPanel){
        super(new BorderLayout());

        setMinimumSize(new Dimension(200, 200));
        this.drawPanel = drawPanel;

        hscroll = new JScrollBar(Adjustable.HORIZONTAL);
        add(hscroll, BorderLayout.SOUTH);

        vscroll = new JScrollBar(Adjustable.VERTICAL);
        add(vscroll, BorderLayout.EAST);

        add(drawPanel, BorderLayout.CENTER);
    }
}
