package com.darren.ca.client.view;

import com.darren.ca.client.clientstate.Client;

import javax.swing.*;
import java.awt.*;

public class FTP_Client_GUI {
    private JTextField hostnameTextField;
    private JLabel hostJLabel;
    private JLabel usernameJLabel;
    private JTextField usernameTextField;
    private JLabel passwordJLabel;
    private JPasswordField passwordField;
    private JButton connectBtn;
    private JPanel rootPanel;
    private JPanel topPanel;
    private JTextArea serverOutputTxtArea;
    private JPanel outputPanel;
    private JPanel splitPanel;
    private JButton logoutBtn;
    private JButton chooseFileBtn;
    private JButton downloadBtn;
    private final JFrame jFrame;

    public FTP_Client_GUI(Client client) {
        jFrame = new JFrame("FTP Server");
        jFrame.add(rootPanel);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        int WIDTH = 1200;
        int HEIGHT = 500;
        jFrame.setSize(WIDTH, HEIGHT);
        jFrame.setMinimumSize(new Dimension(850, 500));
        int x_POS = (WIDTH - jFrame.getWidth()) / 2;
        int y_POS = (HEIGHT - jFrame.getHeight()) / 2;
        jFrame.setLocation(x_POS, y_POS);
        jFrame.setLayout(new FlowLayout());
        jFrame.setVisible(true);
//        attach listeners to buttons
        connectBtn.addActionListener(e ->
//                perform client login function
                client.login(
                        usernameTextField.getText(),
                        String.valueOf(passwordField.getPassword())
                )
        );
        logoutBtn.addActionListener(e ->
//                perform client logout function
                client.logout(
                        usernameTextField.getText(),
                        String.valueOf(passwordField.getPassword())
                )
        );
//        upload function
        chooseFileBtn.addActionListener(e -> client.uploadFile());
//        download function
        downloadBtn.addActionListener(e -> client.downloadFile());
    }

    public JFrame getjFrame() {
        return jFrame;
    }

    public void setServerOutputTxtArea(String text) {
        serverOutputTxtArea.setText(text);
    }
}
