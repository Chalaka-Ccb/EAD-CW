package com.example.starkindustry.ui;

import com.example.starkindustry.dao.UserDAO;
import com.example.starkindustry.model.User;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField txtUsername = new JTextField(15);
    private final JPasswordField txtPassword = new JPasswordField(15);
    private final JButton btnLogin = new JButton("Login");
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("Spare Part Shop - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        buildUI();
        pack();
        setSize(380, 280);
        setLocationRelativeTo(null);   // centre on screen
        setResizable(false);
    }

    private void buildUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 5, 8, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Spare Part Shop", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(title, c);

        c.gridwidth = 1;
        c.gridx = 0; c.gridy = 1; panel.add(new JLabel("Username"), c);
        c.gridx = 1;              panel.add(txtUsername, c);
        c.gridx = 0; c.gridy = 2; panel.add(new JLabel("Password"), c);
        c.gridx = 1;              panel.add(txtPassword, c);

        c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
        panel.add(btnLogin, c);

        add(panel);
        getRootPane().setDefaultButton(btnLogin);   // Enter key = click Login
        btnLogin.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        // ----- validation -----
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        StringBuilder errors = new StringBuilder();
        if (username.isEmpty()) {
            errors.append("- Username is required\n");
        }
        if (password.isEmpty()) {
            errors.append("- Password is required\n");
        }
        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this, errors.toString(), "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ----- login -----
        try {
            User user = userDAO.login(username, password);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login failed", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
                return;
            }
            new MainFrame(user).setVisible(true);
            dispose();   // close the login window
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}