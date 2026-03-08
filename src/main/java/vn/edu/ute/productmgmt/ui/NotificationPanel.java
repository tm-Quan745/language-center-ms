package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Notification;
import vn.edu.ute.productmgmt.service.NotificationService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
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
        UI.stylePanelBorder(wrapper, "Danh sách thông báo");
        JScrollPane scroll = new JScrollPane(table);
        UI.styleTable(table);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private void loadTable() {
        try {
            List<Notification> list = notificationService.findAll();
            tableModel.setData(list);
            lblInfo.setText("Tổng số: " + list.size() + " thông báo.");
            clearSelection();
        } catch (Exception ex) {
            lblInfo.setText("Lỗi: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Không tải được danh sách: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onTableSelection() {
        int row = table.getSelectedRow();
        selectedNotification = row < 0 ? null : tableModel.getNotificationAt(row);
    }

    private void onAdd() {
        NotificationFormDialog dialog = new NotificationFormDialog(SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (!dialog.isSaved()) return;
        NotificationFormDialog.NotificationFormData data = dialog.getResult();
        Notification n = formDataToNotification(data, null);
        if (n == null) return;
        try {
            notificationService.create(n);
            JOptionPane.showMessageDialog(this, "Đã thêm thông báo.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSave() {
        if (selectedNotification == null) {
            JOptionPane.showMessageDialog(this, "Chọn một thông báo để sửa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        NotificationFormDialog.NotificationFormData existing = notificationToFormData(selectedNotification);
        NotificationFormDialog dialog = new NotificationFormDialog(SwingUtilities.getWindowAncestor(this), existing);
        dialog.setVisible(true);
        if (!dialog.isSaved()) return;
        NotificationFormDialog.NotificationFormData data = dialog.getResult();
        if (!applyFormDataToNotification(data, selectedNotification)) return;
        try {
            notificationService.update(selectedNotification);
            JOptionPane.showMessageDialog(this, "Đã cập nhật thông báo.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            clearSelection();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        if (selectedNotification == null) {
            JOptionPane.showMessageDialog(this, "Chọn một thông báo để xóa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa thông báo này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            notificationService.delete(selectedNotification.getId());
            JOptionPane.showMessageDialog(this, "Đã xóa thông báo.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            clearSelection();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private NotificationFormDialog.NotificationFormData notificationToFormData(Notification n) {
        NotificationFormDialog.NotificationFormData data = new NotificationFormDialog.NotificationFormData();
        data.setTitle(n.getTitle());
        data.setContent(n.getContent() != null ? n.getContent() : "");
        data.setTargetRole(n.getTargetRole() != null ? n.getTargetRole() : vn.edu.ute.productmgmt.model.enums.NotificationTargetRole.All);
        return data;
    }

    /** Tạo entity mới từ form (dùng cho Thêm mới). */
    private Notification formDataToNotification(NotificationFormDialog.NotificationFormData data, Long keepId) {
        String title = data.getTitle() != null ? data.getTitle().trim() : "";
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tiêu đề không được để trống.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String content = data.getContent() != null ? data.getContent().trim() : "";
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nội dung không được để trống.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Notification n = new Notification();
        if (keepId != null) n.setId(keepId);
        n.setTitle(title);
        n.setContent(content);
        n.setTargetRole(data.getTargetRole() != null ? data.getTargetRole() : vn.edu.ute.productmgmt.model.enums.NotificationTargetRole.All);
        return n;
    }

    /** Cập nhật entity có sẵn từ form (giữ created_at, created_by_user). */
    private boolean applyFormDataToNotification(NotificationFormDialog.NotificationFormData data, Notification n) {
        String title = data.getTitle() != null ? data.getTitle().trim() : "";
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tiêu đề không được để trống.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String content = data.getContent() != null ? data.getContent().trim() : "";
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nội dung không được để trống.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        n.setTitle(title);
        n.setContent(content);
        n.setTargetRole(data.getTargetRole() != null ? data.getTargetRole() : vn.edu.ute.productmgmt.model.enums.NotificationTargetRole.All);
        return true;
    }

    private void clearSelection() {
        selectedNotification = null;
        table.clearSelection();
    }

    private static class NotificationTableModel extends AbstractTableModel {
        private final String[] columns = {"Tiêu đề", "Đối tượng", "Ngày tạo"};
        private List<Notification> data = new ArrayList<>();

        void setData(List<Notification> data) {
            this.data = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        Notification getNotificationAt(int row) {
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
            Notification n = data.get(row);
            return switch (col) {
                case 0 -> n.getTitle();
                case 1 -> n.getTargetRole() != null ? n.getTargetRole().name() : "";
                case 2 -> n.getCreatedAt() != null ? n.getCreatedAt().format(FMT) : "";
                default -> "";
            };
        }
    }
}
