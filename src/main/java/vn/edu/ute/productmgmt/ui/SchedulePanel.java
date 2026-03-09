package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Schedule;
import vn.edu.ute.productmgmt.service.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SchedulePanel extends JPanel {

    private final ScheduleService scheduleService;
    private final ClassService classService;
    private final RoomService roomService;

    private final JTextField txtSearch = new JTextField(20);
    private final JLabel lblInfo = new JLabel(" ");

    private final ScheduleTableModel tableModel = new ScheduleTableModel();
    private final JTable table = new JTable(tableModel);
    private Schedule selectedSchedule;

    public SchedulePanel(ScheduleService scheduleService, ClassService classService, RoomService roomService) {
        this.scheduleService = scheduleService;
        this.classService = classService;
        this.roomService = roomService;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        buildUI();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            onTableSelection();
        });

        loadTableAll();
    }

    private void buildUI() {
        add(buildActionBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(lblInfo, BorderLayout.SOUTH);
    }

    private JComponent buildActionBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 4));

        // Bên trái: Tìm kiếm
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        left.add(new JLabel("Tìm lịch (Tên lớp):"));
        left.add(txtSearch);
        JButton btnSearch = new JButton("Tìm");
        btnSearch.addActionListener(e -> onSearch());
        left.add(btnSearch);

        // Bên phải: Các thao tác
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton btnAdd = new JButton("Thêm lịch");
        JButton btnEdit = new JButton("Sửa");
        JButton btnDelete = new JButton("Xóa");
        JButton btnRefresh = new JButton("Tải lại");

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> loadTableAll());

        right.add(btnAdd);
        right.add(btnEdit);
        right.add(btnDelete);
        right.add(btnRefresh);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void loadTableAll() {
        try {
            List<Schedule> list = scheduleService.findAll();
            tableModel.setData(list);
            lblInfo.setText("Tổng số: " + list.size() + " mục lịch học.");
            table.clearSelection();
            selectedSchedule = null;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());
        }
    }

    private void onSearch() {
        String kw = txtSearch.getText().trim().toLowerCase();
        List<Schedule> all = scheduleService.findAll();
        List<Schedule> filtered = all.stream()
                .filter(s -> s.getTeachingClass().getClassName().toLowerCase().contains(kw))
                .toList();
        tableModel.setData(filtered);
        lblInfo.setText("Tìm thấy: " + filtered.size() + " kết quả.");
    }

    private void onTableSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            selectedSchedule = null;
            return;
        }
        selectedSchedule = tableModel.getScheduleAt(row);
    }

    private void onAdd() {
        ScheduleFormDialog dialog = new ScheduleFormDialog(
                SwingUtilities.getWindowAncestor(this), null,
                classService, roomService
        );
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            try {
                scheduleService.createSchedule(dialog.getResult());
                loadTableAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi thêm mới: " + ex.getMessage());
            }
        }
    }

    private void onEdit() {
        if (selectedSchedule == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lịch để sửa.");
            return;
        }
        ScheduleFormDialog dialog = new ScheduleFormDialog(
                SwingUtilities.getWindowAncestor(this), selectedSchedule,
                classService, roomService
        );
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            try {
                scheduleService.updateSchedule(dialog.getResult());
                loadTableAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi cập nhật: " + ex.getMessage());
            }
        }
    }

    private void onDelete() {
        if (selectedSchedule == null) return;
        int ok = JOptionPane.showConfirmDialog(this, "Xóa lịch học của lớp " +
                selectedSchedule.getTeachingClass().getClassName() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            try {
                scheduleService.deleteSchedule(selectedSchedule.getId());
                loadTableAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + ex.getMessage());
            }
        }
    }

    // === Table Model đồng bộ với ClassTableModel ===
    private static class ScheduleTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Lớp học", "Phòng", "Ngày học", "Thời gian"};
        private List<Schedule> data = new ArrayList<>();

        void setData(List<Schedule> data) { this.data = data; fireTableDataChanged(); }
        Schedule getScheduleAt(int r) { return data.get(r); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int c) { return columns[c]; }
        @Override public Object getValueAt(int r, int c) {
            Schedule s = data.get(r);
            return switch (c) {
                case 0 -> s.getId();
                case 1 -> s.getTeachingClass() != null ? s.getTeachingClass().getClassName() : "";
                case 2 -> s.getRoom() != null ? s.getRoom().getRoomName() : "N/A";
                case 3 -> s.getStudyDate();
                case 4 -> s.getStartTime() + " - " + s.getEndTime();
                default -> "";
            };
        }
    }
}