package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.enums.ActiveStatus;

import javax.swing.*;
import java.awt.*;

/**
 * Form nhập liệu đơn giản cho Room, dùng cho thao tác thêm/sửa.
 */
public class RoomFormDialog extends JDialog {

    private final JTextField txtName = new JTextField(25);
    private final JTextField txtCapacity = new JTextField(10);
    private final JTextField txtLocation = new JTextField(25);
    private final JComboBox<ActiveStatus> cboStatus = new JComboBox<>(ActiveStatus.values());

    private boolean saved = false;
    private RoomFormData result;

    public RoomFormDialog(Window owner, RoomFormData existing) {
        super(owner, "Room", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();

        if (existing != null) {
            txtName.setText(existing.getName());
            txtCapacity.setText(existing.getCapacity());
            txtLocation.setText(existing.getLocation());
            if (existing.getStatus() != null) {
                cboStatus.setSelectedItem(existing.getStatus());
            }
            result = existing;
        } else {
            result = new RoomFormData();
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
        form.add(new JLabel("Room name:"), g);
        g.gridx = 1;
        form.add(txtName, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Capacity:"), g);
        g.gridx = 1;
        form.add(txtCapacity, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Location:"), g);
        g.gridx = 1;
        form.add(txtLocation, g);

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
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Room name is required.");
            }

            result.setName(name);
            result.setCapacity(txtCapacity.getText().trim());
            result.setLocation(txtLocation.getText().trim());
            result.setStatus((ActiveStatus) cboStatus.getSelectedItem());

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
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
        private String name;
        private String capacity;
        private String location;
        private ActiveStatus status;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCapacity() {
            return capacity;
        }

        public void setCapacity(String capacity) {
            this.capacity = capacity;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public ActiveStatus getStatus() {
            return status;
        }

        public void setStatus(ActiveStatus status) {
            this.status = status;
        }
    }
}

