package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BranchFormDialog extends JDialog {

    private final JTextField txtName = new JTextField();
    private final JTextField txtAddress = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JComboBox<ActiveStatus> cboStatus = new JComboBox<>(ActiveStatus.values());

    private boolean saved = false;
    private BranchFormData result;

    public BranchFormDialog(Window owner, BranchFormData existing) {
        super(owner, "Thông tin chi nhánh", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setSize(550, 450); // Tăng chiều rộng để nhãn và textbox nằm cùng hàng thoải mái
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        buildUI();

        if (existing != null) {
            txtName.setText(existing.getName());
            txtAddress.setText(existing.getAddress());
            txtPhone.setText(existing.getPhone());
            cboStatus.setSelectedItem(existing.getStatus());
            this.result = existing;
        } else {
            this.result = new BranchFormData();
            cboStatus.setSelectedItem(ActiveStatus.Active);
        }

        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- Tiêu đề ---
        JLabel lblHeader = new JLabel("Cấu hình chi nhánh");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setBorder(new EmptyBorder(0, 0, 25, 0));
        root.add(lblHeader, BorderLayout.NORTH);

        // --- Form Body (GridBagLayout để đưa Label và Textbox lên 1 hàng) ---
        JPanel formBody = new JPanel(new GridBagLayout());
        formBody.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0); // Khoảng cách giữa các hàng

        // Thêm các hàng
        addFormRow(formBody, gbc, 0, "Tên chi nhánh:", txtName, "Nhập tên chi nhánh...", " 🏢 ");
        addFormRow(formBody, gbc, 1, "Địa chỉ trụ sở:", txtAddress, "Địa chỉ chi tiết...", " 📍 ");
        addFormRow(formBody, gbc, 2, "Số điện thoại:", txtPhone, "Số hotline...", " 📞 ");

        // Hàng Trạng thái
        gbc.gridy = 3;
        gbc.gridx = 0; gbc.weightx = 0;
        formBody.add(createLabel("Trạng thái:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        gbc.insets = new Insets(8, 15, 8, 0); // Đẩy combo sang phải để tách khỏi label
        styleCombo(cboStatus);
        formBody.add(cboStatus, gbc);

        root.add(formBody, BorderLayout.CENTER);

        // --- Nút hành động ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(25, 0, 0, 0));

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #f2f2f2; borderWidth: 0");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Lưu dữ liệu");
        btnSave.setPreferredSize(new Dimension(130, 40));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #0d6efd; foreground: #ffffff; borderWidth: 0");
        btnSave.addActionListener(e -> onSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        root.add(footer, BorderLayout.SOUTH);

        add(root);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JTextField field, String placeholder, String icon) {
        gbc.gridy = row;

        // Cột 0: Label
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        panel.add(createLabel(labelText), gbc);

        // Cột 1: Textbox
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 15, 8, 0); // Tạo khoảng cách 15px giữa nhãn và ô nhập
        field.setPreferredSize(new Dimension(0, 42));
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new JLabel(icon));
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        panel.add(field, gbc);
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setPreferredSize(new Dimension(0, 42));
        combo.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        lbl.setForeground(new Color(70, 70, 70));
        return lbl;
    }

    private void onSave() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên chi nhánh!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        result.setName(name);
        result.setAddress(txtAddress.getText().trim());
        result.setPhone(txtPhone.getText().trim());
        result.setStatus((ActiveStatus) cboStatus.getSelectedItem());
        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public BranchFormData getResult() { return result; }

    public static class BranchFormData {
        private String name, address, phone;
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