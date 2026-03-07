package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.enums.EnrollmentResult;
import vn.edu.ute.productmgmt.model.enums.EnrollmentStatus;

import javax.swing.*;
import java.awt.*;

public class EnrollmentFormDialog extends JDialog {

    private final JTextField txtStudentId = new JTextField(10);
    private final JTextField txtClassId = new JTextField(10);
    private final JTextField txtEnrollmentDate = new JTextField(10);
    private final JComboBox<EnrollmentStatus> cboStatus = new JComboBox<>(EnrollmentStatus.values());
    private final JComboBox<EnrollmentResult> cboResult = new JComboBox<>(EnrollmentResult.values());

    private boolean saved = false;
    private EnrollmentFormData result;

    public EnrollmentFormDialog(Window owner, EnrollmentFormData existing) {
        super(owner, "Ghi danh", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();

        if (existing != null) {
            txtStudentId.setText(existing.getStudentId());
            txtClassId.setText(existing.getClassId());
            txtEnrollmentDate.setText(existing.getEnrollmentDate());
            if (existing.getStatus() != null) {
                cboStatus.setSelectedItem(existing.getStatus());
            }
            if (existing.getResult() != null) {
                cboResult.setSelectedItem(existing.getResult());
            }
            result = existing;
        } else {
            result = new EnrollmentFormData();
            cboStatus.setSelectedItem(EnrollmentStatus.Enrolled);
            cboResult.setSelectedItem(EnrollmentResult.NA);
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;

        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Student ID:"), g);
        g.gridx = 1;
        form.add(txtStudentId, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Class ID:"), g);
        g.gridx = 1;
        form.add(txtClassId, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Ngày ghi danh (yyyy-MM-dd, để trống = hôm nay):"), g);
        g.gridx = 1;
        form.add(txtEnrollmentDate, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Trạng thái:"), g);
        g.gridx = 1;
        form.add(cboStatus, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Kết quả:"), g);
        g.gridx = 1;
        form.add(cboResult, g);

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
            String studentId = txtStudentId.getText().trim();
            String classId = txtClassId.getText().trim();
            if (studentId.isEmpty() || classId.isEmpty()) {
                throw new IllegalArgumentException("Student ID và Class ID không được để trống.");
            }
            Long.parseLong(studentId);
            Long.parseLong(classId);

            result.setStudentId(studentId);
            result.setClassId(classId);
            result.setEnrollmentDate(txtEnrollmentDate.getText().trim());
            result.setStatus((EnrollmentStatus) cboStatus.getSelectedItem());
            result.setResult((EnrollmentResult) cboResult.getSelectedItem());

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public EnrollmentFormData getResult() {
        return result;
    }

    public static class EnrollmentFormData {
        private String studentId;
        private String classId;
        private String enrollmentDate;
        private EnrollmentStatus status;
        private EnrollmentResult result;

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

        public String getEnrollmentDate() {
            return enrollmentDate;
        }

        public void setEnrollmentDate(String enrollmentDate) {
            this.enrollmentDate = enrollmentDate;
        }

        public EnrollmentStatus getStatus() {
            return status;
        }

        public void setStatus(EnrollmentStatus status) {
            this.status = status;
        }

        public EnrollmentResult getResult() {
            return result;
        }

        public void setResult(EnrollmentResult result) {
            this.result = result;
        }
    }
}

