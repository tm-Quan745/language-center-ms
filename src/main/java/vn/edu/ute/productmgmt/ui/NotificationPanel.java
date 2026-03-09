package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Notification;
import vn.edu.ute.productmgmt.model.enums.NotificationTargetRole;
import vn.edu.ute.productmgmt.service.NotificationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final NotificationService notificationService;

    private final JLabel lblInfo = new JLabel(" ");
    private final NotificationTableModel tableModel = new NotificationTableModel();
    private final JTable table = new JTable(tableModel);
    private Notification selectedNotification;

    public NotificationPanel(NotificationService notificationService) {
        this.notificationService = notificationService;

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

        JLabel lblTitle = new JLabel("Trung tâm Thông báo");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        headerPanel.add(buildActionBar(), BorderLayout.EAST);
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

        JButton btnAdd = createBtn("Soạn tin", "#0d6efd", " ✉ ");
        JButton btnEdit = createBtn("Sửa tin", "#ffc107", " 📝 ");
        JButton btnDelete = createBtn("Xóa bỏ", "#dc3545", " 🗑️ ");
        JButton btnRefresh = new JButton("Tải lại");

        btnRefresh.setPreferredSize(new Dimension(100, 38));
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
        table.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        // Căn lề cột Ngày tạo
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        return scroll;
    }

    private void loadTable() {
        try {
            List<Notification> list = notificationService.findAll();
            tableModel.setData(list);
            lblInfo.setText("Hệ thống có " + list.size() + " thông báo đã phát hành.");
            clearSelection();
        } catch (Exception ex) {
            lblInfo.setText("Lỗi: " + ex.getMessage());
        }
    }

    private void onTableSelection() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int modelRow = table.convertRowIndexToModel(row);
            selectedNotification = tableModel.getNotificationAt(modelRow);
        } else {
            selectedNotification = null;
        }
    }

    private void onAdd() {
        NotificationFormDialog dialog = new NotificationFormDialog(SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            try {
                notificationService.create(formDataToNotification(dialog.getResult(), null));
                loadTable();
                JOptionPane.showMessageDialog(this, "Đã phát hành thông báo mới.");
            } catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private void onSave() {
        if (selectedNotification == null) {
            showWarning("Chọn thông báo cần sửa!");
            return;
        }
        NotificationFormDialog dialog = new NotificationFormDialog(SwingUtilities.getWindowAncestor(this), notificationToFormData(selectedNotification));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            if (applyFormDataToNotification(dialog.getResult(), selectedNotification)) {
                try {
                    notificationService.update(selectedNotification);
                    loadTable();
                    clearSelection();
                } catch (Exception ex) { showError(ex.getMessage()); }
            }
        }
    }

    private void onDelete() {
        if (selectedNotification == null) {
            showWarning("Chọn thông báo cần xóa!");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Xóa thông báo này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            try {
                notificationService.delete(selectedNotification.getId());
                loadTable();
            } catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE); }
    private void showWarning(String msg) { JOptionPane.showMessageDialog(this, msg, "Cảnh báo", JOptionPane.WARNING_MESSAGE); }

    private NotificationFormDialog.NotificationFormData notificationToFormData(Notification n) {
        NotificationFormDialog.NotificationFormData data = new NotificationFormDialog.NotificationFormData();
        data.setTitle(n.getTitle());
        data.setContent(n.getContent());
        data.setTargetRole(n.getTargetRole());
        return data;
    }

    private Notification formDataToNotification(NotificationFormDialog.NotificationFormData data, Long id) {
        Notification n = new Notification();
        if (id != null) n.setId(id);
        n.setTitle(data.getTitle());
        n.setContent(data.getContent());
        n.setTargetRole(data.getTargetRole());
        return n;
    }

    private boolean applyFormDataToNotification(NotificationFormDialog.NotificationFormData data, Notification n) {
        n.setTitle(data.getTitle());
        n.setContent(data.getContent());
        n.setTargetRole(data.getTargetRole());
        return true;
    }

    private void clearSelection() {
        selectedNotification = null;
        table.clearSelection();
    }

    private static class NotificationTableModel extends AbstractTableModel {
        private final String[] columns = {"Tiêu đề thông báo", "Đối tượng nhận", "Ngày phát hành"};
        private List<Notification> data = new ArrayList<>();

        void setData(List<Notification> list) {
            this.data = list != null ? list : new ArrayList<>();
            fireTableDataChanged();
        }

        Notification getNotificationAt(int row) { return data.get(row); }
        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int col) { return columns[col]; }
        @Override public Object getValueAt(int row, int col) {
            Notification n = data.get(row);
            return switch (col) {
                case 0 -> "  " + n.getTitle();
                case 1 -> n.getTargetRole();
                case 2 -> n.getCreatedAt() != null ? n.getCreatedAt().format(FMT) : "---";
                default -> "";
            };
        }
    }
}