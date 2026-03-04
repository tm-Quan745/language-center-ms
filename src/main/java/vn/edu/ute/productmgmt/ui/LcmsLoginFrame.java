package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.UserAccount;
import vn.edu.ute.productmgmt.service.AuthService;
import vn.edu.ute.productmgmt.service.CourseService;
import vn.edu.ute.productmgmt.service.RoomService;

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
    private final CourseService courseService;
    private final RoomService roomService;

    public LcmsLoginFrame(AuthService authService, CourseService courseService, RoomService roomService) {
        this.authService = authService;
        this.courseService = courseService;
        this.roomService = roomService;

        setTitle("LCMS - Login");
        setSize(400, 300);
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

        JButton btnRegister = new JButton("Đăng ký");
        gbc.gridy = 3;
        formPanel.add(btnRegister, gbc);

        root.add(formPanel, BorderLayout.CENTER);

        setContentPane(root);

        // ===== Events =====
        btnLogin.addActionListener(this::handleLogin);
        btnRegister.addActionListener(e -> showRegisterDialog());

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

                // Mở MainFrame và truyền user + services
                LcmsMainFrame mainFrame = new LcmsMainFrame(user, courseService, roomService);
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

    private void showRegisterDialog() {
        JDialog dialog = new JDialog(this, "Đăng ký tài khoản", true);
        dialog.setSize(380, 280);
        dialog.setLocationRelativeTo(this);

        JTextField regUsername = new JTextField(18);
        JPasswordField regPassword = new JPasswordField(18);
        JPasswordField regConfirm = new JPasswordField(18);
        JComboBox<String> cboRole = new JComboBox<>(new String[]{"STUDENT", "TEACHER", "STAFF"});

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 8, 5, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("Tên đăng nhập:"), g);
        g.gridx = 1;
        panel.add(regUsername, g);
        row++;
        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("Mật khẩu:"), g);
        g.gridx = 1;
        panel.add(regPassword, g);
        row++;
        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("Xác nhận mật khẩu:"), g);
        g.gridx = 1;
        panel.add(regConfirm, g);
        row++;
        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("Vai trò:"), g);
        g.gridx = 1;
        panel.add(cboRole, g);
        row++;

        JButton btnSubmit = new JButton("Đăng ký");
        JButton btnCancel = new JButton("Hủy");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);

        g.gridx = 0; g.gridy = row; g.gridwidth = 2;
        panel.add(btnPanel, g);

        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        dialog.add(panel);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSubmit.addActionListener(e -> {
            String username = regUsername.getText().trim();
            String password = new String(regPassword.getPassword());
            String confirm = new String(regConfirm.getPassword());
            String role = (String) cboRole.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập tên đăng nhập và mật khẩu.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog, "Mật khẩu và xác nhận mật khẩu không khớp.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                authService.register(username, password, role);
                JOptionPane.showMessageDialog(dialog, "Đăng ký thành công! Bạn có thể đăng nhập bằng tài khoản vừa tạo.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                txtUsername.setText(username);
                dialog.dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Lỗi đăng ký", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }
}