package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Promotion;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.enums.InvoiceStatus;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Form nhập/sửa hóa đơn: học viên, khuyến mãi, tổng tiền, ngày phát hành, trạng thái, ghi chú.
 */
public class InvoiceFormDialog extends JDialog {

    private static final String DATE_FORMAT = "dd/MM/yyyy";

    private final JComboBox<Student> cboStudent;
    private final JComboBox<Promotion> cboPromotion;
    private final JTextField txtBaseAmount = new JTextField(15);
    private final JLabel lblTotalAfterDiscount = new JLabel("—");
    private final JSpinner spnIssueDate;
    private final JComboBox<InvoiceStatus> cboStatus = new JComboBox<>(InvoiceStatus.values());
    private final JTextArea txtNote = new JTextArea(3, 25);

    private boolean saved = false;
    private InvoiceFormData result;

    public InvoiceFormDialog(Window owner,
                             InvoiceFormData existing,
                             List<Student> students,
                             List<Promotion> promotions) {
        super(owner, "Hóa đơn", ModalityType.APPLICATION_MODAL);
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

        List<Promotion> promoList = new ArrayList<>();
        promoList.add(null); // "— Không —"
        if (promotions != null) promoList.addAll(promotions);
        cboPromotion = new JComboBox<>(promoList.toArray(new Promotion[0]));
        cboPromotion.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "— Không —" : ((Promotion) value).getPromoName());
                return this;
            }
        });

        spnIssueDate = createDateSpinner();

        buildUI();

        if (existing != null) {
            setSelectedStudentById(cboStudent, existing.getStudentId());
            setSelectedPromotionById(cboPromotion, existing.getPromotionId());
            txtBaseAmount.setText(existing.getBaseAmount());
            setSpinnerFromString(spnIssueDate, existing.getIssueDate());
            if (existing.getStatus() != null) cboStatus.setSelectedItem(existing.getStatus());
            txtNote.setText(existing.getNote() != null ? existing.getNote() : "");
            result = existing;
        } else {
            result = new InvoiceFormData();
            spnIssueDate.setValue(new Date());
        }

        cboPromotion.addActionListener(e -> updateTotalLabel());
        txtBaseAmount.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTotalLabel(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTotalLabel(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTotalLabel(); }
        });
        spnIssueDate.addChangeListener(e -> updateTotalLabel());
        updateTotalLabel();

        pack();
        setLocationRelativeTo(owner);
    }

    private JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, DATE_FORMAT);
        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(120, spinner.getPreferredSize().height));
        return spinner;
    }

    private void setSpinnerFromString(JSpinner spinner, String value) {
        if (value == null || value.trim().isEmpty()) return;
        try {
            LocalDate ld = LocalDate.parse(value.trim());
            Date date = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
            spinner.setValue(date);
        } catch (Exception ignored) { }
    }

    private static void setSelectedStudentById(JComboBox<Student> cbo, String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) return;
        try {
            Long id = Long.parseLong(idStr.trim());
            for (int i = 0; i < cbo.getItemCount(); i++) {
                Student s = cbo.getItemAt(i);
                if (s != null && id.equals(s.getId())) {
                    cbo.setSelectedIndex(i);
                    return;
                }
            }
        } catch (Exception ignored) { }
    }

    private static void setSelectedPromotionById(JComboBox<Promotion> cbo, String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) {
            cbo.setSelectedIndex(0); // null
            return;
        }
        try {
            Long id = Long.parseLong(idStr.trim());
            for (int i = 0; i < cbo.getItemCount(); i++) {
                Promotion p = cbo.getItemAt(i);
                if (p != null && id.equals(p.getId())) {
                    cbo.setSelectedIndex(i);
                    return;
                }
            }
        } catch (Exception ignored) { }
    }

    private String getSpinnerDateString(JSpinner spinner) {
        try {
            spinner.commitEdit();
        } catch (Exception ignored) { }
        try {
            Object v = spinner.getValue();
            if (v instanceof Date d) {
                LocalDate ld = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                return ld.toString();
            }
            if (v instanceof java.util.Calendar c) {
                LocalDate ld = c.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                return ld.toString();
            }
        } catch (Exception ignored) { }
        return "";
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
        form.add(new JLabel("Khuyến mãi:"), g);
        g.gridx = 1;
        form.add(cboPromotion, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Số tiền gốc:"), g);
        g.gridx = 1;
        form.add(txtBaseAmount, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Tổng tiền (sau KM):"), g);
        g.gridx = 1;
        form.add(lblTotalAfterDiscount, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Ngày phát hành:"), g);
        g.gridx = 1;
        form.add(spnIssueDate, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Trạng thái:"), g);
        g.gridx = 1;
        form.add(cboStatus, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Ghi chú:"), g);
        g.gridx = 1;
        form.add(new JScrollPane(txtNote), g);

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

    private void updateTotalLabel() {
        BigDecimal base = parseBaseAmount(txtBaseAmount.getText().trim());
        Promotion p = (Promotion) cboPromotion.getSelectedItem();
        BigDecimal total = calculateDiscounted(base, p);
        lblTotalAfterDiscount.setText(total != null ? formatMoney(total) : "—");
    }

    private static BigDecimal parseBaseAmount(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return new BigDecimal(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
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

    private static String formatMoney(BigDecimal v) {
        if (v == null) return "—";
        return String.format("%,.0f", v);
    }

    private void onSave() {
        try {
            Student selStudent = (Student) cboStudent.getSelectedItem();
            if (selStudent == null) {
                throw new IllegalArgumentException("Chọn học viên.");
            }
            Promotion selPromo = (Promotion) cboPromotion.getSelectedItem();
            String baseStr = txtBaseAmount.getText().trim();
            if (baseStr.isEmpty()) {
                throw new IllegalArgumentException("Nhập số tiền gốc.");
            }
            BigDecimal base = new BigDecimal(baseStr);
            if (base.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Số tiền gốc phải >= 0.");
            }

            result.setStudentId(selStudent.getId().toString());
            result.setPromotionId(selPromo != null ? selPromo.getId().toString() : "");
            result.setBaseAmount(baseStr);
            result.setIssueDate(getSpinnerDateString(spnIssueDate));
            result.setStatus((InvoiceStatus) cboStatus.getSelectedItem());
            result.setNote(txtNote.getText().trim());

            saved = true;
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số tiền gốc phải là số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
    public InvoiceFormData getResult() { return result; }

    public static class InvoiceFormData {
        private String studentId;
        private String promotionId;
        private String baseAmount;
        private String issueDate;
        private InvoiceStatus status;
        private String note;

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
