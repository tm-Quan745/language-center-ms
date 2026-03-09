package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Enrollment;
import vn.edu.ute.productmgmt.model.Invoice;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.enums.PaymentMethod;
import vn.edu.ute.productmgmt.model.enums.PaymentStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PaymentFormDialog extends JDialog {

    private static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm";

    private final JComboBox<Student> cboStudent;
    private final JComboBox<Enrollment> cboEnrollment;
    private final JComboBox<Invoice> cboInvoice;
    private final JTextField txtAmount = new JTextField();
    private final JSpinner spnPaymentDate;
    private final JComboBox<PaymentMethod> cboMethod = new JComboBox<>(PaymentMethod.values());
    private final JComboBox<PaymentStatus> cboStatus = new JComboBox<>(PaymentStatus.values());
    private final JTextField txtReferenceCode = new JTextField();

    private boolean saved = false;
    private PaymentFormData result;

    public PaymentFormDialog(Window owner, PaymentFormData existing,
                             List<Student> students, List<Enrollment> enrollments, List<Invoice> invoices) {
        super(owner, "Giao dịch Thanh toán", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 750);
        setLayout(new BorderLayout());

        // --- Renderers & Data Prep ---
        cboStudent = new JComboBox<>(students != null ? students.toArray(new Student[0]) : new Student[0]);
        setupStudentRenderer();

        List<Enrollment> encList = new ArrayList<>();
        encList.add(null);
        if (enrollments != null) encList.addAll(enrollments);
        cboEnrollment = new JComboBox<>(encList.toArray(new Enrollment[0]));
        setupEnrollmentRenderer();

        List<Invoice> invList = new ArrayList<>();
        invList.add(null);
        if (invoices != null) invList.addAll(invoices);
        cboInvoice = new JComboBox<>(invList.toArray(new Invoice[0]));
        setupInvoiceRenderer();

        spnPaymentDate = createDateTimeSpinner();

        // --- Build UI ---
        buildUI();

        // --- Data Loading ---
        if (existing != null) {
            setSelectedStudentById(existing.getStudentId());
            setSelectedEnrollmentById(existing.getEnrollmentId());
            setSelectedInvoiceById(existing.getInvoiceId());
            txtAmount.setText(existing.getAmount());
            setSpinnerFromDateTimeString(existing.getPaymentDate());
            if (existing.getMethod() != null) cboMethod.setSelectedItem(existing.getMethod());
            if (existing.getStatus() != null) cboStatus.setSelectedItem(existing.getStatus());
            txtReferenceCode.setText(existing.getReferenceCode());
            this.result = existing;
        } else {
            this.result = new PaymentFormData();
            cboMethod.setSelectedItem(PaymentMethod.Cash);
            cboStatus.setSelectedItem(PaymentStatus.Completed);
            spnPaymentDate.setValue(new Date());
        }

        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header
        JLabel lblHeader = new JLabel("Thông tin giao dịch");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setBorder(new EmptyBorder(0, 0, 25, 0));
        root.add(lblHeader, BorderLayout.NORTH);

        // Body
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        addFormRow(form, gbc, 0, "Học viên:", cboStudent);
        addFormRow(form, gbc, 1, "Phiếu ghi danh:", cboEnrollment);
        addFormRow(form, gbc, 2, "Hóa đơn liên quan:", cboInvoice);

        // Amount Field
        gbc.gridy = 3; gbc.gridx = 0; gbc.weightx = 0;
        form.add(createLabel("Số tiền thanh toán:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.insets = new Insets(8, 20, 8, 0);
        txtAmount.setPreferredSize(new Dimension(0, 40));
        txtAmount.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        txtAmount.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new JLabel(" 💰 "));
        txtAmount.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập số tiền...");
        form.add(txtAmount, gbc);

        gbc.insets = new Insets(8, 0, 8, 0);
        addFormRow(form, gbc, 4, "Ngày giờ giao dịch:", spnPaymentDate);
        addFormRow(form, gbc, 5, "Phương thức:", cboMethod);
        addFormRow(form, gbc, 6, "Trạng thái:", cboStatus);

        // Reference Code
        gbc.gridy = 7; gbc.gridx = 0; gbc.weightx = 0;
        form.add(createLabel("Mã tham chiếu:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.insets = new Insets(8, 20, 8, 0);
        txtReferenceCode.setPreferredSize(new Dimension(0, 40));
        txtReferenceCode.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        txtReferenceCode.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Mã chuyển khoản, số biên lai...");
        form.add(txtReferenceCode, gbc);

        root.add(form, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(25, 0, 0, 0));

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setPreferredSize(new Dimension(100, 42));
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #f2f2f2; borderWidth: 0");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Xác nhận");
        btnSave.setPreferredSize(new Dimension(140, 42));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #0d6efd; foreground: #ffffff; borderWidth: 0");
        btnSave.addActionListener(e -> onSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        root.add(footer, BorderLayout.SOUTH);

        add(root);
    }

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

    private JSpinner createDateTimeSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.MINUTE);
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, DATE_TIME_FORMAT));
        return spinner;
    }

    // --- Giữ nguyên logic cũ ---

    private void onSave() {
        try {
            Student selStudent = (Student) cboStudent.getSelectedItem();
            if (selStudent == null) throw new IllegalArgumentException("Chọn học viên.");

            String amountStr = txtAmount.getText().trim();
            if (amountStr.isEmpty()) throw new IllegalArgumentException("Số tiền không được để trống.");
            new java.math.BigDecimal(amountStr);

            result.setStudentId(selStudent.getId().toString());
            Enrollment selEnc = (Enrollment) cboEnrollment.getSelectedItem();
            result.setEnrollmentId(selEnc != null ? selEnc.getId().toString() : "");
            Invoice selInv = (Invoice) cboInvoice.getSelectedItem();
            result.setInvoiceId(selInv != null ? selInv.getId().toString() : "");
            result.setAmount(amountStr);
            result.setPaymentDate(getSpinnerDateTimeString());
            result.setMethod((PaymentMethod) cboMethod.getSelectedItem());
            result.setStatus((PaymentStatus) cboStatus.getSelectedItem());
            result.setReferenceCode(txtReferenceCode.getText().trim());

            saved = true;
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số tiền phải là số hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupStudentRenderer() {
        cboStudent.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Student s) setText(s.getFullName());
                return this;
            }
        });
    }

    private void setupEnrollmentRenderer() {
        cboEnrollment.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Enrollment e) {
                    setText("Phiếu #" + e.getId() + " - " + e.getTeachingClass().getClassName());
                } else setText("— Không liên kết —");
                return this;
            }
        });
    }

    private void setupInvoiceRenderer() {
        cboInvoice.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Invoice i) {
                    setText("Hóa đơn #" + i.getId() + " - " + String.format("%,.0f", i.getTotalAmount()) + " VNĐ");
                } else setText("— Không liên kết —");
                return this;
            }
        });
    }

    private void setSpinnerFromDateTimeString(String value) {
        if (value == null || value.isBlank()) return;
        try {
            LocalDateTime ldt = LocalDateTime.parse(value.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            spnPaymentDate.setValue(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
        } catch (Exception ignored) { }
    }

    private String getSpinnerDateTimeString() {
        Date d = (Date) spnPaymentDate.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private void setSelectedStudentById(String id) {
        for (int i = 0; i < cboStudent.getItemCount(); i++) {
            if (cboStudent.getItemAt(i).getId().toString().equals(id)) {
                cboStudent.setSelectedIndex(i); break;
            }
        }
    }

    private void setSelectedEnrollmentById(String id) {
        for (int i = 0; i < cboEnrollment.getItemCount(); i++) {
            Enrollment e = cboEnrollment.getItemAt(i);
            if (e != null && e.getId().toString().equals(id)) {
                cboEnrollment.setSelectedIndex(i); break;
            }
        }
    }

    private void setSelectedInvoiceById(String id) {
        for (int i = 0; i < cboInvoice.getItemCount(); i++) {
            Invoice inv = cboInvoice.getItemAt(i);
            if (inv != null && inv.getId().toString().equals(id)) {
                cboInvoice.setSelectedIndex(i); break;
            }
        }
    }

    public boolean isSaved() { return saved; }
    public PaymentFormData getResult() { return result; }

    public static class PaymentFormData {
        private String studentId, enrollmentId, invoiceId, amount, paymentDate, referenceCode;
        private PaymentMethod method;
        private PaymentStatus status;

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public String getEnrollmentId() { return enrollmentId; }
        public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }
        public String getInvoiceId() { return invoiceId; }
        public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }
        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }
        public String getPaymentDate() { return paymentDate; }
        public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
        public PaymentMethod getMethod() { return method; }
        public void setMethod(PaymentMethod method) { this.method = method; }
        public PaymentStatus getStatus() { return status; }
        public void setStatus(PaymentStatus status) { this.status = status; }
        public String getReferenceCode() { return referenceCode; }
        public void setReferenceCode(String referenceCode) { this.referenceCode = referenceCode; }
    }
}