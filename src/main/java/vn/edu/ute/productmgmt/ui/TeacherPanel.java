package vn.edu.ute.productmgmt.ui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel danh sách giáo viên (Teachers) dùng mock data.
 */
public class TeacherPanel extends JPanel {

    private final TeacherTableModel tableModel = new TeacherTableModel();
    private final JLabel bottomInfo = new JLabel("Total teachers: 0");

    public TeacherPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        loadMockData();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout(8, 8));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(new JLabel("Search:"));
        left.add(new JTextField(20));
        left.add(new JLabel("Specialty:"));
        left.add(new JComboBox<>(new String[]{"All", "IELTS", "TOEIC", "Communication"}));
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
                bottomInfo.setText("Total teachers: " + size)
        );

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomInfo, BorderLayout.SOUTH);
    }

    private void loadMockData() {
        List<TeacherRow> teachers = List.of(
                new TeacherRow(1, "Nguyen Van A", "0904 111 111", "teacher.a@example.com", "IELTS", "Active"),
                new TeacherRow(2, "Tran Thi B", "0905 222 222", "teacher.b@example.com", "TOEIC", "Active"),
                new TeacherRow(3, "Le Van C", "0906 333 333", "teacher.c@example.com", "Communication", "Inactive")
        );
        tableModel.setData(teachers);
    }

    // ==== Kiểu dữ liệu & TableModel nội bộ ====

    private static class TeacherRow {
        final int id;
        final String name;
        final String phone;
        final String email;
        final String specialty;
        final String status;

        TeacherRow(int id, String name, String phone, String email, String specialty, String status) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.email = email;
            this.specialty = specialty;
            this.status = status;
        }
    }

    private interface IntConsumer {
        void accept(int value);
    }

    private static class TeacherTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Full name", "Phone", "Email", "Specialty", "Status"};
        private List<TeacherRow> data = List.of();
        private IntConsumer onDataChanged;

        void setData(List<TeacherRow> data) {
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
            TeacherRow t = data.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return t.id;
                case 1:
                    return t.name;
                case 2:
                    return t.phone;
                case 3:
                    return t.email;
                case 4:
                    return t.specialty;
                case 5:
                    return t.status;
                default:
                    return "";
            }
        }
    }
}

