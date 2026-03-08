package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.TeachingClass;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CertificateFormDialog extends JDialog {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

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
        super(owner, "Chứng chỉ", ModalityType.APPLICATION_MODAL);
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

        spnIssueDate = createDateSpinner();

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
            spnIssueDate.setValue(new Date());
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, DATE_FORMAT);
        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(140, spinner.getPreferredSize().height));
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
        form.add(new JLabel("Lớp (tùy chọn):"), g);
        g.gridx = 1;
        form.add(cboClass, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Tên chứng chỉ:"), g);
        g.gridx = 1;
        form.add(txtCertName, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Ngày cấp:"), g);
        g.gridx = 1;
        form.add(spnIssueDate, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Số seri (tùy chọn):"), g);
        g.gridx = 1;
        form.add(txtSerialNo, g);

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
            Student student = (Student) cboStudent.getSelectedItem();
            if (student == null) {
                throw new IllegalArgumentException("Vui lòng chọn học viên.");
            }
            String certName = txtCertName.getText().trim();
            if (certName.isEmpty()) {
                throw new IllegalArgumentException("Tên chứng chỉ không được để trống.");
            }
            Date d = (Date) spnIssueDate.getValue();
            LocalDate issueDate = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
    public CertificateFormData getResult() { return result; }

    public static class CertificateFormData {
        private String studentId;
        private String classId;
        private String certName;
        private String issueDate;
        private String serialNo;

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public String getClassId() { return classId; }
        public void setClassId(String classId) { this.classId = classId; }
        public String getCertName() { return certName; }
        public void setCertName(String certName) { this.certName = certName; }
        public String getIssueDate() { return issueDate; }
        public void setIssueDate(String issueDate) { this.issueDate = issueDate; }
        public String getSerialNo() { return serialNo; }
        public void setSerialNo(String serialNo) { this.serialNo = serialNo; }
    }
}
