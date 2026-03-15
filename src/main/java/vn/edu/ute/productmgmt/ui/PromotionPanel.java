package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Promotion;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.model.enums.DiscountType;
import vn.edu.ute.productmgmt.service.PromotionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel quản lý khuyến mãi, chuẩn hóa theo NotificationPanel
 */
public class PromotionPanel extends JPanel {

    private final PromotionService promotionService;

    private final JLabel lblInfo = new JLabel(" ");
    private final PromotionTableModel tableModel = new PromotionTableModel();
    private final JTable table = new JTable(tableModel);
    private Promotion selectedPromotion;

    public PromotionPanel(PromotionService promotionService) {
        this.promotionService = promotionService;

        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        buildUI();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            onTableSelection();
        });

        loadTable();
    }

    private void buildUI() {

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // LEFT: search area
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        JTextField txtSearch = new JTextField(18);
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm tên khuyến mãi...");
        txtSearch.setPreferredSize(new Dimension(300, 40));
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new JLabel(" 🔍 "));
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(100, 40));
        btnSearch.putClientProperty(FlatClientProperties.STYLE, "background: #0d6efd; foreground: #ffffff; arc: 12");
        btnSearch.addActionListener(e -> onSearch(txtSearch.getText()));

        left.add(txtSearch);
        left.add(Box.createHorizontalStrut(10));
        left.add(btnSearch);

        headerPanel.add(left, BorderLayout.WEST);
        headerPanel.add(buildActionBar(), BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        add(buildTableArea(), BorderLayout.CENTER);

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

        JButton btnAdd = createBtn("Thêm", "#0d6efd", "➕ ");
        JButton btnEdit = createBtn("Sửa", "#ffc107", "📝 ");
        JButton btnDelete = createBtn("Xóa", "#dc3545", "🗑 ");
        JButton btnRefresh = new JButton("🔄 Tải lại");

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

    private JButton createBtn(String text, String color, String icon) {

        JButton btn = new JButton(icon + text);

        btn.setPreferredSize(new Dimension(110, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String fg = color.equals("#ffc107") ? "#000000" : "#ffffff";

        btn.putClientProperty(FlatClientProperties.STYLE,
                "background:" + color +
                        ";foreground:" + fg +
                        ";arc:10;borderWidth:0");

        return btn;
    }

    private JComponent buildTableArea() {

        JScrollPane scroll = new JScrollPane(table);

        scroll.putClientProperty(FlatClientProperties.STYLE, "arc:15");
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        table.setRowHeight(45);
        table.setShowVerticalLines(false);

        table.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        return scroll;
    }

    private void loadTable() {

        try {

            List<Promotion> list = promotionService.findAll();

            tableModel.setData(list);

            lblInfo.setText("Tổng số: " + list.size() + " khuyến mãi");

            table.clearSelection();

            selectedPromotion = null;

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());

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

        if (dialog.isSaved()) {

            try {

                PromotionFormDialog.PromotionFormData data = dialog.getResult();
                Promotion p = formDataToPromotion(data, null);
                if (p == null) return;

                promotionService.create(p);

                loadTable();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Lỗi thêm khuyến mãi: " + ex.getMessage());

            }

        }

    }

    private void onSave() {

        if (selectedPromotion == null) {

            JOptionPane.showMessageDialog(this, "Vui lòng chọn khuyến mãi để sửa");

            return;

        }

        PromotionFormDialog.PromotionFormData existing = promotionToFormData(selectedPromotion);

        PromotionFormDialog dialog = new PromotionFormDialog(
                SwingUtilities.getWindowAncestor(this),
                existing
        );

        dialog.setVisible(true);

        if (dialog.isSaved()) {

            try {

                PromotionFormDialog.PromotionFormData data = dialog.getResult();
                Promotion p = formDataToPromotion(data, selectedPromotion.getId());
                if (p == null) return;

                promotionService.update(p);

                loadTable();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Lỗi cập nhật: " + ex.getMessage());

            }

        }

    }

    private void onDelete() {

        if (selectedPromotion == null) return;

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Xóa khuyến mãi?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (ok == JOptionPane.YES_OPTION) {

            try {

                promotionService.delete(selectedPromotion.getId());

                loadTable();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Lỗi xóa: " + ex.getMessage());

            }

        }

    }

    private void onSearch(String q) {
        String kw = q != null ? q.trim().toLowerCase() : "";
        if (kw.isEmpty()) {
            loadTable();
            return;
        }
        List<Promotion> all = promotionService.findAll();
        List<Promotion> filtered = new ArrayList<>();
        for (Promotion p : all) {
            String name = p.getPromoName() != null ? p.getPromoName().toLowerCase() : "";
            if (name.contains(kw)) filtered.add(p);
        }
        tableModel.setData(filtered);
        lblInfo.setText("Tìm thấy: " + filtered.size() + " kết quả");
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

        Promotion getPromotionAt(int r) {

            return (r >= 0 && r < data.size()) ? data.get(r) : null;

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
        public String getColumnName(int c) {

            return columns[c];

        }

        @Override
        public Object getValueAt(int r, int c) {

            Promotion p = data.get(r);

            return switch (c) {

                case 0 -> p.getPromoName();

                case 1 -> p.getDiscountType() != null ? p.getDiscountType().name() : "";

                case 2 -> p.getDiscountValue() != null ? p.getDiscountValue().toPlainString() : "";

                case 3 -> p.getStartDate() != null ? p.getStartDate().toString() : "";

                case 4 -> p.getEndDate() != null ? p.getEndDate().toString() : "";

                case 5 -> p.getStatus() != null ? p.getStatus().name() : "";

                default -> "";

            };

        }

    }

}
