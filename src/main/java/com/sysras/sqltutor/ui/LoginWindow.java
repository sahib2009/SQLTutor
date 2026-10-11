package com.sysras.sqltutor.ui;

import com.sysras.sqltutor.model.User;
import com.sysras.sqltutor.service.QuestionService;
import com.sysras.sqltutor.service.UserService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * The first screen. The student logs in here, or clicks register to make an account.
 *
 * @author Sahib Singh
 */
public class LoginWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private UserService userService;
    private QuestionService questionService;

    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginWindow(UserService userService, QuestionService questionService) {
        this.userService = userService;
        this.questionService = questionService;

        setTitle("SQL Tutor - Student Login");
        setSize(400, 260);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
        setVisible(true);
    }

    /**
     * Builds the login screen: title at the top, the two text boxes, then the buttons.
     */
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // title and the little subtitle under it
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        JLabel lblTitle = new JLabel("SQL Tutor", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitle.setForeground(new Color(30, 80, 150));

        JLabel lblSubtitle = new JLabel("IEB Grade 10 - 12 IT Practice", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitle.setForeground(Color.GRAY);

        headerPanel.add(lblTitle);
        headerPanel.add(lblSubtitle);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // username and password boxes
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 12));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtUsername = new JTextField(15);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtPassword = new JPasswordField(15);

        formPanel.add(lblUsername);
        formPanel.add(txtUsername);
        formPanel.add(lblPassword);
        formPanel.add(txtPassword);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // login and register buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton btnLogin = new JButton("Login");
        JButton btnRegister = new JButton("Register New Student");

        btnLogin.addActionListener(e -> handleLogin());
        btnRegister.addActionListener(e -> showRegistrationDialog());

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegister);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * Reads the two boxes and tries to log the student in.
     * If it works, this window closes and the main screen opens.
     */
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        // stop here if they left a box empty, otherwise the login just fails anyway
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both a username and password.",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // UserService checks the username and password against the database
        User user = userService.login(username, password);

        if (user != null) {
            JOptionPane.showMessageDialog(this,
                    "Welcome back, " + user.getFullName() + "!",
                    "Login Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            // close login and open the tutor
            dispose();
            new MainWindow(userService, questionService, user);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password. Please try again or register an account.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Opens a small window where a new student types their name, username and password.
     */
    private void showRegistrationDialog() {
        JDialog dialog = new JDialog(this, "Register New Student Profile", true);
        dialog.setSize(380, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JTextField regName = new JTextField();
        JTextField regUser = new JTextField();
        JPasswordField regPass = new JPasswordField();
        JPasswordField regConfirm = new JPasswordField();

        panel.add(new JLabel("Full Name:"));
        panel.add(regName);
        panel.add(new JLabel("Username:"));
        panel.add(regUser);
        panel.add(new JLabel("Password (min 4 chars):"));
        panel.add(regPass);
        panel.add(new JLabel("Confirm Password:"));
        panel.add(regConfirm);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSubmit = new JButton("Create Account");
        JButton btnCancel = new JButton("Cancel");

        btnSubmit.addActionListener(e -> {
            String name = regName.getText().trim();
            String user = regUser.getText().trim();
            String pass = new String(regPass.getPassword());
            String confirm = new String(regConfirm.getPassword());

            // check the boxes before we try to save anything
            if (name.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "All fields must be filled in.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (user.length() < 3) {
                JOptionPane.showMessageDialog(dialog,
                        "Username must be at least 3 characters long.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (pass.length() < 4) {
                JOptionPane.showMessageDialog(dialog,
                        "Password must be at least 4 characters long.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!pass.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog,
                        "Passwords do not match.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (userService.isUsernameTaken(user)) {
                JOptionPane.showMessageDialog(dialog,
                        "The username '" + user + "' is already taken. Please choose another.",
                        "Username Unavailable",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // UserService saves the account and makes their school database copy
            boolean success = userService.registerUser(user, pass, name);
            if (success) {
                JOptionPane.showMessageDialog(dialog,
                        "Registration successful! You can now log in with your credentials.",
                        "Account Created",
                        JOptionPane.INFORMATION_MESSAGE);
                txtUsername.setText(user);
                txtPassword.setText("");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Failed to create account. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnSubmit);
        btnPanel.add(btnCancel);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}