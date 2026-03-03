package vn.edu.ute.productmgmt.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Form nhập liệu đơn giản cho Class, dùng mock object.
 */
public class ClassFormDialog extends JDialog {

    private final JTextField txtClassName = new JTextField(25);
    private final JComboBox<String> cboCourse = new JComboBox<>(new String[]{
            "IELTS Foundation", "TOEIC 500+", "Communication"
    });
    private final JComboBox<String> cboTeacher = new JComboBox<>(new String[]{
            "Thay An", "Co Binh", "Thay Cuong"
    });
    private final JComboBox<String> cboRoom = new JComboBox<>(new String[]{
            "Room 101", "Room 202", "Room 303"
    });
    private final JTextField txtStartDate = new JTextField(10);
    private final JTextField txtEndDate = new JTextField(10);
    private final JTextField txtMaxStudent = new JTextField(5);
    private final JComboBox<String> cboStatus = new JComboBox<>(new String[]{"Planned", "Running", "Finished"});

    private boolean saved = false;
    private ClassFormData result;

    public ClassFormDialog(Window owner, ClassFormData existing) {
        super(owner, "Class", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();

        if (existing != null) {
            txtClassName.setText(existing.getClassName());
            cboCourse.setSelectedItem(existing.getCourseName());
            cboTeacher.setSelectedItem(existing.getTeacherName());
            cboRoom.setSelectedItem(existing.getRoomName());
            txtStartDate.setText(existing.getStartDate());
            txtEndDate.setText(existing.getEndDate());
            txtMaxStudent.setText(existing.getMaxStudent());
            cboStatus.setSelectedItem(existing.getStatus());
            result = existing;
        } else {
            result = new ClassFormData();
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;

        int r = 0;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Class name:"), g);
        g.gridx = 1;
        form.add(txtClassName, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Course:"), g);
        g.gridx = 1;
        form.add(cboCourse, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Teacher:"), g);
        g.gridx = 1;
        form.add(cboTeacher, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Room:"), g);
        g.gridx = 1;
        form.add(cboRoom, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Start date:"), g);
        g.gridx = 1;
        form.add(txtStartDate, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("End date:"), g);
        g.gridx = 1;
        form.add(txtEndDate, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Max students:"), g);
        g.gridx = 1;
        form.add(txtMaxStudent, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Status:"), g);
        g.gridx = 1;
        form.add(cboStatus, g);

        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnSave);
        actions.add(btnCancel);

        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(actions, BorderLayout.SOUTH);
    }

    private void onSave() {
        try {
            String className = txtClassName.getText().trim();
            if (className.isEmpty()) {
                throw new IllegalArgumentException("Class name is required.");
            }

            result.setClassName(className);
            result.setCourseName((String) cboCourse.getSelectedItem());
            result.setTeacherName((String) cboTeacher.getSelectedItem());
            result.setRoomName((String) cboRoom.getSelectedItem());
            result.setStartDate(txtStartDate.getText().trim());
            result.setEndDate(txtEndDate.getText().trim());
            result.setMaxStudent(txtMaxStudent.getText().trim());
            result.setStatus((String) cboStatus.getSelectedItem());

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public ClassFormData getResult() {
        return result;
    }

    /**
     * DTO đơn giản đại diện thông tin Class cho UI.
     */
    public static class ClassFormData {
        private String className;
        private String courseName;
        private String teacherName;
        private String roomName;
        private String startDate;
        private String endDate;
        private String maxStudent;
        private String status;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getTeacherName() {
            return teacherName;
        }

        public void setTeacherName(String teacherName) {
            this.teacherName = teacherName;
        }

        public String getRoomName() {
            return roomName;
        }

        public void setRoomName(String roomName) {
            this.roomName = roomName;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getMaxStudent() {
            return maxStudent;
        }

        public void setMaxStudent(String maxStudent) {
            this.maxStudent = maxStudent;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}

