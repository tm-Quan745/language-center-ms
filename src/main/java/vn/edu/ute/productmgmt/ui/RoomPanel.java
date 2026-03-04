package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Room;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.service.RoomService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel Phòng học: form (Tên phòng, Sức chứa) + JTable danh sách, ghép RoomService.
 */
public class RoomPanel extends JPanel {

    private final RoomService roomService;

    private final JTextField txtRoomName = new JTextField(25);
    private final JTextField txtCapacity = new JTextField(10);
    private final JTextField txtLocation = new JTextField(25);
    private final JComboBox<ActiveStatus> cboStatus = new JComboBox<>(ActiveStatus.values());
    private final JLabel lblInfo = new JLabel(" ");

    private final RoomTableModel tableModel = new RoomTableModel();
    private final JTable table = new JTable(tableModel);
    private Room selectedRoom;

    public RoomPanel(RoomService roomService) {
        this.roomService = roomService;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            onTableSelection();
        });
        loadTable();
    }

    private void buildUI() {
        add(buildActionBar(), BorderLayout.NORTH);
        add(buildFormAndTable(), BorderLayout.CENTER);
        add(lblInfo, BorderLayout.SOUTH);
    }

    private JComponent buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton btnAdd = new JButton("Thêm mới");
        JButton btnSave = new JButton("Chỉnh sửa");
        JButton btnDelete = new JButton("Xóa");
        JButton btnRefresh = new JButton("Tải lại");

        btnAdd.addActionListener(e -> onAdd());
        btnSave.addActionListener(e -> onSave());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> loadTable());

        bar.add(btnAdd);
        bar.add(btnSave);
        bar.add(btnDelete);
        bar.add(btnRefresh);
        return bar;
    }

    private JComponent buildFormAndTable() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        UI.stylePanelBorder(wrapper, "Danh sách phòng học");

        JScrollPane scroll = new JScrollPane(table);
        UI.styleTable(table);
        wrapper.add(scroll, BorderLayout.CENTER);

        return wrapper;
    }

    private void addField(JPanel form, GridBagConstraints g, int row, int col, String label, JComponent field) {
        g.gridy = row;
        g.gridx = col * 2;
        g.weightx = 0.0;
        form.add(new JLabel(label), g);
        g.gridx = col * 2 + 1;
        g.weightx = 1.0;
        form.add(field, g);
    }

    private void loadTable() {
        try {
            List<Room> list = roomService.findAll();
            tableModel.setData(list);
            lblInfo.setText("Tổng số: " + list.size() + " phòng.");
            clearSelection();
        } catch (Exception ex) {
            lblInfo.setText("Lỗi: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Không tải được danh sách: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onTableSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            selectedRoom = null;
            return;
        }
        selectedRoom = tableModel.getRoomAt(row);
    }

    private void onAdd() {
        RoomFormDialog dialog = new RoomFormDialog(
                SwingUtilities.getWindowAncestor(this),
                null
        );
        dialog.setVisible(true);
        if (!dialog.isSaved()) {
            return;
        }

        RoomFormDialog.RoomFormData data = dialog.getResult();
        Room r = formDataToRoom(data, null);
        if (r == null) return;
        try {
            roomService.create(r);
            JOptionPane.showMessageDialog(this, "Đã thêm phòng học.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSave() {
        if (selectedRoom == null) {
            JOptionPane.showMessageDialog(this, "Chọn một phòng để sửa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        RoomFormDialog.RoomFormData existing = roomToFormData(selectedRoom);
        RoomFormDialog dialog = new RoomFormDialog(
                SwingUtilities.getWindowAncestor(this),
                existing
        );
        dialog.setVisible(true);
        if (!dialog.isSaved()) {
            return;
        }

        RoomFormDialog.RoomFormData data = dialog.getResult();
        Room r = formDataToRoom(data, selectedRoom.getId());
        if (r == null) return;
        try {
            roomService.update(r);
            JOptionPane.showMessageDialog(this, "Đã cập nhật phòng học.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            clearSelection();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private RoomFormDialog.RoomFormData roomToFormData(Room r) {
        RoomFormDialog.RoomFormData data = new RoomFormDialog.RoomFormData();
        data.setName(r.getRoomName());
        data.setCapacity(String.valueOf(r.getCapacity()));
        data.setLocation(r.getLocation() != null ? r.getLocation() : "");
        data.setStatus(r.getStatus());
        return data;
    }

    private void onDelete() {
        if (selectedRoom == null) {
            JOptionPane.showMessageDialog(this, "Chọn một phòng để xóa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa phòng này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            roomService.delete(selectedRoom.getId());
            JOptionPane.showMessageDialog(this, "Đã xóa phòng học.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            clearSelection();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Room formToRoom(Long keepId) {
        String name = txtRoomName.getText().trim();
        String capStr = txtCapacity.getText().trim();
        String location = txtLocation.getText().trim();
        ActiveStatus status = (ActiveStatus) cboStatus.getSelectedItem();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên phòng không được để trống.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int capacity = 0;
        if (!capStr.isEmpty()) {
            try {
                capacity = Integer.parseInt(capStr.trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Sức chứa phải là số nguyên dương.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }
        if (capacity <= 0) {
            JOptionPane.showMessageDialog(this, "Sức chứa phải lớn hơn 0.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Room r = new Room();
        if (keepId != null) r.setId(keepId);
        r.setRoomName(name);
        r.setCapacity(capacity);
        r.setLocation(location.isEmpty() ? null : location);
        r.setStatus(status != null ? status : ActiveStatus.Active);
        return r;
    }

    private Room formDataToRoom(RoomFormDialog.RoomFormData data, Long keepId) {
        String name = data.getName() != null ? data.getName().trim() : "";
        String capStr = data.getCapacity() != null ? data.getCapacity().trim() : "";
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên phòng không được để trống.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int capacity = 0;
        if (!capStr.isEmpty()) {
            try {
                capacity = Integer.parseInt(capStr.trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Sức chứa phải là số nguyên dương.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }
        if (capacity <= 0) {
            JOptionPane.showMessageDialog(this, "Sức chứa phải lớn hơn 0.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Room r = new Room();
        if (keepId != null) r.setId(keepId);
        r.setRoomName(name);
        r.setCapacity(capacity);
        r.setLocation(data.getLocation());
        r.setStatus(data.getStatus());
        return r;
    }

    private void clearForm() {
        txtRoomName.setText("");
        txtCapacity.setText("");
        txtLocation.setText("");
        cboStatus.setSelectedItem(ActiveStatus.Active);
        selectedRoom = null;
    }

    private void clearSelection() {
        selectedRoom = null;
        clearForm();
        table.clearSelection();
    }

    private static class RoomTableModel extends AbstractTableModel {
        private final String[] columns = {"Tên phòng", "Sức chứa", "Vị trí", "Trạng thái"};
        private List<Room> data = new ArrayList<>();

        void setData(List<Room> data) {
            this.data = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        Room getRoomAt(int row) {
            if (row < 0 || row >= data.size()) return null;
            return data.get(row);
        }

        @Override
        public int getRowCount() { return data.size(); }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Room r = data.get(row);
            switch (col) {
                case 0: return r.getRoomName();
                case 1: return r.getCapacity();
                case 2: return r.getLocation() != null ? r.getLocation() : "";
                case 3: return r.getStatus() != null ? r.getStatus().name() : "";
                default: return "";
            }
        }
    }
}
