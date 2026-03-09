package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.TeachingClass;
import vn.edu.ute.productmgmt.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClassPanel extends JPanel {

    private final ClassService classService;
    private final CourseService courseService;
    private final TeacherService teacherService;
    private final RoomService roomService;
    private final BranchService branchService;

    private final JTextField txtSearch = new JTextField(18);
    private final JLabel lblInfo = new JLabel(" ");

    private final ClassTableModel tableModel = new ClassTableModel();
    private final JTable table = new JTable(tableModel);

    private TeachingClass selectedClass;

    public ClassPanel(ClassService classService,
                      CourseService courseService,
                      TeacherService teacherService,
                      RoomService roomService,
                      BranchService branchService) {

        this.classService = classService;
        this.courseService = courseService;
        this.teacherService = teacherService;
        this.roomService = roomService;
        this.branchService = branchService;

        setLayout(new BorderLayout(20,20));
        setOpaque(false);
        setBorder(new EmptyBorder(10,10,10,10));

        buildUI();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            onTableSelection();
        });

        loadTableAll();
    }

    private void buildUI() {

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Quản lý Lớp học");
        title.setFont(new Font("Segoe UI",Font.BOLD,22));

        header.add(title,BorderLayout.WEST);
        header.add(buildActionBar(),BorderLayout.EAST);

        add(header,BorderLayout.NORTH);

        add(buildTableArea(),BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);

        lblInfo.setForeground(Color.GRAY);
        lblInfo.setFont(new Font("Segoe UI",Font.ITALIC,13));

        statusBar.add(lblInfo,BorderLayout.WEST);

        add(statusBar,BorderLayout.SOUTH);
    }

    private JComponent buildActionBar() {

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        bar.setOpaque(false);

        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,"Tìm lớp học...");
        txtSearch.setPreferredSize(new Dimension(180,36));

        JButton btnSearch = new JButton("Tìm");
        btnSearch.setPreferredSize(new Dimension(70,36));

        JButton btnAdd = createBtn("Thêm","#0d6efd","➕ ");
        JButton btnEdit = createBtn("Sửa","#ffc107","📝 ");
        JButton btnDelete = createBtn("Xóa","#dc3545","🗑 ");
        JButton btnRefresh = new JButton("Tải lại");

        btnRefresh.setPreferredSize(new Dimension(90,36));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE,"arc:10");

        btnSearch.addActionListener(e -> onSearch());
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> loadTableAll());

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

    private void loadTableAll() {

        try {

            List<TeachingClass> list = classService.findAll();

            tableModel.setData(list);

            lblInfo.setText("Tổng số: " + list.size() + " lớp học");

            table.clearSelection();

            selectedClass = null;

        } catch (Exception ex){

            JOptionPane.showMessageDialog(this,"Lỗi tải dữ liệu: "+ex.getMessage());

        }
    }

    private void onSearch(){

        String kw = txtSearch.getText().trim().toLowerCase();

        List<TeachingClass> all = classService.findAll();

        List<TeachingClass> filtered = new ArrayList<>();

        for(TeachingClass tc : all){

            if(tc.getClassName()!=null &&
                    tc.getClassName().toLowerCase().contains(kw)){

                filtered.add(tc);

            }

        }

        tableModel.setData(filtered);

        lblInfo.setText("Tìm thấy: "+filtered.size()+" kết quả");

    }

    private void onTableSelection(){

        int row = table.getSelectedRow();

        if(row<0){

            selectedClass=null;

            return;

        }

        selectedClass = tableModel.getClassAt(row);

    }

    private void onAdd(){

        ClassFormDialog dialog = new ClassFormDialog(
                SwingUtilities.getWindowAncestor(this),
                null,
                courseService,
                teacherService,
                roomService,
                branchService
        );

        dialog.setVisible(true);

        if(dialog.isSaved()){

            try{

                classService.createClass(dialog.getResult());

                loadTableAll();

            }catch(Exception ex){

                JOptionPane.showMessageDialog(this,"Lỗi thêm lớp: "+ex.getMessage());

            }

        }

    }

    private void onEdit(){

        if(selectedClass==null){

            JOptionPane.showMessageDialog(this,"Vui lòng chọn lớp để sửa");

            return;

        }

        ClassFormDialog dialog = new ClassFormDialog(
                SwingUtilities.getWindowAncestor(this),
                selectedClass,
                courseService,
                teacherService,
                roomService,
                branchService
        );

        dialog.setVisible(true);

        if(dialog.isSaved()){

            try{

                classService.updateClass(dialog.getResult());

                loadTableAll();

            }catch(Exception ex){

                JOptionPane.showMessageDialog(this,"Lỗi cập nhật: "+ex.getMessage());

            }

        }

    }

    private void onDelete(){

        if(selectedClass==null) return;

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Xóa lớp "+selectedClass.getClassName()+" ?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if(ok==JOptionPane.YES_OPTION){

            try{

                classService.deleteClass(selectedClass.getId());

                loadTableAll();

            }catch(Exception ex){

                JOptionPane.showMessageDialog(this,"Lỗi xóa: "+ex.getMessage());

            }

        }

    }

    private static class ClassTableModel extends AbstractTableModel {

        private final String[] columns = {
                "Mã lớp",
                "Tên lớp",
                "Khóa học",
                "Giảng viên",
                "Phòng",
                "Trạng thái"
        };

        private List<TeachingClass> data = new ArrayList<>();

        void setData(List<TeachingClass> data){

            this.data=data;

            fireTableDataChanged();

        }

        TeachingClass getClassAt(int r){

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

            TeachingClass tc = data.get(r);

            return switch(c){

                case 0 -> tc.getId();

                case 1 -> tc.getClassName();

                case 2 -> tc.getCourse()!=null
                        ? tc.getCourse().getCourseName()
                        : "";

                case 3 -> tc.getTeacher()!=null
                        ? tc.getTeacher().getFullName()
                        : "";

                case 4 -> tc.getRoom()!=null
                        ? tc.getRoom().getRoomName()
                        : "";

                case 5 -> tc.getStatus();

                default -> "";

            };

        }

    }

}