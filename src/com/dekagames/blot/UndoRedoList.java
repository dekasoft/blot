package com.dekagames.blot;

import java.util.ArrayList;

/**
 * Список команд, для которых возможны операции undo/redo
 */
public class UndoRedoList  {
    private ArrayList<UndoRedoCommand> commands;
    private int currIndex;      // индекс последней выполненной команды или -1, если команд нет

    public UndoRedoList(){
        commands = new ArrayList<>();
        currIndex = -1;
    }

    /**
     * добавляет команду в список в соответствии с текущим индексом,
     * все команды выше этого индекса - уничтожаются
     *
     * @param cmd
     */
    public void addCommand(UndoRedoCommand cmd){
        currIndex++;
        commands.add(currIndex, cmd);

        for (int i = commands.size()-1; i > currIndex; i--)
            commands.remove(i);
    }


    /**
     * возвращает true, если операция undo возможна, false если нет
     * @return
     */
    public boolean canUndo(){
        return (commands.size()>0 && currIndex >=0);
    }

    /**
     * возвращает true, если операция redo возможна, false если нет
     * @return
     */
    public boolean canRedo(){
        return (commands.size()>0 && currIndex < commands.size()-1);
    }

}
