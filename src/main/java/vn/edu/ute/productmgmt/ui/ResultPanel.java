package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.config.AppContext;
import vn.edu.ute.productmgmt.model.Result;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.TeachingClass;
import vn.edu.ute.productmgmt.service.AttendanceService;
import vn.edu.ute.productmgmt.service.ClassService;
import vn.edu.ute.productmgmt.service.ResultService;
import vn.edu.ute.productmgmt.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    public ResultPanel() {

        this.resultService = AppContext.resultService;
        this.classService = AppContext.classService;
        this.attendanceService = AppContext.attendanceService;

        initUI();
        loadClasses();
    }

    private void initUI() {

        setLayout(new BorderLayout());

        JPanel top = new JPanel();

        cboClass = new JComboBox<>();
        btnLoad = new JButton("Load");

        top.add(new JLabel("Lớp"));
        top.add(cboClass);
        top.add(btnLoad);

        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"Tên học viên", "Điểm"}, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };

        table = new JTable(model);

        add(new JScrollPane(table), BorderLayout.CENTER);

        btnSave = new JButton("Lưu");

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

        TeachingClass cls = (TeachingClass) cboClass.getSelectedItem();



        if (cls == null) return;

        students = attendanceService.getStudentsByClass(cls.getId());

        for (Student s : students) {

            model.addRow(new Object[]{
                    s.getFullName(),
                    ""
            });
        }
    }

    private void saveResults() {

        TeachingClass cls = (TeachingClass) cboClass.getSelectedItem();

        for (int i = 0; i < students.size(); i++) {

            Student s = students.get(i);

            Object scoreObj = model.getValueAt(i, 1);

            if (scoreObj == null || scoreObj.toString().isEmpty()) continue;

            BigDecimal score = new BigDecimal(scoreObj.toString());

            Result r = new Result();

            r.setStudent(s);
            r.setTeachingClass(cls);
            r.setScore(score);

            resultService.saveResult(r);
        }

        JOptionPane.showMessageDialog(this, "Đã lưu kết quả!");
    }
}