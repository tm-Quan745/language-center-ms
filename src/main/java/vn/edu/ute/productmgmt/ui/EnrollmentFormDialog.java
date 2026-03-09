package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.TeachingClass;
import vn.edu.ute.productmgmt.model.enums.EnrollmentResult;
import vn.edu.ute.productmgmt.model.enums.EnrollmentStatus;
import vn.edu.ute.productmgmt.service.ClassService;
import vn.edu.ute.productmgmt.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class EnrollmentFormDialog extends JDialog {

    private final JComboBox<Student> cboStudent = new JComboBox<>();
    private final JComboBox<TeachingClass> cboClass = new JComboBox<>();
    private final JSpinner spnEnrollmentDate;
    private final JComboBox<EnrollmentStatus> cboStatus = new JComboBox<>(EnrollmentStatus.values());
    private final JComboBox<EnrollmentResult> cboResult = new JComboBox<>(EnrollmentResult.values());

    private boolean saved = false;
    private EnrollmentFormData result;

    public EnrollmentFormDialog(Window owner, EnrollmentFormData existing,
                                StudentService studentService, ClassService classService) {
        super(owner, "Ghi danh học viên", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(580, 650);
        setLayout(new BorderLayout());

        // 1. Khởi tạo Spinner với Style FlatLaf
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        spnEnrollmentDate = new JSpinner(dateModel);
        spnEnrollmentDate.setEditor(new JSpinner.DateEditor(spnEnrollmentDate, "dd/MM/yyyy"));
        spnEnrollmentDate.setPreferredSize(new Dimension(0, 40));
        spnEnrollmentDate.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        // 2. Nạp dữ liệu
        try {
            studentService.findAll().forEach(cboStudent::addItem);
            classService.findAll().forEach(cboClass::addItem);
        } catch (Exception ignored) {}

        setupComboBoxRenderers();
        buildUI();

        // 3. Đổ dữ liệu cũ (Logic giữ nguyên)
        if (existing != null) {
            this.result = existing;
            setSelectedStudent(existing.getStudentId());
            setSelectedClass(existing.getClassId());
            cboStatus.setSelectedItem(existing.getStatus());
            cboResult.setSelectedItem(existing.getResult());

            try {
                if (existing.getEnrollmentDate() != null) {
                    LocalDate ld = LocalDate.parse(existing.getEnrollmentDate());
                    spnEnrollmentDate.setValue(Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                }
            } catch (Exception e) {
                spnEnrollmentDate.setValue(new Date());
            }
        } else {
            result = new EnrollmentFormData();
            cboStatus.setSelectedItem(EnrollmentStatus.Enrolled);
            cboResult.setSelectedItem(EnrollmentResult.NA);
        }

        setLocationRelativeTo(owner);
    }

    private void setupComboBoxRenderers() {
        // Render sạch sẽ hơn, kế thừa Style của hệ thống
        cboStudent.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Student s) setText(s.getFullName() + " (ID: " + s.getId() + ")");
                return this;
            }
        });

        cboClass.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TeachingClass c) setText(c.getClassName());
                return this;
            }
        });
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- Header ---
        JLabel lblHeader = new JLabel("Đăng ký lớp học");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setBorder(new EmptyBorder(0, 0, 25, 0));
        root.add(lblHeader, BorderLayout.NORTH);

        // --- Form Body ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        addFormRow(form, gbc, 0, "Học viên đăng ký:", cboStudent);
        addFormRow(form, gbc, 1, "Lớp học mục tiêu:", cboClass);
        addFormRow(form, gbc, 2, "Ngày ghi danh:", spnEnrollmentDate);
        addFormRow(form, gbc, 3, "Trạng thái học tập:", cboStatus);
        addFormRow(form, gbc, 4, "Kết quả cuối khóa:", cboResult);

        root.add(form, BorderLayout.CENTER);

        // --- Footer Buttons ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(25, 0, 0, 0));

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setPreferredSize(new Dimension(100, 42));
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #f2f2f2; borderWidth: 0");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Lưu ghi danh");
        btnSave.setPreferredSize(new Dimension(140, 42));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #6f42c1; foreground: #ffffff; borderWidth: 0"); // Purple accent
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

    private void onSave() {
        Student selectedS = (Student) cboStudent.getSelectedItem();
        TeachingClass selectedC = (TeachingClass) cboClass.getSelectedItem();

        if (selectedS == null || selectedC == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đầy đủ học viên và lớp!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate localDate = ((Date) spnEnrollmentDate.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        result.setStudentId(selectedS.getId());
        result.setClassId(selectedC.getId());
        result.setEnrollmentDate(localDate.toString());
        result.setStatus((EnrollmentStatus) cboStatus.getSelectedItem());
        result.setResult((EnrollmentResult) cboResult.getSelectedItem());

        saved = true;
        dispose();
    }

    private void setSelectedStudent(Long id) {
        if (id == null) return;
        for (int i = 0; i < cboStudent.getItemCount(); i++) {
            if (cboStudent.getItemAt(i).getId().equals(id)) {
                cboStudent.setSelectedIndex(i);
                break;
            }
        }
    }

    private void setSelectedClass(Long id) {
        if (id == null) return;
        for (int i = 0; i < cboClass.getItemCount(); i++) {
            if (cboClass.getItemAt(i).getId().equals(id)) {
                cboClass.setSelectedIndex(i);
                break;
            }
        }
    }

    public boolean isSaved() { return saved; }
    public EnrollmentFormData getResult() { return result; }

    public static class EnrollmentFormData {
        private Long studentId, classId;
        private String enrollmentDate;
        private EnrollmentStatus status;
        private EnrollmentResult result;

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public Long getClassId() { return classId; }
        public void setClassId(Long classId) { this.classId = classId; }
        public String getEnrollmentDate() { return enrollmentDate; }
        public void setEnrollmentDate(String enrollmentDate) { this.enrollmentDate = enrollmentDate; }
        public EnrollmentStatus getStatus() { return status; }
        public void setStatus(EnrollmentStatus status) { this.status = status; }
        public EnrollmentResult getResult() { return result; }
        public void setResult(EnrollmentResult result) { this.result = result; }
    }
}