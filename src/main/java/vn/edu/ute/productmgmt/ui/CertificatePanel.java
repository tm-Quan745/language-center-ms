package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.*;
import vn.edu.ute.productmgmt.model.enums.UserRole;
import vn.edu.ute.productmgmt.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CertificatePanel extends JPanel {

    private final CertificateService certificateService;
    private final StudentService studentService;
    private final ClassService classService;

    private final UserRole currentRole;
    private final Long studentId;
    private final Long teacherId;

    private final JTable table = new JTable();
    private final CertificateTableModel tableModel = new CertificateTableModel();

    private final JTextField txtSearch = new JTextField(20);
    private final JLabel lblInfo = new JLabel(" ");

    private Certificate selected;


    public CertificatePanel(
            CertificateService certificateService,
            StudentService studentService,
            ClassService classService,
            UserAccount currentUser
    ) {

        this.certificateService = certificateService;
        this.studentService = studentService;
        this.classService = classService;

        this.currentRole = currentUser.getRole();

        this.studentId =
                currentUser.getStudent() != null ?
                        currentUser.getStudent().getId() : null;

        this.teacherId =
                currentUser.getTeacher() != null ?
                        currentUser.getTeacher().getId() : null;

        setLayout(new BorderLayout(20,20));
        setBorder(new EmptyBorder(10,10,10,10));
        setOpaque(false);

        buildUI();
        loadTable();

        table.getSelectionModel().addListSelectionListener(e->{
            if(e.getValueIsAdjusting()) return;
            onSelect();
        });
    }

    private CertificateFormDialog.CertificateFormData certificateToFormData(Certificate c) {

        CertificateFormDialog.CertificateFormData data =
                new CertificateFormDialog.CertificateFormData();

        if (c.getStudent() != null)
            data.setStudentId(c.getStudent().getId().toString());

        if (c.getTeachingClass() != null)
            data.setClassId(c.getTeachingClass().getId().toString());

        data.setCertName(c.getCertName());

        if (c.getIssueDate() != null)
            data.setIssueDate(c.getIssueDate().toString());

        data.setSerialNo(c.getSerialNo());

        return data;
    }

    private void buildUI(){

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        header.add(buildSearch(),BorderLayout.WEST);
        header.add(buildActionBar(),BorderLayout.EAST);

        add(header,BorderLayout.NORTH);

        table.setModel(tableModel);
        table.setRowHeight(42);

        add(new JScrollPane(table),BorderLayout.CENTER);

        lblInfo.setForeground(Color.GRAY);
        add(lblInfo,BorderLayout.SOUTH);
    }

    private JComponent buildSearch(){

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        p.setOpaque(false);

        txtSearch.putClientProperty(
                FlatClientProperties.PLACEHOLDER_TEXT,
                "Tìm chứng chỉ..."
        );

        txtSearch.setPreferredSize(new Dimension(250,36));

        JButton btn = new JButton("Tìm");

        btn.addActionListener(e->onSearch());

        p.add(txtSearch);
        p.add(btn);

        return p;
    }

    private JComponent buildActionBar(){

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        bar.setOpaque(false);

        JButton btnRefresh = new JButton("🔄 Tải lại");
        btnRefresh.addActionListener(e->loadTable());

        bar.add(btnRefresh);

        if(currentRole != UserRole.Student){

            JButton btnAdd = new JButton("➕ Thêm");
            JButton btnEdit = new JButton("📝 Sửa");

            btnAdd.addActionListener(e->onAdd());
            btnEdit.addActionListener(e->onEdit());

            bar.add(btnAdd);
            bar.add(btnEdit);

            if(currentRole == UserRole.Admin){

                JButton btnDelete = new JButton("🗑 Xóa");
                btnDelete.addActionListener(e->onDelete());
                bar.add(btnDelete);

            }
        }

        return bar;
    }

    private void loadTable(){

        try{

            List<Certificate> list;

            switch(currentRole){

                case Admin ->
                        list = certificateService.findAll();

                case Staff ->
                        list = certificateService.findAll();

                case Teacher ->
                        list = certificateService.findAll();

                case Student ->
                        list = certificateService.findByStudent(studentId);

                default ->
                        list = new ArrayList<>();
            }

            tableModel.setData(list);

            lblInfo.setText("Tổng: "+list.size()+" chứng chỉ");

            selected = null;

        }catch(Exception ex){

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải dữ liệu: "+ex.getMessage()
            );
        }
    }

    private void onSelect(){

        int row = table.getSelectedRow();

        if(row<0){
            selected=null;
            return;
        }

        selected = tableModel.get(row);
    }

    private void onSearch(){

        String kw = txtSearch.getText().trim().toLowerCase();

        if(kw.isEmpty()){
            loadTable();
            return;
        }

        List<Certificate> filtered = new ArrayList<>();

        for(Certificate c:tableModel.data){

            String name =
                    c.getCertName()!=null ?
                            c.getCertName().toLowerCase():"";

            String student =
                    c.getStudent()!=null ?
                            c.getStudent().getFullName().toLowerCase():"";

            if(name.contains(kw) || student.contains(kw)){
                filtered.add(c);
            }
        }

        tableModel.setData(filtered);
        lblInfo.setText("Tìm thấy "+filtered.size());
    }

    private void onAdd(){

        List<Student> students = studentService.findAll();
        List<TeachingClass> classes = classService.findAll();

        CertificateFormDialog dialog =
                new CertificateFormDialog(
                        SwingUtilities.getWindowAncestor(this),
                        null,
                        students,
                        classes
                );

        dialog.setVisible(true);

        if(!dialog.isSaved()) return;

        try{

            Certificate c = dialog.toCertificate();
            certificateService.create(c);

            loadTable();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi thêm: "+ex.getMessage()
            );
        }
    }

    private void onEdit(){

        if(selected==null){

            JOptionPane.showMessageDialog(
                    this,
                    "Chọn chứng chỉ cần sửa"
            );

            return;
        }

        List<Student> students = studentService.findAll();
        List<TeachingClass> classes = classService.findAll();

        CertificateFormDialog.CertificateFormData existing =
                certificateToFormData(selected);

        CertificateFormDialog dialog =
                new CertificateFormDialog(
                        SwingUtilities.getWindowAncestor(this),
                        existing,
                        students,
                        classes
                );

        dialog.setVisible(true);

        if(!dialog.isSaved()) return;

        try{

            Certificate c = dialog.toCertificate();
            c.setId(selected.getId());

            certificateService.update(c);

            loadTable();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi cập nhật: "+ex.getMessage()
            );
        }
    }

    private void onDelete(){

        if(selected==null) return;

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Xóa chứng chỉ?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if(ok!=JOptionPane.YES_OPTION) return;

        try{

            certificateService.delete(selected.getId());

            loadTable();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi xóa: "+ex.getMessage()
            );
        }
    }

    private static class CertificateTableModel extends AbstractTableModel{

        private final String[] columns={
                "Chứng chỉ",
                "Học viên",
                "Lớp",
                "Ngày cấp",
                "Serial"
        };

        private List<Certificate> data=new ArrayList<>();

        void setData(List<Certificate> d){
            data=d;
            fireTableDataChanged();
        }

        Certificate get(int r){
            return data.get(r);
        }

        public int getRowCount(){return data.size();}

        public int getColumnCount(){return columns.length;}

        public String getColumnName(int c){return columns[c];}

        public Object getValueAt(int r,int c){

            Certificate cert=data.get(r);

            return switch(c){

                case 0 -> cert.getCertName();
                case 1 -> cert.getStudent()!=null ?
                        cert.getStudent().getFullName():"";
                case 2 -> cert.getTeachingClass()!=null ?
                        cert.getTeachingClass().getClassName():"";
                case 3 -> cert.getIssueDate()!=null ?
                        cert.getIssueDate().toString():"";
                case 4 -> cert.getSerialNo();

                default -> "";
            };
        }
    }
}