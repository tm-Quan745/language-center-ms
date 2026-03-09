package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Promotion;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.enums.InvoiceStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InvoiceFormDialog extends JDialog {

    private final JComboBox<Student> cboStudent;
    private final JComboBox<Promotion> cboPromotion;
    private final JTextField txtBaseAmount = new JTextField();
    private final JLabel lblTotalAfterDiscount = new JLabel("0 VNĐ");
    private final JSpinner spnIssueDate;
    private final JComboBox<InvoiceStatus> cboStatus = new JComboBox<>(InvoiceStatus.values());
    private final JTextArea txtNote = new JTextArea(3, 25);

    private boolean saved = false;
    private InvoiceFormData result;

    public InvoiceFormDialog(Window owner, InvoiceFormData existing, List<Student> students, List<Promotion> promotions) {
        super(owner, "Thông tin Hóa đơn", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 720);
        setLayout(new BorderLayout());

        // --- Khởi tạo và Render dữ liệu ---
        cboStudent = new JComboBox<>(students != null ? students.toArray(new Student[0]) : new Student[0]);
        setupStudentRenderer();

        List<Promotion> promoList = new ArrayList<>();
        promoList.add(null);
        if (promotions != null) promoList.addAll(promotions);
        cboPromotion = new JComboBox<>(promoList.toArray(new Promotion[0]));
        setupPromotionRenderer();

        spnIssueDate = createDateSpinner();

        // --- Xây dựng UI ---
        buildUI();

        // --- Load dữ liệu cũ hoặc mới ---
        if (existing != null) {
            setSelectedStudentById(cboStudent, existing.getStudentId());
            setSelectedPromotionById(cboPromotion, existing.getPromotionId());
            txtBaseAmount.setText(existing.getBaseAmount());
            setSpinnerFromString(spnIssueDate, existing.getIssueDate());
            if (existing.getStatus() != null) cboStatus.setSelectedItem(existing.getStatus());
            txtNote.setText(existing.getNote() != null ? existing.getNote() : "");
            this.result = existing;
        } else {
            this.result = new InvoiceFormData();
            spnIssueDate.setValue(new Date());
        }

        // --- Gắn sự kiện tính toán (Logic giữ nguyên) ---
        setupEventListeners();
        updateTotalLabel();

        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- Header ---
        JLabel lblHeader = new JLabel("Chi tiết Hóa đơn học phí");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setBorder(new EmptyBorder(0, 0, 25, 0));
        root.add(lblHeader, BorderLayout.NORTH);

        // --- Form Body (GridBagLayout) ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        // Các hàng nhập liệu
        addFormRow(form, gbc, 0, "Học viên thanh toán:", cboStudent);
        addFormRow(form, gbc, 1, "Mã khuyến mãi:", cboPromotion);

        // Ô số tiền gốc có icon
        gbc.gridy = 2; gbc.gridx = 0; gbc.weightx = 0;
        form.add(createLabel("Số tiền gốc:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.insets = new Insets(8, 20, 8, 0);
        txtBaseAmount.setPreferredSize(new Dimension(0, 40));
        txtBaseAmount.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        txtBaseAmount.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "0.00");
        txtBaseAmount.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new JLabel(" 💵 "));
        form.add(txtBaseAmount, gbc);

        // Tổng tiền (Nổi bật)
        gbc.gridy = 3; gbc.gridx = 0; gbc.weightx = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        form.add(createLabel("Thực thu:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.insets = new Insets(8, 20, 8, 0);
        lblTotalAfterDiscount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalAfterDiscount.setForeground(new Color(13, 110, 253)); // Blue accent
        form.add(lblTotalAfterDiscount, gbc);

        // Ngày phát hành và trạng thái
        gbc.insets = new Insets(8, 0, 8, 0);
        addFormRow(form, gbc, 4, "Ngày lập hóa đơn:", spnIssueDate);
        addFormRow(form, gbc, 5, "Trạng thái:", cboStatus);

        // Ghi chú
        gbc.gridy = 6; gbc.gridx = 0; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(12, 0, 8, 0);
        form.add(createLabel("Ghi chú:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.insets = new Insets(8, 20, 8, 0);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(txtNote);
        scroll.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        form.add(scroll, gbc);

        root.add(form, BorderLayout.CENTER);

        // --- Buttons Footer ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(25, 0, 0, 0));

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setPreferredSize(new Dimension(100, 42));
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #f2f2f2; borderWidth: 0");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Lưu hóa đơn");
        btnSave.setPreferredSize(new Dimension(140, 42));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #0d6efd; foreground: #ffffff; borderWidth: 0");
        btnSave.addActionListener(e -> onSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        root.add(footer, BorderLayout.SOUTH);

        add(root);
    }

    // --- Helper Methods cho UI ---
    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        p.add(createLabel(label), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 20, 8, 0);
        comp.setPreferredSize(new Dimension(0, 40));
        comp.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        p.add(comp, gbc);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        spinner.setPreferredSize(new Dimension(0, 40));
        spinner.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        return spinner;
    }

    private void setupEventListeners() {
        cboPromotion.addActionListener(e -> updateTotalLabel());
        txtBaseAmount.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTotalLabel(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTotalLabel(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTotalLabel(); }
        });
    }

    // --- Logic Methods (Giữ nguyên logic của bạn) ---
    private void updateTotalLabel() {
        BigDecimal base = parseBaseAmount(txtBaseAmount.getText().trim());
        Promotion p = (Promotion) cboPromotion.getSelectedItem();
        BigDecimal total = calculateDiscounted(base, p);
        lblTotalAfterDiscount.setText(total != null ? String.format("%,.0f VNĐ", total) : "—");
    }

    private static BigDecimal parseBaseAmount(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return new BigDecimal(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private static BigDecimal calculateDiscounted(BigDecimal base, Promotion p) {
        if (base == null || base.compareTo(BigDecimal.ZERO) < 0) return null;
        if (p == null || p.getDiscountValue() == null) return base;
        BigDecimal discount = switch (p.getDiscountType()) {
            case Percent -> base.multiply(p.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case Amount -> p.getDiscountValue().min(base);
        };
        return base.subtract(discount).max(BigDecimal.ZERO);
    }

    private void onSave() {
        try {
            Student selStudent = (Student) cboStudent.getSelectedItem();
            if (selStudent == null) throw new IllegalArgumentException("Vui lòng chọn học viên.");

            String baseStr = txtBaseAmount.getText().trim();
            if (baseStr.isEmpty()) throw new IllegalArgumentException("Nhập số tiền gốc.");

            BigDecimal base = new BigDecimal(baseStr);
            if (base.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Số tiền gốc phải >= 0.");

            result.setStudentId(selStudent.getId().toString());
            Promotion selPromo = (Promotion) cboPromotion.getSelectedItem();
            result.setPromotionId(selPromo != null ? selPromo.getId().toString() : "");
            result.setBaseAmount(baseStr);
            result.setIssueDate(getSpinnerDateString(spnIssueDate));
            result.setStatus((InvoiceStatus) cboStatus.getSelectedItem());
            result.setNote(txtNote.getText().trim());

            saved = true;
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số tiền gốc phải là số hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Renderers & Data Mappers (Giữ nguyên logic của bạn) ---
    private void setupStudentRenderer() {
        cboStudent.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Student s) setText(s.getFullName() != null ? s.getFullName() : "ID " + s.getId());
                return this;
            }
        });
    }

    private void setupPromotionRenderer() {
        cboPromotion.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "— Không áp dụng —" : ((Promotion) value).getPromoName());
                return this;
            }
        });
    }

    private String getSpinnerDateString(JSpinner spinner) {
        try { spinner.commitEdit(); } catch (Exception ignored) { }
        Date d = (Date) spinner.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
    }

    private void setSpinnerFromString(JSpinner spinner, String value) {
        if (value == null || value.trim().isEmpty()) return;
        try {
            Date date = Date.from(LocalDate.parse(value.trim()).atStartOfDay(ZoneId.systemDefault()).toInstant());
            spinner.setValue(date);
        } catch (Exception ignored) { }
    }

    private void setSelectedStudentById(JComboBox<Student> cbo, String idStr) {
        if (idStr == null) return;
        for (int i = 0; i < cbo.getItemCount(); i++) {
            if (cbo.getItemAt(i).getId().toString().equals(idStr)) {
                cbo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void setSelectedPromotionById(JComboBox<Promotion> cbo, String idStr) {
        if (idStr == null || idStr.isEmpty()) { cbo.setSelectedIndex(0); return; }
        for (int i = 0; i < cbo.getItemCount(); i++) {
            Promotion p = cbo.getItemAt(i);
            if (p != null && p.getId().toString().equals(idStr)) {
                cbo.setSelectedIndex(i);
                break;
            }
        }
    }

    public boolean isSaved() { return saved; }
    public InvoiceFormData getResult() { return result; }

    // --- DTO Class (Giữ nguyên) ---
    public static class InvoiceFormData {
        private String studentId, promotionId, baseAmount, issueDate, note;
        private InvoiceStatus status;

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public String getPromotionId() { return promotionId; }
        public void setPromotionId(String promotionId) { this.promotionId = promotionId; }
        public String getBaseAmount() { return baseAmount; }
        public void setBaseAmount(String baseAmount) { this.baseAmount = baseAmount; }
        public String getIssueDate() { return issueDate; }
        public void setIssueDate(String issueDate) { this.issueDate = issueDate; }
        public InvoiceStatus getStatus() { return status; }
        public void setStatus(InvoiceStatus status) { this.status = status; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }
}