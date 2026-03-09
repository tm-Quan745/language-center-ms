package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import vn.edu.ute.productmgmt.model.UserAccount;
import vn.edu.ute.productmgmt.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LcmsLoginFrame extends JFrame {

    // Components
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    // Services
    private final AuthService authService;
    private final CourseService courseService;
    private final RoomService roomService;
    private final BranchService branchService;
    private final CertificateService certificateService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final StaffService staffService;
    private final EnrollmentService enrollmentService;
    private final PaymentService paymentService;
    private final PromotionService promotionService;
    private final InvoiceService invoiceService;
    private final PlacementTestService placementTestService;
    private final NotificationService notificationService;
    private final ClassService classService;
    private final ScheduleService scheduleService;
    private final AttendanceService attendanceService;
    private final ResultService resultService;

    public LcmsLoginFrame(AuthService authService,
                          CourseService courseService,
                          RoomService roomService,
                          BranchService branchService,
                          CertificateService certificateService,
                          StudentService studentService,
                          TeacherService teacherService,
                          StaffService staffService,
                          EnrollmentService enrollmentService,
                          PaymentService paymentService,
                          PromotionService promotionService,
                          InvoiceService invoiceService,
                          PlacementTestService placementTestService,
                          NotificationService notificationService,
                          ClassService classService,
                          ScheduleService scheduleService,
                          AttendanceService attendanceService,
                          ResultService resultService) {

        // 1. Khởi tạo Giao diện Hiện đại
        try {
            UIManager.setLookAndFeel(new FlatMacLightLaf());
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.authService = authService;
        this.courseService = courseService;
        this.roomService = roomService;
        this.branchService = branchService;
        this.certificateService = certificateService;
        this.studentService = studentService;
        this.teacherService = teacherService;
        this.staffService = staffService;
        this.enrollmentService = enrollmentService;
        this.paymentService = paymentService;
        this.promotionService = promotionService;
        this.invoiceService = invoiceService;
        this.placementTestService = placementTestService;
        this.notificationService = notificationService;
        this.classService = classService;
        this.scheduleService = scheduleService;
        this.attendanceService = attendanceService;
        this.resultService = resultService;

        setTitle("LCMS Enterprise - Sign In");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        buildUI();

        setSize(900, 600); // Kích thước bự và sang trọng
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        // Main Panel chia làm 2 cột
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.setBackground(Color.WHITE);

        // --- BÊN TRÁI: BANNER NGHỆ THUẬT ---
        JPanel leftPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Tạo hiệu ứng màu chuyển từ Xanh đậm sang Xanh nhạt
                GradientPaint gp = new GradientPaint(0, 0, new Color(13, 71, 161), 0, getHeight(), new Color(25, 118, 210));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JLabel lblWelcome = new JLabel("<html><div style='text-align: center;'>Hệ Thống Quản Lý<br><span style='font-size: 14px; font-weight: normal;'>Trung Tâm Ngoại Ngữ LCMS</span></div></html>");
        lblWelcome.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 28));
        lblWelcome.setForeground(Color.WHITE);
        lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);
        leftPanel.add(lblWelcome, BorderLayout.CENTER);

        // --- BÊN PHẢI: FORM ĐĂNG NHẬP ---
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(0, 60, 0, 60));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Tiêu đề thương hiệu
        JLabel lblBrand = new JLabel("Đăng Nhập");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblBrand.setForeground(new Color(33, 33, 33));
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 5, 0);
        rightPanel.add(lblBrand, gbc);

        JLabel lblDesc = new Insets(0, 0, 40, 0) == null ? null : new JLabel("Vui lòng nhập tài khoản để quản trị hệ thống");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDesc.setForeground(Color.GRAY);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 40, 0);
        rightPanel.add(lblDesc, gbc);

        // Ô nhập Username
        txtUsername = new JTextField();
        txtUsername.setPreferredSize(new Dimension(0, 45));
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tên đăng nhập / Email");
        txtUsername.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new JLabel(" 👤 "));
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 15, 0);
        rightPanel.add(txtUsername, gbc);

        // Ô nhập Password
        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(0, 45));
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Mật khẩu");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        txtPassword.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new JLabel(" 🔒 "));
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 25, 0);
        rightPanel.add(txtPassword, gbc);

        // Nút Đăng nhập bự
        btnLogin = new JButton("ĐĂNG NHẬP NGAY");
        btnLogin.setPreferredSize(new Dimension(0, 50));
        btnLogin.setFont(new Font("Segoe UI Bold", Font.PLAIN, 14));
        btnLogin.setBackground(new Color(13, 110, 253));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #084298; outlineWidth: 0;");
        gbc.gridy = 4;
        rightPanel.add(btnLogin, gbc);

        // Nút Đăng ký (Register)
        JButton btnReg = new JButton("Bạn chưa có tài khoản? Đăng ký");
        btnReg.setBorderPainted(false);
        btnReg.setContentAreaFilled(false);
        btnReg.setForeground(new Color(13, 110, 253));
        btnReg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridy = 5; gbc.insets = new Insets(20, 0, 0, 0);
        rightPanel.add(btnReg, gbc);

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);
        setContentPane(mainPanel);

        // --- Xử lý Sự kiện ---
        btnLogin.addActionListener(e -> handleLogin());
        btnReg.addActionListener(e -> showRegisterDialog());
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleLogin();
            }
        });
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            btnLogin.setEnabled(false);
            btnLogin.setText("ĐANG XÁC THỰC...");

            UserAccount user = authService.login(username, password);

            if (user != null) {
                LcmsMainFrame mainFrame = new LcmsMainFrame(
                        user, courseService, roomService, branchService, certificateService,
                        studentService, teacherService, staffService, enrollmentService,
                        paymentService, promotionService, invoiceService, placementTestService,
                        notificationService, classService, scheduleService, attendanceService, resultService
                );
                mainFrame.setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally {
            btnLogin.setEnabled(true);
            btnLogin.setText("ĐĂNG NHẬP NGAY");
        }
    }

    private void showRegisterDialog() {
        // Tự động sử dụng giao diện đồng bộ cho Dialog đăng ký
        JDialog dialog = new JDialog(this, "Đăng ký tài khoản", true);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(10, 30, 10, 30);
        g.gridx = 0;

        JTextField regUser = new JTextField(20);
        regUser.setPreferredSize(new Dimension(0, 40));
        regUser.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tên đăng nhập mới");

        JPasswordField regPass = new JPasswordField(20);
        regPass.setPreferredSize(new Dimension(0, 40));
        regPass.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Mật khẩu");

        JComboBox<String> cboRole = new JComboBox<>(new String[]{"STUDENT", "TEACHER", "STAFF"});
        cboRole.setPreferredSize(new Dimension(0, 40));

        JButton btnSubmit = new JButton("XÁC NHẬN ĐĂNG KÝ");
        btnSubmit.setPreferredSize(new Dimension(0, 45));
        btnSubmit.setBackground(new Color(25, 135, 84));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFont(new Font("Segoe UI Bold", Font.PLAIN, 13));

        g.gridy = 0; dialog.add(new JLabel("TẠO TÀI KHOẢN"), g);
        g.gridy = 1; dialog.add(regUser, g);
        g.gridy = 2; dialog.add(regPass, g);
        g.gridy = 3; dialog.add(cboRole, g);
        g.gridy = 4; g.insets = new Insets(20, 30, 20, 30); dialog.add(btnSubmit, g);

        btnSubmit.addActionListener(e -> {
            try {
                authService.register(regUser.getText(), new String(regPass.getPassword()), (String) cboRole.getSelectedItem());
                JOptionPane.showMessageDialog(dialog, "Đăng ký thành công!");
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage());
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}