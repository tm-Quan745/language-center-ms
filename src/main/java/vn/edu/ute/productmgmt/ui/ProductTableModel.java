package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Product;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ProductTableModel extends AbstractTableModel {
    private final String[] columns = {"ID", "Name", "Price", "Quantity", "Category"};
    private List<Product> data = new ArrayList<>();

    public void setData(List<Product> data) {
        this.data = data;
        fireTableDataChanged();
    }

    public Product getAt(int row) {
        if (row < 0 || row >= data.size()) return null;
        return data.get(row);
    }

    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return columns[column]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Product p = data.get(rowIndex);
        switch (columnIndex) {
            case 0: return p.getId();
            case 1: return p.getName();
            case 2: return p.getPrice();
            case 3: return p.getQuantity();
            case 4: return p.getCategory() != null ? p.getCategory().getName() : "";
            default: return "";
        }
    }
}
