package vn.edu.ute.productmgmt.ui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel danh sách phòng học (Rooms) dùng mock data.
 */
public class RoomPanel extends JPanel {

    private final RoomTableModel tableModel = new RoomTableModel();
    private final JLabel bottomInfo = new JLabel("Total rooms: 0");

    public RoomPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        loadMockData();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout(8, 8));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(new JLabel("Status:"));
        left.add(new JComboBox<>(new String[]{"All", "Available", "Maintenance"}));
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
                bottomInfo.setText("Total rooms: " + size)
        );

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomInfo, BorderLayout.SOUTH);
    }

    private void loadMockData() {
        List<RoomRow> rows = List.of(
                new RoomRow(1, "Room 101", 25, "Building A", "Available"),
                new RoomRow(2, "Room 202", 30, "Building B", "Available"),
                new RoomRow(3, "Room 303", 20, "Building A", "Maintenance")
        );
        tableModel.setData(rows);
    }

    // ==== Kiểu dữ liệu & TableModel nội bộ ====

    private static class RoomRow {
        final int id;
        final String name;
        final int capacity;
        final String location;
        final String status;

        RoomRow(int id, String name, int capacity, String location, String status) {
            this.id = id;
            this.name = name;
            this.capacity = capacity;
            this.location = location;
            this.status = status;
        }
    }

    private interface IntConsumer {
        void accept(int value);
    }

    private static class RoomTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Room name", "Capacity", "Location", "Status"};
        private List<RoomRow> data = List.of();
        private IntConsumer onDataChanged;

        void setData(List<RoomRow> data) {
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
            RoomRow r = data.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return r.id;
                case 1:
                    return r.name;
                case 2:
                    return r.capacity;
                case 3:
                    return r.location;
                case 4:
                    return r.status;
                default:
                    return "";
            }
        }
    }
}

