package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Result;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.TeachingClass;
import vn.edu.ute.productmgmt.model.UserAccount;
import vn.edu.ute.productmgmt.model.enums.UserRole;
import vn.edu.ute.productmgmt.service.AttendanceService;
import vn.edu.ute.productmgmt.service.ClassService;
import vn.edu.ute.productmgmt.service.ResultService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Panel quản lý kết quả học tập, chuẩn hóa theo NotificationPanel
 */
public class ResultPanel extends JPanel {

    private final ResultService resultService;
    private final ClassService classService;
    private final AttendanceService attendanceService;

    private JComboBox<TeachingClass> cboClass;
    private final JLabel lblInfo = new JLabel(" ");
    private final ResultTableModel tableModel = new ResultTableModel();
    private final JTable table = new JTable(tableModel);

    private List<Student> students = new ArrayList<>();
    private Map<Long, Result> existingResultsByStudent = Map.of();
    /** Nếu là giáo viên thì chỉ cho chọn lớp mà giáo viên đó phụ trách. */
    private final Long currentTeacherId;

    public ResultPanel(ResultService resultService,
                       ClassService classService,
                       AttendanceService attendanceService,
                       UserAccount currentUser) {
        this.resultService = resultService;
        this.classService = classService;
        this.attendanceService = attendanceService;

        UserRole role = currentUser != null ? currentUser.getRole() : null;
        this.currentTeacherId = (role == UserRole.Teacher && currentUser.getTeacher() != null)
                ? currentUser.getTeacher().getId()
                : null;

        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        buildUI();
        loadClasses();
    }

    private void buildUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // LEFT: class selector + load
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        cboClass = new JComboBox<>();
        cboClass.setPreferredSize(new Dimension(250, 36));
        cboClass.putClientProperty(FlatClientProperties.STYLE, "arc:10");

        JButton btnLoad = new JButton("Tải danh sách");
        btnLoad.putClientProperty(FlatClientProperties.STYLE, "background:#0d6efd;foreground:#fff;arc:10;borderWidth:0");
        btnLoad.setPreferredSize(new Dimension(140, 36));
        btnLoad.addActionListener(e -> loadStudents());

        left.add(new JLabel("Lớp:"));
        left.add(cboClass);
        left.add(btnLoad);

        headerPanel.add(left, BorderLayout.WEST);

        // RIGHT: action bar (save, refresh)
        headerPanel.add(buildActionBar(), BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        add(buildTableArea(), BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);

        lblInfo.setForeground(Color.GRAY);
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 13));

        statusBar.add(lblInfo, BorderLayout.WEST);

        add(statusBar, BorderLayout.SOUTH);
    }

    private JComponent buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bar.setOpaque(false);

        JButton btnSave = createBtn("Lưu kết quả", "#198754", "💾 ");
        JButton btnRefresh = new JButton("🔄 Tải lại");

        btnRefresh.setPreferredSize(new Dimension(100, 36));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc:10");

        btnSave.addActionListener(e -> saveResults());
        btnRefresh.addActionListener(e -> {
            cboClass.setSelectedIndex(-1);
            tableModel.setData(new ArrayList<>());
        });

        bar.add(btnSave);
        bar.add(btnRefresh);

        return bar;
    }

    private JButton createBtn(String text, String color, String icon) {
        JButton btn = new JButton(icon + text);

        btn.setPreferredSize(new Dimension(150, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String fg = color.equals("#ffc107") ? "#000000" : "#ffffff";

        btn.putClientProperty(FlatClientProperties.STYLE,
                "background:" + color +
                        ";foreground:" + fg +
                        ";arc:10;borderWidth:0");

        return btn;
    }

    private JComponent buildTableArea() {
        JScrollPane scroll = new JScrollPane(table);

        scroll.putClientProperty(FlatClientProperties.STYLE, "arc:15");
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        table.setRowHeight(45);
        table.setShowVerticalLines(false);

        table.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        // Cho phép chỉnh sửa cột từ 1 trở đi
        table.setDefaultEditor(Object.class, new DefaultCellEditor(new JTextField()));

        return scroll;
    }

    private void loadClasses() {
        try {
            List<TeachingClass> classes = (currentTeacherId != null)
                    ? classService.findByTeacher(currentTeacherId)
                    : classService.findAll();
            for (TeachingClass c : classes) {
                cboClass.addItem(c);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách lớp: " + ex.getMessage());
        }
    }

    private void loadStudents() {
        try {
            tableModel.setData(new ArrayList<>());
            students.clear();

            TeachingClass cls = (TeachingClass) cboClass.getSelectedItem();
            if (cls == null) {
                lblInfo.setText("Vui lòng chọn lớp học");
                return;
            }

            students = attendanceService.getStudentsByClass(cls.getId());

            List<Result> existingResults = resultService.findResultsByClass(cls);
            existingResultsByStudent = existingResults.stream()
                    .filter(r -> r.getStudent() != null && r.getStudent().getId() != null)
                    .collect(Collectors.toMap(r -> r.getStudent().getId(), r -> r, (a, b) -> a));

            List<ResultData> dataList = new ArrayList<>();
            for (Student s : students) {
                Result r = existingResultsByStudent.get(s.getId());
                ResultData data = new ResultData();
                data.studentName = s.getFullName();
                data.studentId = s.getId();
                data.score = r != null && r.getScore() != null ? r.getScore().toPlainString() : "";
                data.grade = r != null && r.getGrade() != null ? r.getGrade() : "";
                data.comment = r != null && r.getComment() != null ? r.getComment() : "";
                dataList.add(data);
            }

            tableModel.setData(dataList);
            lblInfo.setText("Tổng số: " + students.size() + " học viên");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách: " + ex.getMessage());
        }
    }

    private void saveResults() {
        try {
            TeachingClass cls = (TeachingClass) cboClass.getSelectedItem();
            if (cls == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn lớp.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int saved = 0;
            for (int i = 0; i < students.size(); i++) {
                Student s = students.get(i);

                Object scoreObj = tableModel.getValueAt(i, 1);
                String gradeStr = tableModel.getValueAt(i, 2) != null ? tableModel.getValueAt(i, 2).toString().trim() : "";
                String commentStr = tableModel.getValueAt(i, 3) != null ? tableModel.getValueAt(i, 3).toString().trim() : "";

                if (scoreObj == null || scoreObj.toString().trim().isEmpty()) continue;

                BigDecimal score;
                try {
                    score = new BigDecimal(scoreObj.toString().trim());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Điểm không hợp lệ tại dòng " + (i + 1) + ": " + scoreObj, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Result r = existingResultsByStudent.get(s.getId());
                if (r == null) {
                    r = new Result();
                    r.setStudent(s);
                    r.setTeachingClass(cls);
                }

                r.setScore(score);
                r.setGrade(gradeStr.isEmpty() ? null : gradeStr);
                r.setComment(commentStr.isEmpty() ? null : commentStr);

                resultService.saveResult(r);
                saved++;
            }

            JOptionPane.showMessageDialog(this, "Đã lưu " + saved + " kết quả.");
            lblInfo.setText("Đã lưu: " + saved + " kết quả");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu: " + ex.getMessage());
        }
    }

    private static class ResultTableModel extends AbstractTableModel {

        private final String[] columns = {
                "Tên học viên",
                "Điểm",
                "Xếp loại",
                "Nhận xét"
        };

        private List<ResultData> data = new ArrayList<>();

        void setData(List<ResultData> data) {
            this.data = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
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
        public String getColumnName(int c) {
            return columns[c];
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (r < 0 || r >= data.size()) return "";
            ResultData d = data.get(r);

            return switch (c) {
                case 0 -> d.studentName;
                case 1 -> d.score;
                case 2 -> d.grade;
                case 3 -> d.comment;
                default -> "";
            };
        }

        @Override
        public void setValueAt(Object value, int r, int c) {
            if (r < 0 || r >= data.size()) return;
            ResultData d = data.get(r);

            switch (c) {
                case 1 -> d.score = value != null ? value.toString() : "";
                case 2 -> d.grade = value != null ? value.toString() : "";
                case 3 -> d.comment = value != null ? value.toString() : "";
            }

            fireTableCellUpdated(r, c);
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return c >= 1;
        }
    }

    private static class ResultData {
        String studentName;
        Long studentId;
        String score;
        String grade;
        String comment;
    }
}
