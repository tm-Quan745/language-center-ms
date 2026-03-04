package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Teacher;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.service.TeacherService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel quản lý giáo viên, ghép với TeacherService.
 */
public class TeacherPanel extends JPanel {

    private final TeacherService teacherService;

    private final JTextField txtSearch = new JTextField(20);
    private final JComboBox<String> cboSpecialtyFilter =
            new JComboBox<>(new String[]{"Tất cả", "IELTS", "TOEIC", "Communication"});
    private final JLabel lblInfo = new JLabel(" ");

    private final TeacherTableModel tableModel = new TeacherTableModel();
    private final JTable table = new JTable(tableModel);
    private Teacher selectedTeacher;

    public TeacherPanel(TeacherService teacherService) {
        this.teacherService = teacherService;
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
        left.add(new JLabel("Chuyên môn:"));
        left.add(cboSpecialtyFilter);
        JButton btnSearch = new JButton("Lọc");
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
        return new JScrollPane(table);
    }

    private void loadTableAll() {
        try {
            List<Teacher> list = teacherService.findAll();
            tableModel.setData(list);
            lblInfo.setText("Tổng số: " + list.size() + " giáo viên.");
            clearSelection();
        } catch (Exception ex) {
            lblInfo.setText("Lỗi: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Không tải được danh sách giáo viên: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTableFiltered(String keyword, String specialtyFilter) {
        try {
            List<Teacher> list = teacherService.findAll();
            String kw = keyword == null ? "" : keyword.trim().toLowerCase();
            String spec = specialtyFilter == null ? "" : specialtyFilter.trim().toLowerCase();

            List<Teacher> filtered = new ArrayList<>();
            for (Teacher t : list) {
                boolean matchKw = kw.isEmpty()
                        || (t.getFullName() != null && t.getFullName().toLowerCase().contains(kw))
                        || (t.getPhone() != null && t.getPhone().toLowerCase().contains(kw));
                boolean matchSpec = spec.isEmpty()
                        || "tất cả".equals(spec)
                        || (t.getSpecialty() != null && t.getSpecialty().toLowerCase().contains(spec));
                if (matchKw && matchSpec) {
                    filtered.add(t);
                }
            }

            tableModel.setData(filtered);
            lblInfo.setText("Kết quả: " + filtered.size() + " giáo viên.");
            clearSelection();
        } catch (Exception ex) {
            lblInfo.setText("Lỗi: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Không lọc được danh sách giáo viên: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSearch() {
        String keyword = txtSearch.getText();
        String spec = (String) cboSpecialtyFilter.getSelectedItem();
        loadTableFiltered(keyword, spec);
    }

    private void onTableSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            selectedTeacher = null;
            return;
        }
        selectedTeacher = tableModel.getTeacherAt(row);
    }

    private void onAdd() {
        TeacherFormDialog dialog = new TeacherFormDialog(
                SwingUtilities.getWindowAncestor(this),
                null
        );
        dialog.setVisible(true);
        if (!dialog.isSaved()) return;

        TeacherFormDialog.TeacherFormData data = dialog.getResult();
        Teacher t = formDataToTeacher(data, null);
        if (t == null) return;
        try {
            teacherService.create(t);
            JOptionPane.showMessageDialog(this,
                    "Đã thêm giáo viên.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            loadTableAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEdit() {
        if (selectedTeacher == null) {
            JOptionPane.showMessageDialog(this,
                    "Chọn một giáo viên để sửa.",
                    "Chưa chọn",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        TeacherFormDialog.TeacherFormData existing = teacherToFormData(selectedTeacher);
        TeacherFormDialog dialog = new TeacherFormDialog(
                SwingUtilities.getWindowAncestor(this),
                existing
        );
        dialog.setVisible(true);
        if (!dialog.isSaved()) return;

        TeacherFormDialog.TeacherFormData data = dialog.getResult();
        Teacher t = formDataToTeacher(data, selectedTeacher.getId());
        if (t == null) return;
        try {
            teacherService.update(t);
            JOptionPane.showMessageDialog(this,
                    "Đã cập nhật giáo viên.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            loadTableAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        if (selectedTeacher == null) {
            JOptionPane.showMessageDialog(this,
                    "Chọn một giáo viên để xóa.",
                    "Chưa chọn",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa giáo viên này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            teacherService.delete(selectedTeacher.getId());
            JOptionPane.showMessageDialog(this,
                    "Đã xóa giáo viên.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            loadTableAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private TeacherFormDialog.TeacherFormData teacherToFormData(Teacher t) {
        TeacherFormDialog.TeacherFormData data = new TeacherFormDialog.TeacherFormData();
        data.setFullName(t.getFullName());
        data.setPhone(t.getPhone());
        data.setEmail(t.getEmail());
        data.setSpecialty(t.getSpecialty());
        data.setHireDate(t.getHireDate() != null ? t.getHireDate().toString() : "");
        data.setStatus(t.getStatus());
        return data;
    }

    private Teacher formDataToTeacher(TeacherFormDialog.TeacherFormData data, Long keepId) {
        String fullName = data.getFullName() != null ? data.getFullName().trim() : "";
        String phone = data.getPhone() != null ? data.getPhone().trim() : "";
        String email = data.getEmail() != null ? data.getEmail().trim() : "";
        String specialty = data.getSpecialty() != null ? data.getSpecialty().trim() : "";
        String hireDateStr = data.getHireDate() != null ? data.getHireDate().trim() : "";

        if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Tên giáo viên không được để trống.",
                    "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Số điện thoại không được để trống.",
                    "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Email không được để trống.",
                    "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }

        LocalDate hireDate = null;
        if (!hireDateStr.isEmpty()) {
            try {
                hireDate = LocalDate.parse(hireDateStr);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "Ngày tuyển dụng không hợp lệ. Định dạng đúng: yyyy-MM-dd.",
                        "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }

        Teacher t = new Teacher();
        if (keepId != null) t.setId(keepId);
        t.setFullName(fullName);
        t.setPhone(phone);
        t.setEmail(email);
        t.setSpecialty(specialty.isEmpty() ? null : specialty);
        t.setHireDate(hireDate);
        t.setStatus(data.getStatus() != null ? data.getStatus() : ActiveStatus.Active);
        return t;
    }

    private void clearSelection() {
        selectedTeacher = null;
        table.clearSelection();
    }

    // === Table model ===
    private static class TeacherTableModel extends AbstractTableModel {
        private final String[] columns = {
                "ID",
                "Họ tên",
                "SĐT",
                "Email",
                "Chuyên môn",
                "Trạng thái"
        };
        private List<Teacher> data = new ArrayList<>();

        void setData(List<Teacher> data) {
            this.data = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        Teacher getTeacherAt(int row) {
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
            Teacher t = data.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return t.getId();
                case 1:
                    return t.getFullName();
                case 2:
                    return t.getPhone();
                case 3:
                    return t.getEmail();
                case 4:
                    return t.getSpecialty();
                case 5:
                    return t.getStatus() != null ? t.getStatus().name() : "";
                default:
                    return "";
            }
        }
    }
}

