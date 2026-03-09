package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Staff;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.model.enums.StaffRole;
import vn.edu.ute.productmgmt.service.StaffService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LcmsStaffPanel extends JPanel {

    private final StaffService staffService;

    private final JTextField txtSearch = new JTextField();
    private final JLabel lblInfo = new JLabel(" ");
    private final StaffTableModel tableModel = new StaffTableModel();
    private final JTable table = new JTable(tableModel);

    private List<Staff> staffData = new ArrayList<>();
    private Staff selectedStaff;

    public LcmsStaffPanel(StaffService staffService) {
        this.staffService = staffService;
        setLayout(new BorderLayout(20, 20));
        setOpaque(false); // Để lộ nền của Frame chính
        setBorder(new EmptyBorder(0, 0, 0, 0));

        // Build UI
        add(buildActionBar(), BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        // Table settings
        setupTableUI();

        loadTableAll();
    }

    private void setupTableUI() {
        table.setRowHeight(40); // Dòng cao nhìn sang hơn
        table.setShowVerticalLines(false); // Bỏ kẻ dọc
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));

        // Căn giữa ID và Trạng thái
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onTableSelection();
            }
        });
    }

    private JComponent buildActionBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        // Bên trái: Tìm kiếm
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);

        txtSearch.setPreferredSize(new Dimension(300, 40));
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm tên hoặc số điện thoại nhân viên...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new JLabel(" 🔍 "));
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(100, 40));
        btnSearch.putClientProperty(FlatClientProperties.STYLE, "background: #0d6efd; foreground: #ffffff; arc: 12");
        btnSearch.addActionListener(e -> onSearch());

        left.add(txtSearch);
        left.add(Box.createHorizontalStrut(10));
        left.add(btnSearch);

        // Bên phải: Chức năng
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton btnAdd = createActionButton("Thêm mới", "#198754"); // Green
        JButton btnEdit = createActionButton("Chỉnh sửa", "#ffc107"); // Yellow
        JButton btnDelete = createActionButton("Xóa bỏ", "#dc3545"); // Red
        JButton btnRefresh = new JButton("🔄 Làm mới");
        btnRefresh.setPreferredSize(new Dimension(110, 40));

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadTableAll();
        });

        right.add(btnRefresh);
        right.add(btnAdd);
        right.add(btnEdit);
        right.add(btnDelete);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private JButton createActionButton(String text, String colorHex) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(110, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Nếu là nút Sửa (vàng) thì chữ đen, còn lại chữ trắng
        String fg = colorHex.equals("#ffc107") ? "#000000" : "#ffffff";
        btn.putClientProperty(FlatClientProperties.STYLE,
                "background: " + colorHex + "; foreground: " + fg + "; arc: 12; borderWidth: 0; outlineWidth: 0");
        return btn;
    }

    private JComponent buildTableArea() {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scroll.putClientProperty(FlatClientProperties.STYLE, "arc: 20");
        return scroll;
    }

    private JComponent buildStatusBar() {
        JPanel status = new JPanel(new BorderLayout());
        status.setOpaque(false);
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblInfo.setForeground(Color.GRAY);
        status.add(lblInfo, BorderLayout.WEST);
        return status;
    }

    // --- LOGIC (Giữ nguyên logic của bạn nhưng tối ưu thông báo) ---

    private void loadTableAll() {
        if (staffService == null) {
            staffData = new ArrayList<>();
            tableModel.setData(staffData);
            lblInfo.setText("Hệ thống chưa kết nối dữ liệu.");
            return;
        }
        try {
            staffData = staffService.getAll();
            tableModel.setData(staffData);
            lblInfo.setText("Hiển thị " + staffData.size() + " nhân viên trong hệ thống");
        } catch (Exception ex) {
            showError("Không tải được danh sách: " + ex.getMessage());
        }
    }

    private void onSearch() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadTableAll();
            return;
        }
        List<Staff> filtered = staffData.stream()
                .filter(s -> (s.getFullName() != null && s.getFullName().toLowerCase().contains(keyword)) ||
                        (s.getPhone() != null && s.getPhone().contains(keyword)))
                .toList();
        tableModel.setData(filtered);
        lblInfo.setText("Tìm thấy " + filtered.size() + " kết quả cho: '" + keyword + "'");
    }

    private void onTableSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            selectedStaff = null;
            return;
        }
        // Chuyển đổi index row từ View sang Model (quan trọng nếu table có sort)
        int modelRow = table.convertRowIndexToModel(row);
        selectedStaff = tableModel.getStaffAt(modelRow);
    }

    private void onAdd() {
        StaffFormDialog dialog = new StaffFormDialog(SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            try {
                staffService.create(formDataToStaff(dialog.getResult(), null));
                loadTableAll();
                showSuccess("Đã thêm nhân viên mới thành công!");
            } catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private void onEdit() {
        if (selectedStaff == null) {
            showWarning("Vui lòng chọn nhân viên cần chỉnh sửa!");
            return;
        }
        StaffFormDialog dialog = new StaffFormDialog(SwingUtilities.getWindowAncestor(this), staffToFormData(selectedStaff));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            try {
                staffService.update(formDataToStaff(dialog.getResult(), selectedStaff.getId()));
                loadTableAll();
                showSuccess("Cập nhật thông tin thành công!");
            } catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private void onDelete() {
        if (selectedStaff == null) {
            showWarning("Vui lòng chọn nhân viên muốn xóa!");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa nhân viên [" + selectedStaff.getFullName() + "]?\nDữ liệu không thể khôi phục.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

        if (ok == JOptionPane.YES_OPTION) {
            try {
                staffService.delete(selectedStaff.getId());
                loadTableAll();
                showSuccess("Đã xóa nhân viên khỏi hệ thống.");
            } catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
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
        s.setRole(data.getRole());
        s.setStatus(data.getStatus());
        return s;
    }

    // --- Table Model ---
    private static class StaffTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Tên nhân viên", "Số điện thoại", "Email cá nhân", "Chức vụ", "Trạng thái"};
        private List<Staff> data = new ArrayList<>();

        public void setData(List<Staff> list) {
            this.data = list != null ? new ArrayList<>(list) : new ArrayList<>();
            fireTableDataChanged();
        }

        public Staff getStaffAt(int row) { return data.get(row); }
        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Staff s = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> String.format("#%04d", s.getId()); // Format ID đẹp hơn (#0001)
                case 1 -> s.getFullName();
                case 2 -> s.getPhone();
                case 3 -> s.getEmail();
                case 4 -> s.getRole();
                case 5 -> s.getStatus();
                default -> "";
            };
        }
    }
}