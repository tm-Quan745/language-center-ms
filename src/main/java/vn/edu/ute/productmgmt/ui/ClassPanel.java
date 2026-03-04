package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.config.AppContext;
import vn.edu.ute.productmgmt.model.*;
import vn.edu.ute.productmgmt.service.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ClassPanel extends JPanel {

    private final ClassService classService;
    private final CourseService courseService;
    private final TeacherService teacherService;
    private final RoomService roomService;

    private JComboBox<Course> cboCourse;
    private JComboBox<Teacher> cboTeacher;
    private JComboBox<Room> cboRoom;

    private JTextField txtMaxStudent;
    private JTextField txtStartDate;
    private JTextField txtEndDate;

    private JTable table;
    private DefaultTableModel tableModel;

    public ClassPanel() {

        this.classService = AppContext.classService;
        this.courseService = AppContext.courseService;
        this.teacherService = AppContext.teacherService;
        this.roomService = AppContext.roomService;

        initUI();
        loadComboData();
        loadTable();
    }

    private void initUI() {

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // ===== HEADER =====
        JLabel lblTitle = new JLabel("LCMS - Class Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // ===== MAIN CONTENT =====
        JPanel content = new JPanel(new GridLayout(1, 2, 20, 0));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.setBackground(new Color(245, 247, 250));

        // ================= LEFT CARD (FORM) =================
        JPanel formCard = new JPanel(new BorderLayout(10, 10));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel formTitle = new JLabel("Create New Class");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 15));
        form.setBackground(Color.WHITE);

        cboCourse = new JComboBox<>();
        cboTeacher = new JComboBox<>();
        cboRoom = new JComboBox<>();

        txtMaxStudent = new JTextField();
        txtStartDate = new JTextField("2026-03-01");
        txtEndDate = new JTextField("2026-06-01");

        form.add(new JLabel("Course:"));
        form.add(cboCourse);

        form.add(new JLabel("Teacher:"));
        form.add(cboTeacher);

        form.add(new JLabel("Room:"));
        form.add(cboRoom);

        form.add(new JLabel("Max Student:"));
        form.add(txtMaxStudent);

        form.add(new JLabel("Start Date:"));
        form.add(txtStartDate);

        form.add(new JLabel("End Date:"));
        form.add(txtEndDate);

        formCard.add(form, BorderLayout.CENTER);

        JButton btnCreate = new JButton("Create Class");
        btnCreate.setBackground(new Color(33, 150, 243));
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setFocusPainted(false);
        btnCreate.setPreferredSize(new Dimension(150, 40));
        btnCreate.addActionListener(e -> createClass());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btnCreate);

        formCard.add(btnPanel, BorderLayout.SOUTH);

        // ================= RIGHT CARD (TABLE) =================
        JPanel tableCard = new JPanel(new BorderLayout(10, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel tableTitle = new JLabel("Class List");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

        tableCard.add(tableTitle, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Class Name", "Course", "Teacher", "Room", "Status"},
                0);

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(table);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        content.add(formCard);
        content.add(tableCard);

        add(content, BorderLayout.CENTER);
    }

    private void loadComboData() {

        courseService.findAll().forEach(cboCourse::addItem);
        teacherService.findAll().forEach(cboTeacher::addItem);
        roomService.findAll().forEach(cboRoom::addItem);
    }

    private void loadTable() {

        tableModel.setRowCount(0);
        List<TeachingClass> list = classService.findAll();

        for (TeachingClass tc : list) {
            tableModel.addRow(new Object[]{
                    tc.getClassName(),
                    tc.getCourse().getCourseName(),
                    tc.getTeacher().getFullName(),
                    tc.getRoom() != null ? tc.getRoom().getRoomName() : "",
                    tc.getStatus()
            });
        }
    }

    private void createClass() {

        try {
            Course course = (Course) cboCourse.getSelectedItem();
            Teacher teacher = (Teacher) cboTeacher.getSelectedItem();
            Room room = (Room) cboRoom.getSelectedItem();

            TeachingClass tc = new TeachingClass();
            tc.setClassName(course.getCourseName() + "-01");
            tc.setCourse(course);
            tc.setTeacher(teacher);
            tc.setRoom(room);
            tc.setMaxStudent(Integer.parseInt(txtMaxStudent.getText()));
            tc.setStartDate(LocalDate.parse(txtStartDate.getText()));
            tc.setEndDate(LocalDate.parse(txtEndDate.getText()));
            tc.setStatus(ClassStatus.ACTIVE);

            classService.createClass(tc);

            loadTable();

            JOptionPane.showMessageDialog(this, "Created!");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}