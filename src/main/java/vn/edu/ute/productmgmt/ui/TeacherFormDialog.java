package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.enums.ActiveStatus;

import javax.swing.*;
import java.awt.*;

/**
 * Form nhập liệu đơn giản cho Teacher.
 */
public class TeacherFormDialog extends JDialog {

    private final JTextField txtFullName = new JTextField(25);
    private final JTextField txtPhone = new JTextField(15);
    private final JTextField txtEmail = new JTextField(25);
    private final JTextField txtSpecialty = new JTextField(20);
    private final JTextField txtHireDate = new JTextField(10);
    private final JComboBox<ActiveStatus> cboStatus = new JComboBox<>(ActiveStatus.values());

    private boolean saved = false;
    private TeacherFormData result;

    public TeacherFormDialog(Window owner, TeacherFormData existing) {
        super(owner, "Teacher", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();

        if (existing != null) {
            txtFullName.setText(existing.getFullName());
            txtPhone.setText(existing.getPhone());
            txtEmail.setText(existing.getEmail());
            txtSpecialty.setText(existing.getSpecialty());
            txtHireDate.setText(existing.getHireDate());
            if (existing.getStatus() != null) {
                cboStatus.setSelectedItem(existing.getStatus());
            }
            result = existing;
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
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Full name:"), g);
        g.gridx = 1;
        form.add(txtFullName, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Phone:"), g);
        g.gridx = 1;
        form.add(txtPhone, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Email:"), g);
        g.gridx = 1;
        form.add(txtEmail, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Specialty:"), g);
        g.gridx = 1;
        form.add(txtSpecialty, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Hire date (yyyy-MM-dd):"), g);
        g.gridx = 1;
        form.add(txtHireDate, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Status:"), g);
        g.gridx = 1;
        form.add(cboStatus, g);

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

    private void onSave() {
        try {
            String fullName = txtFullName.getText().trim();
            if (fullName.isEmpty()) {
                throw new IllegalArgumentException("Full name is required.");
            }

            String phone = txtPhone.getText().trim();
            if (phone.isEmpty()) {
                throw new IllegalArgumentException("Phone is required.");
            }

            String email = txtEmail.getText().trim();
            if (email.isEmpty()) {
                throw new IllegalArgumentException("Email is required.");
            }

            result.setFullName(fullName);
            result.setPhone(phone);
            result.setEmail(email);
            result.setSpecialty(txtSpecialty.getText().trim());
            result.setHireDate(txtHireDate.getText().trim());
            result.setStatus((ActiveStatus) cboStatus.getSelectedItem());

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public TeacherFormData getResult() {
        return result;
    }

    /**
     * DTO đơn giản đại diện thông tin Teacher cho UI.
     */
    public static class TeacherFormData {
        private String fullName;
        private String phone;
        private String email;
        private String specialty;
        private String hireDate;
        private ActiveStatus status;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getSpecialty() {
            return specialty;
        }

        public void setSpecialty(String specialty) {
            this.specialty = specialty;
        }

        public String getHireDate() {
            return hireDate;
        }

        public void setHireDate(String hireDate) {
            this.hireDate = hireDate;
        }

        public ActiveStatus getStatus() {
            return status;
        }

        public void setStatus(ActiveStatus status) {
            this.status = status;
        }
    }
}

