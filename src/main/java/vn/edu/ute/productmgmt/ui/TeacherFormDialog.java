package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * Form nhập liệu cho Giáo viên, chuẩn hóa theo ClassFormDialog
 */
public class TeacherFormDialog extends JDialog {

    private final JTextField txtFullName = new JTextField(25);
    private final JTextField txtPhone = new JTextField(15);
    private final JTextField txtEmail = new JTextField(25);
    private final JTextField txtSpecialty = new JTextField(20);
    private final JSpinner spnHireDate;
    private final JComboBox<ActiveStatus> cboStatus = new JComboBox<>(ActiveStatus.values());

    private boolean saved = false;
    private TeacherFormData result;

    public TeacherFormDialog(Window owner, TeacherFormData existing) {
        super(owner, "Thông tin Giáo viên", ModalityType.APPLICATION_MODAL);

        setSize(580, 680);
        setLayout(new BorderLayout());

        // Khởi tạo Spinner trước khi build UI
        spnHireDate = createDateSpinner(new Date());

        buildUI();

        if (existing != null) {
            this.result = existing;
            txtFullName.setText(existing.getFullName());
            txtPhone.setText(existing.getPhone());
            txtEmail.setText(existing.getEmail());
            txtSpecialty.setText(existing.getSpecialty());
            cboStatus.setSelectedItem(existing.getStatus());

            // Đổ dữ liệu ngày vào làm
            try {
                if (existing.getHireDate() != null) {
                    LocalDate ld = LocalDate.parse(existing.getHireDate());
                    Date date = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    spnHireDate.setValue(date);
                }
            } catch (Exception e) {
                spnHireDate.setValue(new Date());
            }
        } else {
            result = new TeacherFormData();
        }

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
        JLabel lblHeader = new JLabel("Thông tin Giáo viên");
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
        addFormRow(form, gbc, 1, "Số điện thoại:", txtPhone);
        addFormRow(form, gbc, 2, "Email:", txtEmail);
        addFormRow(form, gbc, 3, "Chuyên môn:", txtSpecialty);
        addFormRow(form, gbc, 4, "Ngày vào làm:", spnHireDate);
        addFormRow(form, gbc, 5, "Trạng thái:", cboStatus);

        root.add(form, BorderLayout.CENTER);

        // --- Buttons ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(25, 0, 0, 0));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(100, 42));
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #f2f2f2; borderWidth: 0");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Lưu Giáo viên");
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
            spnHireDate.commitEdit();

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

            LocalDate localDate = toLocalDate((Date) spnHireDate.getValue());

            result.setFullName(fullName);
            result.setPhone(phone);
            result.setEmail(email);
            result.setSpecialty(txtSpecialty.getText().trim());
            result.setHireDate(localDate.toString());
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

    public TeacherFormData getResult() {
        return result;
    }

    /**
     * DTO đơn giản đại diện thông tin Giáo viên cho UI.
     */
    public static class TeacherFormData {
        private String fullName;
        private String phone;
        private String email;
        private String specialty;
        private String hireDate;
        private ActiveStatus status;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
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

        public String getSpecialty() {
            return specialty;
        }

        public void setSpecialty(String specialty) {
            this.specialty = specialty;
        }

        public String getHireDate() {
            return hireDate;
        }

        public void setHireDate(String hireDate) {
            this.hireDate = hireDate;
        }

        public ActiveStatus getStatus() {
            return status;
        }

        public void setStatus(ActiveStatus status) {
            this.status = status;
        }
    }
}

