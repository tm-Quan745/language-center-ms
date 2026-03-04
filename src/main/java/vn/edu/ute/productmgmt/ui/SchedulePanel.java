package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.config.AppContext;
import vn.edu.ute.productmgmt.model.*;
import vn.edu.ute.productmgmt.service.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class SchedulePanel extends JPanel {

    private final ClassService classService;
    private final ScheduleService scheduleService;

    private JComboBox<TeachingClass> cboClass;
    private JComboBox<DayOfWeek> cboDay;
    private JTextField txtStart;
    private JTextField txtEnd;

    private JTable table;
    private DefaultTableModel model;

    public SchedulePanel() {

        this.classService = AppContext.classService;
        this.scheduleService = AppContext.scheduleService;

        initUI();
        loadClasses();
    }

    private void initUI() {

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // ===== HEADER =====
        JLabel lblTitle = new JLabel("LCMS - Schedule Management");
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

        JLabel formTitle = new JLabel("Add Schedule");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 15));
        form.setBackground(Color.WHITE);

        cboClass = new JComboBox<>();
        cboDay = new JComboBox<>(DayOfWeek.values());
        txtStart = new JTextField("08:00");
        txtEnd = new JTextField("10:00");

        form.add(new JLabel("Class:"));
        form.add(cboClass);

        form.add(new JLabel("Day:"));
        form.add(cboDay);

        form.add(new JLabel("Start Time (HH:mm):"));
        form.add(txtStart);

        form.add(new JLabel("End Time (HH:mm):"));
        form.add(txtEnd);

        formCard.add(form, BorderLayout.CENTER);

        JButton btnAdd = new JButton("Add Schedule");
        btnAdd.setBackground(new Color(76, 175, 80));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setPreferredSize(new Dimension(150, 40));
        btnAdd.addActionListener(e -> addSchedule());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btnAdd);

        formCard.add(btnPanel, BorderLayout.SOUTH);

        // ================= RIGHT CARD (TABLE) =================
        JPanel tableCard = new JPanel(new BorderLayout(10, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel tableTitle = new JLabel("Schedule List");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableCard.add(tableTitle, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"Day", "Start", "End"}, 0);

        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(table);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        content.add(formCard);
        content.add(tableCard);

        add(content, BorderLayout.CENTER);
    }

    private void loadClasses() {
        classService.findAll().forEach(cboClass::addItem);
    }

    private void addSchedule() {

        try {
            TeachingClass tc = (TeachingClass) cboClass.getSelectedItem();
            DayOfWeek day = (DayOfWeek) cboDay.getSelectedItem();

            Schedule s = new Schedule();
            s.setTeachingClass(tc);
            s.setDayOfWeek(day);
            s.setStartTime(LocalTime.parse(txtStart.getText()));
            s.setEndTime(LocalTime.parse(txtEnd.getText()));

            scheduleService.createSchedule(s);

            model.addRow(new Object[]{
                    day,
                    txtStart.getText(),
                    txtEnd.getText()
            });

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}