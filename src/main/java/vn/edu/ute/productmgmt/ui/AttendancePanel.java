package vn.edu.ute.productmgmt.ui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel danh sách điểm danh (Attendance) dùng mock data.
 */
public class AttendancePanel extends JPanel {

    private final AttendanceTableModel tableModel = new AttendanceTableModel();
    private final JLabel bottomInfo = new JLabel("Total attendance records: 0");

    public AttendancePanel() {
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
        left.add(new JLabel("Status:"));
        left.add(new JComboBox<>(new String[]{"All", "Present", "Absent", "Late"}));
        top.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(new JButton("Mark Attendance"));
        right.add(new JButton("Refresh"));
        top.add(right, BorderLayout.EAST);

        JTable table = new JTable(tableModel);
        UI.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);

        tableModel.setOnDataChangedListener(size ->
                bottomInfo.setText("Total attendance records: " + size)
        );

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomInfo, BorderLayout.SOUTH);
    }

    private void loadMockData() {
        List<AttendanceRow> rows = List.of(
                new AttendanceRow(1, LocalDate.now(), "IELTS Foundation - A", "Nguyen Van A", "Present"),
                new AttendanceRow(2, LocalDate.now(), "TOEIC 500+ - B", "Tran Thi B", "Late"),
                new AttendanceRow(3, LocalDate.now().minusDays(1), "Communication Evening", "Le Van C", "Absent")
        );
        tableModel.setData(rows);
    }

    // ==== Kiểu dữ liệu & TableModel nội bộ ====

    private static class AttendanceRow {
        final int id;
        final LocalDate date;
        final String className;
        final String studentName;
        final String status;

        AttendanceRow(int id, LocalDate date, String className, String studentName, String status) {
            this.id = id;
            this.date = date;
            this.className = className;
            this.studentName = studentName;
            this.status = status;
        }
    }

    private interface IntConsumer {
        void accept(int value);
    }

    private static class AttendanceTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Date", "Class", "Student", "Status"};
        private List<AttendanceRow> data = List.of();
        private IntConsumer onDataChanged;

        void setData(List<AttendanceRow> data) {
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
            AttendanceRow r = data.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return r.id;
                case 1:
                    return r.date;
                case 2:
                    return r.className;
                case 3:
                    return r.studentName;
                case 4:
                    return r.status;
                default:
                    return "";
            }
        }
    }
}

