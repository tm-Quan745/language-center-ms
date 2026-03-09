package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.UserAccount;
import vn.edu.ute.productmgmt.model.enums.UserRole;
import vn.edu.ute.productmgmt.repo.UserAccountRepository;
import vn.edu.ute.productmgmt.repo.jpa.UserAccountRepositoryImpl;
import vn.edu.ute.productmgmt.service.AuthService;
import vn.edu.ute.productmgmt.service.BranchService;
import vn.edu.ute.productmgmt.service.CertificateService;
import vn.edu.ute.productmgmt.service.CourseService;
import vn.edu.ute.productmgmt.service.RoomService;
import vn.edu.ute.productmgmt.service.StudentService;
import vn.edu.ute.productmgmt.service.TeacherService;
import vn.edu.ute.productmgmt.service.StaffService;
import vn.edu.ute.productmgmt.service.EnrollmentService;
import vn.edu.ute.productmgmt.service.InvoiceService;
import vn.edu.ute.productmgmt.service.PaymentService;
import vn.edu.ute.productmgmt.service.PlacementTestService;
import vn.edu.ute.productmgmt.service.PromotionService;
import vn.edu.ute.productmgmt.service.NotificationService;
import vn.edu.ute.productmgmt.service.ClassService;
import vn.edu.ute.productmgmt.service.ScheduleService;
import vn.edu.ute.productmgmt.service.AttendanceService;
import vn.edu.ute.productmgmt.service.ResultService;

import vn.edu.ute.productmgmt.model.Notification;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LcmsMainFrame extends JFrame {

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

    private final StudentPanel studentPanel;
    private final TeacherPanel teacherPanel;
    private final CoursePanel coursePanel;
    private final RoomPanel roomPanel;
    private final BranchPanel branchPanel;
    private final CertificatePanel certificatePanel;
    private final LcmsStaffPanel staffPanel;
    private final EnrollmentPanel enrollmentPanel;
    private final ClassPanel classPanel;
    private final SchedulePanel schedulePanel;
    private final PaymentPanel paymentPanel;
    private final PromotionPanel promotionPanel;
    private final InvoicePanel invoicePanel;
    private final PlacementPanel placementPanel;
    private final NotificationPanel notificationPanel;
    private final AttendancePanel attendancePanel;
    private final ResultPanel resultpanel;

    private final JPanel contentPanel = new JPanel(new CardLayout());
    private JTree menuTree;
    /** Danh sách thông báo theo role, tự tải khi đăng nhập. */
    private List<Notification> cachedNotifications = new ArrayList<>();

    public LcmsMainFrame(UserAccount user,
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
        super("Language Center Management");
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
        this.studentPanel = new StudentPanel(studentService);
        this.teacherPanel = new TeacherPanel(teacherService);
        this.coursePanel = new CoursePanel(courseService);
        this.roomPanel = new RoomPanel(roomService, branchService);
        this.branchPanel = new BranchPanel(branchService);
        this.certificatePanel = new CertificatePanel(certificateService, studentService, classService);
        this.staffPanel = new LcmsStaffPanel(staffService);
        this.enrollmentPanel = new EnrollmentPanel(enrollmentService);
        this.paymentPanel = new PaymentPanel(paymentService, studentService, enrollmentService, invoiceService);
        this.promotionPanel = new PromotionPanel(promotionService);
        this.invoicePanel = new InvoicePanel(invoiceService, studentService, promotionService);
        this.placementPanel = new PlacementPanel(placementTestService, studentService);
        this.notificationPanel = new NotificationPanel(notificationService);
        this.classPanel = new ClassPanel(classService, courseService, teacherService, roomService, branchService);
        this.schedulePanel = new SchedulePanel(classService, scheduleService, roomService);
        this.attendancePanel = new AttendancePanel(attendanceService, classService);
        this.resultpanel = new ResultPanel(resultService, classService, attendanceService);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        buildUI();
        applyAuthorization(); // 🔥 phân quyền
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                loadNotificationsForCurrentUser();
            }
        });
        setSize(1200, 700);
        setLocationRelativeTo(null);
    }

    /** Tự gọi khi đăng nhập xong (frame hiển thị) để lấy thông báo theo role. */
    private void loadNotificationsForCurrentUser() {
        try {
            cachedNotifications = notificationService.findForUserRole(currentUser.getRole());
        } catch (Exception ex) {
            cachedNotifications = List.of();
        }
    }

    private void buildUI() {
        setJMenuBar(createMenuBar());

        JPanel root = new JPanel(new BorderLayout());
        root.add(createTopBar(), BorderLayout.NORTH);
        root.add(createMainArea(), BorderLayout.CENTER);

        setContentPane(root);
    }

    // ===== TOP BAR =====
    private static final DateTimeFormatter NOTIF_DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private JComponent createTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JLabel title = new JLabel("LCMS - Language Center Management");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        JButton btnBell = new JButton("\uD83D\uDD14"); // 🔔
        btnBell.setToolTipText("Thông báo");
        btnBell.setFocusPainted(false);
        btnBell.addActionListener(e -> showNotificationPopup(btnBell));

        JLabel userInfo = new JLabel(
                "Xin chào: " + currentUser.getUsername()
                        + " (" + currentUser.getRole() + ")"
        );
        right.add(btnBell);
        right.add(userInfo);

        top.add(title, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        return top;
    }

    private void showNotificationPopup(Component anchor) {
        List<Notification> list = cachedNotifications;

        JDialog popup = new JDialog(this, "Thông báo", ModalityType.MODELESS);
        popup.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(8, 8));
        main.setBorder(new EmptyBorder(8, 8, 8, 8));

        String[] titles = list.stream()
                .map(n -> (n.getCreatedAt() != null ? n.getCreatedAt().format(NOTIF_DATE_FMT) + " — " : "") + (n.getTitle() != null ? n.getTitle() : ""))
                .toArray(String[]::new);
        JList<String> listUI = new JList<>(titles);
        listUI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listUI.setVisibleRowCount(Math.min(12, Math.max(4, list.size())));
        JScrollPane listScroll = new JScrollPane(listUI);

        JTextArea contentArea = new JTextArea(6, 40);
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScroll = new JScrollPane(contentArea);

        listUI.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int i = listUI.getSelectedIndex();
            if (i >= 0 && i < list.size()) {
                contentArea.setText(list.get(i).getContent() != null ? list.get(i).getContent() : "");
            } else {
                contentArea.setText("");
            }
        });
        if (!list.isEmpty()) {
            listUI.setSelectedIndex(0);
        }

        main.add(new JLabel("Thông báo dành cho: " + currentUser.getRole()), BorderLayout.NORTH);
        main.add(listScroll, BorderLayout.CENTER);
        JPanel detailPanel = new JPanel(new BorderLayout(0, 4));
        detailPanel.add(new JLabel("Nội dung:"), BorderLayout.NORTH);
        detailPanel.add(contentScroll, BorderLayout.CENTER);
        main.add(detailPanel, BorderLayout.SOUTH);

        popup.getContentPane().add(main);
        popup.pack();
        popup.setLocationRelativeTo(anchor);
        if (popup.getLocation().x < 0 || popup.getLocation().y < 0) {
            popup.setLocationRelativeTo(this);
        }
        popup.setVisible(true);
    }

    // ===== MAIN AREA =====
    /** Card key dùng trong contentPanel — phải khớp với key khi add panel. */
    private static final String CARD_STUDENT = "Học viên";
    private static final String CARD_TEACHER = "Giáo viên";
    private static final String CARD_STAFF = "Nhân viên";
    private static final String CARD_BRANCH = "Chi nhánh";
    private static final String CARD_ROOM = "Phòng học";
    private static final String CARD_COURSE = "Khóa học";
    private static final String CARD_CLASS = "Lớp học";
    private static final String CARD_SCHEDULE = "Lịch học";
    private static final String CARD_ENROLLMENT = "Ghi danh";
    private static final String CARD_PLACEMENT = "Kiểm tra xếp lớp";
    private static final String CARD_ATTENDANCE = "Điểm danh";
    private static final String CARD_RESULT = "Kết quả học tập";
    private static final String CARD_CERTIFICATE = "Chứng chỉ";
    private static final String CARD_INVOICE = "Hóa đơn";
    private static final String CARD_PAYMENT = "Thanh toán";
    private static final String CARD_PROMOTION = "Khuyến mãi";
    private static final String CARD_NOTIFICATION = "Thông báo";

    private JComponent createMainArea() {
        JPanel main = new JPanel(new BorderLayout());

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Menu", true);
        menuTree = new JTree(root);
        menuTree.setRootVisible(false);
        menuTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        menuTree.addTreeSelectionListener(e -> {
            TreePath path = e.getNewLeadSelectionPath();
            if (path == null) return;
            Object last = path.getLastPathComponent();
            if (last instanceof DefaultMutableTreeNode node) {
                Object userObj = node.getUserObject();
                if (userObj != null && node.isLeaf()) {
                    CardLayout cl = (CardLayout) contentPanel.getLayout();
                    cl.show(contentPanel, userObj.toString());
                }
            }
        });

        JScrollPane menuScroll = new JScrollPane(menuTree);
        menuScroll.setPreferredSize(new Dimension(220, 0));

        main.add(menuScroll, BorderLayout.WEST);

        // Add tất cả card
        contentPanel.add(studentPanel, CARD_STUDENT);
        contentPanel.add(teacherPanel, CARD_TEACHER);
        contentPanel.add(coursePanel, CARD_COURSE);
        contentPanel.add(roomPanel, CARD_ROOM);
        contentPanel.add(branchPanel, CARD_BRANCH);
        contentPanel.add(certificatePanel, CARD_CERTIFICATE);
        contentPanel.add(staffPanel, CARD_STAFF);
        contentPanel.add(classPanel, CARD_CLASS);
        contentPanel.add(schedulePanel, CARD_SCHEDULE);
        contentPanel.add(enrollmentPanel, CARD_ENROLLMENT);
        contentPanel.add(paymentPanel, CARD_PAYMENT);
        contentPanel.add(promotionPanel, CARD_PROMOTION);
        contentPanel.add(invoicePanel, CARD_INVOICE);
        contentPanel.add(placementPanel, CARD_PLACEMENT);
        contentPanel.add(notificationPanel, CARD_NOTIFICATION);
        contentPanel.add(attendancePanel, CARD_ATTENDANCE);
        contentPanel.add(resultpanel, CARD_RESULT);

        main.add(contentPanel, BorderLayout.CENTER);

        return main;
    }

    /** Xây cây menu theo nhóm chức năng, chỉ thêm các mục user được phép xem. */
    private DefaultMutableTreeNode buildMenuTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Menu", true);

        // Con người
        DefaultMutableTreeNode groupPeople = new DefaultMutableTreeNode("Con người", true);
        groupPeople.add(new DefaultMutableTreeNode(CARD_STUDENT));
        if (currentUser.getRole() == UserRole.Admin) {
            groupPeople.add(new DefaultMutableTreeNode(CARD_TEACHER));
            groupPeople.add(new DefaultMutableTreeNode(CARD_STAFF));
        }
        if (groupPeople.getChildCount() > 0) root.add(groupPeople);

        // Cơ sở
        DefaultMutableTreeNode groupFacility = new DefaultMutableTreeNode("Cơ sở", true);
        groupFacility.add(new DefaultMutableTreeNode(CARD_BRANCH));
        groupFacility.add(new DefaultMutableTreeNode(CARD_ROOM));
        root.add(groupFacility);

        // Học vụ
        DefaultMutableTreeNode groupAcademic = new DefaultMutableTreeNode("Học vụ", true);
        groupAcademic.add(new DefaultMutableTreeNode(CARD_COURSE));
        groupAcademic.add(new DefaultMutableTreeNode(CARD_CLASS));
        groupAcademic.add(new DefaultMutableTreeNode(CARD_SCHEDULE));
        groupAcademic.add(new DefaultMutableTreeNode(CARD_ENROLLMENT));
        groupAcademic.add(new DefaultMutableTreeNode(CARD_PLACEMENT));
        if (currentUser.getRole() == UserRole.Admin) {
            groupAcademic.add(new DefaultMutableTreeNode(CARD_ATTENDANCE));
            groupAcademic.add(new DefaultMutableTreeNode(CARD_RESULT));
        }
        groupAcademic.add(new DefaultMutableTreeNode(CARD_CERTIFICATE));
        root.add(groupAcademic);

        // Tài chính
        DefaultMutableTreeNode groupFinance = new DefaultMutableTreeNode("Tài chính", true);
        groupFinance.add(new DefaultMutableTreeNode(CARD_INVOICE));
        groupFinance.add(new DefaultMutableTreeNode(CARD_PAYMENT));
        groupFinance.add(new DefaultMutableTreeNode(CARD_PROMOTION));
        root.add(groupFinance);

        // Hệ thống
        DefaultMutableTreeNode groupSystem = new DefaultMutableTreeNode("Hệ thống", true);
        groupSystem.add(new DefaultMutableTreeNode(CARD_NOTIFICATION));
        root.add(groupSystem);

        return root;
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

            new LcmsLoginFrame(
                    authService,
                    courseService,
                    roomService,
                    branchService,
                    certificateService,
                    studentService,
                    teacherService,
                    staffService,
                    enrollmentService,
                    paymentService,
                    promotionService,
                    invoiceService,
                    placementTestService,
                    notificationService,
                    classService,
                    scheduleService,
                    attendanceService,
                    resultService
            ).setVisible(true);
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
        DefaultMutableTreeNode root = buildMenuTree();
        menuTree.setModel(new DefaultTreeModel(root));
        for (int i = 0; i < menuTree.getRowCount(); i++) {
            menuTree.expandRow(i);
        }
        // Chọn mục đầu tiên là leaf (card) để hiển thị nội dung
        selectFirstLeaf(root);
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
}