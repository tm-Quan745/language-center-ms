package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.PlacementTest;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.service.PlacementTestService;
import vn.edu.ute.productmgmt.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PlacementPanel extends JPanel {

    private final PlacementTestService placementTestService;
    private final StudentService studentService;

    private final JLabel lblInfo = new JLabel(" ");
    private final JTextField txtSearch = new JTextField(18);

    private final PlacementTableModel tableModel = new PlacementTableModel();
    private final JTable table = new JTable(tableModel);

    private PlacementTest selectedPlacement;

    public PlacementPanel(PlacementTestService placementTestService,
                          StudentService studentService) {

        this.placementTestService = placementTestService;
        this.studentService = studentService;

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

    private void buildUI() {

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        left.setOpaque(false);

        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,"Tìm học viên hoặc ghi chú...");
        txtSearch.setPreferredSize(new Dimension(300,40));
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON,new JLabel(" 🔍 "));
        txtSearch.putClientProperty(FlatClientProperties.STYLE,"arc:12");

        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(100,40));
        btnSearch.putClientProperty(FlatClientProperties.STYLE,"background:#0d6efd;foreground:#ffffff;arc:12");
        btnSearch.addActionListener(e -> onSearch());

        left.add(txtSearch);
        left.add(Box.createHorizontalStrut(10));
        left.add(btnSearch);

        headerPanel.add(left,BorderLayout.WEST);
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

    private JComponent buildActionBar() {

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        bar.setOpaque(false);

        JButton btnAdd = createBtn("Thêm mới","#0d6efd"," ➕ ");
        JButton btnEdit = createBtn("Chỉnh sửa","#ffc107"," 📝 ");
        JButton btnDelete = createBtn("Xóa","#dc3545"," 🗑️ ");

        JButton btnRefresh = new JButton("🔄 Tải lại");
        btnRefresh.setPreferredSize(new Dimension(100,38));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE,"arc:10");

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

        table.getTableHeader().setFont(
                new Font("Segoe UI Semibold",Font.PLAIN,14));

        table.getTableHeader().setPreferredSize(
                new Dimension(0,45));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);

        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(2).setCellRenderer(center);
        table.getColumnModel().getColumn(3).setCellRenderer(center);

        return scroll;
    }

    private void loadTable(){

        try{
            List<PlacementTest> list = placementTestService.findAll();
            tableModel.setData(list);

            lblInfo.setText("Hệ thống có " + list.size() + " bài kiểm tra xếp lớp.");

            clearSelection();

        }catch(Exception ex){

            lblInfo.setText("Lỗi: " + ex.getMessage());
        }
    }

    private void onTableSelection(){

        int row = table.getSelectedRow();

        if(row >= 0){

            int modelRow = table.convertRowIndexToModel(row);
            selectedPlacement = tableModel.getPlacementAt(modelRow);

        }else{
            selectedPlacement = null;
        }
    }

    private void onAdd(){

        List<Student> students = studentService.findAll();

        PlacementFormDialog dialog = new PlacementFormDialog(
                SwingUtilities.getWindowAncestor(this),
                null,
                students
        );

        dialog.setVisible(true);

        if(!dialog.isSaved()) return;

        PlacementFormDialog.PlacementFormData data = dialog.getResult();

        try{

            LocalDate testDate = parseDate(data.getTestDate());
            if(testDate == null) testDate = LocalDate.now();

            BigDecimal score =
                    data.getScore()!=null && !data.getScore().isEmpty()
                            ? new BigDecimal(data.getScore())
                            : null;

            placementTestService.createPlacementTest(
                    Long.parseLong(data.getStudentId()),
                    testDate,
                    score,
                    data.getSuggestedLevel(),
                    data.getNote()
            );

            JOptionPane.showMessageDialog(this,"Đã thêm bài kiểm tra.");

            loadTable();

        }catch(Exception ex){
            showError(ex.getMessage());
        }
    }

    private void onEdit(){

        if(selectedPlacement == null){
            showWarning("Chọn bài kiểm tra cần sửa!");
            return;
        }

        List<Student> students = studentService.findAll();

        PlacementFormDialog dialog = new PlacementFormDialog(
                SwingUtilities.getWindowAncestor(this),
                placementToFormData(selectedPlacement),
                students
        );

        dialog.setVisible(true);

        if(!dialog.isSaved()) return;

        PlacementFormDialog.PlacementFormData data = dialog.getResult();

        try{

            LocalDate testDate = parseDate(data.getTestDate());
            if(testDate == null) testDate = LocalDate.now();

            BigDecimal score =
                    data.getScore()!=null && !data.getScore().isEmpty()
                            ? new BigDecimal(data.getScore())
                            : null;

            placementTestService.updatePlacementTest(
                    selectedPlacement.getId(),
                    Long.parseLong(data.getStudentId()),
                    testDate,
                    score,
                    data.getSuggestedLevel(),
                    data.getNote()
            );

            loadTable();
            clearSelection();

        }catch(Exception ex){
            showError(ex.getMessage());
        }
    }

    private void onDelete(){

        if(selectedPlacement == null){
            showWarning("Chọn bài kiểm tra cần xóa!");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Xóa bài kiểm tra này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if(ok == JOptionPane.YES_OPTION){

            try{

                placementTestService.delete(selectedPlacement.getId());
                loadTable();

            }catch(Exception ex){
                showError(ex.getMessage());
            }
        }
    }

    private void onSearch(){

        String kw = txtSearch.getText()==null ? "" : txtSearch.getText().toLowerCase();

        if(kw.isEmpty()){
            loadTable();
            return;
        }

        List<PlacementTest> all = placementTestService.findAll();
        List<PlacementTest> filtered = new ArrayList<>();

        for(PlacementTest p : all){

            String name = p.getStudent()!=null ?
                    p.getStudent().getFullName().toLowerCase() : "";

            String note = p.getNote()!=null ?
                    p.getNote().toLowerCase() : "";

            if(name.contains(kw) || note.contains(kw))
                filtered.add(p);
        }

        tableModel.setData(filtered);

        lblInfo.setText("Tìm thấy: " + filtered.size() + " kết quả");
    }

    private void showError(String msg){
        JOptionPane.showMessageDialog(this,msg,"Lỗi",JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String msg){
        JOptionPane.showMessageDialog(this,msg,"Cảnh báo",JOptionPane.WARNING_MESSAGE);
    }

    private PlacementFormDialog.PlacementFormData placementToFormData(PlacementTest pt){

        PlacementFormDialog.PlacementFormData data =
                new PlacementFormDialog.PlacementFormData();

        data.setStudentId(pt.getStudent()!=null ? pt.getStudent().getId().toString() : "");
        data.setTestDate(pt.getTestDate()!=null ? pt.getTestDate().toString() : "");
        data.setScore(pt.getScore()!=null ? pt.getScore().toPlainString() : "");
        data.setSuggestedLevel(pt.getSuggestedLevel());
        data.setNote(pt.getNote());

        return data;
    }

    private LocalDate parseDate(String value){

        if(value == null || value.isEmpty()) return null;

        try{
            return LocalDate.parse(value);
        }catch(Exception e){
            return null;
        }
    }

    private void clearSelection(){
        selectedPlacement = null;
        table.clearSelection();
    }

    private static class PlacementTableModel extends AbstractTableModel {

        private final String[] columns =
                {"ID","Học viên","Ngày kiểm tra","Điểm","Cấp độ","Ghi chú"};

        private List<PlacementTest> data = new ArrayList<>();

        void setData(List<PlacementTest> list){
            data = list!=null ? list : new ArrayList<>();
            fireTableDataChanged();
        }

        PlacementTest getPlacementAt(int row){
            return data.get(row);
        }

        public int getRowCount(){ return data.size(); }
        public int getColumnCount(){ return columns.length; }
        public String getColumnName(int col){ return columns[col]; }

        public Object getValueAt(int row,int col){

            PlacementTest p = data.get(row);

            return switch(col){
                case 0 -> p.getId();
                case 1 -> p.getStudent()!=null ? p.getStudent().getFullName() : "";
                case 2 -> p.getTestDate()!=null ? p.getTestDate().toString() : "";
                case 3 -> p.getScore()!=null ? p.getScore().toPlainString() : "";
                case 4 -> p.getSuggestedLevel()!=null ? p.getSuggestedLevel().name() : "";
                case 5 -> p.getNote()!=null ? p.getNote() : "";
                default -> "";
            };
        }
    }
}