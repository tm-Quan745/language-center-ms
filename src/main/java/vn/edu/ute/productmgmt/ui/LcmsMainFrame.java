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

import vn.edu.ute.productmgmt.model.Notification;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

    private final StudentPanel studentPanel;
    private final TeacherPanel teacherPanel;
    private final CoursePanel coursePanel;
    private final RoomPanel roomPanel;
    private final BranchPanel branchPanel;
    private final CertificatePanel certificatePanel;
    private final LcmsStaffPanel staffPanel;
    private final EnrollmentPanel enrollmentPanel;
    private final ClassPanel classPanel = new ClassPanel();
    private final SchedulePanel schedulePanel = new SchedulePanel();
    private final PaymentPanel paymentPanel;
    private final PromotionPanel promotionPanel;
    private final InvoicePanel invoicePanel;
    private final PlacementPanel placementPanel;
    private final NotificationPanel notificationPanel;
    private final AttendancePanel attendancePanel = new AttendancePanel();
    private final ResultPanel resultpanel = new ResultPanel();

    private final JPanel contentPanel = new JPanel(new CardLayout());
    private JList<String> menuList;

    private final List<String> menuItems = new ArrayList<>();
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
                         NotificationService notificationService) {
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
        this.studentPanel = new StudentPanel(studentService);
        this.teacherPanel = new TeacherPanel(teacherService);
        this.coursePanel = new CoursePanel(courseService);
        this.roomPanel = new RoomPanel(roomService, branchService);
        this.branchPanel = new BranchPanel(branchService);
        this.certificatePanel = new CertificatePanel(certificateService, studentService);
        this.staffPanel = new LcmsStaffPanel(staffService);
        this.enrollmentPanel = new EnrollmentPanel(enrollmentService);
        this.paymentPanel = new PaymentPanel(paymentService, studentService, enrollmentService, invoiceService);
        this.promotionPanel = new PromotionPanel(promotionService);
        this.invoicePanel = new InvoicePanel(invoiceService, studentService, promotionService);
        this.placementPanel = new PlacementPanel(placementTestService, studentService);
        this.notificationPanel = new NotificationPanel(notificationService);

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
    private JComponent createMainArea() {
        JPanel main = new JPanel(new BorderLayout());

        menuList = new JList<>();
        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        menuList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            String value = menuList.getSelectedValue();
            if (value != null) {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, value);
            }
        });

        JScrollPane menuScroll = new JScrollPane(menuList);
        menuScroll.setPreferredSize(new Dimension(200, 0));

        main.add(menuScroll, BorderLayout.WEST);

        // Add tất cả card (phân quyền sẽ quyết định hiển thị)
        contentPanel.add(studentPanel, "Học viên");
        contentPanel.add(teacherPanel, "Giáo viên");
        contentPanel.add(coursePanel, "Khóa học");
        contentPanel.add(roomPanel, "Phòng học");
        contentPanel.add(branchPanel, "Chi nhánh");
        contentPanel.add(certificatePanel, "Chứng chỉ");
        contentPanel.add(staffPanel, "Nhân viên");
        contentPanel.add(classPanel, "Lớp học");
        contentPanel.add(schedulePanel, "Lịch học");
        contentPanel.add(enrollmentPanel, "Ghi danh");
        contentPanel.add(paymentPanel, "Thanh toán");
        contentPanel.add(promotionPanel, "Khuyến mãi");
        contentPanel.add(invoicePanel, "Hóa đơn");
        contentPanel.add(placementPanel, "Kiểm tra xếp lớp");
        contentPanel.add(notificationPanel, "Thông báo");
        contentPanel.add(attendancePanel, "Điểm danh");
        contentPanel.add(resultpanel, "Kết quả học tập");


        main.add(contentPanel, BorderLayout.CENTER);

        return main;
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
                    notificationService
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

        UserRole role = currentUser.getRole();

        menuItems.clear();

        // ADMIN: thấy tất cả
        if (role == UserRole.Admin) {

            addAllMenus();

        }
        // STAFF: hạn chế
        else if (role == UserRole.Staff) {

            menuItems.add("Học viên");
            menuItems.add("Khóa học");
            menuItems.add("Chứng chỉ");
            menuItems.add("Phòng học");
            menuItems.add("Chi nhánh");
            menuItems.add("Lớp học");
            menuItems.add("Lịch học");
            menuItems.add("Ghi danh");
            menuItems.add("Thanh toán");
            menuItems.add("Khuyến mãi");
            menuItems.add("Hóa đơn");
            menuItems.add("Kiểm tra xếp lớp");
            menuItems.add("Thông báo");
        }

        menuList.setListData(menuItems.toArray(new String[0]));
        if (!menuItems.isEmpty()) {
            menuList.setSelectedIndex(0);
        }
    }

    private void addAllMenus() {
        menuItems.add("Học viên");
        menuItems.add("Giáo viên");
        menuItems.add("Khóa học");
        menuItems.add("Phòng học");
        menuItems.add("Chi nhánh");
        menuItems.add("Chứng chỉ");
        menuItems.add("Lớp học");
        menuItems.add("Lịch học");
        menuItems.add("Nhân viên");
        menuItems.add("Ghi danh");
        menuItems.add("Thanh toán");
        menuItems.add("Khuyến mãi");
        menuItems.add("Hóa đơn");
        menuItems.add("Kiểm tra xếp lớp");
        menuItems.add("Thông báo");
        menuItems.add("Điểm danh");
        menuItems.add("Kết quả học tập");
    }
}