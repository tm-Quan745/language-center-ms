package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Notification;
import vn.edu.ute.productmgmt.model.UserAccount;
import vn.edu.ute.productmgmt.model.enums.StaffRole;
import vn.edu.ute.productmgmt.model.enums.UserRole;
import vn.edu.ute.productmgmt.repo.UserAccountRepository;
import vn.edu.ute.productmgmt.repo.jpa.UserAccountRepositoryImpl;
import vn.edu.ute.productmgmt.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class LcmsMainFrame extends JFrame {

    // Services
    private final UserAccount currentUser;
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

    // UI Components
    private final JPanel contentPanel = new JPanel(new CardLayout());
    private JTree menuTree;
    private JLabel lblCurrentTitle;
    private List<Notification> cachedNotifications = new ArrayList<>();

    // Menu Constants
    private static final String CARD_STUDENT = "Quản lý Học viên";
    private static final String CARD_TEACHER = "Quản lý Giáo viên";
    private static final String CARD_STAFF = "Nhân viên hệ thống";
    private static final String CARD_BRANCH = "Chi nhánh trung tâm";
    private static final String CARD_ROOM = "Phòng học & Cơ sở";
    private static final String CARD_COURSE = "Danh mục Khóa học";
    private static final String CARD_CLASS = "Danh sách Lớp học";
    private static final String CARD_SCHEDULE = "Thời khóa biểu";
    private static final String CARD_ENROLLMENT = "Đăng ký Ghi danh";
    private static final String CARD_PLACEMENT = "Thi xếp lớp";
    private static final String CARD_ATTENDANCE = "Điểm danh học viên";
    private static final String CARD_RESULT = "Bảng điểm & Kết quả";
    private static final String CARD_CERTIFICATE = "Cấp chứng chỉ";
    private static final String CARD_INVOICE = "Quản lý Hóa đơn";
    private static final String CARD_PAYMENT = "Lịch sử Thanh toán";
    private static final String CARD_PROMOTION = "Chương trình Ưu đãi";
    private static final String CARD_NOTIFICATION = "Thông báo chung";
    private static final String CARD_ACCOUNT = "Tạo tài khoản";

    public LcmsMainFrame(UserAccount user, CourseService courseService, RoomService roomService,
                         BranchService branchService, CertificateService certificateService,
                         StudentService studentService, TeacherService teacherService,
                         StaffService staffService, EnrollmentService enrollmentService,
                         PaymentService paymentService, PromotionService promotionService,
                         InvoiceService invoiceService, PlacementTestService placementTestService,
                         NotificationService notificationService, ClassService classService,
                         ScheduleService scheduleService, AttendanceService attendanceService,
                         ResultService resultService) {

        setupTheme();

        this.currentUser = user;
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

        setTitle("LCMS Enterprise Edition - " + currentUser.getUsername());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        buildUI();
        applyAuthorization();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                loadNotificationsForCurrentUser();
            }
        });

        setSize(1400, 850);
        setLocationRelativeTo(null);
    }

    private void setupTheme() {
        try {
            UIManager.setLookAndFeel(new FlatMacLightLaf());
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("ScrollBar.width", 8);
            UIManager.put("Tree.selectionBackground", new Color(240, 247, 255));
            UIManager.put("Tree.selectionForeground", new Color(13, 110, 253));
            UIManager.put("Tree.rowHeight", 45);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());

        // 1. Sidebar (Menu bên trái)
        root.add(createSidebar(), BorderLayout.WEST);

        // 2. Main Content (TopBar + Content)
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(new Color(252, 253, 255));

        mainArea.add(createTopBar(), BorderLayout.NORTH);

        setupContentPanel();
        mainArea.add(contentPanel, BorderLayout.CENTER);

        root.add(mainArea, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JComponent createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBackground(new Color(248, 249, 252));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 235, 240)));

        // Header Sidebar (Logo)
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(30, 25, 20, 25));

        JLabel logo = new JLabel("LCMS PRO");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logo.setForeground(new Color(13, 110, 253));
        header.add(logo, BorderLayout.NORTH);

        JLabel subLogo = new JLabel("Management System");
        subLogo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLogo.setForeground(Color.GRAY);
        header.add(subLogo, BorderLayout.SOUTH);

        sidebar.add(header, BorderLayout.NORTH);

        // Menu Tree
        menuTree = new JTree();
        menuTree.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        menuTree.setRootVisible(false);
        menuTree.setOpaque(false);
        menuTree.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Renderer tùy chỉnh để Sidebar sạch sẽ hơn
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);
        renderer.setBackgroundNonSelectionColor(new Color(248, 249, 252));
        menuTree.setCellRenderer(renderer);

        menuTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) menuTree.getLastSelectedPathComponent();
            if (node != null && node.isLeaf()) {
                String cardName = node.getUserObject().toString();
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, cardName);
                lblCurrentTitle.setText(cardName);
            }
        });

        JScrollPane scrollPane = new JScrollPane(menuTree);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        sidebar.add(scrollPane, BorderLayout.CENTER);

        return sidebar;
    }

    private JComponent createTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setPreferredSize(new Dimension(0, 80));
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));

        // Title động bên trái
        lblCurrentTitle = new JLabel("Dashboard");
        lblCurrentTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblCurrentTitle.setBorder(new EmptyBorder(0, 30, 0, 0));

        // Phía bên phải (Thông báo + User)
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        rightActions.setOpaque(false);

        JButton btnBell = new JButton("🔔");
        btnBell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBell.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btnBell.addActionListener(e -> showNotificationPopup(btnBell));

        JPanel userBox = new JPanel(new GridLayout(2, 1));
        userBox.setOpaque(false);
        JLabel uName = new JLabel(currentUser.getUsername());
        uName.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        JLabel uRole = new JLabel(currentUser.getRole().toString());
        uRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        uRole.setForeground(Color.GRAY);
        userBox.add(uName);
        userBox.add(uRole);

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.putClientProperty(FlatClientProperties.STYLE, "background: #fff0f0; foreground: #e63946; borderWidth: 0");
        btnLogout.addActionListener(e -> handleLogout());

        rightActions.add(btnBell);
        rightActions.add(userBox);
        rightActions.add(btnLogout);

        top.add(lblCurrentTitle, BorderLayout.WEST);
        top.add(rightActions, BorderLayout.EAST);

        return top;
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            UserAccountRepository userRepo = new UserAccountRepositoryImpl();
            AuthService auth = new AuthService(userRepo, new TransactionManager());
            new LcmsLoginFrame(auth, courseService, roomService, branchService, certificateService,
                    studentService, teacherService, staffService, enrollmentService, paymentService,
                    promotionService, invoiceService, placementTestService, notificationService,
                    classService, scheduleService, attendanceService, resultService).setVisible(true);
        }
    }

    private void setupContentPanel() {
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(25, 30, 30, 30));

        // Các Panel con (Giữ nguyên logic của bạn)
        contentPanel.add(new StudentPanel(studentService), CARD_STUDENT);
        contentPanel.add(new TeacherPanel(teacherService), CARD_TEACHER);
        contentPanel.add(new CoursePanel(courseService), CARD_COURSE);
        contentPanel.add(new RoomPanel(roomService, branchService), CARD_ROOM);
        contentPanel.add(new BranchPanel(branchService), CARD_BRANCH);
        contentPanel.add(new UserAccountPanel(), CARD_ACCOUNT);
        contentPanel.add(new CertificatePanel(certificateService, studentService, classService,currentUser), CARD_CERTIFICATE);
        contentPanel.add(new LcmsStaffPanel(staffService), CARD_STAFF);
        contentPanel.add(new ClassPanel(classService, courseService, teacherService, roomService, branchService, currentUser), CARD_CLASS);
        contentPanel.add(new SchedulePanel(scheduleService, classService, roomService, currentUser), CARD_SCHEDULE);
        contentPanel.add(new EnrollmentPanel(enrollmentService, studentService, classService), CARD_ENROLLMENT);

        // Create PaymentPanel and InvoicePanel
        PaymentPanel paymentPanel = new PaymentPanel(paymentService, studentService, enrollmentService, invoiceService);
        InvoicePanel invoicePanel = new InvoicePanel(invoiceService, studentService, promotionService);

        // Add listener so InvoicePanel refreshes when Payment changes
        paymentPanel.addPropertyChangeListener("invoicesChanged", evt -> {
            invoicePanel.loadTable();
        });

        contentPanel.add(paymentPanel, CARD_PAYMENT);
        contentPanel.add(new PromotionPanel(promotionService), CARD_PROMOTION);
        contentPanel.add(invoicePanel, CARD_INVOICE);

        contentPanel.add(new PlacementPanel(placementTestService, studentService), CARD_PLACEMENT);
        contentPanel.add(new NotificationPanel(notificationService), CARD_NOTIFICATION);
        contentPanel.add(new AttendancePanel(attendanceService, classService, currentUser), CARD_ATTENDANCE);
        contentPanel.add(new ResultPanel(resultService, classService, attendanceService, currentUser), CARD_RESULT);
//        contentPanel.add(new StudentClassPanel(classService,currentUser),CARD_STUDENTCLASS);
    }

    private DefaultMutableTreeNode buildMenuTree() {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");

        UserRole role = currentUser.getRole();
        StaffRole staffRole = null;
        if (role == UserRole.Staff && currentUser.getStaff() != null) {
            staffRole = currentUser.getStaff().getRole();
        }

        /* ===================== ADMIN ===================== */
        if (role == UserRole.Admin) {

            DefaultMutableTreeNode gAdmin = new DefaultMutableTreeNode("HỆ THỐNG");
            gAdmin.add(new DefaultMutableTreeNode(CARD_STAFF));
            gAdmin.add(new DefaultMutableTreeNode(CARD_BRANCH));
            gAdmin.add(new DefaultMutableTreeNode(CARD_ACCOUNT));
            gAdmin.add(new DefaultMutableTreeNode(CARD_NOTIFICATION));
            root.add(gAdmin);

            DefaultMutableTreeNode gAcademic = new DefaultMutableTreeNode("HỌC VỤ");
            gAcademic.add(new DefaultMutableTreeNode(CARD_COURSE));
            gAcademic.add(new DefaultMutableTreeNode(CARD_CLASS));
            gAcademic.add(new DefaultMutableTreeNode(CARD_SCHEDULE));
            gAcademic.add(new DefaultMutableTreeNode(CARD_ROOM));
            root.add(gAcademic);

            DefaultMutableTreeNode gPeople = new DefaultMutableTreeNode("NHÂN SỰ");
            gPeople.add(new DefaultMutableTreeNode(CARD_STUDENT));
            gPeople.add(new DefaultMutableTreeNode(CARD_TEACHER));
            gPeople.add(new DefaultMutableTreeNode(CARD_ENROLLMENT));
            gPeople.add(new DefaultMutableTreeNode(CARD_PLACEMENT));
            gPeople.add(new DefaultMutableTreeNode(CARD_ATTENDANCE));
            gPeople.add(new DefaultMutableTreeNode(CARD_RESULT));
            gPeople.add(new DefaultMutableTreeNode(CARD_CERTIFICATE));
            root.add(gPeople);

            DefaultMutableTreeNode gFinance = new DefaultMutableTreeNode("TÀI CHÍNH");
            gFinance.add(new DefaultMutableTreeNode(CARD_INVOICE));
            gFinance.add(new DefaultMutableTreeNode(CARD_PAYMENT));
            gFinance.add(new DefaultMutableTreeNode(CARD_PROMOTION));
            root.add(gFinance);
        }

        /* ===================== STAFF ===================== */
        if (role == UserRole.Staff) {

            // Nhóm HỌC VIÊN
            DefaultMutableTreeNode gPeople = new DefaultMutableTreeNode("HỌC VIÊN");
            gPeople.add(new DefaultMutableTreeNode(CARD_STUDENT));

            if (staffRole == StaffRole.Consultant || staffRole == StaffRole.Manager || staffRole == StaffRole.Admin) {
                gPeople.add(new DefaultMutableTreeNode(CARD_ENROLLMENT));   // Ghi danh
                gPeople.add(new DefaultMutableTreeNode(CARD_PLACEMENT));    // Kiểm tra xếp lớp
            }
            if (staffRole == StaffRole.Manager || staffRole == StaffRole.Admin) {
                gPeople.add(new DefaultMutableTreeNode(CARD_RESULT));       // Kết quả
                gPeople.add(new DefaultMutableTreeNode(CARD_CERTIFICATE));  // Chứng chỉ
            }
            root.add(gPeople);

            // Nhóm TÀI CHÍNH
            if (staffRole == StaffRole.Accountant || staffRole == StaffRole.Manager || staffRole == StaffRole.Admin) {
                DefaultMutableTreeNode gFinance = new DefaultMutableTreeNode("TÀI CHÍNH");
                gFinance.add(new DefaultMutableTreeNode(CARD_INVOICE));     // Hóa đơn
                gFinance.add(new DefaultMutableTreeNode(CARD_PAYMENT));     // Thanh toán
                gFinance.add(new DefaultMutableTreeNode(CARD_PROMOTION));   // Khuyến mãi
                root.add(gFinance);
            }
        }

        /* ===================== TEACHER ===================== */
        if (role == UserRole.Teacher) {

            DefaultMutableTreeNode gTeaching = new DefaultMutableTreeNode("GIẢNG DẠY");

            gTeaching.add(new DefaultMutableTreeNode(CARD_CLASS));
            gTeaching.add(new DefaultMutableTreeNode(CARD_SCHEDULE));
            gTeaching.add(new DefaultMutableTreeNode(CARD_ATTENDANCE));
            gTeaching.add(new DefaultMutableTreeNode(CARD_RESULT));

            root.add(gTeaching);
        }

        /* ===================== STUDENT ===================== */
        if (role == UserRole.Student) {

            DefaultMutableTreeNode gStudent = new DefaultMutableTreeNode("HỌC TẬP");

            gStudent.add(new DefaultMutableTreeNode(CARD_CLASS));
            gStudent.add(new DefaultMutableTreeNode(CARD_SCHEDULE));
            gStudent.add(new DefaultMutableTreeNode(CARD_RESULT));
            gStudent.add(new DefaultMutableTreeNode(CARD_CERTIFICATE));

            root.add(gStudent);
        }

        return root;
    }

    private void applyAuthorization() {
        menuTree.setModel(new DefaultTreeModel(buildMenuTree()));
        // Mở rộng tất cả các nhánh menu khi khởi động
        for (int i = 0; i < menuTree.getRowCount(); i++) {
            menuTree.expandRow(i);
        }
        selectFirstLeaf((DefaultMutableTreeNode) menuTree.getModel().getRoot());
    }

    private void selectFirstLeaf(DefaultMutableTreeNode node) {
        if (node.isLeaf()) {
            menuTree.setSelectionPath(new TreePath(node.getPath()));
            return;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            selectFirstLeaf((DefaultMutableTreeNode) node.getChildAt(i));
            if (menuTree.getSelectionPath() != null) return;
        }
    }

    private void loadNotificationsForCurrentUser() {
        try {
            cachedNotifications = notificationService.findForUserRole(currentUser.getRole());
        } catch (Exception ex) {
            cachedNotifications = List.of();
        }
    }

    private void showNotificationPopup(Component anchor) {

        JDialog popup = new JDialog(this, "Thông báo hệ thống", false);
        popup.setLayout(new BorderLayout());

        DefaultListModel<String> model = new DefaultListModel<>();

        if(cachedNotifications.isEmpty()){
            model.addElement("Không có thông báo");
        }

        for (Notification n : cachedNotifications) {
            model.addElement(
                    "<html><b>" + n.getTitle() +
                            "</b><br><small>" +
                            n.getContent() +
                            "</small></html>"
            );
        }

        JList<String> list = new JList<>(model);
        list.setFixedCellHeight(60);
        list.setBorder(new EmptyBorder(10,10,10,10));

        popup.add(new JScrollPane(list), BorderLayout.CENTER);

        popup.setSize(350,450);

        Point p = anchor.getLocationOnScreen();
        popup.setLocation(p.x - 300, p.y + 40);

        popup.setVisible(true);
    }
}

