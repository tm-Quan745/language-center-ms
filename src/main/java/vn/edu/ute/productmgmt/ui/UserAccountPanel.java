package vn.edu.ute.productmgmt.ui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel danh sách tài khoản người dùng (User accounts) dùng mock data.
 */
public class UserAccountPanel extends JPanel {

    private final UserAccountTableModel tableModel = new UserAccountTableModel();
    private final JLabel bottomInfo = new JLabel("Total user accounts: 0");

    public UserAccountPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        loadMockData();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout(8, 8));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(new JLabel("Role:"));
        left.add(new JComboBox<>(new String[]{"All", "Admin", "Teacher", "Student"}));
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
                bottomInfo.setText("Total user accounts: " + size)
        );

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomInfo, BorderLayout.SOUTH);
    }

    private void loadMockData() {
        List<UserAccountRow> rows = List.of(
            new UserAccountRow(1, "admin", "Admin", "Staff", "Admin A"),
            new UserAccountRow(2, "teacher.an", "Teacher", "Teacher", "Nguyen Van A"),
            new UserAccountRow(3, "student.001", "Student", "Student", "Nguyen Van A")
        );
        tableModel.setData(rows);
    }

    // ==== Kiểu dữ liệu & TableModel nội bộ ====

    private static class UserAccountRow {
        final int id;
        final String username;
        final String role;
        final String relatedType;
        final String relatedName;

        UserAccountRow(int id, String username, String role, String relatedType, String relatedName) {
            this.id = id;
            this.username = username;
            this.role = role;
            this.relatedType = relatedType;
            this.relatedName = relatedName;
        }
    }

    private interface IntConsumer {
        void accept(int value);
    }

    private static class UserAccountTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Username", "Role", "Related type", "Related name"};
        private List<UserAccountRow> data = List.of();
        private IntConsumer onDataChanged;

        void setData(List<UserAccountRow> data) {
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
            UserAccountRow r = data.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return r.id;
                case 1:
                    return r.username;
                case 2:
                    return r.role;
                case 3:
                    return r.relatedType;
                case 4:
                    return r.relatedName;
                default:
                    return "";
            }
        }
    }
}

