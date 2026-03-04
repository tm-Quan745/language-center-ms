package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.service.StudentService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình quản lý học viên, ghép với StudentService.
 */
public class StudentPanel extends JPanel {

    private final StudentService studentService;

    private final JTextField txtSearch = new JTextField(20);
    private final JLabel lblInfo = new JLabel(" ");

    private final StudentTableModel tableModel = new StudentTableModel();
    private final JTable table = new JTable(tableModel);
    private Student selectedStudent;

    public StudentPanel(StudentService studentService) {
        this.studentService = studentService;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            onTableSelection();
        });
        loadTableAll();
    }

    private void buildUI() {
        add(buildActionBar(), BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
        add(lblInfo, BorderLayout.SOUTH);
    }

    private JComponent buildActionBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 4));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        left.add(new JLabel("Tìm (tên/SĐT):"));
        left.add(txtSearch);
        JButton btnSearch = new JButton("Tìm");
        btnSearch.addActionListener(e -> onSearch());
        left.add(btnSearch);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton btnAdd = new JButton("Thêm mới");
        JButton btnEdit = new JButton("Sửa");
        JButton btnDelete = new JButton("Xóa");
        JButton btnRefresh = new JButton("Tải lại");

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> loadTableAll());

        right.add(btnAdd);
        right.add(btnEdit);
        right.add(btnDelete);
        right.add(btnRefresh);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JComponent buildTableArea() {
        UI.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        return scroll;
    }

    private void loadTableAll() {
        try {
            List<Student> list = studentService.findAll();
            tableModel.setData(list);
            lblInfo.setText("Tổng số: " + list.size() + " học viên.");
            clearSelection();
        } catch (Exception ex) {
            lblInfo.setText("Lỗi: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Không tải được danh sách học viên: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTableSearch(String keyword) {
        try {
            List<Student> list = studentService.searchByFullNameOrPhone(keyword);
            tableModel.setData(list);
            lblInfo.setText("Kết quả: " + list.size() + " học viên.");
            clearSelection();
        } catch (Exception ex) {
            lblInfo.setText("Lỗi: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Không tìm được học viên: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSearch() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadTableAll();
        } else {
            loadTableSearch(keyword);
        }
    }

    private void onTableSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            selectedStudent = null;
            return;
        }
        selectedStudent = tableModel.getStudentAt(row);
    }

    private void onAdd() {
        StudentFormDialog dialog = new StudentFormDialog(
                SwingUtilities.getWindowAncestor(this),
                null
        );
        dialog.setVisible(true);
        if (!dialog.isSaved()) {
            return;
        }
        StudentFormDialog.StudentFormData data = dialog.getResult();
        Student s = formDataToStudent(data, null);
        if (s == null) return;
        try {
            studentService.create(s);
            JOptionPane.showMessageDialog(this, "Đã thêm học viên.", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            loadTableAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEdit() {
        if (selectedStudent == null) {
            JOptionPane.showMessageDialog(this, "Chọn một học viên để sửa.", "Chưa chọn",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        StudentFormDialog.StudentFormData existing = studentToFormData(selectedStudent);
        StudentFormDialog dialog = new StudentFormDialog(
                SwingUtilities.getWindowAncestor(this),
                existing
        );
        dialog.setVisible(true);
        if (!dialog.isSaved()) {
            return;
        }
        StudentFormDialog.StudentFormData data = dialog.getResult();
        Student s = formDataToStudent(data, selectedStudent.getId());
        if (s == null) return;
        try {
            studentService.update(s);
            JOptionPane.showMessageDialog(this, "Đã cập nhật học viên.", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            loadTableAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        if (selectedStudent == null) {
            JOptionPane.showMessageDialog(this, "Chọn một học viên để xóa.", "Chưa chọn",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa học viên này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            studentService.delete(selectedStudent.getId());
            JOptionPane.showMessageDialog(this, "Đã xóa học viên.", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            loadTableAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private StudentFormDialog.StudentFormData studentToFormData(Student s) {
        StudentFormDialog.StudentFormData data = new StudentFormDialog.StudentFormData();
        data.setFullName(s.getFullName());
        data.setDateOfBirth(s.getDateOfBirth() != null ? s.getDateOfBirth().toString() : "");
        data.setGender(s.getGender());
        data.setPhone(s.getPhone());
        data.setEmail(s.getEmail());
        data.setAddress(s.getAddress());
        data.setStatus(s.getStatus());
        return data;
    }

    private Student formDataToStudent(StudentFormDialog.StudentFormData data, Long keepId) {
        String fullName = data.getFullName() != null ? data.getFullName().trim() : "";
        String phone = data.getPhone() != null ? data.getPhone().trim() : "";
        String email = data.getEmail() != null ? data.getEmail().trim() : "";
        String dobStr = data.getDateOfBirth() != null ? data.getDateOfBirth().trim() : "";
        String address = data.getAddress() != null ? data.getAddress().trim() : "";

        if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên học viên không được để trống.", "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không được để trống.", "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email không được để trống.", "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }

        LocalDate dob = null;
        if (!dobStr.isEmpty()) {
            try {
                dob = LocalDate.parse(dobStr);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "Ngày sinh không hợp lệ. Định dạng đúng: yyyy-MM-dd.",
                        "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }

        Student s = new Student();
        if (keepId != null) s.setId(keepId);
        s.setFullName(fullName);
        s.setDateOfBirth(dob);
        s.setGender(data.getGender());
        s.setPhone(phone);
        s.setEmail(email);
        s.setAddress(address.isEmpty() ? null : address);
        s.setStatus(data.getStatus());
        // registrationDate sẽ được set trong @PrePersist nếu null
        return s;
    }

    private void clearSelection() {
        selectedStudent = null;
        table.clearSelection();
    }

    // === Table model ===
    private static class StudentTableModel extends AbstractTableModel {
        private final String[] columns = {
                "ID",
                "Họ tên",
                "SĐT",
                "Email",
                "Trạng thái",
                "Ngày đăng ký"
        };
        private List<Student> data = new ArrayList<>();

        void setData(List<Student> data) {
            this.data = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        Student getStudentAt(int row) {
            if (row < 0 || row >= data.size()) return null;
            return data.get(row);
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Student s = data.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return s.getId();
                case 1:
                    return s.getFullName();
                case 2:
                    return s.getPhone();
                case 3:
                    return s.getEmail();
                case 4:
                    return s.getStatus() != null ? s.getStatus().name() : "";
                case 5:
                    return s.getRegistrationDate() != null ? s.getRegistrationDate().toString() : "";
                default:
                    return "";
            }
        }
    }
}

