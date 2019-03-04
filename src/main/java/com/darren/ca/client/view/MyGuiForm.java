package com.darren.ca.client.view;

import com.darren.ca.client.Client;
import com.darren.ca.client.FileTransferClient;

import javax.swing.*;
import java.awt.*;

public class MyGuiForm {
    private final int WIDTH = 1200;
    private final int HEIGHT = 985;
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
    private JFrame jFrame;
    private Client client;

    public MyGuiForm(FileTransferClient fileTransferClient) {
        client = fileTransferClient;
        jFrame = new JFrame("FTP Server");
        jFrame.add(rootPanel);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        jFrame.setSize(WIDTH, HEIGHT);
        jFrame.setMinimumSize(new Dimension(850, 600));
        jFrame.setLocation((WIDTH - jFrame.getWidth()) / 2,
                (HEIGHT - jFrame.getHeight()) / 2);

        jFrame.setLayout(new FlowLayout());
        jFrame.setVisible(true);
        connectBtn.addActionListener(e ->
                client.login(
                        usernameTextField.getText(),
                        String.valueOf(passwordField.getPassword())
                )
        );
    }

}
