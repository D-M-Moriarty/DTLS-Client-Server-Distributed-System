package com.darren.ca.client.view;

import javax.swing.*;
import java.awt.*;

public class ClientUI {
    private JFrame jFrame;
    private JPasswordField passwordField;
    private JTextField usernameField;
    private JTextArea textArea;

    public ClientUI() {
        jFrame = new JFrame("Client UI");

        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Double width = screenSize.width * 0.5;
        Double height = screenSize.height * 0.7;
        jFrame.setSize(width.intValue(), height.intValue());
        jFrame.setMinimumSize(new Dimension(850, 600));
        jFrame.setLocation((screenSize.width - jFrame.getWidth()) / 2,
                (screenSize.height - jFrame.getHeight()) / 2);

        jFrame.setLayout(new FlowLayout());
        jFrame.setVisible(true);
    }

    public static void main(String[] args) {
        new ClientUI();
    }
}
