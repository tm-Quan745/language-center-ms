package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Attendance;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.TeachingClass;
import vn.edu.ute.productmgmt.model.enums.AttendanceStatus;
import vn.edu.ute.productmgmt.service.AttendanceService;
import vn.edu.ute.productmgmt.service.ClassService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

    public AttendancePanel(AttendanceService attendanceService, ClassService classService) {
        this.attendanceService = attendanceService;
        this.classService = classService;

        setLayout(new BorderLayout(20,20));
        setBorder(new EmptyBorder(10,10,10,10));
        setOpaque(false);

        buildUI();
        loadClasses();
    }

    private void buildUI(){

        add(createHeader(),BorderLayout.NORTH);

        add(createTableArea(),BorderLayout.CENTER);

        add(createBottomBar(),BorderLayout.SOUTH);

    }

    private JComponent createHeader(){

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Điểm danh học viên");
        title.setFont(new Font("Segoe UI",Font.BOLD,22));

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        actionBar.setOpaque(false);

        cboClass = new JComboBox<>();
        cboClass.setPreferredSize(new Dimension(220,36));

        btnLoad = new JButton("Tải danh sách");
        btnLoad.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnLoad.putClientProperty(
                FlatClientProperties.STYLE,
                "background:#0d6efd;foreground:#fff;arc:10;borderWidth:0"
        );

        actionBar.add(new JLabel("Lớp"));
        actionBar.add(cboClass);
        actionBar.add(btnLoad);

        header.add(title,BorderLayout.WEST);
        header.add(actionBar,BorderLayout.EAST);

        btnLoad.addActionListener(e->loadStudents());

        return header;
    }

    private JComponent createTableArea(){

        model = new DefaultTableModel(
                new Object[]{"Tên học viên","Có mặt"},0){

            @Override
            public Class<?> getColumnClass(int columnIndex){
                return columnIndex==1 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row,int column){
                return column==1;
            }

        };

        table = new JTable(model);

        table.setRowHeight(42);
        table.setShowVerticalLines(false);

        table.getTableHeader().setFont(
                new Font("Segoe UI Semibold",Font.PLAIN,14)
        );

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

        btnSave = new JButton("Lưu điểm danh");
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

        List<TeachingClass> classes = classService.findAll();

        for(TeachingClass c : classes){
            cboClass.addItem(c);
        }

    }

    private void loadStudents(){

        model.setRowCount(0);

        TeachingClass cls = (TeachingClass) cboClass.getSelectedItem();

        if(cls==null) return;

        students = attendanceService.getStudentsByClass(cls.getId());

        for(Student s : students){

            model.addRow(new Object[]{
                    s.getFullName(),
                    true
            });

        }

    }

    private void saveAttendance(){

        TeachingClass cls = (TeachingClass) cboClass.getSelectedItem();

        if(cls==null) return;

        List<Attendance> list = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for(int i=0;i<students.size();i++){

            Student s = students.get(i);

            Boolean present = (Boolean) model.getValueAt(i,1);

            Attendance a = new Attendance();

            a.setStudent(s);
            a.setTeachingClass(cls);
            a.setAttendDate(today);

            a.setStatus(
                    present
                            ? AttendanceStatus.Present
                            : AttendanceStatus.Absent
            );

            list.add(a);

        }

        attendanceService.saveAttendanceBatch(list);

        JOptionPane.showMessageDialog(
                this,
                "Đã lưu điểm danh thành công!"
        );
    }

}