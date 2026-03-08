package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Invoice;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.Promotion;
import vn.edu.ute.productmgmt.service.InvoiceService;
import vn.edu.ute.productmgmt.service.StudentService;
import vn.edu.ute.productmgmt.service.PromotionService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel Hóa đơn: bảng danh sách + Thêm / Sửa / Xóa.
 */
public class InvoicePanel extends JPanel {

    private final InvoiceService invoiceService;
    private final StudentService studentService;
    private final PromotionService promotionService;

    private final JLabel lblInfo = new JLabel(" ");
    private final InvoiceTableModel tableModel = new InvoiceTableModel();
    private final JTable table = new JTable(tableModel);
    private Invoice selectedInvoice;

    public InvoicePanel(InvoiceService invoiceService,
                       StudentService studentService,
                       PromotionService promotionService) {
        this.invoiceService = invoiceService;
        this.studentService = studentService;
        this.promotionService = promotionService;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onTableSelection();
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
        JButton btnEdit = new JButton("Chỉnh sửa");
        JButton btnDelete = new JButton("Xóa");
        JButton btnRefresh = new JButton("Tải lại");
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> loadTable());
        bar.add(btnAdd);
        bar.add(btnEdit);
        bar.add(btnDelete);
        bar.add(btnRefresh);
        return bar;
    }

    private JComponent buildTableArea() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        UI.stylePanelBorder(wrapper, "Danh sách hóa đơn");
        JScrollPane scroll = new JScrollPane(table);
        UI.styleTable(table);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private void loadTable() {
        try {
            List<Invoice> list = invoiceService.findAll();
            tableModel.setData(list);
            lblInfo.setText("Tổng số: " + list.size() + " hóa đơn.");
            clearSelection();
        } catch (Exception ex) {
            lblInfo.setText("Lỗi: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Không tải được danh sách: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onTableSelection() {
        int row = table.getSelectedRow();
        selectedInvoice = row < 0 ? null : tableModel.getInvoiceAt(row);
    }

    private void onAdd() {
        List<Student> students = studentService.findAll();
        List<Promotion> allPromos = promotionService.findAll();
        LocalDate today = LocalDate.now();
        List<Promotion> promotions = allPromos.stream()
                .filter(p -> p != null && promotionService.isPromotionValid(p, today))
                .toList();
        InvoiceFormDialog dialog = new InvoiceFormDialog(
                SwingUtilities.getWindowAncestor(this),
                null,
                students,
                promotions
        );
        dialog.setVisible(true);
        if (!dialog.isSaved()) return;

        InvoiceFormDialog.InvoiceFormData data = dialog.getResult();
        try {
            BigDecimal baseAmount = new BigDecimal(data.getBaseAmount().trim());
            LocalDate issueDate = parseDate(data.getIssueDate());
            if (issueDate == null) issueDate = LocalDate.now();
            Long promoId = data.getPromotionId() != null && !data.getPromotionId().trim().isEmpty()
                    ? Long.parseLong(data.getPromotionId().trim()) : null;

            invoiceService.createInvoice(
                    Long.parseLong(data.getStudentId().trim()),
                    promoId,
                    baseAmount,
                    issueDate,
                    data.getStatus(),
                    data.getNote()
            );
            JOptionPane.showMessageDialog(this, "Đã thêm hóa đơn.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEdit() {
        if (selectedInvoice == null) {
            JOptionPane.showMessageDialog(this, "Chọn một hóa đơn để sửa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Student> students = studentService.findAll();
        List<Promotion> allPromos = promotionService.findAll();
        LocalDate issueDate = selectedInvoice.getIssueDate() != null ? selectedInvoice.getIssueDate() : LocalDate.now();
        Promotion currentPromo = selectedInvoice.getPromotion();
        Long currentPromoId = currentPromo != null ? currentPromo.getId() : null;
        List<Promotion> promotions = allPromos.stream()
                .filter(p -> p != null && (promotionService.isPromotionValid(p, issueDate)
                        || (currentPromoId != null && currentPromoId.equals(p.getId()))))
                .toList();
        InvoiceFormDialog.InvoiceFormData existing = invoiceToFormData(selectedInvoice);
        InvoiceFormDialog dialog = new InvoiceFormDialog(
                SwingUtilities.getWindowAncestor(this),
                existing,
                students,
                promotions
        );
        dialog.setVisible(true);
        if (!dialog.isSaved()) return;

        InvoiceFormDialog.InvoiceFormData data = dialog.getResult();
        try {
            BigDecimal baseAmount = new BigDecimal(data.getBaseAmount().trim());
            LocalDate effectiveIssueDate = parseDate(data.getIssueDate());
            if (effectiveIssueDate == null) effectiveIssueDate = LocalDate.now();
            Long promoId = data.getPromotionId() != null && !data.getPromotionId().trim().isEmpty()
                    ? Long.parseLong(data.getPromotionId().trim()) : null;

            invoiceService.updateInvoice(
                    selectedInvoice.getId(),
                    Long.parseLong(data.getStudentId().trim()),
                    promoId,
                    baseAmount,
                    effectiveIssueDate,
                    data.getStatus(),
                    data.getNote()
            );
            JOptionPane.showMessageDialog(this, "Đã cập nhật hóa đơn.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            clearSelection();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        if (selectedInvoice == null) {
            JOptionPane.showMessageDialog(this, "Chọn một hóa đơn để xóa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa hóa đơn này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            invoiceService.delete(selectedInvoice.getId());
            JOptionPane.showMessageDialog(this, "Đã xóa hóa đơn.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            clearSelection();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private InvoiceFormDialog.InvoiceFormData invoiceToFormData(Invoice inv) {
        InvoiceFormDialog.InvoiceFormData data = new InvoiceFormDialog.InvoiceFormData();
        data.setStudentId(inv.getStudent() != null && inv.getStudent().getId() != null ? inv.getStudent().getId().toString() : "");
        data.setPromotionId(inv.getPromotion() != null && inv.getPromotion().getId() != null ? inv.getPromotion().getId().toString() : "");
        // Khi sửa: dùng totalAmount làm baseAmount (ước lượng, user có thể chỉnh)
        data.setBaseAmount(inv.getTotalAmount() != null ? inv.getTotalAmount().toPlainString() : "");
        data.setIssueDate(inv.getIssueDate() != null ? inv.getIssueDate().toString() : "");
        data.setStatus(inv.getStatus());
        data.setNote(inv.getNote());
        return data;
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
        selectedInvoice = null;
        table.clearSelection();
    }

    private static class InvoiceTableModel extends AbstractTableModel {
        private final String[] columns = { "ID", "Học viên", "Khuyến mãi", "Tổng tiền", "Ngày PH", "Trạng thái", "Ghi chú" };
        private List<Invoice> data = new ArrayList<>();

        void setData(List<Invoice> data) {
            this.data = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        Invoice getInvoiceAt(int row) {
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
            Invoice i = data.get(row);
            return switch (col) {
                case 0 -> i.getId();
                case 1 -> i.getStudent() != null ? i.getStudent().getFullName() : "";
                case 2 -> i.getPromotion() != null ? i.getPromotion().getPromoName() : "";
                case 3 -> i.getTotalAmount() != null ? i.getTotalAmount().toPlainString() : "";
                case 4 -> i.getIssueDate() != null ? i.getIssueDate().toString() : "";
                case 5 -> i.getStatus() != null ? i.getStatus().name() : "";
                case 6 -> i.getNote() != null ? i.getNote() : "";
                default -> "";
            };
        }
    }
}
