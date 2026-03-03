package vn.edu.ute.productmgmt.ui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel danh sách đăng ký học (Enrollments) dùng mock data.
 */
public class EnrollmentPanel extends JPanel {

    private final EnrollmentTableModel tableModel = new EnrollmentTableModel();
    private final JLabel bottomInfo = new JLabel("Total enrollments: 0");

    public EnrollmentPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        loadMockData();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout(8, 8));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(new JLabel("Course:"));
        left.add(new JComboBox<>(new String[]{"All", "IELTS Foundation", "TOEIC 500+", "Communication"}));
        left.add(new JLabel("Status:"));
        left.add(new JComboBox<>(new String[]{"All", "Active", "Cancelled"}));
        top.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(new JButton("New Enrollment"));
        right.add(new JButton("Cancel"));
        right.add(new JButton("Refresh"));
        top.add(right, BorderLayout.EAST);

        JTable table = new JTable(tableModel);
        UI.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);

        tableModel.setOnDataChangedListener(size ->
                bottomInfo.setText("Total enrollments: " + size)
        );

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomInfo, BorderLayout.SOUTH);
    }

    private void loadMockData() {
        List<EnrollmentRow> rows = List.of(
                new EnrollmentRow(1, "Nguyen Van A", "IELTS Foundation - A", LocalDate.now().minusDays(10), "Active", "Pass"),
                new EnrollmentRow(2, "Tran Thi B", "TOEIC 500+ - B", LocalDate.now().minusDays(5), "Active", "Pending"),
                new EnrollmentRow(3, "Le Van C", "Communication Evening", LocalDate.now().minusDays(20), "Cancelled", "Fail")
        );
        tableModel.setData(rows);
    }

    // ==== Kiểu dữ liệu & TableModel nội bộ ====

    private static class EnrollmentRow {
        final int id;
        final String studentName;
        final String className;
        final LocalDate enrollDate;
        final String status;
        final String result;

        EnrollmentRow(int id, String studentName, String className,
                      LocalDate enrollDate, String status, String result) {
            this.id = id;
            this.studentName = studentName;
            this.className = className;
            this.enrollDate = enrollDate;
            this.status = status;
            this.result = result;
        }
    }

    private interface IntConsumer {
        void accept(int value);
    }

    private static class EnrollmentTableModel extends AbstractTableModel {
        private final String[] columns = {
                "ID", "Student", "Class", "Enroll date", "Status", "Result"
        };
        private List<EnrollmentRow> data = List.of();
        private IntConsumer onDataChanged;

        void setData(List<EnrollmentRow> data) {
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
            EnrollmentRow r = data.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return r.id;
                case 1:
                    return r.studentName;
                case 2:
                    return r.className;
                case 3:
                    return r.enrollDate;
                case 4:
                    return r.status;
                case 5:
                    return r.result;
                default:
                    return "";
            }
        }
    }
}

