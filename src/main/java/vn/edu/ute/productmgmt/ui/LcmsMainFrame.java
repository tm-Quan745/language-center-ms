package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.UserAccount;
import vn.edu.ute.productmgmt.repo.UserAccountRepository;
import vn.edu.ute.productmgmt.repo.jpa.UserAccountRepositoryImpl;
import vn.edu.ute.productmgmt.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LcmsMainFrame extends JFrame {

    private final UserAccount currentUser;

    private final StudentPanel studentPanel = new StudentPanel();
    private final TeacherPanel teacherPanel = new TeacherPanel();
    private final CoursePanel coursePanel = new CoursePanel();
    private final ClassPanel classPanel = new ClassPanel();
    private final EnrollmentPanel enrollmentPanel = new EnrollmentPanel();
    private final PaymentPanel paymentPanel = new PaymentPanel();
    private final SchedulePanel schedulePanel = new SchedulePanel();
    private final AttendancePanel attendancePanel = new AttendancePanel();
    private final RoomPanel roomPanel = new RoomPanel();
    private final ResultPanel resultPanel = new ResultPanel();
    private final InvoicePanel invoicePanel = new InvoicePanel();
    private final LcmsStaffPanel staffPanel = new LcmsStaffPanel();
    private final UserAccountPanel userAccountPanel = new UserAccountPanel();

    private final JPanel contentPanel = new JPanel(new CardLayout());
    private JList<String> menuList;

    private final List<String> menuItems = new ArrayList<>();

    private JPanel sidebarPanel;
    public LcmsMainFrame(UserAccount user) {
        super("Language Center Management");
        this.currentUser = user;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        buildUI();
        applyAuthorization(); // 🔥 phân quyền
        setSize(1200, 700);
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        setJMenuBar(createMenuBar());

        JPanel root = new JPanel(new BorderLayout());
        root.add(createTopBar(), BorderLayout.NORTH);
        root.add(createMainArea(), BorderLayout.CENTER);

        setContentPane(root);
    }

    // ===== TOP BAR =====
    private JComponent createTopBar() {

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("LCMS - Language Center Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel userInfo = new JLabel(
                currentUser.getUsername() + " (" + currentUser.getRole() + ")"
        );
        userInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        top.add(title, BorderLayout.WEST);
        top.add(userInfo, BorderLayout.EAST);

        return top;
    }

    private void addAllCards() {
        contentPanel.add(studentPanel, "Học viên");
        contentPanel.add(teacherPanel, "Giáo viên");
        contentPanel.add(coursePanel, "Khóa học");
        contentPanel.add(classPanel, "Lớp học");
        contentPanel.add(enrollmentPanel, "Ghi danh");
        contentPanel.add(paymentPanel, "Thanh toán");
        contentPanel.add(schedulePanel, "Lịch học");
        contentPanel.add(attendancePanel, "Điểm danh");
        contentPanel.add(roomPanel, "Phòng học");
        contentPanel.add(resultPanel, "Kết quả");
        contentPanel.add(invoicePanel, "Hóa đơn");
        contentPanel.add(staffPanel, "Nhân viên");
        contentPanel.add(userAccountPanel, "Tài khoản");
    }
    // ===== MAIN AREA =====
    private JComponent createMainArea() {

        JPanel main = new JPanel(new BorderLayout());

        // ===== SIDEBAR =====
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(33, 150, 243));
        sidebar.setPreferredSize(new Dimension(220, 0));

        JScrollPane sidebarScroll = new JScrollPane(sidebar);
        sidebarScroll.setBorder(null);

        main.add(sidebarScroll, BorderLayout.WEST);

        // ===== CONTENT =====
        contentPanel.setBackground(new Color(245, 247, 250));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addAllCards();

        main.add(contentPanel, BorderLayout.CENTER);

        // 🔥 Lưu sidebar để applyAuthorization sử dụng
        this.sidebarPanel = sidebar;

        return main;
    }

    private JButton createMenuButton(String name) {

        JButton btn = new JButton(name);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(33, 150, 243));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        btn.addActionListener(e -> {
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            cl.show(contentPanel, name);
        });

        return btn;
    }

    // ===== MENU BAR =====
    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu mSystem = new JMenu("System");

        JMenuItem miLogout = new JMenuItem("Logout");

        miLogout.addActionListener(e -> {
            dispose();

            UserAccountRepository userRepo = new UserAccountRepositoryImpl();
            TransactionManager tx = new TransactionManager();
            AuthService authService = new AuthService(userRepo, tx);

            new LcmsLoginFrame(authService).setVisible(true);
        });

        JMenuItem miExit = new JMenuItem("Exit");
        miExit.addActionListener(e -> System.exit(0));

        mSystem.add(miLogout);
        mSystem.addSeparator();
        mSystem.add(miExit);

        bar.add(mSystem);

        return bar;
    }

    // ===== PHÂN QUYỀN =====
    private void applyAuthorization() {

        String role = currentUser.getRole();

        sidebarPanel.removeAll();

        if ("ADMIN".equalsIgnoreCase(role)) {

            addSidebarButton("Học viên");
            addSidebarButton("Giáo viên");
            addSidebarButton("Khóa học");
            addSidebarButton("Lớp học");
            addSidebarButton("Ghi danh");
            addSidebarButton("Thanh toán");
            addSidebarButton("Lịch học");
            addSidebarButton("Điểm danh");
            addSidebarButton("Phòng học");
            addSidebarButton("Kết quả");
            addSidebarButton("Hóa đơn");
            addSidebarButton("Nhân viên");
            addSidebarButton("Tài khoản");

        } else if ("STAFF".equalsIgnoreCase(role)) {

            addSidebarButton("Học viên");
            addSidebarButton("Khóa học");
            addSidebarButton("Lớp học");
            addSidebarButton("Ghi danh");
            addSidebarButton("Thanh toán");
            addSidebarButton("Lịch học");
            addSidebarButton("Điểm danh");
            addSidebarButton("Kết quả");
            addSidebarButton("Hóa đơn");
        }

        sidebarPanel.revalidate();
        sidebarPanel.repaint();
    }
    private void addSidebarButton(String name) {
        sidebarPanel.add(createMenuButton(name));
    }

    private void addAllMenus() {
        menuItems.add("Học viên");
        menuItems.add("Giáo viên");
        menuItems.add("Khóa học");
        menuItems.add("Lớp học");
        menuItems.add("Ghi danh");
        menuItems.add("Thanh toán");
        menuItems.add("Lịch học");
        menuItems.add("Điểm danh");
        menuItems.add("Phòng học");
        menuItems.add("Kết quả");
        menuItems.add("Hóa đơn");
        menuItems.add("Nhân viên");
        menuItems.add("Tài khoản");
    }
}