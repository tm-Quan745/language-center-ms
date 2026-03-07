package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.UserAccount;
import vn.edu.ute.productmgmt.model.enums.UserRole;
import vn.edu.ute.productmgmt.repo.UserAccountRepository;
import vn.edu.ute.productmgmt.repo.jpa.UserAccountRepositoryImpl;
import vn.edu.ute.productmgmt.service.AuthService;
import vn.edu.ute.productmgmt.service.CourseService;
import vn.edu.ute.productmgmt.service.RoomService;
import vn.edu.ute.productmgmt.service.StudentService;
import vn.edu.ute.productmgmt.service.TeacherService;
import vn.edu.ute.productmgmt.service.StaffService;
import vn.edu.ute.productmgmt.service.EnrollmentService;
import vn.edu.ute.productmgmt.service.PaymentService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LcmsMainFrame extends JFrame {

    private final UserAccount currentUser;
    private final CourseService courseService;
    private final RoomService roomService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final StaffService staffService;
    private final EnrollmentService enrollmentService;
    private final PaymentService paymentService;

    private final StudentPanel studentPanel;
    private final TeacherPanel teacherPanel;
    private final CoursePanel coursePanel;
    private final RoomPanel roomPanel;
    private final LcmsStaffPanel staffPanel;
    private final EnrollmentPanel enrollmentPanel;
    private final ClassPanel classPanel = new ClassPanel();
    private final SchedulePanel schedulePanel = new SchedulePanel();
    private final PaymentPanel paymentPanel;
    private final AttendancePanel attendancePanel = new AttendancePanel();
    private final ResultPanel resultpanel = new ResultPanel();

    private final JPanel contentPanel = new JPanel(new CardLayout());
    private JList<String> menuList;

    private final List<String> menuItems = new ArrayList<>();

    public LcmsMainFrame(UserAccount user,
                         CourseService courseService,
                         RoomService roomService,
                         StudentService studentService,
                         TeacherService teacherService,
                         StaffService staffService,
                         EnrollmentService enrollmentService,
                         PaymentService paymentService) {
        super("Language Center Management");
        this.currentUser = user;
        this.courseService = courseService;
        this.roomService = roomService;
        this.studentService = studentService;
        this.teacherService = teacherService;
        this.staffService = staffService;
        this.enrollmentService = enrollmentService;
        this.paymentService = paymentService;
        this.studentPanel = new StudentPanel(studentService);
        this.teacherPanel = new TeacherPanel(teacherService);
        this.coursePanel = new CoursePanel(courseService);
        this.roomPanel = new RoomPanel(roomService);
        this.staffPanel = new LcmsStaffPanel(staffService);
        this.enrollmentPanel = new EnrollmentPanel(enrollmentService);
        this.paymentPanel = new PaymentPanel(paymentService);

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
        top.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JLabel title = new JLabel("LCMS - Language Center Management");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        JLabel userInfo = new JLabel(
                "Xin chào: " + currentUser.getUsername()
                        + " (" + currentUser.getRole() + ")"
        );

        top.add(title, BorderLayout.WEST);
        top.add(userInfo, BorderLayout.EAST);

        return top;
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
        contentPanel.add(staffPanel, "Nhân viên");
        contentPanel.add(classPanel, "Lớp học");
        contentPanel.add(schedulePanel, "Lịch học");
        contentPanel.add(enrollmentPanel, "Ghi danh");
        contentPanel.add(paymentPanel, "Thanh toán");
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
                    studentService,
                    teacherService,
                    staffService,
                    enrollmentService,
                    paymentService
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
            menuItems.add("Phòng học");
            menuItems.add("Lớp học");
            menuItems.add("Lịch học");
            menuItems.add("Ghi danh");
            menuItems.add("Thanh toán");
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
        menuItems.add("Lớp học");
        menuItems.add("Lịch học");
        menuItems.add("Nhân viên");
        menuItems.add("Ghi danh");
        menuItems.add("Thanh toán");
        menuItems.add("Điểm danh");
        menuItems.add("Kết quả học tập");
    }
}