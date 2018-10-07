package com.dekagames.blot.tools;

import com.dekagames.blot.UndoRedoCommand;

public class BrushCommand implements UndoRedoCommand {
    @Override
    public boolean undo() {
        return false;
    }

    @Override
    public boolean redo() {
        return false;
    }
}
