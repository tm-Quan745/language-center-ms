package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Attendance;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.TeachingClass;
import vn.edu.ute.productmgmt.model.UserAccount;
import vn.edu.ute.productmgmt.model.enums.UserRole;
import vn.edu.ute.productmgmt.model.enums.AttendanceStatus;
import vn.edu.ute.productmgmt.service.AttendanceService;
import vn.edu.ute.productmgmt.service.ClassService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class AttendancePanel extends JPanel {

    private final AttendanceService attendanceService;
    private final ClassService classService;

    private JComboBox<TeachingClass> cboClass;
    private final JTextField txtSearch = new JTextField(18);
    private final JTextField txtDate = new JTextField(10);
    private final JLabel lblInfo = new JLabel(" ");

    private final AttendanceTableModel tableModel = new AttendanceTableModel();
    private final JTable table = new JTable(tableModel);

    /** Nếu là giáo viên thì chỉ cho chọn lớp mà giáo viên đó phụ trách. */
    private final Long currentTeacherId;

    public AttendancePanel(AttendanceService attendanceService,
                           ClassService classService,
                           UserAccount currentUser) {
        this.attendanceService = attendanceService;
        this.classService = classService;
        UserRole role = currentUser != null ? currentUser.getRole() : null;
        this.currentTeacherId = (role == UserRole.Teacher && currentUser.getTeacher() != null)
                ? currentUser.getTeacher().getId()
                : null;

        setLayout(new BorderLayout(20,20));
        setBorder(new EmptyBorder(10,10,10,10));
        setOpaque(false);

        buildUI();
        loadClasses();
    }

    private void buildUI(){

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        // LEFT: search theo tên lớp, giống StudentPanel style
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm lớp...");
        txtSearch.setPreferredSize(new Dimension(260, 36));
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new JLabel(" 🔍 "));
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(100, 36));
        btnSearch.putClientProperty(FlatClientProperties.STYLE,
                "background: #0d6efd; foreground: #ffffff; arc: 12");
        btnSearch.addActionListener(e -> onSearch());

        left.add(txtSearch);
        left.add(Box.createHorizontalStrut(10));
        left.add(btnSearch);

        header.add(left, BorderLayout.WEST);
        header.add(buildActionBar(), BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        add(createTableArea(), BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);
        lblInfo.setForeground(Color.GRAY);
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        statusBar.add(lblInfo, BorderLayout.WEST);
        statusBar.add(createBottomBar(), BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JComponent buildActionBar(){

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        actionBar.setOpaque(false);

        cboClass = new JComboBox<>();
        cboClass.setPreferredSize(new Dimension(220,36));

        txtDate.setPreferredSize(new Dimension(110, 36));
        txtDate.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ngày điểm danh (yyyy-MM-dd)");
        txtDate.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        txtDate.setText(LocalDate.now().toString());

        JButton btnLoad = new JButton("Tải danh sách");
        btnLoad.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLoad.putClientProperty(
                FlatClientProperties.STYLE,
                "background:#0d6efd;foreground:#fff;arc:10;borderWidth:0"
        );

        actionBar.add(new JLabel("Lớp"));
        actionBar.add(cboClass);
        actionBar.add(new JLabel("Ngày"));
        actionBar.add(txtDate);
        actionBar.add(btnLoad);

        btnLoad.addActionListener(e->loadStudents());

        return actionBar;
    }

    private JComponent createTableArea(){

        table.setRowHeight(42);
        table.setShowVerticalLines(false);
        table.getTableHeader().setFont(
                new Font("Segoe UI Semibold",Font.PLAIN,14)
        );

        // Editor cho cột trạng thái: combobox AttendanceStatus
        JComboBox<AttendanceStatus> statusEditor = new JComboBox<>(AttendanceStatus.values());
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(statusEditor));

        JScrollPane scroll = new JScrollPane(table);

        scroll.setBorder(
                BorderFactory.createLineBorder(new Color(230,230,230))
        );

        scroll.putClientProperty(
                FlatClientProperties.STYLE,
                "arc:15"
        );

        return scroll;
    }

    private JComponent createBottomBar(){

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);

        JButton btnSave = new JButton("Lưu điểm danh");
        btnSave.setPreferredSize(new Dimension(160,38));

        btnSave.putClientProperty(
                FlatClientProperties.STYLE,
                "background:#198754;foreground:#fff;arc:10;borderWidth:0"
        );

        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        bottom.add(btnSave);

        btnSave.addActionListener(e->saveAttendance());

        return bottom;
    }

    private void loadClasses(){

        List<TeachingClass> classes = (currentTeacherId != null)
                ? classService.findByTeacher(currentTeacherId)
                : classService.findAll();

        for(TeachingClass c : classes){
            cboClass.addItem(c);
        }

    }

    private void loadStudents(){

        TeachingClass cls = (TeachingClass) cboClass.getSelectedItem();

        if(cls==null) return;

        String dateStr = txtDate.getText() != null ? txtDate.getText().trim() : "";
        if (dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập ngày điểm danh (yyyy-MM-dd) trước khi tải danh sách.",
                    "Thiếu ngày",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate attendDate;
        try {
            attendDate = LocalDate.parse(dateStr);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Ngày điểm danh không hợp lệ. Định dạng đúng: yyyy-MM-dd.",
                    "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Student> students = attendanceService.getStudentsByClass(cls.getId());

        // Lấy các bản ghi điểm danh đã có cho lớp + ngày này để fill lại
        List<Attendance> existing = attendanceService.findByClassAndDate(cls, attendDate);
        java.util.Map<Long, Attendance> existingByStudentId = new java.util.HashMap<>();
        for (Attendance a : existing) {
            if (a.getStudent() != null && a.getStudent().getId() != null) {
                existingByStudentId.put(a.getStudent().getId(), a);
            }
        }

        List<AttendanceRow> rows = new ArrayList<>();
        for (Student s : students) {
            AttendanceRow row = new AttendanceRow();
            row.student = s;
            Attendance existed = existingByStudentId.get(s.getId());
            if (existed != null) {
                row.status = existed.getStatus();
                row.note = existed.getNote();
            } else {
                row.status = AttendanceStatus.Present;
                row.note = "";
            }
            rows.add(row);
        }

        tableModel.setData(rows);
        lblInfo.setText("Lớp " + cls.getClassName() + " - " + students.size()
                + " học viên, ngày " + attendDate);
    }

    private void saveAttendance(){

        TeachingClass cls = (TeachingClass) cboClass.getSelectedItem();

        if(cls==null) return;

        String dateStr = txtDate.getText() != null ? txtDate.getText().trim() : "";
        if (dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập ngày điểm danh (yyyy-MM-dd).",
                    "Thiếu ngày",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate attendDate;
        try {
            attendDate = LocalDate.parse(dateStr);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Ngày điểm danh không hợp lệ. Định dạng đúng: yyyy-MM-dd.",
                    "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Attendance> list = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            AttendanceRow row = tableModel.getRowAt(i);
            if (row.student == null) continue;

            Attendance a = new Attendance();
            a.setStudent(row.student);
            a.setTeachingClass(cls);
            a.setAttendDate(attendDate);
            a.setStatus(row.status != null ? row.status : AttendanceStatus.Present);
            a.setNote(row.note != null && !row.note.isBlank() ? row.note.trim() : null);

            list.add(a);
        }

        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không có bản ghi điểm danh nào để lưu.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        attendanceService.saveAttendanceBatch(list);

        JOptionPane.showMessageDialog(
                this,
                "Đã lưu điểm danh thành công!"
        );
    }

    private void onSearch(){
        String kw = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        if (kw.isEmpty()) {
            cboClass.setSelectedIndex(-1);
            return;
        }
        for (int i = 0; i < cboClass.getItemCount(); i++) {
            TeachingClass c = cboClass.getItemAt(i);
            if (c != null && c.getClassName() != null && c.getClassName().toLowerCase().contains(kw)) {
                cboClass.setSelectedIndex(i);
                loadStudents();
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Không tìm thấy lớp: " + kw);
    }

    /**
     * Dòng dữ liệu điểm danh cho bảng: sử dụng đủ trường chính của Attendance
     * (student, teachingClass, attendDate, status, note). id/createdAt do JPA tự sinh.
     */
    private static class AttendanceRow {
        Student student;
        AttendanceStatus status;
        String note;
    }

    private static class AttendanceTableModel extends AbstractTableModel {

        private final String[] columns = {
                "Mã học viên",
                "Họ tên",
                "Trạng thái",
                "Ghi chú"
        };

        private final List<AttendanceRow> data = new ArrayList<>();

        void setData(List<AttendanceRow> rows) {
            data.clear();
            if (rows != null) {
                data.addAll(rows);
            }
            fireTableDataChanged();
        }

        AttendanceRow getRowAt(int r) {
            return data.get(r);
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
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 0 -> Long.class;
                case 1, 3 -> String.class;
                case 2 -> AttendanceStatus.class;
                default -> Object.class;
            };
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            // Cho phép sửa trạng thái và ghi chú
            return columnIndex == 2 || columnIndex == 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            AttendanceRow row = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.student != null ? row.student.getId() : null;
                case 1 -> row.student != null ? row.student.getFullName() : "";
                case 2 -> row.status != null ? row.status : AttendanceStatus.Present;
                case 3 -> row.note != null ? row.note : "";
                default -> "";
            };
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            AttendanceRow row = data.get(rowIndex);
            if (columnIndex == 2 && aValue instanceof AttendanceStatus) {
                row.status = (AttendanceStatus) aValue;
            } else if (columnIndex == 3 && aValue != null) {
                row.note = aValue.toString();
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}
