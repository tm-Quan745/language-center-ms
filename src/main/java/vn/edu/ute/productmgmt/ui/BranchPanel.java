package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Branch;
import vn.edu.ute.productmgmt.service.BranchService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BranchPanel extends JPanel {

    private final BranchService branchService;

    private final JLabel lblInfo = new JLabel(" ");
    private final BranchTableModel tableModel = new BranchTableModel();
    private final JTable table = new JTable(tableModel);
    private Branch selectedBranch;

    public BranchPanel(BranchService branchService) {
        this.branchService = branchService;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            onTableSelection();
        });
        loadTable();
    }

    private void buildUI() {
        add(buildActionBar(), BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
        add(lblInfo, BorderLayout.SOUTH);
    }

    private JComponent buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton btnAdd = new JButton("Thêm mới");
        JButton btnSave = new JButton("Chỉnh sửa");
        JButton btnDelete = new JButton("Xóa");
        JButton btnRefresh = new JButton("Tải lại");
        btnAdd.addActionListener(e -> onAdd());
        btnSave.addActionListener(e -> onSave());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> loadTable());
        bar.add(btnAdd);
        bar.add(btnSave);
        bar.add(btnDelete);
        bar.add(btnRefresh);
        return bar;
    }

    private JComponent buildTableArea() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        UI.stylePanelBorder(wrapper, "Danh sách chi nhánh");
        JScrollPane scroll = new JScrollPane(table);
        UI.styleTable(table);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private void loadTable() {
        try {
            List<Branch> list = branchService.findAll();
            tableModel.setData(list);
            lblInfo.setText("Tổng số: " + list.size() + " chi nhánh.");
            clearSelection();
        } catch (Exception ex) {
            lblInfo.setText("Lỗi: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Không tải được danh sách: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onTableSelection() {
        int row = table.getSelectedRow();
        selectedBranch = row < 0 ? null : tableModel.getBranchAt(row);
    }

    private void onAdd() {
        BranchFormDialog dialog = new BranchFormDialog(SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (!dialog.isSaved()) return;
        BranchFormDialog.BranchFormData data = dialog.getResult();
        Branch b = formDataToBranch(data, null);
        if (b == null) return;
        try {
            branchService.create(b);
            JOptionPane.showMessageDialog(this, "Đã thêm chi nhánh.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSave() {
        if (selectedBranch == null) {
            JOptionPane.showMessageDialog(this, "Chọn một chi nhánh để sửa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BranchFormDialog.BranchFormData existing = branchToFormData(selectedBranch);
        BranchFormDialog dialog = new BranchFormDialog(SwingUtilities.getWindowAncestor(this), existing);
        dialog.setVisible(true);
        if (!dialog.isSaved()) return;
        BranchFormDialog.BranchFormData data = dialog.getResult();
        Branch b = formDataToBranch(data, selectedBranch.getId());
        if (b == null) return;
        try {
            branchService.update(b);
            JOptionPane.showMessageDialog(this, "Đã cập nhật chi nhánh.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            clearSelection();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        if (selectedBranch == null) {
            JOptionPane.showMessageDialog(this, "Chọn một chi nhánh để xóa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa chi nhánh này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            branchService.delete(selectedBranch.getId());
            JOptionPane.showMessageDialog(this, "Đã xóa chi nhánh.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            clearSelection();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BranchFormDialog.BranchFormData branchToFormData(Branch b) {
        BranchFormDialog.BranchFormData data = new BranchFormDialog.BranchFormData();
        data.setName(b.getBranchName());
        data.setAddress(b.getAddress() != null ? b.getAddress() : "");
        data.setPhone(b.getPhone() != null ? b.getPhone() : "");
        data.setStatus(b.getStatus());
        return data;
    }

    private Branch formDataToBranch(BranchFormDialog.BranchFormData data, Long keepId) {
        String name = data.getName() != null ? data.getName().trim() : "";
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên chi nhánh không được để trống.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Branch b = new Branch();
        if (keepId != null) b.setId(keepId);
        b.setBranchName(name);
        b.setAddress(data.getAddress() != null && !data.getAddress().isEmpty() ? data.getAddress() : null);
        b.setPhone(data.getPhone() != null && !data.getPhone().isEmpty() ? data.getPhone() : null);
        b.setStatus(data.getStatus() != null ? data.getStatus() : vn.edu.ute.productmgmt.model.enums.ActiveStatus.Active);
        return b;
    }

    private void clearSelection() {
        selectedBranch = null;
        table.clearSelection();
    }

    private static class BranchTableModel extends AbstractTableModel {
        private final String[] columns = {"Tên chi nhánh", "Địa chỉ", "Điện thoại", "Trạng thái"};
        private List<Branch> data = new ArrayList<>();

        void setData(List<Branch> data) {
            this.data = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        Branch getBranchAt(int row) {
            return (row >= 0 && row < data.size()) ? data.get(row) : null;
        }

        @Override
        public int getRowCount() { return data.size(); }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Branch b = data.get(row);
            return switch (col) {
                case 0 -> b.getBranchName();
                case 1 -> b.getAddress() != null ? b.getAddress() : "";
                case 2 -> b.getPhone() != null ? b.getPhone() : "";
                case 3 -> b.getStatus() != null ? b.getStatus().name() : "";
                default -> "";
            };
        }
    }
}
