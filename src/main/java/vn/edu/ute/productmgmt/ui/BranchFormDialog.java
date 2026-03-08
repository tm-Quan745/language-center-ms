package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.enums.ActiveStatus;

import javax.swing.*;
import java.awt.*;

public class BranchFormDialog extends JDialog {

    private final JTextField txtName = new JTextField(25);
    private final JTextField txtAddress = new JTextField(25);
    private final JTextField txtPhone = new JTextField(15);
    private final JComboBox<ActiveStatus> cboStatus = new JComboBox<>(ActiveStatus.values());

    private boolean saved = false;
    private BranchFormData result;

    public BranchFormDialog(Window owner, BranchFormData existing) {
        super(owner, "Chi nhánh", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();

        if (existing != null) {
            txtName.setText(existing.getName());
            txtAddress.setText(existing.getAddress());
            txtPhone.setText(existing.getPhone());
            if (existing.getStatus() != null) cboStatus.setSelectedItem(existing.getStatus());
            result = existing;
        } else {
            result = new BranchFormData();
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
        form.add(new JLabel("Tên chi nhánh:"), g);
        g.gridx = 1;
        form.add(txtName, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Địa chỉ:"), g);
        g.gridx = 1;
        form.add(txtAddress, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Điện thoại:"), g);
        g.gridx = 1;
        form.add(txtPhone, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Trạng thái:"), g);
        g.gridx = 1;
        form.add(cboStatus, g);

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
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Tên chi nhánh không được để trống.");
            }
            result.setName(name);
            result.setAddress(txtAddress.getText().trim());
            result.setPhone(txtPhone.getText().trim());
            result.setStatus((ActiveStatus) cboStatus.getSelectedItem());
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
    public BranchFormData getResult() { return result; }

    public static class BranchFormData {
        private String name;
        private String address;
        private String phone;
        private ActiveStatus status;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public ActiveStatus getStatus() { return status; }
        public void setStatus(ActiveStatus status) { this.status = status; }
    }
}
