package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.TeachingClass;
import vn.edu.ute.productmgmt.model.Certificate;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Form nhập liệu cho Chứng chỉ, chuẩn hóa theo ClassFormDialog
 */
public class CertificateFormDialog extends JDialog {

    private final JComboBox<Student> cboStudent;
    private final JComboBox<TeachingClass> cboClass;
    private final JTextField txtCertName = new JTextField(30);
    private final JSpinner spnIssueDate;
    private final JTextField txtSerialNo = new JTextField(25);

    private boolean saved = false;
    private CertificateFormData result;

    public CertificateFormDialog(Window owner,
                                CertificateFormData existing,
                                List<Student> students,
                                List<TeachingClass> classes) {
        super(owner, "Thông tin Chứng chỉ", ModalityType.APPLICATION_MODAL);

        setSize(580, 680);
        setLayout(new BorderLayout());

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

        List<TeachingClass> classList = new ArrayList<>();
        classList.add(null);
        if (classes != null) classList.addAll(classes);
        cboClass = new JComboBox<>(classList.toArray(new TeachingClass[0]));
        cboClass.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "— Không chọn —" : ((TeachingClass) value).getClassName());
                return this;
            }
        });

        spnIssueDate = createDateSpinner(new Date());

        buildUI();

        if (existing != null) {
            setSelectedStudentById(existing.getStudentId());
            setSelectedClassById(existing.getClassId());
            txtCertName.setText(existing.getCertName());
            setSpinnerFromString(existing.getIssueDate());
            txtSerialNo.setText(existing.getSerialNo() != null ? existing.getSerialNo() : "");
            result = existing;
        } else {
            result = new CertificateFormData();
        }

        setLocationRelativeTo(owner);
    }

    private JSpinner createDateSpinner(Date defaultDate) {
        SpinnerDateModel model = new SpinnerDateModel(defaultDate, null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        spinner.setPreferredSize(new Dimension(0, 40));
        spinner.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        return spinner;
    }

    private void setSpinnerFromString(String value) {
        if (value == null || value.trim().isEmpty()) return;
        try {
            LocalDate ld = LocalDate.parse(value.trim());
            Date date = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
            spnIssueDate.setValue(date);
        } catch (Exception ignored) { }
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

    private void setSelectedClassById(String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) {
            cboClass.setSelectedIndex(0);
            return;
        }
        try {
            Long id = Long.parseLong(idStr.trim());
            for (int i = 0; i < cboClass.getItemCount(); i++) {
                TeachingClass tc = cboClass.getItemAt(i);
                if (tc != null && id.equals(tc.getId())) {
                    cboClass.setSelectedIndex(i);
                    return;
                }
            }
        } catch (Exception ignored) { }
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- Header ---
        JLabel lblHeader = new JLabel("Thông tin Chứng chỉ");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setBorder(new EmptyBorder(0, 0, 25, 0));
        root.add(lblHeader, BorderLayout.NORTH);

        // --- Form Body ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        // Các hàng dữ liệu
        addFormRow(form, gbc, 0, "Học viên:", cboStudent);
        addFormRow(form, gbc, 1, "Lớp học:", cboClass);
        addFormRow(form, gbc, 2, "Tên chứng chỉ:", txtCertName);
        addFormRow(form, gbc, 3, "Ngày cấp:", spnIssueDate);
        addFormRow(form, gbc, 4, "Số seri:", txtSerialNo);

        root.add(form, BorderLayout.CENTER);

        // --- Buttons ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(25, 0, 0, 0));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(100, 42));
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #f2f2f2; borderWidth: 0");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Lưu Chứng chỉ");
        btnSave.setPreferredSize(new Dimension(140, 42));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #198754; foreground: #ffffff; borderWidth: 0");
        btnSave.addActionListener(e -> onSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        root.add(footer, BorderLayout.SOUTH);

        add(root);
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridy = row;
        gbc.gridx = 0; gbc.weightx = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        p.add(createLabel(label), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 20, 8, 0);
        if (comp instanceof JComboBox || comp instanceof JSpinner || comp instanceof JTextField) {
            comp.setPreferredSize(new Dimension(0, 40));
            comp.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        }
        p.add(comp, gbc);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private void onSave() {
        try {
            spnIssueDate.commitEdit();

            Student student = (Student) cboStudent.getSelectedItem();
            if (student == null) {
                throw new IllegalArgumentException("Vui lòng chọn học viên.");
            }

            String certName = txtCertName.getText().trim();
            if (certName.isEmpty()) {
                throw new IllegalArgumentException("Tên chứng chỉ không được để trống.");
            }

            LocalDate issueDate = toLocalDate((Date) spnIssueDate.getValue());
            String serialNo = txtSerialNo.getText().trim();
            if (serialNo.isEmpty()) serialNo = null;

            result.setStudentId(student.getId().toString());
            result.setCertName(certName);
            result.setIssueDate(issueDate.toString());
            result.setSerialNo(serialNo);

            TeachingClass tc = (TeachingClass) cboClass.getSelectedItem();
            result.setClassId(tc != null && tc.getId() != null ? tc.getId().toString() : null);

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Thông báo lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public boolean isSaved() {
        return saved;
    }

    public CertificateFormData getResult() {
        return result;

    }

    public Certificate toCertificate() {

        if (result == null) return null;

        Certificate cert = new Certificate();

        // student
        if (result.getStudentId() != null && !result.getStudentId().isBlank()) {

            Student s = new Student();
            s.setId(Long.parseLong(result.getStudentId()));

            cert.setStudent(s);
        }

        // class
        if (result.getClassId() != null && !result.getClassId().isBlank()) {

            TeachingClass tc = new TeachingClass();
            tc.setId(Long.parseLong(result.getClassId()));

            cert.setTeachingClass(tc);
        }

        cert.setCertName(result.getCertName());

        if (result.getIssueDate() != null && !result.getIssueDate().isBlank()) {
            cert.setIssueDate(LocalDate.parse(result.getIssueDate()));
        }

        cert.setSerialNo(result.getSerialNo());

        return cert;
    }

    /**
     * DTO đơn giản đại diện thông tin Chứng chỉ cho UI.
     */
    public static class CertificateFormData {
        private String studentId;
        private String classId;
        private String certName;
        private String issueDate;
        private String serialNo;

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public String getClassId() {
            return classId;
        }

        public void setClassId(String classId) {
            this.classId = classId;
        }

        public String getCertName() {
            return certName;
        }

        public void setCertName(String certName) {
            this.certName = certName;
        }

        public String getIssueDate() {
            return issueDate;
        }

        public void setIssueDate(String issueDate) {
            this.issueDate = issueDate;
        }

        public String getSerialNo() {
            return serialNo;
        }

        public void setSerialNo(String serialNo) {
            this.serialNo = serialNo;
        }
    }
}
