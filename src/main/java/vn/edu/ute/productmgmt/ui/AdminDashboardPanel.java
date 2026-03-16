package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Invoice;
import vn.edu.ute.productmgmt.model.Staff;
import vn.edu.ute.productmgmt.model.Teacher;
import vn.edu.ute.productmgmt.model.enums.InvoiceStatus;
import vn.edu.ute.productmgmt.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Dashboard tổng quan dành cho Admin (Giao diện đã được cải tiến).
 */
public class AdminDashboardPanel extends JPanel {

    private final StudentService studentService;
    private final ClassService classService;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final StaffService staffService;
    private final TeacherService teacherService;

    private final JLabel lblStudentCount = new JLabel("0");
    private final JLabel lblClassCount = new JLabel("0");
    private final JLabel lblInvoiceCount = new JLabel("0");
    private final JLabel lblPaymentCount = new JLabel("0");
    private final JLabel lblStaffCount = new JLabel("0");
    private final JLabel lblTeacherCount = new JLabel("0");
    private final JLabel lblRevenue = new JLabel("0 đ");
    private final JLabel lblReceivable = new JLabel("0 đ");

    private final RevenueChartPanel revenueChartPanel = new RevenueChartPanel();

    public AdminDashboardPanel(StudentService studentService,
                               ClassService classService,
                               InvoiceService invoiceService,
                               PaymentService paymentService,
                               StaffService staffService,
                               TeacherService teacherService) {
        this.studentService = studentService;
        this.classService = classService;
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
        this.staffService = staffService;
        this.teacherService = teacherService;

        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 25, 20, 25)); // Tăng padding tổng thể cho thoáng

        buildUI();
        loadStats();
    }

    private void buildUI() {
        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Bảng điều khiển quản trị");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(40, 40, 40));

        JLabel subtitle = new JLabel("Tổng quan nhanh về hoạt động và doanh thu trung tâm");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(120, 120, 120));
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(subtitle);

        header.add(titleBox, BorderLayout.WEST);

        JButton btnRefresh = new JButton("🔄 Làm mới dữ liệu");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setPreferredSize(new Dimension(160, 40));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE,
                "background:#0d6efd;foreground:#ffffff;arc:10;borderWidth:0;focusWidth:0;hoverBackground:#0b5ed7");
        btnRefresh.addActionListener(e -> loadStats());
        
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        right.setOpaque(false);
        right.add(btnRefresh);
        header.add(right, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // --- CENTER CONTENT ---
        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setOpaque(false);

        // Hàng 1: Tài chính (2 ô lớn - Solid Color)
        JPanel financialRow = new JPanel(new GridLayout(1, 2, 20, 0));
        financialRow.setOpaque(false);
        financialRow.setPreferredSize(new Dimension(0, 130)); // Đặt chiều cao cố định cho đẹp
        financialRow.add(createStatCard("Tổng doanh thu (Đã nhận)", "💰", lblRevenue, new Color(25, 135, 84), true));
        financialRow.add(createStatCard("Công nợ (Cần thu)", "⚠️", lblReceivable, new Color(220, 53, 69), true));

        // Hàng 2 & 3: Chỉ số hoạt động (Lưới 2x3 - Outline/White Color)
        JPanel metricsGrid = new JPanel(new GridLayout(2, 3, 20, 20));
        metricsGrid.setOpaque(false);
        metricsGrid.add(createStatCard("Học viên", "👨‍🎓", lblStudentCount, new Color(13, 110, 253), false));
        metricsGrid.add(createStatCard("Lớp học", "🏫", lblClassCount, new Color(13, 202, 240), false));
        metricsGrid.add(createStatCard("Hóa đơn", "🧾", lblInvoiceCount, new Color(255, 193, 7), false));
        metricsGrid.add(createStatCard("Thanh toán", "💳", lblPaymentCount, new Color(253, 126, 20), false));
        metricsGrid.add(createStatCard("Nhân viên", "👔", lblStaffCount, new Color(111, 66, 193), false));
        metricsGrid.add(createStatCard("Giáo viên", "📚", lblTeacherCount, new Color(32, 201, 151), false));

        center.add(financialRow, BorderLayout.NORTH);
        center.add(metricsGrid, BorderLayout.CENTER);

        // Biểu đồ doanh thu ở dưới cùng
        JPanel chartWrapper = new JPanel(new BorderLayout());
        chartWrapper.setOpaque(false);
        chartWrapper.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel chartTitle = new JLabel("Biểu đồ doanh thu theo tháng (chỉ tính hóa đơn Paid)");
        chartTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chartTitle.setForeground(new Color(120, 120, 120));
        chartTitle.setBorder(new EmptyBorder(0, 4, 4, 0));

        chartWrapper.add(chartTitle, BorderLayout.NORTH);
        chartWrapper.add(revenueChartPanel, BorderLayout.CENTER);

        center.add(chartWrapper, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);
    }

    /**
     * Hàm tạo Card thống kê linh hoạt.
     * @param isSolid true nếu muốn đổ nền màu đậm (chữ trắng), false nếu muốn nền trắng (chữ màu)
     */
    private JPanel createStatCard(String label, String icon, JLabel valueLabel, Color themeColor, boolean isSolid) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Cấu hình UI với FlatLaf
        if (isSolid) {
            card.setBackground(themeColor);
            card.putClientProperty(FlatClientProperties.STYLE, "arc:16; borderWidth:0");
            valueLabel.setForeground(Color.WHITE);
        } else {
            card.setBackground(Color.WHITE);
            // Tạo viền nhạt cùng màu với themeColor
            String hexColor = String.format("#%02x%02x%02x", themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue());
            card.putClientProperty(FlatClientProperties.STYLE, 
                    "arc:16; borderWidth:1; borderColor:" + hexColor + "50"); // 50 là alpha (độ trong suốt)
            valueLabel.setForeground(themeColor);
        }

        // Header chứa Icon và Tiêu đề
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerPanel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(isSolid ? new Color(255, 255, 255, 220) : new Color(100, 100, 100));

        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);

        // Value Label (Số lượng)
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, isSolid ? 36 : 30));
        valueLabel.setBorder(new EmptyBorder(10, 5, 0, 0));

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void loadStats() {
        try {
            // Lấy dữ liệu (sử dụng Thread để không làm đơ UI nếu database chậm - Tùy chọn)
            int students = studentService.findAll().size();
            int classes = classService.findAll().size();
            
            int staffs = 0;
            try {
                List<Staff> staffList = staffService.getAll();
                staffs = staffList != null ? staffList.size() : 0;
            } catch (Exception ignored) {}
            
            int teachers = 0;
            try {
                List<Teacher> teacherList = teacherService.findAll();
                teachers = teacherList != null ? teacherList.size() : 0;
            } catch (Exception ignored) {}
            
            List<Invoice> invoicesList = invoiceService.findAll();
            int invoices = invoicesList.size();
            int payments = paymentService.findAll().size();

            BigDecimal totalPaid = BigDecimal.ZERO;
            BigDecimal totalReceivable = BigDecimal.ZERO;

            // Chuẩn bị dữ liệu doanh thu theo tháng (6 tháng gần nhất)
            Map<YearMonth, BigDecimal> revenueByMonth = new LinkedHashMap<>();
            YearMonth now = YearMonth.now();
            List<YearMonth> months = new ArrayList<>();
            for (int i = 5; i >= 0; i--) {
                YearMonth ym = now.minusMonths(i);
                months.add(ym);
                revenueByMonth.put(ym, BigDecimal.ZERO);
            }

            for (Invoice inv : invoicesList) {
                if (inv.getTotalAmount() == null) continue;

                if (inv.getStatus() == InvoiceStatus.Paid) {
                    totalPaid = totalPaid.add(inv.getTotalAmount());

                    LocalDate issue = inv.getIssueDate();
                    if (issue != null) {
                        YearMonth ym = YearMonth.from(issue);
                        if (revenueByMonth.containsKey(ym)) {
                            revenueByMonth.put(ym,
                                    revenueByMonth.get(ym).add(inv.getTotalAmount()));
                        }
                    }
                } else if (inv.getStatus() == InvoiceStatus.Draft || inv.getStatus() == InvoiceStatus.Issued) {
                    totalReceivable = totalReceivable.add(inv.getTotalAmount());
                }
            }

            // Định dạng tiền tệ VND
            NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

            // Cập nhật UI
            lblStudentCount.setText(String.format("%,d", students));
            lblClassCount.setText(String.format("%,d", classes));
            lblInvoiceCount.setText(String.format("%,d", invoices));
            lblPaymentCount.setText(String.format("%,d", payments));
            lblStaffCount.setText(String.format("%,d", staffs));
            lblTeacherCount.setText(String.format("%,d", teachers));
            
            lblRevenue.setText(nf.format(totalPaid) + " đ");
            lblReceivable.setText(nf.format(totalReceivable) + " đ");

            revenueChartPanel.setData(revenueByMonth);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không tải được số liệu dashboard: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Panel vẽ biểu đồ cột doanh thu theo tháng (dùng cho dashboard Admin).
     */
    private static class RevenueChartPanel extends JPanel {

        private Map<YearMonth, BigDecimal> data = new LinkedHashMap<>();

        RevenueChartPanel() {
            setPreferredSize(new Dimension(0, 180));
            setOpaque(false);
        }

        void setData(Map<YearMonth, BigDecimal> data) {
            this.data = data != null ? data : new LinkedHashMap<>();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            int leftPad = 40;
            int rightPad = 10;
            int bottomPad = 30;
            int topPad = 10;

            int chartWidth = width - leftPad - rightPad;
            int chartHeight = height - topPad - bottomPad;
            if (chartWidth <= 0 || chartHeight <= 0) {
                g2.dispose();
                return;
            }

            // Tìm max để scale
            BigDecimal max = BigDecimal.ZERO;
            for (BigDecimal v : data.values()) {
                if (v != null && v.compareTo(max) > 0) {
                    max = v;
                }
            }
            if (max.compareTo(BigDecimal.ZERO) == 0) {
                g2.dispose();
                return;
            }

            int x0 = leftPad;
            int y0 = height - bottomPad;

            // Vẽ trục
            g2.setColor(new Color(220, 220, 220));
            g2.drawLine(x0, y0, x0 + chartWidth, y0);

            int n = data.size();
            int gap = 12;
            int barWidth = Math.max(18, (chartWidth - gap * (n + 1)) / n);

            NumberFormat nfShort = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

            int i = 0;
            for (Map.Entry<YearMonth, BigDecimal> entry : data.entrySet()) {
                YearMonth ym = entry.getKey();
                BigDecimal value = entry.getValue() != null ? entry.getValue() : BigDecimal.ZERO;

                double ratio = value.doubleValue() / max.doubleValue();
                int barHeight = (int) (chartHeight * ratio);

                int x = x0 + gap + i * (barWidth + gap);
                int y = y0 - barHeight;

                // Cột
                g2.setPaint(new GradientPaint(x, y, new Color(13, 110, 253, 220),
                        x, y0, new Color(13, 110, 253, 120)));
                g2.fillRoundRect(x, y, barWidth, barHeight, 10, 10);

                // Nhãn tháng (MM/yy)
                g2.setColor(new Color(80, 80, 80));
                String monthLabel = ym.getMonthValue() + "/" + String.valueOf(ym.getYear()).substring(2);
                FontMetrics fm = g2.getFontMetrics();
                int lw = fm.stringWidth(monthLabel);
                g2.drawString(monthLabel, x + (barWidth - lw) / 2, y0 + fm.getAscent());

                // Giá trị ngắn
                String valLabel = nfShort.format(value);
                Font small = g2.getFont().deriveFont(Font.PLAIN, 10f);
                g2.setFont(small);
                FontMetrics fm2 = g2.getFontMetrics();
                int vw = fm2.stringWidth(valLabel);
                g2.setColor(new Color(60, 60, 60));
                g2.drawString(valLabel, x + (barWidth - vw) / 2, y - 4);

                // Khôi phục font
                g2.setFont(fm.getFont());

                i++;
            }

            g2.dispose();
        }
    }
}