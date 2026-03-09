package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Result;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.TeachingClass;
import vn.edu.ute.productmgmt.service.AttendanceService;
import vn.edu.ute.productmgmt.service.ClassService;
import vn.edu.ute.productmgmt.service.ResultService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResultPanel extends JPanel {

    private final ResultService resultService;
    private final ClassService classService;
    private final AttendanceService attendanceService;

    private JComboBox<TeachingClass> cboClass;
    private JButton btnLoad;
    private JButton btnSave;

    private JTable table;
    private DefaultTableModel model;

    private List<Student> students = new ArrayList<>();
    /** Map studentId -> Result (khi load có kết quả sẵn) */
    private Map<Long, Result> existingResultsByStudent = Map.of();

    public ResultPanel(ResultService resultService,
                       ClassService classService,
                       AttendanceService attendanceService) {
        this.resultService = resultService;
        this.classService = classService;
        this.attendanceService = attendanceService;

        initUI();
        loadClasses();
    }

    private void initUI() {

        setLayout(new BorderLayout());

        JPanel top = new JPanel();

        cboClass = new JComboBox<>();
        btnLoad = new JButton("Tải danh sách");

        top.add(new JLabel("Lớp:"));
        top.add(cboClass);
        top.add(btnLoad);

        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"Tên học viên", "Điểm", "Xếp loại", "Nhận xét"}, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 1;
            }
        };

        table = new JTable(model);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);

        add(new JScrollPane(table), BorderLayout.CENTER);

        btnSave = new JButton("Lưu kết quả");

        JPanel bottom = new JPanel();
        bottom.add(btnSave);

        add(bottom, BorderLayout.SOUTH);

        btnLoad.addActionListener(e -> loadStudents());

        btnSave.addActionListener(e -> saveResults());
    }

    private void loadClasses() {

        List<TeachingClass> classes = classService.findAll();

        for (TeachingClass c : classes) {
            cboClass.addItem(c);
        }
    }

    private void loadStudents() {

        model.setRowCount(0);
        students.clear();

        TeachingClass cls = (TeachingClass) cboClass.getSelectedItem();
        if (cls == null) return;

        students = attendanceService.getStudentsByClass(cls.getId());

        List<Result> existingResults = resultService.findResultsByClass(cls);
        existingResultsByStudent = existingResults.stream()
                .filter(r -> r.getStudent() != null && r.getStudent().getId() != null)
                .collect(Collectors.toMap(r -> r.getStudent().getId(), r -> r, (a, b) -> a));

        for (Student s : students) {
            Result r = existingResultsByStudent.get(s.getId());
            String score = r != null && r.getScore() != null ? r.getScore().toPlainString() : "";
            String grade = r != null && r.getGrade() != null ? r.getGrade() : "";
            String comment = r != null && r.getComment() != null ? r.getComment() : "";

            model.addRow(new Object[]{
                    s.getFullName(),
                    score,
                    grade,
                    comment
            });
        }
    }

    private void saveResults() {

        TeachingClass cls = (TeachingClass) cboClass.getSelectedItem();
        if (cls == null) {
            JOptionPane.showMessageDialog(this, "Chọn lớp.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int saved = 0;
        for (int i = 0; i < students.size(); i++) {

            Student s = students.get(i);

            Object scoreObj = model.getValueAt(i, 1);
            String gradeStr = model.getValueAt(i, 2) != null ? model.getValueAt(i, 2).toString().trim() : "";
            String commentStr = model.getValueAt(i, 3) != null ? model.getValueAt(i, 3).toString().trim() : "";

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
        loadStudents();
    }
}
