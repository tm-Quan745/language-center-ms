package vn.edu.ute.productmgmt.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Màn hình Lớp học: form chi tiết lớp theo style dashboard, dùng mock data.
 */
public class ClassPanel extends JPanel {

    private final JTextField txtClassId = new JTextField(10);
    private final JTextField txtClassName = new JTextField(25);
    private final JComboBox<String> cboCourse = new JComboBox<>(new String[]{
            "IELTS Foundation", "TOEIC 500+", "English Communication"
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
    private final JComboBox<String> cboStatus = new JComboBox<>(new String[]{
            "Planned", "Running", "Finished"
    });

    public ClassPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        loadMockData();
    }

    private void buildUI() {
        add(buildActionBar(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
    }

    private JComponent buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));

        JButton btnBack = new JButton("Quay lại");
        JButton btnAdd = new JButton("Thêm lớp");
        JButton btnSave = new JButton("Lưu");
        JButton btnDelete = new JButton("Xóa");

        btnBack.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Back (mock) - chưa xử lý điều hướng."));
        btnAdd.addActionListener(e -> clearForm());
        btnSave.addActionListener(e -> JOptionPane.showMessageDialog(this, "Đã lưu lớp học (mock)."));
        btnDelete.addActionListener(e -> JOptionPane.showMessageDialog(this, "Đã xóa lớp học (mock)."));

        bar.add(btnBack);
        bar.add(btnAdd);
        bar.add(btnSave);
        bar.add(btnDelete);

        return bar;
    }

    private JComponent buildForm() {
        JPanel wrapper = new JPanel(new BorderLayout());
        UI.stylePanelBorder(wrapper, "Thông tin lớp học");

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;

        addField(form, g, r, 0, "Mã lớp:", txtClassId);
        addField(form, g, r, 1, "Tên lớp:", txtClassName);

        r++;
        addField(form, g, r, 0, "Khóa học:", cboCourse);
        addField(form, g, r, 1, "Giáo viên:", cboTeacher);

        r++;
        addField(form, g, r, 0, "Phòng học:", cboRoom);
        addField(form, g, r, 1, "Sĩ số tối đa:", txtMaxStudent);

        r++;
        addField(form, g, r, 0, "Ngày bắt đầu:", txtStartDate);
        addField(form, g, r, 1, "Ngày kết thúc:", txtEndDate);

        r++;
        addField(form, g, r, 0, "Trạng thái:", cboStatus);

        wrapper.add(form, BorderLayout.CENTER);
        return wrapper;
    }

    private void addField(JPanel form, GridBagConstraints g, int row, int col,
                          String label, JComponent field) {
        int baseGridX = col * 2;
        g.gridy = row;

        g.gridx = baseGridX;
        g.weightx = 0.0;
        form.add(new JLabel(label), g);

        g.gridx = baseGridX + 1;
        g.weightx = 1.0;
        form.add(field, g);
    }

    private void loadMockData() {
        txtClassId.setText("CL101");
        txtClassName.setText("IELTS Foundation - A");
        cboCourse.setSelectedItem("IELTS Foundation");
        cboTeacher.setSelectedItem("Thay An");
        cboRoom.setSelectedItem("Room 101");
        txtStartDate.setText("01/04/2026");
        txtEndDate.setText("01/07/2026");
        txtMaxStudent.setText("20");
        cboStatus.setSelectedItem("Running");
    }

    private void clearForm() {
        txtClassId.setText("");
        txtClassName.setText("");
        txtStartDate.setText("");
        txtEndDate.setText("");
        txtMaxStudent.setText("");
        cboCourse.setSelectedIndex(0);
        cboTeacher.setSelectedIndex(0);
        cboRoom.setSelectedIndex(0);
        cboStatus.setSelectedIndex(0);
    }
}


