package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Invoice;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.Promotion;
import vn.edu.ute.productmgmt.service.InvoiceService;
import vn.edu.ute.productmgmt.service.StudentService;
import vn.edu.ute.productmgmt.service.PromotionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

        setLayout(new BorderLayout(20,20));
        setOpaque(false);
        setBorder(new EmptyBorder(10,10,10,10));

        buildUI();

        table.getSelectionModel().addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()) onTableSelection();
        });

        loadTable();
    }

    private void buildUI(){

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // Left: search field + button
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        JTextField txtSearch = new JTextField(18);
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm hóa đơn...");
        txtSearch.setPreferredSize(new Dimension(300, 40));
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new JLabel(" 🔍 "));
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(100, 40));
        btnSearch.putClientProperty(FlatClientProperties.STYLE, "background: #0d6efd; foreground: #ffffff; arc: 12");
        btnSearch.addActionListener(e -> loadTable());

        left.add(txtSearch);
        left.add(Box.createHorizontalStrut(10));
        left.add(btnSearch);

        headerPanel.add(left, BorderLayout.WEST);
        headerPanel.add(buildActionBar(), BorderLayout.EAST);

        add(headerPanel,BorderLayout.NORTH);

        add(buildTableArea(),BorderLayout.CENTER);

        JPanel status = new JPanel(new BorderLayout());
        status.setOpaque(false);

        lblInfo.setForeground(Color.GRAY);
        lblInfo.setFont(new Font("Segoe UI",Font.ITALIC,13));

        status.add(lblInfo,BorderLayout.WEST);

        add(status,BorderLayout.SOUTH);
    }

    private JComponent buildActionBar(){

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        bar.setOpaque(false);

        JButton btnAdd = createBtn("Thêm","#0d6efd","➕ ");
        JButton btnEdit = createBtn("Sửa","#ffc107","📝 ");
        JButton btnDelete = createBtn("Xóa","#dc3545","🗑 ");
        JButton btnRefresh = new JButton("🔄 Tải lại");

        btnRefresh.setPreferredSize(new Dimension(100,38));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE,"arc: 10");

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> loadTable());

        bar.add(btnRefresh);
        bar.add(btnAdd);
        bar.add(btnEdit);
        bar.add(btnDelete);

        return bar;
    }

    private JButton createBtn(String text,String color,String icon){

        JButton btn = new JButton(icon + text);

        btn.setPreferredSize(new Dimension(110,36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String fg = color.equals("#ffc107") ? "#000000" : "#ffffff";

        btn.putClientProperty(FlatClientProperties.STYLE,
                "background:" + color +
                        ";foreground:" + fg +
                        ";arc:10;borderWidth:0");

        return btn;
    }

    private JComponent buildTableArea(){

        JScrollPane scroll = new JScrollPane(table);

        scroll.putClientProperty(FlatClientProperties.STYLE,"arc:15");
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230,230,230)));

        table.setRowHeight(45);
        table.setShowVerticalLines(false);

        table.getTableHeader().setFont(new Font("Segoe UI Semibold",Font.PLAIN,14));
        table.getTableHeader().setPreferredSize(new Dimension(0,45));

        return scroll;
    }

    private void loadTable(){

        try{

            List<Invoice> list = invoiceService.findAll();

            tableModel.setData(list);

            lblInfo.setText("Tổng số: "+list.size()+" hóa đơn");

            clearSelection();

        }catch(Exception ex){

            lblInfo.setText("Lỗi: "+ex.getMessage());

            JOptionPane.showMessageDialog(
                    this,
                    "Không tải được danh sách: "+ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void onTableSelection(){

        int row = table.getSelectedRow();

        selectedInvoice = row<0 ? null : tableModel.getInvoiceAt(row);

    }

    private void onAdd(){

        List<Student> students = studentService.findAll();

        List<Promotion> allPromos = promotionService.findAll();

        LocalDate today = LocalDate.now();

        List<Promotion> promotions = allPromos.stream()
                .filter(p -> p!=null && promotionService.isPromotionValid(p,today))
                .toList();

        InvoiceFormDialog dialog = new InvoiceFormDialog(
                SwingUtilities.getWindowAncestor(this),
                null,
                students,
                promotions
        );

        dialog.setVisible(true);

        if(!dialog.isSaved()) return;

        InvoiceFormDialog.InvoiceFormData data = dialog.getResult();

        try{

            BigDecimal baseAmount = new BigDecimal(data.getBaseAmount().trim());

            LocalDate issueDate = parseDate(data.getIssueDate());

            if(issueDate==null) issueDate = LocalDate.now();

            Long promoId = data.getPromotionId()!=null &&
                    !data.getPromotionId().trim().isEmpty()
                    ? Long.parseLong(data.getPromotionId().trim())
                    : null;

            invoiceService.createInvoice(
                    Long.parseLong(data.getStudentId().trim()),
                    promoId,
                    baseAmount,
                    issueDate,
                    data.getStatus(),
                    data.getNote()
            );

            JOptionPane.showMessageDialog(this,"Đã thêm hóa đơn");

            loadTable();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);

        }

    }

    private void onEdit(){

        if(selectedInvoice==null){

            JOptionPane.showMessageDialog(this,"Chọn hóa đơn để sửa");

            return;

        }

        List<Student> students = studentService.findAll();

        List<Promotion> allPromos = promotionService.findAll();

        LocalDate issueDate = selectedInvoice.getIssueDate()!=null
                ? selectedInvoice.getIssueDate()
                : LocalDate.now();

        Promotion currentPromo = selectedInvoice.getPromotion();

        Long currentPromoId = currentPromo!=null ? currentPromo.getId() : null;

        List<Promotion> promotions = allPromos.stream()
                .filter(p -> p!=null &&
                        (promotionService.isPromotionValid(p,issueDate)
                                || (currentPromoId!=null && currentPromoId.equals(p.getId()))))
                .toList();

        InvoiceFormDialog.InvoiceFormData existing = invoiceToFormData(selectedInvoice);

        InvoiceFormDialog dialog = new InvoiceFormDialog(
                SwingUtilities.getWindowAncestor(this),
                existing,
                students,
                promotions
        );

        dialog.setVisible(true);

        if(!dialog.isSaved()) return;

        InvoiceFormDialog.InvoiceFormData data = dialog.getResult();

        try{

            BigDecimal baseAmount = new BigDecimal(data.getBaseAmount().trim());

            LocalDate effectiveIssueDate = parseDate(data.getIssueDate());

            if(effectiveIssueDate==null) effectiveIssueDate = LocalDate.now();

            Long promoId = data.getPromotionId()!=null &&
                    !data.getPromotionId().trim().isEmpty()
                    ? Long.parseLong(data.getPromotionId().trim())
                    : null;

            invoiceService.updateInvoice(
                    selectedInvoice.getId(),
                    Long.parseLong(data.getStudentId().trim()),
                    promoId,
                    baseAmount,
                    effectiveIssueDate,
                    data.getStatus(),
                    data.getNote()
            );

            JOptionPane.showMessageDialog(this,"Đã cập nhật hóa đơn");

            loadTable();

            clearSelection();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);

        }

    }

    private void onDelete(){

        if(selectedInvoice==null){

            JOptionPane.showMessageDialog(this,"Chọn hóa đơn để xóa");

            return;

        }

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa hóa đơn này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if(ok!=JOptionPane.YES_OPTION) return;

        try{

            invoiceService.delete(selectedInvoice.getId());

            JOptionPane.showMessageDialog(this,"Đã xóa hóa đơn");

            loadTable();

            clearSelection();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);

        }

    }

    private InvoiceFormDialog.InvoiceFormData invoiceToFormData(Invoice inv){

        InvoiceFormDialog.InvoiceFormData data = new InvoiceFormDialog.InvoiceFormData();

        data.setStudentId(inv.getStudent()!=null && inv.getStudent().getId()!=null
                ? inv.getStudent().getId().toString()
                : "");

        data.setPromotionId(inv.getPromotion()!=null && inv.getPromotion().getId()!=null
                ? inv.getPromotion().getId().toString()
                : "");

        data.setBaseAmount(inv.getTotalAmount()!=null
                ? inv.getTotalAmount().toPlainString()
                : "");

        data.setIssueDate(inv.getIssueDate()!=null
                ? inv.getIssueDate().toString()
                : "");

        data.setStatus(inv.getStatus());

        data.setNote(inv.getNote());

        return data;

    }

    private LocalDate parseDate(String value){

        if(value==null || value.trim().isEmpty()) return null;

        try{

            return LocalDate.parse(value.trim());

        }catch(Exception e){

            return null;

        }

    }

    private void clearSelection(){

        selectedInvoice=null;

        table.clearSelection();

    }

    private static class InvoiceTableModel extends AbstractTableModel {

        private final String[] columns = {
                "ID",
                "Học viên",
                "Khuyến mãi",
                "Tổng tiền",
                "Ngày PH",
                "Trạng thái",
                "Ghi chú"
        };

        private List<Invoice> data = new ArrayList<>();

        void setData(List<Invoice> data){

            this.data = data!=null ? data : new ArrayList<>();

            fireTableDataChanged();

        }

        Invoice getInvoiceAt(int row){

            return row>=0 && row<data.size() ? data.get(row) : null;

        }

        @Override
        public int getRowCount(){

            return data.size();

        }

        @Override
        public int getColumnCount(){

            return columns.length;

        }

        @Override
        public String getColumnName(int col){

            return columns[col];

        }

        @Override
        public Object getValueAt(int row,int col){

            Invoice i = data.get(row);

            return switch(col){

                case 0 -> i.getId();

                case 1 -> i.getStudent()!=null
                        ? i.getStudent().getFullName()
                        : "";

                case 2 -> i.getPromotion()!=null
                        ? i.getPromotion().getPromoName()
                        : "";

                case 3 -> i.getTotalAmount()!=null
                        ? i.getTotalAmount().toPlainString()
                        : "";

                case 4 -> i.getIssueDate()!=null
                        ? i.getIssueDate().toString()
                        : "";

                case 5 -> i.getStatus()!=null
                        ? i.getStatus().name()
                        : "";

                case 6 -> i.getNote()!=null
                        ? i.getNote()
                        : "";

                default -> "";

            };

        }

    }

}

