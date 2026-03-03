package vn.edu.ute.productmgmt.ui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel danh sách nhân viên (Staff) dùng mock data.
 */
public class StaffPanel extends JPanel {

    private final StaffTableModel tableModel = new StaffTableModel();
    private final JLabel bottomInfo = new JLabel("Total staff: 0");

    public StaffPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        loadMockData();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout(8, 8));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(new JLabel("Role:"));
        left.add(new JComboBox<>(new String[]{"All", "Admin", "Consultant", "Accountant"}));
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
                bottomInfo.setText("Total staff: " + size)
        );

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomInfo, BorderLayout.SOUTH);
    }

    private void loadMockData() {
        List<StaffRow> rows = List.of(
                new StaffRow(1, "Admin A", "Admin", "0907 111 111", "admin.a@example.com"),
                new StaffRow(2, "Nguyen Tu Van", "Consultant", "0908 222 222", "tv@example.com"),
                new StaffRow(3, "Le Ke Toan", "Accountant", "0909 333 333", "kt@example.com")
        );
        tableModel.setData(rows);
    }

    // ==== Kiểu dữ liệu & TableModel nội bộ ====

    private static class StaffRow {
        final int id;
        final String name;
        final String role;
        final String phone;
        final String email;

        StaffRow(int id, String name, String role, String phone, String email) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.phone = phone;
            this.email = email;
        }
    }

    private interface IntConsumer {
        void accept(int value);
    }

    private static class StaffTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Full name", "Role", "Phone", "Email"};
        private List<StaffRow> data = List.of();
        private IntConsumer onDataChanged;

        void setData(List<StaffRow> data) {
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
            StaffRow r = data.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return r.id;
                case 1:
                    return r.name;
                case 2:
                    return r.role;
                case 3:
                    return r.phone;
                case 4:
                    return r.email;
                default:
                    return "";
            }
        }
    }
}

