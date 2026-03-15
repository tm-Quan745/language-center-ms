package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Schedule;
import vn.edu.ute.productmgmt.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel quản lý lịch học, chuẩn hóa theo NotificationPanel
 */
public class SchedulePanel extends JPanel {

    private final ScheduleService scheduleService;
    private final ClassService classService;
    private final RoomService roomService;

    private final JTextField txtSearch = new JTextField(18);
    private final JLabel lblInfo = new JLabel(" ");

    private final ScheduleTableModel tableModel = new ScheduleTableModel();
    private final JTable table = new JTable(tableModel);
    private Schedule selectedSchedule;

    public SchedulePanel(ScheduleService scheduleService, ClassService classService, RoomService roomService) {
        this.scheduleService = scheduleService;
        this.classService = classService;
        this.roomService = roomService;

        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        buildUI();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            onTableSelection();
        });

        loadTableAll();
    }

    private void buildUI() {

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm lịch học...");
        txtSearch.setPreferredSize(new Dimension(300, 40));
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new JLabel(" 🔍 "));
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(100, 40));
        btnSearch.putClientProperty(FlatClientProperties.STYLE, "background: #0d6efd; foreground: #ffffff; arc: 12");
        btnSearch.addActionListener(e -> onSearch());

        left.add(txtSearch);
        left.add(Box.createHorizontalStrut(10));
        left.add(btnSearch);

        headerPanel.add(left, BorderLayout.WEST);
        headerPanel.add(buildActionBar(), BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

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
        JButton btnRefresh = new JButton("🔄 Tải lại");

        btnRefresh.setPreferredSize(new Dimension(100, 38));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> loadTableAll());

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

    private void loadTableAll() {

        try {

            List<Schedule> list = scheduleService.findAll();

            tableModel.setData(list);

            lblInfo.setText("Tổng số: " + list.size() + " lịch học");

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

        lblInfo.setText("Tìm thấy: " + filtered.size() + " kết quả");

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

                JOptionPane.showMessageDialog(this, "Lỗi thêm lịch học: " + ex.getMessage());

            }

        }

    }

    private void onEdit() {

        if (selectedSchedule == null) {

            JOptionPane.showMessageDialog(this, "Vui lòng chọn lịch học để sửa");

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

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Xóa lịch học?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (ok == JOptionPane.YES_OPTION) {

            try {

                scheduleService.deleteSchedule(selectedSchedule.getId());

                loadTableAll();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Lỗi xóa: " + ex.getMessage());

            }

        }

    }

    private static class ScheduleTableModel extends AbstractTableModel {

        private final String[] columns = {
                "Mã lịch",
                "Lớp học",
                "Phòng",
                "Ngày học",
                "Thời gian"
        };

        private List<Schedule> data = new ArrayList<>();

        void setData(List<Schedule> data) {

            this.data = data != null ? data : new ArrayList<>();

            fireTableDataChanged();

        }

        Schedule getScheduleAt(int r) {

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

            Schedule s = data.get(r);

            return switch (c) {

                case 0 -> s.getId();

                case 1 -> s.getTeachingClass() != null ? s.getTeachingClass().getClassName() : "";

                case 2 -> s.getRoom() != null ? s.getRoom().getRoomName() : "";

                case 3 -> s.getStudyDate();

                case 4 -> s.getStartTime() + " - " + s.getEndTime();

                default -> "";

            };

        }

    }

}