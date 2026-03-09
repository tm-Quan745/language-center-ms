package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.model.enums.StaffRole;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StaffFormDialog extends JDialog {

    private final JTextField txtFullName = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JComboBox<StaffRole> cboRole = new JComboBox<>(StaffRole.values());
    private final JComboBox<ActiveStatus> cboStatus = new JComboBox<>(ActiveStatus.values());

    private boolean saved = false;
    private StaffFormData result;

    public StaffFormDialog(Window owner, StaffFormData existing) {
        super(owner, "Thông tin nhân viên", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setSize(520, 600); // Kích thước rộng rãi hơn
        setLayout(new BorderLayout());

        buildUI();

        if (existing != null) {
            txtFullName.setText(existing.getFullName());
            txtPhone.setText(existing.getPhone());
            txtEmail.setText(existing.getEmail());
            cboRole.setSelectedItem(existing.getRole());
            cboStatus.setSelectedItem(existing.getStatus());
        } else {
            cboRole.setSelectedItem(StaffRole.Other);
            cboStatus.setSelectedItem(ActiveStatus.Active);
        }

        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(30, 40, 30, 40));
        root.setBackground(Color.WHITE);

        // --- Tiêu đề ---
        JLabel lblHeader = new JLabel("Chi tiết hồ sơ nhân viên");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setBorder(new EmptyBorder(0, 0, 25, 0));
        root.add(lblHeader, BorderLayout.NORTH);

        // --- Form nhập liệu ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0); // Khoảng cách giữa các hàng

        // Hàm helper để thêm hàng cho Form
        addRow(form, gbc, 0, "Họ và tên", txtFullName, "Nhập tên nhân viên...", " 👤 ");
        addRow(form, gbc, 1, "Số điện thoại", txtPhone, "Nhập số liên lạc...", " 📞 ");
        addRow(form, gbc, 2, "Địa chỉ Email", txtEmail, "example@email.com", " ✉ ");

        // Hàng Chức vụ
        gbc.gridy = 3; gbc.gridx = 0; gbc.weightx = 0;
        form.add(createFieldLabel("Chức vụ"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.insets = new Insets(8, 15, 8, 0);
        styleCombo(cboRole);
        form.add(cboRole, gbc);

        // Hàng Trạng thái
        gbc.gridy = 4; gbc.gridx = 0; gbc.weightx = 0; gbc.insets = new Insets(8, 0, 8, 0);
        form.add(createFieldLabel("Trạng thái"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.insets = new Insets(8, 15, 8, 0);
        styleCombo(cboStatus);
        form.add(cboStatus, gbc);

        root.add(form, BorderLayout.CENTER);

        // --- Nút hành động ---
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(30, 0, 0, 0));

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setPreferredSize(new Dimension(100, 42));
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc:12; background:#f2f2f2; borderWidth:0");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Lưu hồ sơ");
        btnSave.setPreferredSize(new Dimension(130, 42));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc:12; background:#0d6efd; foreground:#fff; borderWidth:0");
        btnSave.addActionListener(e -> onSave());

        actions.add(btnCancel);
        actions.add(btnSave);
        root.add(actions, BorderLayout.SOUTH);

        add(root);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field, String placeholder, String icon) {
        gbc.gridy = row;

        // Label
        gbc.gridx = 0; gbc.weightx = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        panel.add(createFieldLabel(label), gbc);

        // Field
        gbc.gridx = 1; gbc.weightx = 1;
        gbc.insets = new Insets(8, 15, 8, 0); // Đẩy field sang phải một chút cho thoáng
        field.setPreferredSize(new Dimension(0, 42));
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new JLabel(icon));
        field.putClientProperty(FlatClientProperties.STYLE, "arc:12");
        panel.add(field, gbc);
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setPreferredSize(new Dimension(0, 42));
        combo.putClientProperty(FlatClientProperties.STYLE, "arc:12");
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private void onSave() {
        if (txtFullName.getText().isBlank() || txtPhone.getText().isBlank() || txtEmail.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        result = new StaffFormData();
        result.setFullName(txtFullName.getText().trim());
        result.setPhone(txtPhone.getText().trim());
        result.setEmail(txtEmail.getText().trim());
        result.setRole((StaffRole) cboRole.getSelectedItem());
        result.setStatus((ActiveStatus) cboStatus.getSelectedItem());

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public StaffFormData getResult() { return result; }

    public static class StaffFormData {
        private String fullName, phone, email;
        private StaffRole role;
        private ActiveStatus status;
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public StaffRole getRole() { return role; }
        public void setRole(StaffRole role) { this.role = role; }
        public ActiveStatus getStatus() { return status; }
        public void setStatus(ActiveStatus status) { this.status = status; }
    }
}