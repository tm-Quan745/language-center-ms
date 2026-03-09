package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Course;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.model.enums.CourseLevel;
import vn.edu.ute.productmgmt.model.enums.DurationUnit;
import vn.edu.ute.productmgmt.service.CourseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CoursePanel extends JPanel {

    private final CourseService courseService;

    private final JLabel lblInfo = new JLabel(" ");

    private final CourseTableModel tableModel = new CourseTableModel();
    private final JTable table = new JTable(tableModel);

    private Course selectedCourse;

    public CoursePanel(CourseService courseService) {

        this.courseService = courseService;

        setLayout(new BorderLayout(20,20));
        setOpaque(false);
        setBorder(new EmptyBorder(0,0,0,0));

        buildUI();

        table.getSelectionModel().addListSelectionListener(e -> {
            if(e.getValueIsAdjusting()) return;
            onTableSelection();
        });

        loadTable();
    }

    private void buildUI(){

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý Khóa học");
        lblTitle.setFont(new Font("Segoe UI",Font.BOLD,22));

        headerPanel.add(lblTitle,BorderLayout.WEST);
        headerPanel.add(buildActionBar(),BorderLayout.EAST);

        add(headerPanel,BorderLayout.NORTH);

        add(buildTableArea(),BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);

        lblInfo.setForeground(Color.GRAY);
        lblInfo.setFont(new Font("Segoe UI",Font.ITALIC,13));

        statusBar.add(lblInfo,BorderLayout.WEST);

        add(statusBar,BorderLayout.SOUTH);
    }

    private JComponent buildActionBar(){

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        bar.setOpaque(false);

        JButton btnAdd = createBtn("Thêm","#0d6efd"," ➕ ");
        JButton btnSave = createBtn("Chỉnh sửa","#ffc107"," 📝 ");
        JButton btnDelete = createBtn("Xóa","#dc3545"," 🗑 ");
        JButton btnRefresh = new JButton("Tải lại");

        btnRefresh.setPreferredSize(new Dimension(100,38));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE,"arc:10");

        btnAdd.addActionListener(e->onAdd());
        btnSave.addActionListener(e->onSave());
        btnDelete.addActionListener(e->onDelete());
        btnRefresh.addActionListener(e->loadTable());

        bar.add(btnRefresh);
        bar.add(btnAdd);
        bar.add(btnSave);
        bar.add(btnDelete);

        return bar;
    }

    private JButton createBtn(String text,String colorHex,String icon){

        JButton btn = new JButton(icon + text);

        btn.setPreferredSize(new Dimension(120,38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String fg = colorHex.equals("#ffc107") ? "#000000" : "#ffffff";

        btn.putClientProperty(
                FlatClientProperties.STYLE,
                "background:" + colorHex +
                        ";foreground:" + fg +
                        ";arc:10;borderWidth:0"
        );

        return btn;
    }

    private JComponent buildTableArea(){

        JScrollPane scroll = new JScrollPane(table);

        scroll.setBorder(BorderFactory.createLineBorder(new Color(230,230,230)));
        scroll.putClientProperty(FlatClientProperties.STYLE,"arc:15");

        table.setRowHeight(45);
        table.setShowVerticalLines(false);

        table.getTableHeader().setFont(new Font("Segoe UI Semibold",Font.PLAIN,14));
        table.getTableHeader().setPreferredSize(new Dimension(0,45));

        return scroll;
    }

    private void loadTable(){

        try{

            List<Course> list = courseService.findAll();

            tableModel.setData(list);

            lblInfo.setText("Tổng số: " + list.size() + " khóa học.");

            clearSelection();

        }catch(Exception ex){

            lblInfo.setText("Lỗi: " + ex.getMessage());

            JOptionPane.showMessageDialog(
                    this,
                    "Không tải được danh sách: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void onTableSelection(){

        int row = table.getSelectedRow();

        if(row < 0){

            selectedCourse = null;
            return;
        }

        selectedCourse = tableModel.getCourseAt(row);
    }

    private void onAdd(){

        CourseFormDialog dialog =
                new CourseFormDialog(
                        SwingUtilities.getWindowAncestor(this),
                        null
                );

        dialog.setVisible(true);

        if(!dialog.isSaved()) return;

        CourseFormDialog.CourseFormData data = dialog.getResult();

        Course c = formDataToCourse(data,null);

        if(c == null) return;

        try{

            courseService.create(c);

            JOptionPane.showMessageDialog(
                    this,
                    "Đã thêm khóa học.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadTable();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void onSave(){

        if(selectedCourse == null){

            JOptionPane.showMessageDialog(
                    this,
                    "Chọn một khóa học để sửa.",
                    "Chưa chọn",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        CourseFormDialog.CourseFormData existing = courseToFormData(selectedCourse);

        CourseFormDialog dialog =
                new CourseFormDialog(
                        SwingUtilities.getWindowAncestor(this),
                        existing
                );

        dialog.setVisible(true);

        if(!dialog.isSaved()) return;

        CourseFormDialog.CourseFormData data = dialog.getResult();

        Course c = formDataToCourse(data,selectedCourse.getId());

        if(c == null) return;

        try{

            courseService.update(c);

            JOptionPane.showMessageDialog(
                    this,
                    "Đã cập nhật khóa học.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadTable();

            clearSelection();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void onDelete(){

        if(selectedCourse == null){

            JOptionPane.showMessageDialog(
                    this,
                    "Chọn một khóa học để xóa.",
                    "Chưa chọn",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa khóa học này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if(ok != JOptionPane.YES_OPTION) return;

        try{

            courseService.delete(selectedCourse.getId());

            JOptionPane.showMessageDialog(
                    this,
                    "Đã xóa khóa học.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadTable();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private CourseFormDialog.CourseFormData courseToFormData(Course c){

        CourseFormDialog.CourseFormData data =
                new CourseFormDialog.CourseFormData();

        data.setName(c.getCourseName());
        data.setDescription(c.getDescription());
        data.setLevel(c.getLevel());
        data.setDuration(c.getDuration()!=null?c.getDuration().toString():"");
        data.setDurationUnit(c.getDurationUnit());
        data.setFee(c.getFee()!=null?c.getFee().toPlainString():"");
        data.setStatus(c.getStatus());

        return data;
    }

    private Course formDataToCourse(
            CourseFormDialog.CourseFormData data,
            Long keepId
    ){

        String name = data.getName()!=null?data.getName().trim():"";

        if(name.isEmpty()){

            JOptionPane.showMessageDialog(
                    this,
                    "Tên khóa học không được để trống.",
                    "Lỗi",
                    JOptionPane.WARNING_MESSAGE
            );

            return null;
        }

        BigDecimal fee = BigDecimal.ZERO;

        if(data.getFee()!=null && !data.getFee().trim().isEmpty()){

            try{
                fee = new BigDecimal(data.getFee());
            }catch(Exception e){

                JOptionPane.showMessageDialog(
                        this,
                        "Mức phí không hợp lệ.",
                        "Lỗi",
                        JOptionPane.WARNING_MESSAGE
                );

                return null;
            }
        }

        Integer duration = null;

        if(data.getDuration()!=null && !data.getDuration().trim().isEmpty()){

            try{
                duration = Integer.parseInt(data.getDuration());
            }catch(Exception ignored){}
        }

        Course c = new Course();

        if(keepId!=null) c.setId(keepId);

        c.setCourseName(name);
        c.setDescription(data.getDescription());
        c.setLevel(data.getLevel());
        c.setDuration(duration);
        c.setDurationUnit(data.getDurationUnit()!=null?data.getDurationUnit(): DurationUnit.Week);
        c.setFee(fee);
        c.setStatus(data.getStatus());

        return c;
    }

    private void clearSelection(){

        selectedCourse = null;

        table.clearSelection();
    }

    private static class CourseTableModel extends AbstractTableModel{

        private final String[] columns = {
                "Tên khóa học",
                "Mức phí",
                "Thời lượng",
                "Mức độ",
                "Trạng thái",
                "Mô tả"
        };

        private List<Course> data = new ArrayList<>();

        void setData(List<Course> data){

            this.data = data!=null?data:new ArrayList<>();

            fireTableDataChanged();
        }

        Course getCourseAt(int row){

            if(row<0 || row>=data.size()) return null;

            return data.get(row);
        }

        public int getRowCount(){ return data.size(); }

        public int getColumnCount(){ return columns.length; }

        public String getColumnName(int col){ return columns[col]; }

        public Object getValueAt(int row,int col){

            Course c = data.get(row);

            switch(col){

                case 0: return c.getCourseName();

                case 1: return c.getFee()!=null?c.getFee().toPlainString():"";

                case 2:
                    return c.getDuration()!=null
                            ? c.getDuration()+" "+(c.getDurationUnit()!=null?c.getDurationUnit().name():"")
                            : "";

                case 3: return c.getLevel()!=null?c.getLevel().name():"";

                case 4: return c.getStatus()!=null?c.getStatus().name():"";

                case 5: return c.getDescription()!=null?c.getDescription():"";

            }

            return "";
        }
    }
}