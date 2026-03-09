package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Room;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.service.BranchService;
import vn.edu.ute.productmgmt.service.RoomService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel quản lý phòng học, chuẩn hóa theo ClassPanel
 */
public class RoomPanel extends JPanel {

    private final RoomService roomService;
    private final BranchService branchService;

    private final JLabel lblInfo = new JLabel(" ");
    private final RoomTableModel tableModel = new RoomTableModel();
    private final JTable table = new JTable(tableModel);
    private Room selectedRoom;

    public RoomPanel(RoomService roomService, BranchService branchService) {
        this.roomService = roomService;
        this.branchService = branchService;

        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        buildUI();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            onTableSelection();
        });

        loadTable();
    }

    private void buildUI() {

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Quản lý Phòng học");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        header.add(title, BorderLayout.WEST);
        header.add(buildActionBar(), BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        add(buildTableArea(), BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);

        lblInfo.setForeground(Color.GRAY);
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 13));

        statusBar.add(lblInfo, BorderLayout.WEST);

        add(statusBar, BorderLayout.SOUTH);
    }

    private JComponent buildActionBar() {

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bar.setOpaque(false);

        JButton btnAdd = createBtn("Thêm", "#0d6efd", "➕ ");
        JButton btnEdit = createBtn("Sửa", "#ffc107", "📝 ");
        JButton btnDelete = createBtn("Xóa", "#dc3545", "🗑 ");
        JButton btnRefresh = new JButton("Tải lại");

        btnRefresh.setPreferredSize(new Dimension(90, 36));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc:10");

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onSave());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> loadTable());

        bar.add(btnRefresh);
        bar.add(btnAdd);
        bar.add(btnEdit);
        bar.add(btnDelete);

        return bar;
    }

    private JButton createBtn(String text, String color, String icon) {

        JButton btn = new JButton(icon + text);

        btn.setPreferredSize(new Dimension(110, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String fg = color.equals("#ffc107") ? "#000000" : "#ffffff";

        btn.putClientProperty(FlatClientProperties.STYLE,
                "background:" + color +
                        ";foreground:" + fg +
                        ";arc:10;borderWidth:0");

        return btn;
    }

    private JComponent buildTableArea() {

        JScrollPane scroll = new JScrollPane(table);

        scroll.putClientProperty(FlatClientProperties.STYLE, "arc:15");
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        table.setRowHeight(45);
        table.setShowVerticalLines(false);

        table.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        return scroll;
    }

    private void loadTable() {

        try {

            List<Room> list = roomService.findAll();

            tableModel.setData(list);

            lblInfo.setText("Tổng số: " + list.size() + " phòng học");

            table.clearSelection();

            selectedRoom = null;

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());

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
                null,
                branchService.findAll()
        );

        dialog.setVisible(true);

        if (dialog.isSaved()) {

            try {

                RoomFormDialog.RoomFormData data = dialog.getResult();
                Room r = formDataToRoom(data, null);
                if (r == null) return;

                roomService.create(r);

                loadTable();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Lỗi thêm phòng học: " + ex.getMessage());

            }

        }

    }

    private void onSave() {

        if (selectedRoom == null) {

            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng học để sửa");

            return;

        }

        RoomFormDialog.RoomFormData existing = roomToFormData(selectedRoom);

        RoomFormDialog dialog = new RoomFormDialog(
                SwingUtilities.getWindowAncestor(this),
                existing,
                branchService.findAll()
        );

        dialog.setVisible(true);

        if (dialog.isSaved()) {

            try {

                RoomFormDialog.RoomFormData data = dialog.getResult();
                Room r = formDataToRoom(data, selectedRoom.getId());
                if (r == null) return;

                roomService.update(r);

                loadTable();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Lỗi cập nhật: " + ex.getMessage());

            }

        }

    }

    private void onDelete() {

        if (selectedRoom == null) return;

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Xóa phòng học " + selectedRoom.getRoomName() + " ?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (ok == JOptionPane.YES_OPTION) {

            try {

                roomService.delete(selectedRoom.getId());

                loadTable();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Lỗi xóa: " + ex.getMessage());

            }

        }

    }

    private RoomFormDialog.RoomFormData roomToFormData(Room r) {
        RoomFormDialog.RoomFormData data = new RoomFormDialog.RoomFormData();
        data.setBranchId(r.getBranch() != null && r.getBranch().getId() != null ? r.getBranch().getId().toString() : "");
        data.setName(r.getRoomName());
        data.setCapacity(String.valueOf(r.getCapacity()));
        data.setLocation(r.getLocation() != null ? r.getLocation() : "");
        data.setStatus(r.getStatus());
        return data;
    }

    private Room formDataToRoom(RoomFormDialog.RoomFormData data, Long keepId) {
        String name = data.getName() != null ? data.getName().trim() : "";
        String capStr = data.getCapacity() != null ? data.getCapacity().trim() : "";

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên phòng học không được để trống.", "Lỗi", JOptionPane.WARNING_MESSAGE);
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

        if (data.getBranchId() != null && !data.getBranchId().trim().isEmpty()) {
            try {
                r.setBranch(branchService.findById(Long.parseLong(data.getBranchId().trim())));
            } catch (Exception ignored) { }
        }

        r.setRoomName(name);
        r.setCapacity(capacity);
        r.setLocation(data.getLocation());
        r.setStatus(data.getStatus());
        return r;
    }

    private static class RoomTableModel extends AbstractTableModel {

        private final String[] columns = {
                "Tên phòng",
                "Chi nhánh",
                "Sức chứa",
                "Vị trí",
                "Trạng thái"
        };

        private List<Room> data = new ArrayList<>();

        void setData(List<Room> data) {

            this.data = data != null ? data : new ArrayList<>();

            fireTableDataChanged();

        }

        Room getRoomAt(int r) {

            return (r >= 0 && r < data.size()) ? data.get(r) : null;

        }

        @Override
        public int getRowCount() {

            return data.size();

        }

        @Override
        public int getColumnCount() {

            return columns.length;

        }

        @Override
        public String getColumnName(int c) {

            return columns[c];

        }

        @Override
        public Object getValueAt(int r, int c) {

            Room room = data.get(r);

            return switch (c) {

                case 0 -> room.getRoomName();

                case 1 -> room.getBranch() != null ? room.getBranch().getBranchName() : "";

                case 2 -> room.getCapacity();

                case 3 -> room.getLocation() != null ? room.getLocation() : "";

                case 4 -> room.getStatus() != null ? room.getStatus().name() : "";

                default -> "";

            };

        }

    }

}
