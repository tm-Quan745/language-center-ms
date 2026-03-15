package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Payment;
import vn.edu.ute.productmgmt.model.enums.PaymentMethod;
import vn.edu.ute.productmgmt.model.enums.PaymentStatus;
import vn.edu.ute.productmgmt.service.EnrollmentService;
import vn.edu.ute.productmgmt.service.InvoiceService;
import vn.edu.ute.productmgmt.service.PaymentService;
import vn.edu.ute.productmgmt.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PaymentPanel extends JPanel {

    private final PaymentService paymentService;
    private final StudentService studentService;
    private final EnrollmentService enrollmentService;
    private final InvoiceService invoiceService;

    private final JTextField txtSearch = new JTextField(18);
    private final JLabel lblInfo = new JLabel(" ");

    private final PaymentTableModel tableModel = new PaymentTableModel();
    private final JTable table = new JTable(tableModel);

    private Payment selectedPayment;

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public PaymentPanel(PaymentService paymentService,
                        StudentService studentService,
                        EnrollmentService enrollmentService,
                        InvoiceService invoiceService) {

        this.paymentService = paymentService;
        this.studentService = studentService;
        this.enrollmentService = enrollmentService;
        this.invoiceService = invoiceService;

        setLayout(new BorderLayout(20,20));
        setOpaque(false);
        setBorder(new EmptyBorder(10,10,10,10));

        buildUI();

        table.getSelectionModel().addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()) onTableSelection();
        });

        loadTableAll();
    }

    private void buildUI(){

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // Left: search field + button
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm tên học viên hoặc mã tham chiếu...");
        txtSearch.setPreferredSize(new Dimension(300, 40));
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new JLabel(" 🔍 "));
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(100, 40));
        btnSearch.putClientProperty(FlatClientProperties.STYLE, "background: #0d6efd; foreground: #ffffff; arc: 12");
        btnSearch.addActionListener(e -> onSearch());

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
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadTableAll();
        });

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

        btn.putClientProperty(
                FlatClientProperties.STYLE,
                "background:"+color+
                        ";foreground:"+fg+
                        ";arc:10;borderWidth:0"
        );

        return btn;
    }

    private JComponent buildTableArea(){

        JScrollPane scroll = new JScrollPane(table);

        scroll.putClientProperty(FlatClientProperties.STYLE,"arc:15");
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230,230,230)));

        table.setRowHeight(45);
        table.setShowVerticalLines(false);

        table.getTableHeader().setFont(
                new Font("Segoe UI Semibold",Font.PLAIN,14)
        );
        table.getTableHeader().setPreferredSize(new Dimension(0,45));

        return scroll;
    }

    private void loadTableAll(){

        try{

            List<Payment> list = paymentService.findAll();

            tableModel.setData(list);

            lblInfo.setText("Tổng số: "+list.size()+" thanh toán");

            clearSelection();

        }catch(Exception ex){

            lblInfo.setText("Lỗi: "+ex.getMessage());

            JOptionPane.showMessageDialog(
                    this,
                    "Không tải được danh sách thanh toán: "+ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );

        }

    }

    private void onSearch(){

        String keyword = txtSearch.getText().trim().toLowerCase();

        if(keyword.isEmpty()){
            loadTableAll();
            return;
        }

        List<Payment> src = tableModel.getRawData();

        List<Payment> filtered = new ArrayList<>();

        for(Payment p : src){

            String name =
                    p.getStudent()!=null &&
                            p.getStudent().getFullName()!=null
                            ? p.getStudent().getFullName().toLowerCase()
                            : "";

            String ref =
                    p.getReferenceCode()!=null
                            ? p.getReferenceCode().toLowerCase()
                            : "";

            if(name.contains(keyword) || ref.contains(keyword)){
                filtered.add(p);
            }

        }

        tableModel.setData(filtered);

        lblInfo.setText("Kết quả: "+filtered.size()+" thanh toán");

        clearSelection();

    }

    private void onTableSelection(){

        int row = table.getSelectedRow();

        selectedPayment = row<0 ? null : tableModel.getPaymentAt(row);

    }

    private void onAdd(){

        PaymentFormDialog dialog = new PaymentFormDialog(
                SwingUtilities.getWindowAncestor(this),
                null,
                studentService.findAll(),
                enrollmentService.findAll(),
                invoiceService.findAll()
        );

        dialog.setVisible(true);

        if(!dialog.isSaved()) return;

        PaymentFormDialog.PaymentFormData data = dialog.getResult();

        try{

            Long studentId = Long.parseLong(data.getStudentId().trim());

            Long enrollmentId = parseLongOrNull(data.getEnrollmentId());
            Long invoiceId = parseLongOrNull(data.getInvoiceId());

            BigDecimal amount = new BigDecimal(data.getAmount().trim());

            LocalDateTime paymentDate = parsePaymentDate(data.getPaymentDate());

            paymentService.createPayment(
                    studentId,
                    enrollmentId,
                    invoiceId,
                    amount,
                    paymentDate,
                    data.getMethod(),
                    data.getStatus(),
                    data.getReferenceCode()
            );

            JOptionPane.showMessageDialog(this,"Đã thêm thanh toán");

            loadTableAll();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);

        }

    }

    private void onEdit(){

        if(selectedPayment==null){

            JOptionPane.showMessageDialog(this,"Chọn thanh toán để sửa");

            return;

        }

        PaymentFormDialog.PaymentFormData existing =
                paymentToFormData(selectedPayment);

        PaymentFormDialog dialog = new PaymentFormDialog(
                SwingUtilities.getWindowAncestor(this),
                existing,
                studentService.findAll(),
                enrollmentService.findAll(),
                invoiceService.findAll()
        );

        dialog.setVisible(true);

        if(!dialog.isSaved()) return;

        PaymentFormDialog.PaymentFormData data = dialog.getResult();

        try{

            Long studentId = Long.parseLong(data.getStudentId().trim());

            Long enrollmentId = parseLongOrNull(data.getEnrollmentId());
            Long invoiceId = parseLongOrNull(data.getInvoiceId());

            BigDecimal amount = new BigDecimal(data.getAmount().trim());

            LocalDateTime paymentDate = parsePaymentDate(data.getPaymentDate());

            paymentService.updatePayment(
                    selectedPayment.getId(),
                    studentId,
                    enrollmentId,
                    invoiceId,
                    amount,
                    paymentDate,
                    data.getMethod(),
                    data.getStatus(),
                    data.getReferenceCode()
            );

            JOptionPane.showMessageDialog(this,"Đã cập nhật thanh toán");

            loadTableAll();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);

        }

    }

    private void onDelete(){

        if(selectedPayment==null){

            JOptionPane.showMessageDialog(this,"Chọn thanh toán để xóa");

            return;

        }

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa thanh toán này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if(ok!=JOptionPane.YES_OPTION) return;

        try{

            paymentService.delete(selectedPayment.getId());

            JOptionPane.showMessageDialog(this,"Đã xóa thanh toán");

            loadTableAll();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);

        }

    }

    private Long parseLongOrNull(String value){

        if(value==null || value.trim().isEmpty()) return null;

        try{
            return Long.parseLong(value.trim());
        }catch(Exception e){
            return null;
        }

    }

    private LocalDateTime parsePaymentDate(String dateStr){

        if(dateStr==null || dateStr.trim().isEmpty()) return null;

        try{
            return LocalDateTime.parse(dateStr.trim(),DATE_TIME_FMT);
        }catch(DateTimeParseException ex){
            return null;
        }

    }

    private PaymentFormDialog.PaymentFormData paymentToFormData(Payment p){

        PaymentFormDialog.PaymentFormData data =
                new PaymentFormDialog.PaymentFormData();

        data.setStudentId(
                p.getStudent()!=null && p.getStudent().getId()!=null
                        ? p.getStudent().getId().toString()
                        : ""
        );

        data.setEnrollmentId(
                p.getEnrollment()!=null && p.getEnrollment().getId()!=null
                        ? p.getEnrollment().getId().toString()
                        : ""
        );

        data.setInvoiceId(
                p.getInvoice()!=null && p.getInvoice().getId()!=null
                        ? p.getInvoice().getId().toString()
                        : ""
        );

        data.setAmount(
                p.getAmount()!=null ? p.getAmount().toPlainString() : ""
        );

        data.setPaymentDate(
                p.getPaymentDate()!=null
                        ? p.getPaymentDate().format(DATE_TIME_FMT)
                        : ""
        );

        data.setMethod(
                p.getPaymentMethod()!=null
                        ? p.getPaymentMethod()
                        : PaymentMethod.Cash
        );

        data.setStatus(
                p.getStatus()!=null
                        ? p.getStatus()
                        : PaymentStatus.Completed
        );

        data.setReferenceCode(
                p.getReferenceCode()!=null ? p.getReferenceCode() : ""
        );

        return data;

    }

    private void clearSelection(){

        selectedPayment = null;

        table.clearSelection();

    }

    private static class PaymentTableModel extends AbstractTableModel {

        private final String[] columns = {
                "ID",
                "Học viên",
                "Ghi danh",
                "Hóa đơn",
                "Số tiền",
                "Ngày thanh toán",
                "Phương thức",
                "Trạng thái",
                "Mã tham chiếu"
        };

        private List<Payment> data = new ArrayList<>();

        void setData(List<Payment> data){

            this.data = data!=null ? data : new ArrayList<>();

            fireTableDataChanged();

        }

        List<Payment> getRawData(){
            return data;
        }

        Payment getPaymentAt(int row){
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
        public String getColumnName(int column){
            return columns[column];
        }

        @Override
        public Object getValueAt(int row,int col){

            Payment p = data.get(row);

            return switch(col){

                case 0 -> p.getId();

                case 1 -> p.getStudent()!=null
                        ? p.getStudent().getFullName()
                        : "";

                case 2 -> p.getEnrollment()!=null
                        ? "GĐ #"+p.getEnrollment().getId()
                        : "";

                case 3 -> p.getInvoice()!=null
                        ? "HD #"+p.getInvoice().getId()
                        : "";

                case 4 -> p.getAmount()!=null
                        ? p.getAmount().toPlainString()
                        : "";

                case 5 -> p.getPaymentDate()!=null
                        ? p.getPaymentDate().format(DATE_TIME_FMT)
                        : "";

                case 6 -> p.getPaymentMethod()!=null
                        ? p.getPaymentMethod().name()
                        : "";

                case 7 -> p.getStatus()!=null
                        ? p.getStatus().name()
                        : "";

                case 8 -> p.getReferenceCode();

                default -> "";

            };

        }

    }

}

