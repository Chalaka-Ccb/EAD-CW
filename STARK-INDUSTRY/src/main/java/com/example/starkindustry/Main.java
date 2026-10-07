package com.example.starkindustry;

import com.formdev.flatlaf.FlatLightLaf;
import com.example.starkindustry.ui.LoginFrame;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        FlatLightLaf.setup();   // modern look for all Swing screens
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}