package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.*;
import vn.edu.ute.productmgmt.model.enums.UserRole;
import vn.edu.ute.productmgmt.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ResultPanel extends JPanel {

    private final ResultService resultService;
    private final ClassService classService;
    private final AttendanceService attendanceService;

    private final UserRole currentRole;
    private final Long teacherId;
    private final Long studentId;

    private JComboBox<TeachingClass> cboClass;

    private final JTable table = new JTable();
    private final ResultTableModel tableModel = new ResultTableModel();

    private final JLabel lblInfo = new JLabel(" ");

    private List<Student> students = new ArrayList<>();
    private Map<Long, Result> existingResults = new HashMap<>();

    public ResultPanel(ResultService resultService,
                       ClassService classService,
                       AttendanceService attendanceService,
                       UserAccount currentUser) {

        this.resultService = resultService;
        this.classService = classService;
        this.attendanceService = attendanceService;

        this.currentRole = currentUser.getRole();

        this.teacherId =
                (currentRole == UserRole.Teacher && currentUser.getTeacher()!=null)
                        ? currentUser.getTeacher().getId()
                        : null;

        this.studentId =
                (currentRole == UserRole.Student && currentUser.getStudent()!=null)
                        ? currentUser.getStudent().getId()
                        : null;

        setLayout(new BorderLayout(20,20));
        setBorder(new EmptyBorder(10,10,10,10));
        setOpaque(false);

        buildUI();
        loadClasses();
    }

    // ================= UI =================

    private void buildUI(){

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        header.add(buildLeftPanel(),BorderLayout.WEST);
        header.add(buildActionBar(),BorderLayout.EAST);

        add(header,BorderLayout.NORTH);
        add(buildTableArea(),BorderLayout.CENTER);

        lblInfo.setForeground(Color.GRAY);
        add(lblInfo,BorderLayout.SOUTH);
    }

    private JComponent buildLeftPanel(){

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        panel.setOpaque(false);

        cboClass = new JComboBox<>();
        cboClass.setPreferredSize(new Dimension(250,36));

        JButton btnLoad = new JButton("Tải danh sách");
        btnLoad.addActionListener(e->loadStudents());

        panel.add(new JLabel("Lớp:"));
        panel.add(cboClass);
        panel.add(btnLoad);

        return panel;
    }

    private JComponent buildActionBar(){

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        bar.setOpaque(false);

        JButton btnRefresh = new JButton("🔄 Tải lại");
        btnRefresh.addActionListener(e->loadStudents());

        bar.add(btnRefresh);

        if(currentRole != UserRole.Student){

            JButton btnSave = createBtn("Lưu","#198754","💾");
            btnSave.addActionListener(e->saveResults());

            bar.add(btnSave);
        }

        return bar;
    }

    private JButton createBtn(String text,String color,String icon){

        JButton btn = new JButton(icon+" "+text);

        btn.putClientProperty(
                FlatClientProperties.STYLE,
                "background:"+color+";foreground:#fff;arc:10;borderWidth:0"
        );

        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private JComponent buildTableArea(){

        table.setModel(tableModel);
        table.setRowHeight(42);

        return new JScrollPane(table);
    }

    // ================= LOAD CLASSES =================

    private void loadClasses(){

        try{

            List<TeachingClass> classes;

            switch(currentRole){

                case Admin -> classes = classService.findAll();

                case Staff -> classes = classService.findAll();

                case Teacher -> classes = classService.findByTeacher(teacherId);

                case Student -> classes = classService.findByStudent(studentId);

                default -> classes = new ArrayList<>();
            }

            for(TeachingClass c : classes){
                cboClass.addItem(c);
            }

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,
                    "Lỗi tải lớp: "+ex.getMessage());
        }
    }

    // ================= LOAD STUDENTS =================

    private void loadStudents(){

        try{

            TeachingClass cls = (TeachingClass) cboClass.getSelectedItem();

            if(cls==null){
                lblInfo.setText("Chọn lớp học");
                return;
            }

            // ADMIN + TEACHER
            if(currentRole != UserRole.Student){

                students = attendanceService.getStudentsByClass(cls.getId());

            }
            // STUDENT
            else{

                Student me = attendanceService.getStudentById(studentId);

                students = new ArrayList<>();
                students.add(me);
            }

            List<Result> results = resultService.findResultsByClass(cls);

            existingResults = results.stream()
                    .filter(r -> r.getStudent()!=null)
                    .collect(Collectors.toMap(
                            r -> r.getStudent().getId(),
                            r -> r,
                            (a,b)->a
                    ));

            List<ResultData> data = new ArrayList<>();

            for(Student s : students){

                Result r = existingResults.get(s.getId());

                ResultData d = new ResultData();

                d.studentId = s.getId();
                d.studentName = s.getFullName();

                if(r!=null){
                    d.score = r.getScore()!=null ? r.getScore().toString() : "";
                    d.grade = r.getGrade();
                    d.comment = r.getComment();
                }

                data.add(d);
            }

            tableModel.setData(data);

            lblInfo.setText("Tổng "+students.size()+" học viên");

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,
                    "Lỗi tải danh sách: "+ex.getMessage());
        }
    }

    // ================= SAVE =================

    private void saveResults(){

        try{

            TeachingClass cls = (TeachingClass) cboClass.getSelectedItem();

            int saved = 0;

            for(int i=0;i<tableModel.data.size();i++){

                ResultData d = tableModel.data.get(i);

                if(d.score==null || d.score.isBlank()) continue;

                BigDecimal score = new BigDecimal(d.score);

                Result r = existingResults.get(d.studentId);

                if(r==null){
                    r = new Result();
                    r.setTeachingClass(cls);
                    r.setStudent(students.get(i));
                }

                r.setScore(score);
                r.setGrade(d.grade);
                r.setComment(d.comment);

                resultService.saveResult(r);

                saved++;
            }

            JOptionPane.showMessageDialog(this,"Đã lưu "+saved+" kết quả");

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,"Lỗi lưu: "+ex.getMessage());
        }
    }

    // ================= TABLE MODEL =================

    private class ResultTableModel extends AbstractTableModel{

        private final String[] columns = {
                "Tên học viên",
                "Điểm",
                "Xếp loại",
                "Nhận xét"
        };

        private List<ResultData> data = new ArrayList<>();

        void setData(List<ResultData> d){
            data=d;
            fireTableDataChanged();
        }

        public int getRowCount(){ return data.size(); }

        public int getColumnCount(){ return columns.length; }

        public String getColumnName(int c){ return columns[c]; }

        public Object getValueAt(int r,int c){

            ResultData d = data.get(r);

            return switch(c){
                case 0 -> d.studentName;
                case 1 -> d.score;
                case 2 -> d.grade;
                case 3 -> d.comment;
                default -> "";
            };
        }

        public boolean isCellEditable(int r,int c){

            return currentRole != UserRole.Student && c>=1;
        }

        public void setValueAt(Object v,int r,int c){

            ResultData d = data.get(r);

            switch(c){
                case 1 -> d.score = v.toString();
                case 2 -> d.grade = v.toString();
                case 3 -> d.comment = v.toString();
            }
        }
    }

    private static class ResultData{

        Long studentId;
        String studentName;

        String score="";
        String grade="";
        String comment="";
    }
}