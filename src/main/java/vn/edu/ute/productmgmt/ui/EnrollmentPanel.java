package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Enrollment;
import vn.edu.ute.productmgmt.service.ClassService;
import vn.edu.ute.productmgmt.service.EnrollmentService;
import vn.edu.ute.productmgmt.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentPanel extends JPanel {

    private final EnrollmentService enrollmentService;
    private final StudentService studentService;
    private final ClassService classService;

    private final JTextField txtSearch = new JTextField(18);
    private final JLabel lblInfo = new JLabel(" ");

    private final EnrollmentTableModel tableModel = new EnrollmentTableModel();
    private final JTable table = new JTable(tableModel);

    private Enrollment selectedEnrollment;

    public EnrollmentPanel(EnrollmentService enrollmentService,
                           StudentService studentService,
                           ClassService classService) {

        this.enrollmentService = enrollmentService;
        this.studentService = studentService;
        this.classService = classService;

        setLayout(new BorderLayout(20,20));
        setOpaque(false);
        setBorder(new EmptyBorder(10,10,10,10));

        buildUI();
        loadTableAll();
    }

    private void buildUI(){

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Quản lý Ghi danh");
        title.setFont(new Font("Segoe UI",Font.BOLD,22));

        header.add(title,BorderLayout.WEST);
        header.add(buildActionBar(),BorderLayout.EAST);

        add(header,BorderLayout.NORTH);

        add(buildTableArea(),BorderLayout.CENTER);

        JPanel status = new JPanel(new BorderLayout());
        status.setOpaque(false);

        lblInfo.setFont(new Font("Segoe UI",Font.ITALIC,13));
        lblInfo.setForeground(Color.GRAY);

        status.add(lblInfo,BorderLayout.WEST);

        add(status,BorderLayout.SOUTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getSelectionModel().addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()) onTableSelection();
        });

    }

    private JComponent buildActionBar(){

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        bar.setOpaque(false);

        txtSearch.putClientProperty(
                FlatClientProperties.PLACEHOLDER_TEXT,
                "Tìm theo học viên hoặc lớp..."
        );
        txtSearch.setPreferredSize(new Dimension(200,36));

        JButton btnSearch = new JButton("Tìm");
        btnSearch.setPreferredSize(new Dimension(70,36));

        JButton btnAdd = createBtn("Thêm","#0d6efd","➕ ");
        JButton btnEdit = createBtn("Sửa","#ffc107","📝 ");
        JButton btnDelete = createBtn("Xóa","#dc3545","🗑 ");
        JButton btnRefresh = new JButton("Tải lại");

        btnRefresh.setPreferredSize(new Dimension(90,36));
        btnRefresh.putClientProperty(
                FlatClientProperties.STYLE,
                "arc:10"
        );

        btnSearch.addActionListener(e -> onSearch());
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadTableAll();
        });

        bar.add(txtSearch);
        bar.add(btnSearch);
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

        scroll.putClientProperty(
                FlatClientProperties.STYLE,
                "arc:15"
        );

        scroll.setBorder(
                BorderFactory.createLineBorder(new Color(230,230,230))
        );

        table.setRowHeight(45);
        table.setShowVerticalLines(false);

        table.getTableHeader().setFont(
                new Font("Segoe UI Semibold",Font.PLAIN,14)
        );

        table.getTableHeader().setPreferredSize(
                new Dimension(0,45)
        );

        return scroll;

    }

    private void loadTableAll(){

        try{

            List<Enrollment> list = enrollmentService.findAll();

            tableModel.setData(list);

            lblInfo.setText("Tổng số: "+list.size()+" ghi danh");

            clearSelection();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi: "+ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

        }

    }

    private void onAdd(){

        EnrollmentFormDialog dialog = new EnrollmentFormDialog(
                SwingUtilities.getWindowAncestor(this),
                null,
                studentService,
                classService
        );

        dialog.setVisible(true);

        if(dialog.isSaved()){

            try{

                EnrollmentFormDialog.EnrollmentFormData data = dialog.getResult();

                enrollmentService.createEnrollment(
                        data.getStudentId(),
                        data.getClassId(),
                        java.time.LocalDate.parse(data.getEnrollmentDate()),
                        data.getStatus(),
                        data.getResult()
                );

                loadTableAll();

            }catch(Exception ex){

                JOptionPane.showMessageDialog(this,"Lỗi thêm: "+ex.getMessage());

            }

        }

    }

    private void onEdit(){

        if(selectedEnrollment==null){

            JOptionPane.showMessageDialog(this,"Chọn bản ghi để sửa");

            return;

        }

        EnrollmentFormDialog dialog = new EnrollmentFormDialog(
                SwingUtilities.getWindowAncestor(this),
                enrollmentToFormData(selectedEnrollment),
                studentService,
                classService
        );

        dialog.setVisible(true);

        if(dialog.isSaved()){

            try{

                EnrollmentFormDialog.EnrollmentFormData data = dialog.getResult();

                enrollmentService.updateEnrollment(
                        selectedEnrollment.getId(),
                        data.getStudentId(),
                        data.getClassId(),
                        java.time.LocalDate.parse(data.getEnrollmentDate()),
                        data.getStatus(),
                        data.getResult()
                );

                loadTableAll();

            }catch(Exception ex){

                JOptionPane.showMessageDialog(this,"Lỗi cập nhật: "+ex.getMessage());

            }

        }

    }

    private void onDelete(){

        if(selectedEnrollment==null) return;

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận xóa ghi danh này?",
                "Xóa",
                JOptionPane.YES_NO_OPTION
        );

        if(ok==JOptionPane.YES_OPTION){

            try{

                enrollmentService.delete(selectedEnrollment.getId());

                loadTableAll();

            }catch(Exception ex){

                JOptionPane.showMessageDialog(this,"Lỗi xóa: "+ex.getMessage());

            }

        }

    }

    private EnrollmentFormDialog.EnrollmentFormData enrollmentToFormData(Enrollment e){

        EnrollmentFormDialog.EnrollmentFormData data =
                new EnrollmentFormDialog.EnrollmentFormData();

        data.setStudentId(e.getStudent().getId());
        data.setClassId(e.getTeachingClass().getId());
        data.setEnrollmentDate(e.getEnrollmentDate().toString());
        data.setStatus(e.getStatus());
        data.setResult(e.getResult());

        return data;

    }

    private void onSearch(){

        String kw = txtSearch.getText().trim().toLowerCase();

        List<Enrollment> filtered = enrollmentService.findAll().stream()
                .filter(e ->
                        e.getStudent().getFullName().toLowerCase().contains(kw)
                                ||
                                e.getTeachingClass().getClassName().toLowerCase().contains(kw)
                )
                .toList();

        tableModel.setData(filtered);

        lblInfo.setText("Tìm thấy: "+filtered.size());

    }

    private void onTableSelection(){

        int row = table.getSelectedRow();

        if(row>=0) selectedEnrollment = tableModel.getEnrollmentAt(row);

    }

    private void clearSelection(){

        selectedEnrollment = null;

        table.clearSelection();

    }

    private static class EnrollmentTableModel extends AbstractTableModel {

        private final String[] columns = {
                "ID",
                "Học viên",
                "Lớp",
                "Ngày ghi danh",
                "Trạng thái",
                "Kết quả"
        };

        private List<Enrollment> data = new ArrayList<>();

        void setData(List<Enrollment> data){

            this.data = data!=null ? data : new ArrayList<>();

            fireTableDataChanged();

        }

        Enrollment getEnrollmentAt(int r){

            return data.get(r);

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
        public String getColumnName(int c){
            return columns[c];
        }

        @Override
        public Object getValueAt(int r,int c){

            Enrollment e = data.get(r);

            return switch(c){

                case 0 -> e.getId();
                case 1 -> e.getStudent().getFullName();
                case 2 -> e.getTeachingClass().getClassName();
                case 3 -> e.getEnrollmentDate();
                case 4 -> e.getStatus();
                case 5 -> e.getResult();
                default -> "";

            };

        }

    }

}