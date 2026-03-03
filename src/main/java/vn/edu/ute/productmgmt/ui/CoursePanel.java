package vn.edu.ute.productmgmt.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Màn hình Khóa học: form chi tiết giống dashboard, dùng mock data.
 */
public class CoursePanel extends JPanel {

    private final JTextField txtCourseId = new JTextField(10);
    private final JTextField txtCourseName = new JTextField(25);
    private final JTextArea txtDescription = new JTextArea(3, 25);
    private final JComboBox<String> cboLevel = new JComboBox<>(new String[]{"Beginner", "Intermediate", "Advanced"});
    private final JTextField txtDuration = new JTextField(10);
    private final JTextField txtFee = new JTextField(10);
    private final JComboBox<String> cboStatus = new JComboBox<>(new String[]{"Active", "Inactive"});

    public CoursePanel() {
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
        JButton btnAdd = new JButton("Thêm mới");
        JButton btnSave = new JButton("Lưu");
        JButton btnDelete = new JButton("Xóa");

        btnBack.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Back (mock) - chưa xử lý điều hướng."));
        btnAdd.addActionListener(e -> clearForm());
        btnSave.addActionListener(e -> JOptionPane.showMessageDialog(this, "Đã lưu khóa học (mock)."));
        btnDelete.addActionListener(e -> JOptionPane.showMessageDialog(this, "Đã xóa khóa học (mock)."));

        bar.add(btnBack);
        bar.add(btnAdd);
        bar.add(btnSave);
        bar.add(btnDelete);

        return bar;
    }

    private JComponent buildForm() {
        JPanel wrapper = new JPanel(new BorderLayout());
        UI.stylePanelBorder(wrapper, "Thông tin khóa học");

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;

        addField(form, g, r, 0, "Mã khóa:", txtCourseId);
        addField(form, g, r, 1, "Tên khóa học:", txtCourseName);
        addField(form, g, r, 2, "Level:", cboLevel);

        r++;
        addField(form, g, r, 0, "Thời lượng:", txtDuration);
        addField(form, g, r, 1, "Học phí:", txtFee);
        addField(form, g, r, 2, "Trạng thái:", cboStatus);

        r++;
        addField(form, g, r, 0, "Mô tả:", new JScrollPane(txtDescription));

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
        txtCourseId.setText("C001");
        txtCourseName.setText("IELTS Foundation");
        txtDescription.setText("Khóa học nền tảng cho IELTS, tập trung 4 kỹ năng.");
        cboLevel.setSelectedItem("Beginner");
        txtDuration.setText("12 tuần");
        txtFee.setText("5,000,000");
        cboStatus.setSelectedItem("Active");
    }

    private void clearForm() {
        txtCourseId.setText("");
        txtCourseName.setText("");
        txtDescription.setText("");
        txtDuration.setText("");
        txtFee.setText("");
        cboLevel.setSelectedIndex(0);
        cboStatus.setSelectedIndex(0);
    }
}


