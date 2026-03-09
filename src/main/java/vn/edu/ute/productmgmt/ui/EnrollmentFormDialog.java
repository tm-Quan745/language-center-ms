package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.TeachingClass;
import vn.edu.ute.productmgmt.model.enums.EnrollmentResult;
import vn.edu.ute.productmgmt.model.enums.EnrollmentStatus;
import vn.edu.ute.productmgmt.service.ClassService;
import vn.edu.ute.productmgmt.service.StudentService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class EnrollmentFormDialog extends JDialog {

    // Sử dụng JComboBox chứa đối tượng Entity thay vì String
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

        // 1. Khởi tạo Spinner
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        spnEnrollmentDate = new JSpinner(dateModel);
        spnEnrollmentDate.setEditor(new JSpinner.DateEditor(spnEnrollmentDate, "dd/MM/yyyy"));

        // 2. Nạp dữ liệu thực tế từ Service
        try {
            studentService.findAll().forEach(cboStudent::addItem);
            classService.findAll().forEach(cboClass::addItem);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách: " + e.getMessage());
        }

        // 3. Tùy chỉnh hiển thị cho ComboBox (Renderer)
        // Nếu không có cái này, ComboBox sẽ hiển thị mã băm của Object
        setupComboBoxRenderers();

        buildUI();

        // 4. Đổ dữ liệu cũ (nếu có)
        if (existing != null) {
            this.result = existing;
            setSelectedStudent(existing.getStudentId());
            setSelectedClass(existing.getClassId());
            cboStatus.setSelectedItem(existing.getStatus());
            cboResult.setSelectedItem(existing.getResult());

            try {
                if (existing.getEnrollmentDate() != null) {
                    LocalDate ld = LocalDate.parse(existing.getEnrollmentDate());
                    Date date = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    spnEnrollmentDate.setValue(date);
                }
            } catch (Exception e) {
                spnEnrollmentDate.setValue(new Date());
            }
        } else {
            result = new EnrollmentFormData();
            cboStatus.setSelectedItem(EnrollmentStatus.Enrolled);
            cboResult.setSelectedItem(EnrollmentResult.NA);
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private void setupComboBoxRenderers() {
        cboStudent.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel();
            if (value != null) label.setText(value.getFullName() + " (" + value.getId() + ")");
            return label;
        });

        cboClass.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel();
            if (value != null) label.setText(value.getClassName());
            return label;
        });
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;
        addFormRow(form, "Học viên:", cboStudent, g, r++);
        addFormRow(form, "Lớp học:", cboClass, g, r++);
        addFormRow(form, "Ngày ghi danh:", spnEnrollmentDate, g, r++);
        addFormRow(form, "Trạng thái:", cboStatus, g, r++);
        addFormRow(form, "Kết quả:", cboResult, g, r++);

        JButton btnSave = new JButton("Lưu");
        btnSave.addActionListener(e -> onSave());
        JButton btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnSave);
        actions.add(btnCancel);

        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel p, String label, JComponent comp, GridBagConstraints g, int row) {
        g.gridy = row;
        g.gridx = 0; g.weightx = 0; p.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 1.0; p.add(comp, g);
    }

    private void onSave() {
        Student selectedS = (Student) cboStudent.getSelectedItem();
        TeachingClass selectedC = (TeachingClass) cboClass.getSelectedItem();

        if (selectedS == null || selectedC == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đầy đủ học viên và lớp!");
            return;
        }

        Date dateValue = (Date) spnEnrollmentDate.getValue();
        LocalDate localDate = dateValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        result.setStudentId(selectedS.getId()); // Lưu ID từ Object
        result.setClassId(selectedC.getId());   // Lưu ID từ Object
        result.setEnrollmentDate(localDate.toString());
        result.setStatus((EnrollmentStatus) cboStatus.getSelectedItem());
        result.setResult((EnrollmentResult) cboResult.getSelectedItem());

        saved = true;
        dispose();
    }

    // Helper để chọn đúng Item trong ComboBox khi Edit
    private void setSelectedStudent(Long id) {
        for (int i = 0; i < cboStudent.getItemCount(); i++) {
            if (cboStudent.getItemAt(i).getId().equals(id)) {
                cboStudent.setSelectedIndex(i);
                break;
            };
        }
    }

    private void setSelectedClass(Long id) {
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
        private Long studentId;
        private Long classId;
        private String enrollmentDate;
        private EnrollmentStatus status;
        private EnrollmentResult result;

        // Getters & Setters ...
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