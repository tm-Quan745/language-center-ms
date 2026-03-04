package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Staff;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.model.enums.StaffRole;
import vn.edu.ute.productmgmt.service.StaffService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LcmsStaffPanel extends JPanel {

    private final StaffService staffService;

    private final JTextField txtSearch = new JTextField(20);
    private final JLabel lblInfo = new JLabel(" ");
    private final StaffTableModel tableModel = new StaffTableModel();
    private final JTable table = new JTable(tableModel);

    private List<Staff> staffData = new ArrayList<>();
    private Staff selectedStaff;

    public LcmsStaffPanel(StaffService staffService) {
        this.staffService = staffService;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildActionBar(), BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
        add(lblInfo, BorderLayout.SOUTH);

        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onTableSelection();
            }
        });

        loadTableAll();
    }

    private JComponent buildActionBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 8));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        left.add(new JLabel("Tìm kiếm:"));
        left.add(txtSearch);
        JButton btnSearch = new JButton("Tìm");
        btnSearch.addActionListener(e -> onSearch());
        left.add(btnSearch);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JButton btnAdd = new JButton("Thêm");
        JButton btnEdit = new JButton("Sửa");
        JButton btnDelete = new JButton("Xóa");
        JButton btnRefresh = new JButton("Làm mới");

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadTableAll();
        });

        right.add(btnAdd);
        right.add(btnEdit);
        right.add(btnDelete);
        right.add(btnRefresh);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private JComponent buildTableArea() {
        JScrollPane scroll = new JScrollPane(table);
        return scroll;
    }

    private void loadTableAll() {
        if (staffService == null) {
            staffData = new ArrayList<>();
            tableModel.setData(staffData);
            lblInfo.setText("Không có dịch vụ nhân viên.");
            return;
        }
        try {
            staffData = staffService.getAll();
            tableModel.setData(staffData);
            lblInfo.setText("Tổng nhân viên: " + staffData.size());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không tải được danh sách nhân viên: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSearch() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadTableAll();
            return;
        }
        List<Staff> filtered = new ArrayList<>();
        for (Staff s : staffData) {
            String name = s.getFullName() != null ? s.getFullName().toLowerCase() : "";
            String phone = s.getPhone() != null ? s.getPhone().toLowerCase() : "";
            if (name.contains(keyword) || phone.contains(keyword)) {
                filtered.add(s);
            }
        }
        tableModel.setData(filtered);
        lblInfo.setText("Tìm thấy " + filtered.size() + " nhân viên.");
    }

    private void onTableSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            selectedStaff = null;
            return;
        }
        selectedStaff = tableModel.getStaffAt(row);
    }

    private void onAdd() {
        StaffFormDialog.StaffFormData existing = null;
        StaffFormDialog dialog = new StaffFormDialog(
                SwingUtilities.getWindowAncestor(this),
                existing
        );
        dialog.setVisible(true);
        if (!dialog.isSaved()) return;

        StaffFormDialog.StaffFormData data = dialog.getResult();
        Staff staff = formDataToStaff(data, null);
        if (staff == null) return;

        try {
            staffService.create(staff);
            JOptionPane.showMessageDialog(this, "Đã thêm nhân viên.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            txtSearch.setText("");
            loadTableAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thêm được nhân viên: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEdit() {
        if (selectedStaff == null) {
            JOptionPane.showMessageDialog(this,
                    "Chọn một nhân viên để sửa.",
                    "Chưa chọn",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        StaffFormDialog.StaffFormData existing = staffToFormData(selectedStaff);
        StaffFormDialog dialog = new StaffFormDialog(
                SwingUtilities.getWindowAncestor(this),
                existing
        );
        dialog.setVisible(true);
        if (!dialog.isSaved()) return;

        StaffFormDialog.StaffFormData data = dialog.getResult();
        Staff updated = formDataToStaff(data, selectedStaff.getId());
        if (updated == null) return;

        try {
            staffService.update(updated);
            JOptionPane.showMessageDialog(this, "Đã cập nhật nhân viên.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            txtSearch.setText("");
            loadTableAll();
            selectedStaff = null;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không cập nhật được nhân viên: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        if (selectedStaff == null) {
            JOptionPane.showMessageDialog(this,
                    "Chọn một nhân viên để xóa.",
                    "Chưa chọn",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa nhân viên này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            staffService.delete(selectedStaff.getId());
            JOptionPane.showMessageDialog(this, "Đã xóa nhân viên.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            txtSearch.setText("");
            loadTableAll();
            selectedStaff = null;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không xóa được nhân viên: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private StaffFormDialog.StaffFormData staffToFormData(Staff s) {
        StaffFormDialog.StaffFormData data = new StaffFormDialog.StaffFormData();
        data.setFullName(s.getFullName());
        data.setPhone(s.getPhone());
        data.setEmail(s.getEmail());
        data.setRole(s.getRole() != null ? s.getRole() : StaffRole.Other);
        data.setStatus(s.getStatus() != null ? s.getStatus() : ActiveStatus.Active);
        return data;
    }

    private Staff formDataToStaff(StaffFormDialog.StaffFormData data, Long keepId) {
        if (data == null) return null;
        Staff s = new Staff();
        s.setId(keepId);
        s.setFullName(data.getFullName());
        s.setPhone(data.getPhone());
        s.setEmail(data.getEmail());
        s.setRole(data.getRole() != null ? data.getRole() : StaffRole.Other);
        s.setStatus(data.getStatus() != null ? data.getStatus() : ActiveStatus.Active);
        return s;
    }

    private static class StaffTableModel extends AbstractTableModel {
        private final String[] columns = {
                "ID",
                "Tên nhân viên",
                "Điện thoại",
                "Email",
                "Chức vụ",
                "Trạng thái"
        };
        private List<Staff> data = new ArrayList<>();

        public void setData(List<Staff> list) {
            this.data = list != null ? new ArrayList<>(list) : new ArrayList<>();
            fireTableDataChanged();
        }

        public Staff getStaffAt(int row) {
            if (row < 0 || row >= data.size()) return null;
            return data.get(row);
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
            Staff s = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> s.getId();
                case 1 -> s.getFullName();
                case 2 -> s.getPhone();
                case 3 -> s.getEmail();
                case 4 -> s.getRole() != null ? s.getRole().name() : "";
                case 5 -> s.getStatus() != null ? s.getStatus().name() : "";
                default -> "";
            };
        }
    }
}
