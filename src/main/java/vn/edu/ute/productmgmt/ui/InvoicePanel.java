package vn.edu.ute.productmgmt.ui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel danh sách hóa đơn (Invoices) dùng mock data.
 */
public class InvoicePanel extends JPanel {

    private final InvoiceTableModel tableModel = new InvoiceTableModel();
    private final JLabel bottomInfo = new JLabel("Total invoices: 0");

    public InvoicePanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        loadMockData();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout(8, 8));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(new JLabel("Status:"));
        left.add(new JComboBox<>(new String[]{"All", "Paid", "Unpaid"}));
        top.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(new JButton("Add Invoice"));
        right.add(new JButton("Mark Paid"));
        right.add(new JButton("Refresh"));
        top.add(right, BorderLayout.EAST);

        JTable table = new JTable(tableModel);
        UI.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);

        tableModel.setOnDataChangedListener(size ->
                bottomInfo.setText("Total invoices: " + size)
        );

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomInfo, BorderLayout.SOUTH);
    }

    private void loadMockData() {
        List<InvoiceRow> rows = List.of(
                new InvoiceRow(1, "Nguyen Van A", 5_000_000L, LocalDate.now().minusDays(3), "Paid"),
                new InvoiceRow(2, "Tran Thi B", 4_500_000L, LocalDate.now().minusDays(1), "Unpaid"),
                new InvoiceRow(3, "Le Van C", 3_000_000L, LocalDate.now().minusDays(10), "Paid")
        );
        tableModel.setData(rows);
    }

    // ==== Kiểu dữ liệu & TableModel nội bộ ====

    private static class InvoiceRow {
        final int id;
        final String studentName;
        final long totalAmount;
        final LocalDate issueDate;
        final String status;

        InvoiceRow(int id, String studentName, long totalAmount, LocalDate issueDate, String status) {
            this.id = id;
            this.studentName = studentName;
            this.totalAmount = totalAmount;
            this.issueDate = issueDate;
            this.status = status;
        }
    }

    private interface IntConsumer {
        void accept(int value);
    }

    private static class InvoiceTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Student", "Total", "Issue date", "Status"};
        private List<InvoiceRow> data = List.of();
        private IntConsumer onDataChanged;

        void setData(List<InvoiceRow> data) {
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
            InvoiceRow r = data.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return r.id;
                case 1:
                    return r.studentName;
                case 2:
                    return r.totalAmount;
                case 3:
                    return r.issueDate;
                case 4:
                    return r.status;
                default:
                    return "";
            }
        }
    }
}

