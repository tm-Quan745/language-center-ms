package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.UserAccount;
import vn.edu.ute.productmgmt.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LcmsLoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    private final AuthService authService;

    public LcmsLoginFrame(AuthService authService) {
        this.authService = authService;

        setTitle("LCMS - Login");
        setSize(400, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();
    }


    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ===== Title =====
        JLabel lblTitle = new JLabel("Language Center Management", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        root.add(lblTitle, BorderLayout.NORTH);

        // ===== Form Panel =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblUsername = new JLabel("Username:");
        JLabel lblPassword = new JLabel("Password:");

        txtUsername = new JTextField(15);
        txtPassword = new JPasswordField(15);

        btnLogin = new JButton("Đăng nhập");

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(lblUsername, gbc);

        gbc.gridx = 1;
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(lblPassword, gbc);

        gbc.gridx = 1;
        formPanel.add(txtPassword, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(btnLogin, gbc);

        root.add(formPanel, BorderLayout.CENTER);

        setContentPane(root);

        // ===== Events =====
        btnLogin.addActionListener(this::handleLogin);

        // Nhấn Enter để login
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin(null);
                }
            }
        });
    }

    private void handleLogin(ActionEvent e) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập đầy đủ Username và Password!",
                    "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            btnLogin.setEnabled(false);
            btnLogin.setText("Đang xử lý...");

            UserAccount user = authService.login(username, password);

            if (user != null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Đăng nhập thành công!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                // Mở MainFrame và truyền user
                LcmsMainFrame mainFrame = new LcmsMainFrame(user);
                mainFrame.setVisible(true);

                dispose(); // đóng login
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Sai tài khoản hoặc mật khẩu!",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi hệ thống: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } finally {
            btnLogin.setEnabled(true);
            btnLogin.setText("Đăng nhập");
        }
    }
}