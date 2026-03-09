package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.model.enums.Gender;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * Form nhập liệu cho Học viên, chuẩn hóa theo ClassFormDialog
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

        setSize(580, 680);
        setLayout(new BorderLayout());

        // Khởi tạo Spinner trước khi build UI
        spinnerDob = createDateSpinner(new Date());

        // Đổ dữ liệu vào các field
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
                    LocalDate ld = LocalDate.parse(existing.getDateOfBirth());
                    Date date = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    spinnerDob.setValue(date);
                }
            } catch (Exception e) {
                spinnerDob.setValue(new Date());
            }
        } else {
            this.result = new StudentFormData();
        }

        buildUI();
        setLocationRelativeTo(owner);
    }

    private JSpinner createDateSpinner(Date defaultDate) {
        SpinnerDateModel model = new SpinnerDateModel(defaultDate, null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        spinner.setPreferredSize(new Dimension(0, 40));
        spinner.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        return spinner;
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- Header ---
        JLabel lblHeader = new JLabel("Thông tin Học viên");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setBorder(new EmptyBorder(0, 0, 25, 0));
        root.add(lblHeader, BorderLayout.NORTH);

        // --- Form Body ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        // Các hàng dữ liệu
        addFormRow(form, gbc, 0, "Họ và tên:", txtFullName);
        addFormRow(form, gbc, 1, "Ngày sinh:", spinnerDob);
        addFormRow(form, gbc, 2, "Giới tính:", cboGender);
        addFormRow(form, gbc, 3, "Số điện thoại:", txtPhone);
        addFormRow(form, gbc, 4, "Email:", txtEmail);
        addFormRow(form, gbc, 5, "Địa chỉ:", txtAddress);
        addFormRow(form, gbc, 6, "Trạng thái:", cboStatus);

        root.add(form, BorderLayout.CENTER);

        // --- Buttons ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(25, 0, 0, 0));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(100, 42));
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #f2f2f2; borderWidth: 0");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Lưu Học viên");
        btnSave.setPreferredSize(new Dimension(140, 42));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #198754; foreground: #ffffff; borderWidth: 0");
        btnSave.addActionListener(e -> onSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        root.add(footer, BorderLayout.SOUTH);

        add(root);
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridy = row;
        gbc.gridx = 0; gbc.weightx = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        p.add(createLabel(label), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 20, 8, 0);
        if (comp instanceof JComboBox || comp instanceof JSpinner || comp instanceof JTextField) {
            comp.setPreferredSize(new Dimension(0, 40));
            comp.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        }
        p.add(comp, gbc);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private void onSave() {
        try {
            spinnerDob.commitEdit();

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

            LocalDate localDate = toLocalDate((Date) spinnerDob.getValue());

            result.setFullName(fullName);
            result.setDateOfBirth(localDate.toString());
            result.setGender((Gender) cboGender.getSelectedItem());
            result.setPhone(phone);
            result.setEmail(email);
            result.setAddress(txtAddress.getText().trim());
            result.setStatus((ActiveStatus) cboStatus.getSelectedItem());

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Thông báo lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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
