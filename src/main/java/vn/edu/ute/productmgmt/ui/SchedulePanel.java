package vn.edu.ute.productmgmt.ui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Panel danh sách lịch học (Schedule) dùng mock data.
 */
public class SchedulePanel extends JPanel {

    private final ScheduleTableModel tableModel = new ScheduleTableModel();
    private final JLabel bottomInfo = new JLabel("Total sessions: 0");

    public SchedulePanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        loadMockData();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout(8, 8));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(new JLabel("Class:"));
        left.add(new JComboBox<>(new String[]{"All", "IELTS Foundation - A", "TOEIC 500+ - B"}));
        left.add(new JLabel("Teacher:"));
        left.add(new JComboBox<>(new String[]{"All", "Thay An", "Co Binh"}));
        top.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(new JButton("Add"));
        right.add(new JButton("Edit"));
        right.add(new JButton("Delete"));
        right.add(new JButton("Refresh"));
        top.add(right, BorderLayout.EAST);

        JTable table = new JTable(tableModel);
        UI.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);

        tableModel.setOnDataChangedListener(size ->
                bottomInfo.setText("Total sessions: " + size)
        );

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomInfo, BorderLayout.SOUTH);
    }

    private void loadMockData() {
        List<ScheduleRow> rows = List.of(
                new ScheduleRow(1, LocalDate.now(), LocalTime.of(18, 0), LocalTime.of(20, 0),
                        "IELTS Foundation - A", "Room 101", "Thay An"),
                new ScheduleRow(2, LocalDate.now().plusDays(1), LocalTime.of(18, 0), LocalTime.of(20, 0),
                        "TOEIC 500+ - B", "Room 202", "Co Binh"),
                new ScheduleRow(3, LocalDate.now().plusDays(2), LocalTime.of(19, 0), LocalTime.of(21, 0),
                        "Communication Evening", "Room 303", "Thay Cuong")
        );
        tableModel.setData(rows);
    }

    // ==== Kiểu dữ liệu & TableModel nội bộ ====

    private static class ScheduleRow {
        final int id;
        final LocalDate date;
        final LocalTime startTime;
        final LocalTime endTime;
        final String className;
        final String room;
        final String teacher;

        ScheduleRow(int id, LocalDate date, LocalTime startTime, LocalTime endTime,
                    String className, String room, String teacher) {
            this.id = id;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.className = className;
            this.room = room;
            this.teacher = teacher;
        }
    }

    private interface IntConsumer {
        void accept(int value);
    }

    private static class ScheduleTableModel extends AbstractTableModel {
        private final String[] columns = {
                "ID", "Date", "Start time", "End time", "Class", "Room", "Teacher"
        };
        private List<ScheduleRow> data = List.of();
        private IntConsumer onDataChanged;

        void setData(List<ScheduleRow> data) {
            this.data = data;
            fireTableDataChanged();
            if (onDataChanged != null) {
                onDataChanged.accept(data.size());
            }
        }

        void setOnDataChangedListener(IntConsumer listener) {
            this.onDataChanged = listener;
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
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ScheduleRow r = data.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return r.id;
                case 1:
                    return r.date;
                case 2:
                    return r.startTime;
                case 3:
                    return r.endTime;
                case 4:
                    return r.className;
                case 5:
                    return r.room;
                case 6:
                    return r.teacher;
                default:
                    return "";
            }
        }
    }
}

