package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Teacher;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.service.TeacherService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel quản lý giáo viên, chuẩn hóa theo ClassPanel
 */
public class TeacherPanel extends JPanel {

    private final TeacherService teacherService;

    private final JTextField txtSearch = new JTextField(18);
    private final JLabel lblInfo = new JLabel(" ");

    private final TeacherTableModel tableModel = new TeacherTableModel();
    private final JTable table = new JTable(tableModel);
    private Teacher selectedTeacher;

    public TeacherPanel(TeacherService teacherService) {
        this.teacherService = teacherService;

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

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Quản lý Giáo viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        header.add(title, BorderLayout.WEST);
        header.add(buildActionBar(), BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

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

        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm giáo viên...");
        txtSearch.setPreferredSize(new Dimension(180, 36));

        JButton btnSearch = new JButton("Tìm");
        btnSearch.setPreferredSize(new Dimension(70, 36));

        JButton btnAdd = createBtn("Thêm", "#0d6efd", "➕ ");
        JButton btnEdit = createBtn("Sửa", "#ffc107", "📝 ");
        JButton btnDelete = createBtn("Xóa", "#dc3545", "🗑 ");
        JButton btnRefresh = new JButton("Tải lại");

        btnRefresh.setPreferredSize(new Dimension(90, 36));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc:10");

        btnSearch.addActionListener(e -> onSearch());
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> loadTableAll());

        bar.add(txtSearch);
        bar.add(btnSearch);
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

            List<Teacher> list = teacherService.findAll();

            tableModel.setData(list);

            lblInfo.setText("Tổng số: " + list.size() + " giáo viên");

            table.clearSelection();

            selectedTeacher = null;

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());

        }
    }

    private void onSearch() {

        String kw = txtSearch.getText().trim().toLowerCase();

        List<Teacher> all = teacherService.findAll();

        List<Teacher> filtered = new ArrayList<>();

        for (Teacher t : all) {

            if ((t.getFullName() != null &&
                    t.getFullName().toLowerCase().contains(kw)) ||
                    (t.getPhone() != null &&
                            t.getPhone().toLowerCase().contains(kw)) ||
                    (t.getSpecialty() != null &&
                            t.getSpecialty().toLowerCase().contains(kw))) {

                filtered.add(t);

            }

        }

        tableModel.setData(filtered);

        lblInfo.setText("Tìm thấy: " + filtered.size() + " kết quả");

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

        if (dialog.isSaved()) {

            try {

                TeacherFormDialog.TeacherFormData data = dialog.getResult();
                Teacher t = formDataToTeacher(data, null);
                if (t == null) return;

                teacherService.create(t);

                loadTableAll();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Lỗi thêm giáo viên: " + ex.getMessage());

            }

        }

    }

    private void onEdit() {

        if (selectedTeacher == null) {

            JOptionPane.showMessageDialog(this, "Vui lòng chọn giáo viên để sửa");

            return;

        }

        TeacherFormDialog.TeacherFormData existing = teacherToFormData(selectedTeacher);
        TeacherFormDialog dialog = new TeacherFormDialog(
                SwingUtilities.getWindowAncestor(this),
                existing
        );

        dialog.setVisible(true);

        if (dialog.isSaved()) {

            try {

                TeacherFormDialog.TeacherFormData data = dialog.getResult();
                Teacher t = formDataToTeacher(data, selectedTeacher.getId());
                if (t == null) return;

                teacherService.update(t);

                loadTableAll();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Lỗi cập nhật: " + ex.getMessage());

            }

        }

    }

    private void onDelete() {

        if (selectedTeacher == null) return;

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Xóa giáo viên " + selectedTeacher.getFullName() + " ?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (ok == JOptionPane.YES_OPTION) {

            try {

                teacherService.delete(selectedTeacher.getId());

                loadTableAll();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Lỗi xóa: " + ex.getMessage());

            }

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
            JOptionPane.showMessageDialog(this, "Tên giáo viên không được để trống.", "Lỗi",
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

        LocalDate hireDate = null;
        if (!hireDateStr.isEmpty()) {
            try {
                hireDate = LocalDate.parse(hireDateStr);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "Ngày vào làm không hợp lệ. Định dạng đúng: yyyy-MM-dd.",
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

    private static class TeacherTableModel extends AbstractTableModel {

        private final String[] columns = {
                "Mã giáo viên",
                "Họ tên",
                "Số điện thoại",
                "Email",
                "Chuyên môn",
                "Trạng thái"
        };

        private List<Teacher> data = new ArrayList<>();

        void setData(List<Teacher> data) {

            this.data = data != null ? data : new ArrayList<>();

            fireTableDataChanged();

        }

        Teacher getTeacherAt(int r) {

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

            Teacher t = data.get(r);

            return switch (c) {

                case 0 -> t.getId();

                case 1 -> t.getFullName();

                case 2 -> t.getPhone();

                case 3 -> t.getEmail();

                case 4 -> t.getSpecialty() != null ? t.getSpecialty() : "";

                case 5 -> t.getStatus() != null ? t.getStatus().name() : "";

                default -> "";

            };

        }

    }

}
