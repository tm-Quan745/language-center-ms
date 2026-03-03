package vn.edu.ute.productmgmt.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class LcmsStaffPanel extends JPanel {

    private JTextField txtId;
    private JTextField txtName;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtPosition;

    private JTable table;
    private DefaultTableModel tableModel;

    public LcmsStaffPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createFormPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    // ===== FORM =====
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Thông tin nhân viên"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtId = new JTextField(15);
        txtName = new JTextField(15);
        txtPhone = new JTextField(15);
        txtEmail = new JTextField(15);
        txtPosition = new JTextField(15);

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Mã NV:"), gbc);
        gbc.gridx = 1;
        panel.add(txtId, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Tên NV:"), gbc);
        gbc.gridx = 3;
        panel.add(txtName, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Điện thoại:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPhone, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 3;
        panel.add(txtEmail, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Chức vụ:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPosition, gbc);

        return panel;
    }

    // ===== TABLE =====
    private JScrollPane createTablePanel() {

        String[] columns = {
                "Mã NV",
                "Tên nhân viên",
                "Điện thoại",
                "Email",
                "Chức vụ"
        };

        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);

        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Click vào row để load lên form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;

            int row = table.getSelectedRow();
            if (row >= 0) {
                txtId.setText(tableModel.getValueAt(row, 0).toString());
                txtName.setText(tableModel.getValueAt(row, 1).toString());
                txtPhone.setText(tableModel.getValueAt(row, 2).toString());
                txtEmail.setText(tableModel.getValueAt(row, 3).toString());
                txtPosition.setText(tableModel.getValueAt(row, 4).toString());
            }
        });

        return new JScrollPane(table);
    }

    // ===== BUTTONS =====
    private JPanel createButtonPanel() {

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnAdd = new JButton("Thêm");
        JButton btnUpdate = new JButton("Sửa");
        JButton btnDelete = new JButton("Xóa");
        JButton btnClear = new JButton("Làm mới");

        btnAdd.addActionListener(e -> addStaff());
        btnUpdate.addActionListener(e -> updateStaff());
        btnDelete.addActionListener(e -> deleteStaff());
        btnClear.addActionListener(e -> clearForm());

        panel.add(btnAdd);
        panel.add(btnUpdate);
        panel.add(btnDelete);
        panel.add(btnClear);

        return panel;
    }

    // ===== ACTIONS =====
    private void addStaff() {
        // Lấy dữ liệu và xóa khoảng trắng thừa
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String position = txtPosition.getText().trim();

        // Kiểm tra nếu bất kỳ ô nào bị rỗng
        if (id.isEmpty() || name.isEmpty() || phone.isEmpty() || email.isEmpty() || position.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return; // Dừng hàm tại đây, không add vào table
        }

        // Nếu mọi thứ ổn, tiến hành thêm vào bảng
        tableModel.addRow(new Object[]{id, name, phone, email, position});
        clearForm();
    }

    private void updateStaff() {

        int row = table.getSelectedRow();
        if (row >= 0) {
            tableModel.setValueAt(txtId.getText(), row, 0);
            tableModel.setValueAt(txtName.getText(), row, 1);
            tableModel.setValueAt(txtPhone.getText(), row, 2);
            tableModel.setValueAt(txtEmail.getText(), row, 3);
            tableModel.setValueAt(txtPosition.getText(), row, 4);
        }
    }

    private void deleteStaff() {

        int row = table.getSelectedRow();
        if (row >= 0) {
            tableModel.removeRow(row);
            clearForm();
        }
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtPosition.setText("");
        table.clearSelection();
    }
}