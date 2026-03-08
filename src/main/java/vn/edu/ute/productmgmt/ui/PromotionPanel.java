package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Promotion;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.model.enums.DiscountType;
import vn.edu.ute.productmgmt.service.PromotionService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel Khuyến mãi: JTable danh sách + CRUD, theo mẫu CoursePanel.
 */
public class PromotionPanel extends JPanel {

    private final PromotionService promotionService;

    private final JLabel lblInfo = new JLabel(" ");
    private final PromotionTableModel tableModel = new PromotionTableModel();
    private final JTable table = new JTable(tableModel);
    private Promotion selectedPromotion;

    public PromotionPanel(PromotionService promotionService) {
        this.promotionService = promotionService;
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
        UI.stylePanelBorder(wrapper, "Danh sách khuyến mãi");

        JScrollPane scroll = new JScrollPane(table);
        UI.styleTable(table);
        wrapper.add(scroll, BorderLayout.CENTER);

        return wrapper;
    }

    private void loadTable() {
        try {
            List<Promotion> list = promotionService.findAll();
            tableModel.setData(list);
            lblInfo.setText("Tổng số: " + list.size() + " khuyến mãi.");
            clearSelection();
        } catch (Exception ex) {
            lblInfo.setText("Lỗi: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Không tải được danh sách: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onTableSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            selectedPromotion = null;
            return;
        }
        selectedPromotion = tableModel.getPromotionAt(row);
    }

    private void onAdd() {
        PromotionFormDialog dialog = new PromotionFormDialog(
                SwingUtilities.getWindowAncestor(this),
                null
        );
        dialog.setVisible(true);
        if (!dialog.isSaved()) return;

        PromotionFormDialog.PromotionFormData data = dialog.getResult();
        Promotion p = formDataToPromotion(data, null);
        if (p == null) return;
        try {
            promotionService.create(p);
            JOptionPane.showMessageDialog(this, "Đã thêm khuyến mãi.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSave() {
        if (selectedPromotion == null) {
            JOptionPane.showMessageDialog(this, "Chọn một khuyến mãi để sửa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PromotionFormDialog.PromotionFormData existing = promotionToFormData(selectedPromotion);
        PromotionFormDialog dialog = new PromotionFormDialog(
                SwingUtilities.getWindowAncestor(this),
                existing
        );
        dialog.setVisible(true);
        if (!dialog.isSaved()) return;

        PromotionFormDialog.PromotionFormData data = dialog.getResult();
        Promotion p = formDataToPromotion(data, selectedPromotion.getId());
        if (p == null) return;
        try {
            promotionService.update(p);
            JOptionPane.showMessageDialog(this, "Đã cập nhật khuyến mãi.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            clearSelection();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        if (selectedPromotion == null) {
            JOptionPane.showMessageDialog(this, "Chọn một khuyến mãi để xóa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa khuyến mãi này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            promotionService.delete(selectedPromotion.getId());
            JOptionPane.showMessageDialog(this, "Đã xóa khuyến mãi.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            clearSelection();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private PromotionFormDialog.PromotionFormData promotionToFormData(Promotion p) {
        PromotionFormDialog.PromotionFormData data = new PromotionFormDialog.PromotionFormData();
        data.setPromoName(p.getPromoName());
        data.setDiscountType(p.getDiscountType());
        data.setDiscountValue(p.getDiscountValue() != null ? p.getDiscountValue().toPlainString() : "");
        data.setStartDate(p.getStartDate() != null ? p.getStartDate().toString() : "");
        data.setEndDate(p.getEndDate() != null ? p.getEndDate().toString() : "");
        data.setStatus(p.getStatus());
        return data;
    }

    private Promotion formDataToPromotion(PromotionFormDialog.PromotionFormData data, Long keepId) {
        String name = data.getPromoName() != null ? data.getPromoName().trim() : "";
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên khuyến mãi không được để trống.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        BigDecimal discountValue = BigDecimal.ZERO;
        String valueStr = data.getDiscountValue() != null ? data.getDiscountValue().trim() : "";
        if (!valueStr.isEmpty()) {
            try {
                discountValue = new BigDecimal(valueStr.replace(",", "."));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Giá trị giảm giá không hợp lệ.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }

        LocalDate startDate = parseDate(data.getStartDate());
        LocalDate endDate = parseDate(data.getEndDate());
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            JOptionPane.showMessageDialog(this, "Ngày bắt đầu phải trước ngày kết thúc.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Promotion p = new Promotion();
        if (keepId != null) p.setId(keepId);
        p.setPromoName(name);
        p.setDiscountType(data.getDiscountType() != null ? data.getDiscountType() : DiscountType.Percent);
        p.setDiscountValue(discountValue);
        p.setStartDate(startDate);
        p.setEndDate(endDate);
        p.setStatus(data.getStatus() != null ? data.getStatus() : ActiveStatus.Active);
        return p;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void clearSelection() {
        selectedPromotion = null;
        table.clearSelection();
    }

    private static class PromotionTableModel extends AbstractTableModel {
        private final String[] columns = {
                "Tên khuyến mãi",
                "Loại giảm",
                "Giá trị",
                "Từ ngày",
                "Đến ngày",
                "Trạng thái"
        };
        private List<Promotion> data = new ArrayList<>();

        void setData(List<Promotion> data) {
            this.data = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        Promotion getPromotionAt(int row) {
            if (row < 0 || row >= data.size()) return null;
            return data.get(row);
        }

        @Override
        public int getRowCount() { return data.size(); }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Promotion p = data.get(row);
            switch (col) {
                case 0: return p.getPromoName();
                case 1: return p.getDiscountType() != null ? p.getDiscountType().name() : "";
                case 2: return p.getDiscountValue() != null ? p.getDiscountValue().toPlainString() : "";
                case 3: return p.getStartDate() != null ? p.getStartDate().toString() : "";
                case 4: return p.getEndDate() != null ? p.getEndDate().toString() : "";
                case 5: return p.getStatus() != null ? p.getStatus().name() : "";
                default: return "";
            }
        }
    }
}
