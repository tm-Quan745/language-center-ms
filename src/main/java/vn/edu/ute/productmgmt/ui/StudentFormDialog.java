package vn.edu.ute.productmgmt.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Form nhập liệu đơn giản cho Student, dùng mock object.
 */
public class StudentFormDialog extends JDialog {

    private final JTextField txtFullName = new JTextField(25);
    private final JTextField txtDob = new JTextField(10);
    private final JComboBox<String> cboGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
    private final JTextField txtPhone = new JTextField(15);
    private final JTextField txtEmail = new JTextField(25);
    private final JTextField txtAddress = new JTextField(25);
    private final JComboBox<String> cboStatus = new JComboBox<>(new String[]{"Active", "Inactive"});

    private boolean saved = false;
    private StudentFormData result;

    public StudentFormDialog(Window owner, StudentFormData existing) {
        super(owner, "Student", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();

        if (existing != null) {
            txtFullName.setText(existing.getFullName());
            txtDob.setText(existing.getDateOfBirth());
            cboGender.setSelectedItem(existing.getGender());
            txtPhone.setText(existing.getPhone());
            txtEmail.setText(existing.getEmail());
            txtAddress.setText(existing.getAddress());
            cboStatus.setSelectedItem(existing.getStatus());
            result = existing;
        } else {
            result = new StudentFormData();
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;

        int r = 0;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Full name:"), g);
        g.gridx = 1;
        form.add(txtFullName, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Date of birth:"), g);
        g.gridx = 1;
        form.add(txtDob, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Gender:"), g);
        g.gridx = 1;
        form.add(cboGender, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Phone:"), g);
        g.gridx = 1;
        form.add(txtPhone, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Email:"), g);
        g.gridx = 1;
        form.add(txtEmail, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Address:"), g);
        g.gridx = 1;
        form.add(txtAddress, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
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
            result.setDateOfBirth(txtDob.getText().trim());
            result.setGender((String) cboGender.getSelectedItem());
            result.setPhone(phone);
            result.setEmail(email);
            result.setAddress(txtAddress.getText().trim());
            result.setStatus((String) cboStatus.getSelectedItem());

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public StudentFormData getResult() {
        return result;
    }

    /**
     * DTO đơn giản đại diện thông tin Student cho UI.
     */
    public static class StudentFormData {
        private String fullName;
        private String dateOfBirth;
        private String gender;
        private String phone;
        private String email;
        private String address;
        private String status;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
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

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}

