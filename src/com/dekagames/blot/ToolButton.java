package com.dekagames.blot;

import javax.swing.*;

public class ToolButton extends JToggleButton {
    private Tool tool;

    public ToolButton(Tool tool){
        super(tool.icon);
        this.tool = tool;
        setToolTipText(tool.hint);
    }

}
