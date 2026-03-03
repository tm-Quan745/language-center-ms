package vn.edu.ute.productmgmt.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Màn hình Học viên kiểu dashboard: thanh action trên cùng + form thông tin nhiều cột.
 * Dùng mock data, chưa nối DB.
 */
public class StudentPanel extends JPanel {

    private final JTextField txtStudentCode = new JTextField(20);
    private final JTextField txtFullName = new JTextField(25);
    private final JComboBox<String> cboGender = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
    private final JTextField txtPhone = new JTextField(15);
    private final JTextField txtAddress = new JTextField(25);
    private final JTextField txtEmail = new JTextField(25);
    private final JTextField txtParentPhone = new JTextField(15);
    private final JTextField txtSchool = new JTextField(25);
    private final JTextField txtDob = new JTextField(10);
    private final JTextField txtJoinDate = new JTextField(10);
    private final JTextField txtSource = new JTextField(15);
    private final JTextField txtMarketing = new JTextField(20);
    private final JComboBox<String> cboStatus = new JComboBox<>(new String[]{"Active", "Inactive"});

    public StudentPanel() {
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
        JButton btnLock = new JButton("Khóa dữ liệu");

        btnBack.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Back (mock) - chưa xử lý điều hướng."));
        btnAdd.addActionListener(e -> clearForm());
        btnSave.addActionListener(e -> JOptionPane.showMessageDialog(this, "Đã lưu (mock)."));
        btnDelete.addActionListener(e -> JOptionPane.showMessageDialog(this, "Đã xóa (mock)."));
        btnLock.addActionListener(e -> JOptionPane.showMessageDialog(this, "Đã khóa dữ liệu (mock)."));

        bar.add(btnBack);
        bar.add(btnAdd);
        bar.add(btnSave);
        bar.add(btnDelete);
        bar.add(btnLock);

        return bar;
    }

    private JComponent buildForm() {
        JPanel wrapper = new JPanel(new BorderLayout());
        UI.stylePanelBorder(wrapper, "Thông tin học viên");

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 0.0;

        int r = 0;

        // Hàng 1: Mã HV, Họ và tên, Giới tính, SĐT
        addField(form, g, r, 0, "Mã HV:", txtStudentCode);
        addField(form, g, r, 1, "Họ và tên:", txtFullName);
        addField(form, g, r, 2, "Giới tính:", cboGender);
        addField(form, g, r, 3, "SĐT:", txtPhone);

        // Hàng 2: Địa chỉ, Email, SĐT phụ huynh
        r++;
        addField(form, g, r, 0, "Địa chỉ:", txtAddress);
        addField(form, g, r, 1, "Email:", txtEmail);
        addField(form, g, r, 2, "SĐT phụ huynh:", txtParentPhone);

        // Hàng 3: Trường đang theo học, Ngày sinh, Ngày đến trung tâm
        r++;
        addField(form, g, r, 0, "Trường đang theo học:", txtSchool);
        addField(form, g, r, 1, "Ngày sinh:", txtDob);
        addField(form, g, r, 2, "Ngày đến trung tâm:", txtJoinDate);

        // Hàng 4: Nguồn, Marketing, Trạng thái
        r++;
        addField(form, g, r, 0, "Nguồn:", txtSource);
        addField(form, g, r, 1, "Marketing:", txtMarketing);
        addField(form, g, r, 2, "Trạng thái:", cboStatus);

        wrapper.add(form, BorderLayout.CENTER);
        return wrapper;
    }

    private void addField(JPanel form, GridBagConstraints g, int row, int col,
                          String label, JComponent field) {
        int baseGridX = col * 2;

        g.gridy = row;

        // Label
        g.gridx = baseGridX;
        g.weightx = 0.0;
        form.add(new JLabel(label), g);

        // Field
        g.gridx = baseGridX + 1;
        g.weightx = 1.0;
        form.add(field, g);
    }

    private void loadMockData() {
        txtStudentCode.setText("HV001");
        txtFullName.setText("Nguyen Huyen My");
        cboGender.setSelectedItem("Nữ");
        txtPhone.setText("01688754563");
        txtAddress.setText("Hà Nội");
        txtEmail.setText("nguyenhuyenmy@example.com");
        txtParentPhone.setText("098854567");
        txtSchool.setText("ĐH Luật Hà Nội");
        txtDob.setText("06/12/1995");
        txtJoinDate.setText("09/09/2016");
        txtSource.setText("Event");
        txtMarketing.setText("Tổ chức event");
        cboStatus.setSelectedItem("Active");
    }

    private void clearForm() {
        txtStudentCode.setText("");
        txtFullName.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        txtEmail.setText("");
        txtParentPhone.setText("");
        txtSchool.setText("");
        txtDob.setText("");
        txtJoinDate.setText("");
        txtSource.setText("");
        txtMarketing.setText("");
        cboGender.setSelectedIndex(0);
        cboStatus.setSelectedIndex(0);
    }
}


