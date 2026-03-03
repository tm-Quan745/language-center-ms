package vn.edu.ute.productmgmt.ui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel danh sách kết quả học tập (Results) dùng mock data.
 */
public class ResultPanel extends JPanel {

    private final ResultTableModel tableModel = new ResultTableModel();
    private final JLabel bottomInfo = new JLabel("Total results: 0");

    public ResultPanel() {
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
        top.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(new JButton("Refresh"));
        top.add(right, BorderLayout.EAST);

        JTable table = new JTable(tableModel);
        UI.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);

        tableModel.setOnDataChangedListener(size ->
                bottomInfo.setText("Total results: " + size)
        );

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomInfo, BorderLayout.SOUTH);
    }

    private void loadMockData() {
        List<ResultRow> rows = List.of(
                new ResultRow(1, "Nguyen Van A", "IELTS Foundation - A", 7.0, "Pass"),
                new ResultRow(2, "Tran Thi B", "TOEIC 500+ - B", 650, "Pass"),
                new ResultRow(3, "Le Van C", "Communication Evening", 5.0, "Fail")
        );
        tableModel.setData(rows);
    }

    // ==== Kiểu dữ liệu & TableModel nội bộ ====

    private static class ResultRow {
        final int id;
        final String studentName;
        final String className;
        final double score;
        final String grade;

        ResultRow(int id, String studentName, String className, double score, String grade) {
            this.id = id;
            this.studentName = studentName;
            this.className = className;
            this.score = score;
            this.grade = grade;
        }
    }

    private interface IntConsumer {
        void accept(int value);
    }

    private static class ResultTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Student", "Class", "Score", "Grade"};
        private List<ResultRow> data = List.of();
        private IntConsumer onDataChanged;

        void setData(List<ResultRow> data) {
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
            ResultRow r = data.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return r.id;
                case 1:
                    return r.studentName;
                case 2:
                    return r.className;
                case 3:
                    return r.score;
                case 4:
                    return r.grade;
                default:
                    return "";
            }
        }
    }
}

