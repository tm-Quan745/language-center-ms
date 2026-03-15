package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Schedule;
import vn.edu.ute.productmgmt.model.UserAccount;
import vn.edu.ute.productmgmt.model.enums.UserRole;
import vn.edu.ute.productmgmt.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SchedulePanel extends JPanel {

    private final ScheduleService scheduleService;
    private final ClassService classService;
    private final RoomService roomService;

    private final UserRole currentUserRole;
    private final Long currentTeacherId;
    private final Long currentStudentId;

    private final JTextField txtSearch = new JTextField(18);
    private final JLabel lblInfo = new JLabel(" ");

    private final ScheduleTableModel tableModel = new ScheduleTableModel();
    private final JTable table = new JTable(tableModel);

    private List<Schedule> allSchedules = new ArrayList<>();
    private Schedule selectedSchedule;

    public SchedulePanel(
            ScheduleService scheduleService,
            ClassService classService,
            RoomService roomService,
            UserAccount currentUser
    ) {

        this.scheduleService = scheduleService;
        this.classService = classService;
        this.roomService = roomService;

        this.currentUserRole = currentUser != null ? currentUser.getRole() : null;

        this.currentTeacherId =
                (currentUserRole == UserRole.Teacher && currentUser.getTeacher() != null)
                        ? currentUser.getTeacher().getId()
                        : null;

        this.currentStudentId =
                (currentUserRole == UserRole.Student && currentUser.getStudent() != null)
                        ? currentUser.getStudent().getId()
                        : null;

        setLayout(new BorderLayout(20,20));
        setOpaque(false);
        setBorder(new EmptyBorder(10,10,10,10));

        buildUI();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onTableSelection();
        });

        loadSchedules();
    }

    // ===============================
    // UI
    // ===============================

    private void buildUI(){

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        header.add(buildSearchBar(), BorderLayout.WEST);
        header.add(buildActionBar(), BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);

        JPanel status = new JPanel(new BorderLayout());
        status.setOpaque(false);

        lblInfo.setForeground(Color.GRAY);
        lblInfo.setFont(new Font("Segoe UI",Font.ITALIC,13));

        status.add(lblInfo,BorderLayout.WEST);

        add(status,BorderLayout.SOUTH);
    }

    private JComponent buildSearchBar(){

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        panel.setOpaque(false);

        txtSearch.putClientProperty(
                FlatClientProperties.PLACEHOLDER_TEXT,
                "Tìm lịch học..."
        );

        txtSearch.setPreferredSize(new Dimension(280,40));

        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(100,40));

        btnSearch.addActionListener(e -> onSearch());

        panel.add(txtSearch);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(btnSearch);

        return panel;
    }

    private JComponent buildActionBar(){

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        bar.setOpaque(false);

        JButton btnRefresh = new JButton("🔄 Tải lại");
        btnRefresh.setPreferredSize(new Dimension(110,36));

        btnRefresh.addActionListener(e -> loadSchedules());

        bar.add(btnRefresh);

        // Chỉ ADMIN được CRUD
        if(currentUserRole == UserRole.Admin){

            JButton btnAdd = createBtn("Thêm","#0d6efd","➕ ");
            JButton btnEdit = createBtn("Sửa","#ffc107","📝 ");
            JButton btnDelete = createBtn("Xóa","#dc3545","🗑 ");

            btnAdd.addActionListener(e -> onAdd());
            btnEdit.addActionListener(e -> onEdit());
            btnDelete.addActionListener(e -> onDelete());

            bar.add(btnAdd);
            bar.add(btnEdit);
            bar.add(btnDelete);
        }

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

        table.setRowHeight(45);
        table.setShowVerticalLines(false);

        return scroll;
    }

    // ===============================
    // LOAD DATA
    // ===============================

    private void loadSchedules() {

        try {

            if (currentUserRole == UserRole.Admin) {

                allSchedules = scheduleService.findAll();

            }
            else if (currentUserRole == UserRole.Teacher && currentTeacherId != null) {

                allSchedules = scheduleService.findByTeacher(currentTeacherId);

            }
            else if (currentUserRole == UserRole.Student && currentStudentId != null) {

                allSchedules = scheduleService.findByStudent(currentStudentId);

            }
            else {

                allSchedules = new ArrayList<>();

            }

            tableModel.setData(allSchedules);

            lblInfo.setText("Tổng: " + allSchedules.size() + " lịch học");

            table.clearSelection();
            selectedSchedule = null;

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(this,
                    "Lỗi tải dữ liệu: " + ex.getMessage());

        }
    }

    // ===============================
    // SEARCH
    // ===============================

    private void onSearch(){

        String kw = txtSearch.getText().trim().toLowerCase();

        if(kw.isEmpty()){

            tableModel.setData(allSchedules);
            lblInfo.setText("Tổng: "+allSchedules.size());
            return;
        }

        List<Schedule> filtered = allSchedules.stream()
                .filter(s ->
                        s.getTeachingClass()!=null &&
                                s.getTeachingClass()
                                        .getClassName()
                                        .toLowerCase()
                                        .contains(kw))
                .toList();

        tableModel.setData(filtered);

        lblInfo.setText("Tìm thấy: "+filtered.size());
    }

    // ===============================
    // TABLE
    // ===============================

    private void onTableSelection(){

        int row = table.getSelectedRow();

        if(row < 0){

            selectedSchedule = null;
            return;
        }

        selectedSchedule = tableModel.getScheduleAt(row);
    }

    // ===============================
    // CRUD (Admin only)
    // ===============================

    private void onAdd(){

        ScheduleFormDialog dialog =
                new ScheduleFormDialog(
                        SwingUtilities.getWindowAncestor(this),
                        null,
                        classService,
                        roomService
                );

        dialog.setVisible(true);

        if(dialog.isSaved()){

            try{

                scheduleService.createSchedule(dialog.getResult());
                loadSchedules();

            }catch(Exception ex){

                JOptionPane.showMessageDialog(this,
                        "Lỗi thêm: "+ex.getMessage());

            }

        }

    }

    private void onEdit(){

        if(selectedSchedule == null){

            JOptionPane.showMessageDialog(this,
                    "Chọn lịch học để sửa");
            return;
        }

        ScheduleFormDialog dialog =
                new ScheduleFormDialog(
                        SwingUtilities.getWindowAncestor(this),
                        selectedSchedule,
                        classService,
                        roomService
                );

        dialog.setVisible(true);

        if(dialog.isSaved()){

            try{

                scheduleService.updateSchedule(dialog.getResult());
                loadSchedules();

            }catch(Exception ex){

                JOptionPane.showMessageDialog(this,
                        "Lỗi cập nhật: "+ex.getMessage());

            }

        }

    }

    private void onDelete(){

        if(selectedSchedule == null) return;

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Xóa lịch học?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if(ok == JOptionPane.YES_OPTION){

            try{

                scheduleService.deleteSchedule(selectedSchedule.getId());
                loadSchedules();

            }catch(Exception ex){

                JOptionPane.showMessageDialog(this,
                        "Lỗi xóa: "+ex.getMessage());

            }

        }

    }

    // ===============================
    // TABLE MODEL
    // ===============================

    private static class ScheduleTableModel extends AbstractTableModel{

        private final String[] columns = {
                "Mã lịch",
                "Lớp học",
                "Phòng",
                "Ngày học",
                "Thời gian"
        };

        private List<Schedule> data = new ArrayList<>();

        void setData(List<Schedule> list){

            data = list != null ? list : new ArrayList<>();
            fireTableDataChanged();
        }

        Schedule getScheduleAt(int r){

            return (r>=0 && r<data.size()) ? data.get(r) : null;
        }

        public int getRowCount(){
            return data.size();
        }

        public int getColumnCount(){
            return columns.length;
        }

        public String getColumnName(int c){
            return columns[c];
        }

        public Object getValueAt(int r,int c){

            Schedule s = data.get(r);

            return switch(c){

                case 0 -> s.getId();

                case 1 -> s.getTeachingClass()!=null
                        ? s.getTeachingClass().getClassName()
                        : "";

                case 2 -> s.getRoom()!=null
                        ? s.getRoom().getRoomName()
                        : "";

                case 3 -> s.getStudyDate();

                case 4 -> s.getStartTime()+" - "+s.getEndTime();

                default -> "";
            };
        }
    }
}