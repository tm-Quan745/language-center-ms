package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.enums.ActiveStatus;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class TeacherFormDialog extends JDialog {

    private final JTextField txtFullName = new JTextField(25);
    private final JTextField txtPhone = new JTextField(15);
    private final JTextField txtEmail = new JTextField(25);
    private final JTextField txtSpecialty = new JTextField(20);

    // 1. Khai báo JSpinner thay cho JTextField
    private final JSpinner spnHireDate;

    private final JComboBox<ActiveStatus> cboStatus = new JComboBox<>(ActiveStatus.values());

    private boolean saved = false;
    private TeacherFormData result;

    public TeacherFormDialog(Window owner, TeacherFormData existing) {
        super(owner, "Teacher", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 2. Khởi tạo Spinner với giới hạn: Không quá ngày hiện tại
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, new Date(), Calendar.DAY_OF_MONTH);
        spnHireDate = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spnHireDate, "dd/MM/yyyy");
        spnHireDate.setEditor(dateEditor);

        buildUI();

        if (existing != null) {
            this.result = existing;
            txtFullName.setText(existing.getFullName());
            txtPhone.setText(existing.getPhone());
            txtEmail.setText(existing.getEmail());
            txtSpecialty.setText(existing.getSpecialty());
            cboStatus.setSelectedItem(existing.getStatus());

            // Đổ dữ liệu ngày vào làm
            try {
                if (existing.getHireDate() != null) {
                    LocalDate ld = LocalDate.parse(existing.getHireDate());
                    Date date = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    spnHireDate.setValue(date);
                }
            } catch (Exception e) {
                spnHireDate.setValue(new Date());
            }
        } else {
            result = new TeacherFormData();
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
        addFormRow(form, "Full name:", txtFullName, g, r++);
        addFormRow(form, "Phone:", txtPhone, g, r++);
        addFormRow(form, "Email:", txtEmail, g, r++);
        addFormRow(form, "Specialty:", txtSpecialty, g, r++);
        addFormRow(form, "Hire date:", spnHireDate, g, r++); // Sử dụng Spinner
        addFormRow(form, "Status:", cboStatus, g, r++);

        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnSave);
        actions.add(btnCancel);

        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(actions, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel p, String label, JComponent comp, GridBagConstraints g, int row) {
        g.gridy = row;
        g.gridx = 0; g.weightx = 0; p.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 1.0; p.add(comp, g);
    }

    private void onSave() {
        try {
            // Quan trọng: Đẩy giá trị đang gõ tay vào model của Spinner
            spnHireDate.commitEdit();

            String fullName = txtFullName.getText().trim();
            if (fullName.isEmpty()) throw new IllegalArgumentException("Full name is required.");

            // Xử lý ngày từ Spinner
            Date dateValue = (Date) spnHireDate.getValue();
            LocalDate localDate = dateValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            result.setFullName(fullName);
            result.setPhone(txtPhone.getText().trim());
            result.setEmail(txtEmail.getText().trim());
            result.setSpecialty(txtSpecialty.getText().trim());
            result.setHireDate(localDate.toString()); // Lưu yyyy-MM-dd
            result.setStatus((ActiveStatus) cboStatus.getSelectedItem());

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
    public TeacherFormData getResult() { return result; }

    // DTO giữ nguyên...
    public static class TeacherFormData {
        private String fullName;
        private String phone;
        private String email;
        private String specialty;
        private String hireDate;
        private ActiveStatus status;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getSpecialty() { return specialty; }
        public void setSpecialty(String specialty) { this.specialty = specialty; }
        public String getHireDate() { return hireDate; }
        public void setHireDate(String hireDate) { this.hireDate = hireDate; }
        public ActiveStatus getStatus() { return status; }
        public void setStatus(ActiveStatus status) { this.status = status; }
    }
}