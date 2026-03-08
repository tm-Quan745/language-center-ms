package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Enrollment;
import vn.edu.ute.productmgmt.model.Invoice;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.enums.PaymentMethod;
import vn.edu.ute.productmgmt.model.enums.PaymentStatus;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PaymentFormDialog extends JDialog {

    private static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm";

    private final JComboBox<Student> cboStudent;
    private final JComboBox<Enrollment> cboEnrollment;
    private final JComboBox<Invoice> cboInvoice;
    private final JTextField txtAmount = new JTextField(10);
    private final JSpinner spnPaymentDate;
    private final JComboBox<PaymentMethod> cboMethod = new JComboBox<>(PaymentMethod.values());
    private final JComboBox<PaymentStatus> cboStatus = new JComboBox<>(PaymentStatus.values());
    private final JTextField txtReferenceCode = new JTextField(20);

    private boolean saved = false;
    private PaymentFormData result;

    public PaymentFormDialog(Window owner,
                             PaymentFormData existing,
                             List<Student> students,
                             List<Enrollment> enrollments,
                             List<Invoice> invoices) {
        super(owner, "Thanh toán", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        cboStudent = new JComboBox<>(students != null ? students.toArray(new Student[0]) : new Student[0]);
        cboStudent.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Student s) {
                    setText(s.getFullName() != null ? s.getFullName() : "ID " + s.getId());
                }
                return this;
            }
        });

        List<Enrollment> encList = new ArrayList<>();
        encList.add(null);
        if (enrollments != null) encList.addAll(enrollments);
        cboEnrollment = new JComboBox<>(encList.toArray(new Enrollment[0]));
        cboEnrollment.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Enrollment e) {
                    String student = e.getStudent() != null ? e.getStudent().getFullName() : "";
                    String cls = e.getTeachingClass() != null ? e.getTeachingClass().getClassName() : "";
                    setText("ID " + e.getId() + " - " + student + " - " + cls);
                } else {
                    setText("— Không —");
                }
                return this;
            }
        });

        List<Invoice> invList = new ArrayList<>();
        invList.add(null);
        if (invoices != null) invList.addAll(invoices);
        cboInvoice = new JComboBox<>(invList.toArray(new Invoice[0]));
        cboInvoice.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Invoice i) {
                    String student = i.getStudent() != null ? i.getStudent().getFullName() : "";
                    String amt = i.getTotalAmount() != null ? i.getTotalAmount().toPlainString() : "";
                    setText("HD #" + i.getId() + " - " + student + " - " + amt);
                } else {
                    setText("— Không —");
                }
                return this;
            }
        });

        spnPaymentDate = createDateTimeSpinner();

        buildUI();

        if (existing != null) {
            setSelectedStudentById(existing.getStudentId());
            setSelectedEnrollmentById(existing.getEnrollmentId());
            setSelectedInvoiceById(existing.getInvoiceId());
            txtAmount.setText(existing.getAmount());
            setSpinnerFromDateTimeString(existing.getPaymentDate());
            if (existing.getMethod() != null) cboMethod.setSelectedItem(existing.getMethod());
            if (existing.getStatus() != null) cboStatus.setSelectedItem(existing.getStatus());
            txtReferenceCode.setText(existing.getReferenceCode());
            result = existing;
        } else {
            result = new PaymentFormData();
            cboMethod.setSelectedItem(PaymentMethod.Cash);
            cboStatus.setSelectedItem(PaymentStatus.Completed);
            spnPaymentDate.setValue(new Date());
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private JSpinner createDateTimeSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.MINUTE);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, DATE_TIME_FORMAT);
        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(160, spinner.getPreferredSize().height));
        return spinner;
    }

    private void setSpinnerFromDateTimeString(String value) {
        if (value == null || value.trim().isEmpty()) return;
        try {
            LocalDateTime ldt = LocalDateTime.parse(value.trim(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
            spnPaymentDate.setValue(date);
        } catch (Exception ignored) { }
    }

    private String getSpinnerDateTimeString() {
        try {
            spnPaymentDate.commitEdit();
        } catch (Exception ignored) { }
        try {
            Object v = spnPaymentDate.getValue();
            if (v instanceof Date d) {
                LocalDateTime ldt = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                return ldt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            }
            if (v instanceof java.util.Calendar c) {
                LocalDateTime ldt = c.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                return ldt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            }
        } catch (Exception ignored) { }
        return "";
    }

    private void setSelectedStudentById(String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) return;
        try {
            Long id = Long.parseLong(idStr.trim());
            for (int i = 0; i < cboStudent.getItemCount(); i++) {
                Student s = cboStudent.getItemAt(i);
                if (s != null && id.equals(s.getId())) {
                    cboStudent.setSelectedIndex(i);
                    return;
                }
            }
        } catch (Exception ignored) { }
    }

    private void setSelectedEnrollmentById(String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) {
            cboEnrollment.setSelectedIndex(0);
            return;
        }
        try {
            Long id = Long.parseLong(idStr.trim());
            for (int i = 0; i < cboEnrollment.getItemCount(); i++) {
                Enrollment e = cboEnrollment.getItemAt(i);
                if (e != null && id.equals(e.getId())) {
                    cboEnrollment.setSelectedIndex(i);
                    return;
                }
            }
        } catch (Exception ignored) { }
    }

    private void setSelectedInvoiceById(String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) {
            cboInvoice.setSelectedIndex(0);
            return;
        }
        try {
            Long id = Long.parseLong(idStr.trim());
            for (int i = 0; i < cboInvoice.getItemCount(); i++) {
                Invoice inv = cboInvoice.getItemAt(i);
                if (inv != null && id.equals(inv.getId())) {
                    cboInvoice.setSelectedIndex(i);
                    return;
                }
            }
        } catch (Exception ignored) { }
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;

        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Học viên:"), g);
        g.gridx = 1;
        form.add(cboStudent, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Ghi danh (Enrollment):"), g);
        g.gridx = 1;
        form.add(cboEnrollment, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Hóa đơn (Invoice):"), g);
        g.gridx = 1;
        form.add(cboInvoice, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Số tiền:"), g);
        g.gridx = 1;
        form.add(txtAmount, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Ngày giờ thanh toán:"), g);
        g.gridx = 1;
        form.add(spnPaymentDate, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Phương thức:"), g);
        g.gridx = 1;
        form.add(cboMethod, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Trạng thái:"), g);
        g.gridx = 1;
        form.add(cboStatus, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Mã tham chiếu:"), g);
        g.gridx = 1;
        form.add(txtReferenceCode, g);

        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnSave);
        actions.add(btnCancel);

        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(actions, BorderLayout.SOUTH);
    }

    private void onSave() {
        try {
            Student selStudent = (Student) cboStudent.getSelectedItem();
            if (selStudent == null) {
                throw new IllegalArgumentException("Chọn học viên.");
            }

            String amountStr = txtAmount.getText().trim();
            if (amountStr.isEmpty()) {
                throw new IllegalArgumentException("Số tiền không được để trống.");
            }
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
            JOptionPane.showMessageDialog(this, "Số tiền phải là số.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
    public PaymentFormData getResult() { return result; }

    public static class PaymentFormData {
        private String studentId;
        private String enrollmentId;
        private String invoiceId;
        private String amount;
        private String paymentDate;
        private PaymentMethod method;
        private PaymentStatus status;
        private String referenceCode;

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
