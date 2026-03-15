package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình quản lý học viên, chuẩn hóa theo ClassPanel
 */
public class StudentPanel extends JPanel {

    private final StudentService studentService;

    private final JTextField txtSearch = new JTextField(18);
    private final JLabel lblInfo = new JLabel(" ");

    private final StudentTableModel tableModel = new StudentTableModel();
    private final JTable table = new JTable(tableModel);
    private Student selectedStudent;

    public StudentPanel(StudentService studentService) {
        this.studentService = studentService;

        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        buildUI();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            onTableSelection();
        });

        loadTableAll();
    }

    private void buildUI() {

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // Left: search field + button (replaces title)
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm học viên...");
        txtSearch.setPreferredSize(new Dimension(300, 40));
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new JLabel(" 🔍 "));
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(100, 40));
        btnSearch.putClientProperty(FlatClientProperties.STYLE, "background: #0d6efd; foreground: #ffffff; arc: 12");
        btnSearch.addActionListener(e -> onSearch());
        left.add(txtSearch);
        left.add(Box.createHorizontalStrut(10));
        left.add(btnSearch);

        headerPanel.add(left, BorderLayout.WEST);
        headerPanel.add(buildActionBar(), BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        add(buildTableArea(), BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);

        lblInfo.setForeground(Color.GRAY);
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        statusBar.add(lblInfo, BorderLayout.WEST);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JComponent buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bar.setOpaque(false);

        JButton btnAdd = createBtn("Thêm", "#0d6efd", "➕ ");
        JButton btnEdit = createBtn("Sửa", "#ffc107", "📝 ");
        JButton btnDelete = createBtn("Xóa", "#dc3545", "🗑 ");
        JButton btnRefresh = new JButton("🔄 Tải lại");

        btnRefresh.setPreferredSize(new Dimension(100, 38));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> loadTableAll());

        bar.add(btnRefresh);
        bar.add(btnAdd);
        bar.add(btnEdit);
        bar.add(btnDelete);

        return bar;
    }

    private JButton createBtn(String text, String color, String icon) {

        JButton btn = new JButton(icon + text);

        btn.setPreferredSize(new Dimension(110, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String fg = color.equals("#ffc107") ? "#000000" : "#ffffff";

        btn.putClientProperty(FlatClientProperties.STYLE,
                "background:" + color +
                        ";foreground:" + fg +
                        ";arc:10;borderWidth:0");

        return btn;
    }

    private JComponent buildTableArea() {

        JScrollPane scroll = new JScrollPane(table);

        scroll.putClientProperty(FlatClientProperties.STYLE, "arc:15");
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        table.setRowHeight(45);
        table.setShowVerticalLines(false);

        table.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        return scroll;
    }

    private void loadTableAll() {

        try {

            List<Student> list = studentService.findAll();

            tableModel.setData(list);

            lblInfo.setText("Tổng số: " + list.size() + " học viên");

            table.clearSelection();

            selectedStudent = null;

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());

        }
    }

    private void onSearch() {

        String kw = txtSearch.getText().trim().toLowerCase();

        List<Student> all = studentService.findAll();

        List<Student> filtered = new ArrayList<>();

        for (Student s : all) {

            if ((s.getFullName() != null &&
                    s.getFullName().toLowerCase().contains(kw)) ||
                    (s.getPhone() != null &&
                            s.getPhone().toLowerCase().contains(kw))) {

                filtered.add(s);

            }

        }

        tableModel.setData(filtered);

        lblInfo.setText("Tìm thấy: " + filtered.size() + " kết quả");

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

        if (dialog.isSaved()) {

            try {

                StudentFormDialog.StudentFormData data = dialog.getResult();
                Student s = formDataToStudent(data, null);
                if (s == null) return;

                studentService.create(s);

                loadTableAll();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Lỗi thêm học viên: " + ex.getMessage());

            }

        }

    }

    private void onEdit() {

        if (selectedStudent == null) {

            JOptionPane.showMessageDialog(this, "Vui lòng chọn học viên để sửa");

            return;

        }

        StudentFormDialog.StudentFormData existing = studentToFormData(selectedStudent);
        StudentFormDialog dialog = new StudentFormDialog(
                SwingUtilities.getWindowAncestor(this),
                existing
        );

        dialog.setVisible(true);

        if (dialog.isSaved()) {

            try {

                StudentFormDialog.StudentFormData data = dialog.getResult();
                Student s = formDataToStudent(data, selectedStudent.getId());
                if (s == null) return;

                studentService.update(s);

                loadTableAll();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Lỗi cập nhật: " + ex.getMessage());

            }

        }

    }

    private void onDelete() {

        if (selectedStudent == null) return;

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Xóa học viên " + selectedStudent.getFullName() + " ?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (ok == JOptionPane.YES_OPTION) {

            try {

                studentService.delete(selectedStudent.getId());

                loadTableAll();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Lỗi xóa: " + ex.getMessage());

            }

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
        return s;
    }

    private static class StudentTableModel extends AbstractTableModel {

        private final String[] columns = {
                "Mã học viên",
                "Họ tên",
                "Số điện thoại",
                "Email",
                "Trạng thái",
                "Ngày đăng ký"
        };

        private List<Student> data = new ArrayList<>();

        void setData(List<Student> data) {

            this.data = data != null ? data : new ArrayList<>();

            fireTableDataChanged();

        }

        Student getStudentAt(int r) {

            return data.get(r);

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
        public String getColumnName(int c) {

            return columns[c];

        }

        @Override
        public Object getValueAt(int r, int c) {

            Student s = data.get(r);

            return switch (c) {

                case 0 -> s.getId();

                case 1 -> s.getFullName();

                case 2 -> s.getPhone();

                case 3 -> s.getEmail();

                case 4 -> s.getStatus() != null ? s.getStatus().name() : "";

                case 5 -> s.getRegistrationDate() != null ? s.getRegistrationDate().toString() : "";

                default -> "";

            };

        }

    }

}
