package vn.edu.ute.productmgmt.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Demo UI cho hệ thống Language Center Management dùng mock data,
 * layout kiểu dashboard: top bar + sidebar + vùng nội dung (CardLayout).
 */
public class LcmsMainFrame extends JFrame {

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
    private final StaffPanel staffPanel = new StaffPanel();
    private final UserAccountPanel userAccountPanel = new UserAccountPanel();

    private final JPanel contentPanel = new JPanel(new CardLayout());

    public LcmsMainFrame() {
        super("Language Center Management (Demo UI)");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
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

    private JComponent createTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JLabel title = new JLabel("LCMS - Language Center Management");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        JLabel userInfo = new JLabel("Xin chào: Admin");

        top.add(title, BorderLayout.WEST);
        top.add(userInfo, BorderLayout.EAST);
        return top;
    }

    private JComponent createMainArea() {
        JPanel main = new JPanel(new BorderLayout());

        // Sidebar
        String[] items = {
                "Học viên",
                "Giáo viên",
                "Khóa học",
                "Lớp học",
                "Ghi danh",
                "Thanh toán",
                "Lịch học",
                "Điểm danh",
                "Phòng học",
                "Kết quả",
                "Hóa đơn",
                "Nhân viên",
                "Tài khoản"
        };
        JList<String> menu = new JList<>(items);
        menu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menu.setSelectedIndex(0);
        menu.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            String value = menu.getSelectedValue();
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            cl.show(contentPanel, value);
        });
        JScrollPane menuScroll = new JScrollPane(menu);
        menuScroll.setPreferredSize(new Dimension(200, 0));

        main.add(menuScroll, BorderLayout.WEST);

        // Content cards
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

        main.add(contentPanel, BorderLayout.CENTER);

        return main;
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu mSystem = new JMenu("System");
        JMenuItem miExit = new JMenuItem("Exit");
        miExit.addActionListener(e -> dispose());
        mSystem.add(miExit);

        JMenu mReports = new JMenu("Reports");
        mReports.add(new JMenuItem("Revenue by Course"));
        mReports.add(new JMenuItem("Student Summary"));

        bar.add(mSystem);
        bar.add(mReports);
        return bar;
    }
}

