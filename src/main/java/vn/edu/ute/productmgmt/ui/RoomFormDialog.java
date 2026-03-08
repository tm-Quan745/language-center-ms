package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Branch;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Form nhập liệu đơn giản cho Room, dùng cho thao tác thêm/sửa.
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
        super(owner, "Phòng học", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        List<Branch> list = new ArrayList<>();
        list.add(null);
        if (branches != null) list.addAll(branches);
        cboBranch = new JComboBox<>(list.toArray(new Branch[0]));
        cboBranch.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "— Không —" : ((Branch) value).getBranchName());
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

        pack();
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
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Chi nhánh:"), g);
        g.gridx = 1;
        form.add(cboBranch, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Tên phòng:"), g);
        g.gridx = 1;
        form.add(txtName, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Sức chứa:"), g);
        g.gridx = 1;
        form.add(txtCapacity, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Vị trí:"), g);
        g.gridx = 1;
        form.add(txtLocation, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Trạng thái:"), g);
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
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Tên phòng không được để trống.");
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
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public RoomFormData getResult() {
        return result;
    }

    /**
     * DTO đơn giản đại diện thông tin Room cho UI.
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

