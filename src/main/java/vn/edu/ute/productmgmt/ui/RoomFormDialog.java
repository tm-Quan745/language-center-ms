package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Branch;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Form nhập liệu cho Phòng học, chuẩn hóa theo ClassFormDialog
 */
public class RoomFormDialog extends JDialog {

    private final JComboBox<Branch> cboBranch;
    private final JTextField txtName = new JTextField(25);
    private final JTextField txtCapacity = new JTextField(10);
    private final JTextField txtLocation = new JTextField(25);
    private final JComboBox<ActiveStatus> cboStatus = new JComboBox<>(ActiveStatus.values());

    private boolean saved = false;
    private RoomFormData result;

    public RoomFormDialog(Window owner, RoomFormData existing, List<Branch> branches) {
        super(owner, "Thông tin Phòng học", ModalityType.APPLICATION_MODAL);

        setSize(580, 680);
        setLayout(new BorderLayout());

        List<Branch> list = new ArrayList<>();
        list.add(null);
        if (branches != null) list.addAll(branches);
        cboBranch = new JComboBox<>(list.toArray(new Branch[0]));
        cboBranch.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "— Không chọn —" : ((Branch) value).getBranchName());
                return this;
            }
        });

        buildUI();

        if (existing != null) {
            setSelectedBranchById(existing.getBranchId());
            txtName.setText(existing.getName());
            txtCapacity.setText(existing.getCapacity());
            txtLocation.setText(existing.getLocation());
            if (existing.getStatus() != null) cboStatus.setSelectedItem(existing.getStatus());
            result = existing;
        } else {
            result = new RoomFormData();
        }

        setLocationRelativeTo(owner);
    }

    private void setSelectedBranchById(String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) {
            cboBranch.setSelectedIndex(0);
            return;
        }
        try {
            Long id = Long.parseLong(idStr.trim());
            for (int i = 0; i < cboBranch.getItemCount(); i++) {
                Branch b = cboBranch.getItemAt(i);
                if (b != null && id.equals(b.getId())) {
                    cboBranch.setSelectedIndex(i);
                    return;
                }
            }
        } catch (Exception ignored) { }
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- Header ---
        JLabel lblHeader = new JLabel("Thông tin Phòng học");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setBorder(new EmptyBorder(0, 0, 25, 0));
        root.add(lblHeader, BorderLayout.NORTH);

        // --- Form Body ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        // Các hàng dữ liệu
        addFormRow(form, gbc, 0, "Chi nhánh:", cboBranch);
        addFormRow(form, gbc, 1, "Tên phòng học:", txtName);
        addFormRow(form, gbc, 2, "Sức chứa:", txtCapacity);
        addFormRow(form, gbc, 3, "Vị trí:", txtLocation);
        addFormRow(form, gbc, 4, "Trạng thái:", cboStatus);

        root.add(form, BorderLayout.CENTER);

        // --- Buttons ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(25, 0, 0, 0));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(100, 42));
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #f2f2f2; borderWidth: 0");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Lưu Phòng học");
        btnSave.setPreferredSize(new Dimension(140, 42));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #198754; foreground: #ffffff; borderWidth: 0");
        btnSave.addActionListener(e -> onSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        root.add(footer, BorderLayout.SOUTH);

        add(root);
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridy = row;
        gbc.gridx = 0; gbc.weightx = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        p.add(createLabel(label), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 20, 8, 0);
        if (comp instanceof JComboBox || comp instanceof JTextField) {
            comp.setPreferredSize(new Dimension(0, 40));
            comp.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        }
        p.add(comp, gbc);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private void onSave() {
        try {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Tên phòng học không được để trống.");
            }

            Branch sel = (Branch) cboBranch.getSelectedItem();
            result.setBranchId(sel != null ? sel.getId().toString() : "");
            result.setName(name);
            result.setCapacity(txtCapacity.getText().trim());
            result.setLocation(txtLocation.getText().trim());
            result.setStatus((ActiveStatus) cboStatus.getSelectedItem());

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Thông báo lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public RoomFormData getResult() {
        return result;
    }

    /**
     * DTO đơn giản đại diện thông tin Phòng học cho UI.
     */
    public static class RoomFormData {
        private String branchId;
        private String name;
        private String capacity;
        private String location;
        private ActiveStatus status;

        public String getBranchId() { return branchId; }
        public void setBranchId(String branchId) { this.branchId = branchId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCapacity() { return capacity; }
        public void setCapacity(String capacity) { this.capacity = capacity; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public ActiveStatus getStatus() { return status; }
        public void setStatus(ActiveStatus status) { this.status = status; }
    }
}
