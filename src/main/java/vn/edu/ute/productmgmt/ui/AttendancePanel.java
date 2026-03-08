package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.config.AppContext;
import vn.edu.ute.productmgmt.model.Attendance;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.TeachingClass;
import vn.edu.ute.productmgmt.model.enums.AttendanceStatus;
import vn.edu.ute.productmgmt.service.AttendanceService;
import vn.edu.ute.productmgmt.service.ClassService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendancePanel extends JPanel {

    private final AttendanceService attendanceService;
    private final ClassService classService;

    private JComboBox<TeachingClass> cboClass;
    private JButton btnLoad;
    private JButton btnSave;

    private JTable table;
    private DefaultTableModel model;

    private List<Student> students = new ArrayList<>();

    public AttendancePanel() {

        this.attendanceService = AppContext.attendanceService;
        this.classService = AppContext.classService;

        initUI();
        loadClasses();
    }

    private void initUI() {

        setLayout(new BorderLayout());

        JPanel top = new JPanel();

        cboClass = new JComboBox<>();
        btnLoad = new JButton("Load danh sách");

        top.add(new JLabel("Lớp"));
        top.add(cboClass);
        top.add(btnLoad);

        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"Tên học viên", "Có mặt"}, 0) {

            @Override
            public Class<?> getColumnClass(int columnIndex) {

                if (columnIndex == 1) return Boolean.class;
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };

        table = new JTable(model);

        add(new JScrollPane(table), BorderLayout.CENTER);

        btnSave = new JButton("Lưu điểm danh");

        JPanel bottom = new JPanel();
        bottom.add(btnSave);

        add(bottom, BorderLayout.SOUTH);

        btnLoad.addActionListener(e -> loadStudents());

        btnSave.addActionListener(e -> saveAttendance());
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

        Long classId = cls.getId();

        students = attendanceService.getStudentsByClass(classId);

        for (Student s : students) {

            model.addRow(new Object[]{
                    s.getFullName(),
                    true
            });
        }
    }

    private void saveAttendance() {

        TeachingClass cls = (TeachingClass) cboClass.getSelectedItem();

        if (cls == null) return;

        List<Attendance> list = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (int i = 0; i < students.size(); i++) {

            Student s = students.get(i);

            Boolean present = (Boolean) model.getValueAt(i, 1);

            Attendance a = new Attendance();

            a.setStudent(s);
            a.setTeachingClass(cls);
            a.setAttendDate(today);

            if (present) {
                a.setStatus(AttendanceStatus.Present);
            } else {
                a.setStatus(AttendanceStatus.Absent);
            }

            list.add(a);
        }

        attendanceService.saveAttendanceBatch(list);

        JOptionPane.showMessageDialog(this, "Đã lưu điểm danh!");
    }
}