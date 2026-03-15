package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Branch;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.service.BranchService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BranchPanel extends JPanel {

    private final BranchService branchService;

    private final JLabel lblInfo = new JLabel(" ");
    private final JTextField txtSearch = new JTextField(18);
    private final BranchTableModel tableModel = new BranchTableModel();
    private final JTable table = new JTable(tableModel);
    private Branch selectedBranch;

    public BranchPanel(BranchService branchService) {
        this.branchService = branchService;

        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        buildUI();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            onTableSelection();
        });

        loadTable();
    }

    private void buildUI() {
        // --- Header Section ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // Left: search box + search button (replaces title)
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        txtSearch.setPreferredSize(new Dimension(300, 40));
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm theo tên chi nhánh hoặc địa chỉ...");
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

        right.add(buildActionBar());

        headerPanel.add(left, BorderLayout.WEST);
        headerPanel.add(right, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- Table Section ---
        add(buildTableArea(), BorderLayout.CENTER);

        // --- Status Bar ---
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);
        lblInfo.setForeground(Color.GRAY);
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        statusBar.add(lblInfo, BorderLayout.WEST);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JComponent buildActionBar() {

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bar.setOpaque(false);

        JButton btnAdd = createBtn("Thêm mới", "#198754", " ➕ ");
        JButton btnEdit = createBtn("Chỉnh sửa", "#ffc107", " 📝 ");
        JButton btnDelete = createBtn("Xóa bỏ", "#dc3545", " 🗑️ ");
        JButton btnRefresh = new JButton("🔄 Tải lại");

        btnRefresh.setPreferredSize(new Dimension(110, 38));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onSave());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> loadTable());

        bar.add(btnRefresh);
        bar.add(btnAdd);
        bar.add(btnEdit);
        bar.add(btnDelete);

        return bar;
    }

    private JButton createBtn(String text, String colorHex, String icon) {
        JButton btn = new JButton(icon + text);
        btn.setPreferredSize(new Dimension(120, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        String fg = colorHex.equals("#ffc107") ? "#000000" : "#ffffff";
        btn.putClientProperty(FlatClientProperties.STYLE,
                "background: " + colorHex + "; foreground: " + fg + "; arc: 10; borderWidth: 0");
        return btn;
    }

    private JComponent buildTableArea() {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scroll.putClientProperty(FlatClientProperties.STYLE, "arc: 15");

        // Table UI Styling
        table.setRowHeight(45);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        // Custom Renderer cho cột Trạng thái
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                if ("Active".equals(value.toString())) {
                    c.setForeground(new Color(25, 135, 84));
                    c.setText(" ● Hoạt động ");
                } else {
                    c.setForeground(Color.RED);
                    c.setText(" ● Ngừng hoạt động ");
                }
                return c;
            }
        });

        return scroll;
    }

    // --- Logic Methods ---

    private void loadTable() {
        try {
            List<Branch> list = branchService.findAll();
            tableModel.setData(list);
            lblInfo.setText("Hệ thống hiện có " + list.size() + " chi nhánh đang vận hành.");
            clearSelection();
        } catch (Exception ex) {
            lblInfo.setText("Lỗi kết nối dữ liệu.");
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onTableSelection() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int modelRow = table.convertRowIndexToModel(row);
            selectedBranch = tableModel.getBranchAt(modelRow);
        } else {
            selectedBranch = null;
        }
    }

    private void onAdd() {
        BranchFormDialog dialog = new BranchFormDialog(SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            try {
                branchService.create(formDataToBranch(dialog.getResult(), null));
                loadTable();
                JOptionPane.showMessageDialog(this, "Đã khởi tạo chi nhánh mới thành công!");
            } catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private void onSave() {
        if (selectedBranch == null) {
            showWarning("Vui lòng chọn một chi nhánh để cập nhật thông tin!");
            return;
        }
        BranchFormDialog dialog = new BranchFormDialog(SwingUtilities.getWindowAncestor(this), branchToFormData(selectedBranch));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            try {
                branchService.update(formDataToBranch(dialog.getResult(), selectedBranch.getId()));
                loadTable();
                JOptionPane.showMessageDialog(this, "Thông tin chi nhánh đã được cập nhật.");
            } catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private void onDelete() {
        if (selectedBranch == null) {
            showWarning("Chọn chi nhánh cần xóa!");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Xác nhận xóa chi nhánh: " + selectedBranch.getBranchName() + "?\nHành động này không thể hoàn tác.",
                "Cảnh báo", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

        if (ok == JOptionPane.YES_OPTION) {
            try {
                branchService.delete(selectedBranch.getId());
                loadTable();
            } catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private BranchFormDialog.BranchFormData branchToFormData(Branch b) {
        BranchFormDialog.BranchFormData data = new BranchFormDialog.BranchFormData();
        data.setName(b.getBranchName());
        data.setAddress(b.getAddress());
        data.setPhone(b.getPhone());
        data.setStatus(b.getStatus());
        return data;
    }

    private Branch formDataToBranch(BranchFormDialog.BranchFormData data, Long keepId) {
        Branch b = new Branch();
        if (keepId != null) b.setId(keepId);
        b.setBranchName(data.getName());
        b.setAddress(data.getAddress());
        b.setPhone(data.getPhone());
        b.setStatus(data.getStatus() != null ? data.getStatus() : ActiveStatus.Active);
        return b;
    }

    private void clearSelection() {
        selectedBranch = null;
        table.clearSelection();
    }

    private void onSearch() {
        String kw = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        if (kw.isEmpty()) {
            loadTable();
            return;
        }
        List<Branch> all = branchService.findAll();
        List<Branch> filtered = new ArrayList<>();
        for (Branch b : all) {
            String name = b.getBranchName() != null ? b.getBranchName().toLowerCase() : "";
            String addr = b.getAddress() != null ? b.getAddress().toLowerCase() : "";
            if (name.contains(kw) || addr.contains(kw)) filtered.add(b);
        }
        tableModel.setData(filtered);
        lblInfo.setText("Tìm thấy: " + filtered.size() + " kết quả");
    }

    // --- Table Model ---
    private static class BranchTableModel extends AbstractTableModel {
        private final String[] columns = {"Tên chi nhánh", "Địa chỉ trụ sở", "Số điện thoại", "Trạng thái"};
        private List<Branch> data = new ArrayList<>();

        void setData(List<Branch> data) {
            this.data = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        Branch getBranchAt(int row) { return data.get(row); }
        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int col) { return columns[col]; }
        @Override public Object getValueAt(int row, int col) {
            Branch b = data.get(row);
            return switch (col) {
                case 0 -> "  " + b.getBranchName(); // Padding nhẹ cho text
                case 1 -> b.getAddress();
                case 2 -> b.getPhone();
                case 3 -> b.getStatus();
                default -> "";
            };
        }
    }
}