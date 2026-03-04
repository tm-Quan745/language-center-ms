package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.model.enums.StaffRole;

import javax.swing.*;
import java.awt.*;

public class StaffFormDialog extends JDialog {

    private final JTextField txtFullName = new JTextField(25);
    private final JTextField txtPhone = new JTextField(15);
    private final JTextField txtEmail = new JTextField(25);
    private final JComboBox<StaffRole> cboRole = new JComboBox<>(StaffRole.values());
    private final JComboBox<ActiveStatus> cboStatus = new JComboBox<>(ActiveStatus.values());

    private boolean saved = false;
    private StaffFormData result;

    public StaffFormDialog(Window owner, StaffFormData existing) {
        super(owner, "Thông tin nhân viên", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        buildUI();

        if (existing != null) {
            txtFullName.setText(existing.getFullName());
            txtPhone.setText(existing.getPhone());
            txtEmail.setText(existing.getEmail());
            if (existing.getRole() != null) {
                cboRole.setSelectedItem(existing.getRole());
            }
            if (existing.getStatus() != null) {
                cboStatus.setSelectedItem(existing.getStatus());
            }
        } else {
            cboRole.setSelectedItem(StaffRole.Other);
            cboStatus.setSelectedItem(ActiveStatus.Active);
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.gridy = 0;

        // Họ tên
        form.add(new JLabel("Họ tên:"), g);
        g.gridx = 1;
        form.add(txtFullName, g);

        // Điện thoại
        g.gridx = 0;
        g.gridy++;
        form.add(new JLabel("Điện thoại:"), g);
        g.gridx = 1;
        form.add(txtPhone, g);

        // Email
        g.gridx = 0;
        g.gridy++;
        form.add(new JLabel("Email:"), g);
        g.gridx = 1;
        form.add(txtEmail, g);

        // Chức vụ
        g.gridx = 0;
        g.gridy++;
        form.add(new JLabel("Chức vụ:"), g);
        g.gridx = 1;
        form.add(cboRole, g);

        // Trạng thái
        g.gridx = 0;
        g.gridy++;
        form.add(new JLabel("Trạng thái:"), g);
        g.gridx = 1;
        form.add(cboStatus, g);

        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());
        actions.add(btnCancel);
        actions.add(btnSave);

        add(actions, BorderLayout.SOUTH);
    }

    private void onSave() {
        String fullName = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        StaffRole role = (StaffRole) cboRole.getSelectedItem();
        ActiveStatus status = (ActiveStatus) cboStatus.getSelectedItem();

        if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Họ tên, Điện thoại, Email.");
            return;
        }

        StaffFormData data = new StaffFormData();
        data.setFullName(fullName);
        data.setPhone(phone);
        data.setEmail(email);
        data.setRole(role != null ? role : StaffRole.Other);
        data.setStatus(status != null ? status : ActiveStatus.Active);

        this.result = data;
        this.saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    public StaffFormData getResult() {
        return result;
    }

    public static class StaffFormData {
        private String fullName;
        private String phone;
        private String email;
        private StaffRole role;
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

        public StaffRole getRole() {
            return role;
        }

        public void setRole(StaffRole role) {
            this.role = role;
        }

        public ActiveStatus getStatus() {
            return status;
        }

        public void setStatus(ActiveStatus status) {
            this.status = status;
        }
    }
}

