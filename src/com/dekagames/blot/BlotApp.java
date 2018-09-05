package com.dekagames.blot;

import javax.swing.*;

public class BlotApp {
    public static MainWindow mainWindow;

    public BlotApp(){
        mainWindow = new MainWindow(this);
        mainWindow.setVisible(true);
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new BlotApp();
            }
        });
    }

}
