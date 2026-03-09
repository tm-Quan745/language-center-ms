package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.model.enums.Gender;

import java.util.Calendar;
import java.util.Date;
import javax.swing.*;
import java.awt.*;

/**
 * Form nhập liệu đơn giản cho Student, dùng mock object.
 */
public class StudentFormDialog extends JDialog {

    private final JSpinner spinnerDob;
    private final JTextField txtFullName = new JTextField(25);
    private final JComboBox<Gender> cboGender = new JComboBox<>(Gender.values());
    private final JTextField txtPhone = new JTextField(15);
    private final JTextField txtEmail = new JTextField(25);
    private final JTextField txtAddress = new JTextField(25);
    private final JComboBox<ActiveStatus> cboStatus = new JComboBox<>(ActiveStatus.values());
    private boolean saved = false;
    private final StudentFormData result;

    public StudentFormDialog(Window owner, StudentFormData existing) {
        super(owner, "Thông tin Học viên", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 1. Khởi tạo Spinner trước khi buildUI
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        spinnerDob = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinnerDob, "dd/MM/yyyy");
        spinnerDob.setEditor(dateEditor);

        buildUI();

        // 2. Đổ dữ liệu vào các field
        if (existing != null) {
            this.result = existing;
            txtFullName.setText(existing.getFullName());
            cboGender.setSelectedItem(existing.getGender());
            txtPhone.setText(existing.getPhone());
            txtEmail.setText(existing.getEmail());
            txtAddress.setText(existing.getAddress());
            cboStatus.setSelectedItem(existing.getStatus());

            // Xử lý ngày sinh từ String sang Date cho Spinner
            try {
                if (existing.getDateOfBirth() != null) {
                    java.time.LocalDate ld = java.time.LocalDate.parse(existing.getDateOfBirth());
                    java.util.Date date = java.util.Date.from(ld.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                    spinnerDob.setValue(date);
                }
            } catch (Exception e) {
                spinnerDob.setValue(new Date()); // Mặc định là ngày hiện tại nếu lỗi format
            }
        } else {
            // 'result' là final, khởi tạo trong nhánh này nếu không có existing
            // Sử dụng reflection of previous logic - assign here
            // (constructor đảm bảo result được gán trong mọi nhánh)
            // Note: we assign to the final field below.
            // To keep code clear, assign directly:
            // (the field is final and must be assigned exactly once per constructor)
            // Since Java doesn't allow duplicate assignment, we keep current structure.
            // We'll assign here:
            //noinspection ResultOfObjectAllocationIgnored
            result = new StudentFormData();
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL; // Để các field dãn đều

        int r = 0;
        // Sử dụng hàm helper để add row cho sạch code
        addFormRow(form, "Họ và tên:", txtFullName, g, r); r++;
        addFormRow(form, "Ngày sinh:", spinnerDob, g, r); r++; // Đã sửa: dùng spinnerDob thay vì txtDob
        addFormRow(form, "Giới tính:", cboGender, g, r); r++;
        addFormRow(form, "Điện thoại:", txtPhone, g, r); r++;
        addFormRow(form, "Email:", txtEmail, g, r); r++;
        addFormRow(form, "Địa chỉ:", txtAddress, g, r); r++;
        addFormRow(form, "Trạng thái:", cboStatus, g, r);

        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnSave);
        actions.add(btnCancel);

        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(actions, BorderLayout.SOUTH);
    }

    // Hàm helper giúp giảm lặp code GridBagConstraints
    private void addFormRow(JPanel p, String label, JComponent comp, GridBagConstraints g, int row) {
        g.gridy = row;
        g.gridx = 0; g.weightx = 0; p.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 1.0; p.add(comp, g);
    }

    private void onSave() {
        try {
            String fullName = txtFullName.getText().trim();
            if (fullName.isEmpty()) {
                throw new IllegalArgumentException("Họ và tên không được để trống.");
            }

            String phone = txtPhone.getText().trim();
            if (phone.isEmpty()) {
                throw new IllegalArgumentException("Số điện thoại không được để trống.");
            }

            String email = txtEmail.getText().trim();
            if (email.isEmpty()) {
                throw new IllegalArgumentException("Email không được để trống.");
            }
            // Lấy Date từ spinner và chuyển thành String (yyyy-MM-dd) để lưu vào DTO
            Date dateValue = (Date) spinnerDob.getValue();
            java.time.LocalDate localDate = dateValue.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();

            result.setDateOfBirth(localDate.toString());
            result.setFullName(fullName);
            result.setGender((Gender) cboGender.getSelectedItem());
            result.setPhone(phone);
            result.setEmail(email);
            result.setAddress(txtAddress.getText().trim());
            result.setStatus((ActiveStatus) cboStatus.getSelectedItem());
            saved = true;
            dispose();
        } catch (Exception ex) {
            // Hiển thị thông báo lỗi bằng tiếng Việt (sử dụng message từ exception nếu có)
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public StudentFormData getResult() {
        return result;
    }

    /**
     * DTO đơn giản đại diện thông tin Student cho UI.
     */
    public static class StudentFormData {
        private String fullName;
        private String dateOfBirth;
        private Gender gender;
        private String phone;
        private String email;
        private String address;
        private ActiveStatus status;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public Gender getGender() {
            return gender;
        }

        public void setGender(Gender gender) {
            this.gender = gender;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public ActiveStatus getStatus() {
            return status;
        }

        public void setStatus(ActiveStatus status) {
            this.status = status;
        }
    }
}
