package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Course;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.model.enums.CourseLevel;
import vn.edu.ute.productmgmt.model.enums.DurationUnit;
import vn.edu.ute.productmgmt.service.CourseService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Panel Khóa học: form (Tên KH, Mức phí, Thời lượng) + JTable danh sách, ghép CourseService.
 */
public class CoursePanel extends JPanel {

    private final CourseService courseService;

    private final JTextField txtCourseName = new JTextField(25);
    private final JTextField txtFee = new JTextField(10);
    private final JTextField txtDuration = new JTextField(15);
    private final JLabel lblInfo = new JLabel(" ");
    private final JComboBox<DurationUnit> cboDurationUnit = new JComboBox<>(DurationUnit.values());
    private final JComboBox<CourseLevel> cboLevel = new JComboBox<>(CourseLevel.values());
    private final JComboBox<ActiveStatus> cboStatus = new JComboBox<>(ActiveStatus.values());
    private final JTextArea txtDescription = new JTextArea(4, 25);
    private final CourseTableModel tableModel = new CourseTableModel();
    private final JTable table = new JTable(tableModel);
    private Course selectedCourse;

    public CoursePanel(CourseService courseService) {
        this.courseService = courseService;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            onTableSelection();
        });
        loadTable();
    }

    private void buildUI() {
        add(buildActionBar(), BorderLayout.NORTH);
        add(buildFormAndTable(), BorderLayout.CENTER);
        add(lblInfo, BorderLayout.SOUTH);
    }

    private JComponent buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton btnAdd = new JButton("Thêm mới");
        JButton btnSave = new JButton("Chỉnh sửa");
        JButton btnDelete = new JButton("Xóa");
        JButton btnRefresh = new JButton("Tải lại");

        btnAdd.addActionListener(e -> onAdd());
        btnSave.addActionListener(e -> onSave());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> loadTable());

        bar.add(btnAdd);
        bar.add(btnSave);
        bar.add(btnDelete);
        bar.add(btnRefresh);
        return bar;
    }

    private JComponent buildFormAndTable() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        UI.stylePanelBorder(wrapper, "Danh sách khóa học");

        JScrollPane scroll = new JScrollPane(table);
        UI.styleTable(table);
        wrapper.add(scroll, BorderLayout.CENTER);

        return wrapper;
    }

    private void addField(JPanel form, GridBagConstraints g, int row, int col, String label, JComponent field) {
        g.gridy = row;
        g.gridx = col * 2;
        g.weightx = 0.0;
        form.add(new JLabel(label), g);
        g.gridx = col * 2 + 1;
        g.weightx = 1.0;
        form.add(field, g);
    }

    private void loadTable() {
        try {
            List<Course> list = courseService.findAll();
            tableModel.setData(list);
            lblInfo.setText("Tổng số: " + list.size() + " khóa học.");
            clearSelection();
        } catch (Exception ex) {
            lblInfo.setText("Lỗi: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Không tải được danh sách: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onTableSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            selectedCourse = null;
            return;
        }
        selectedCourse = tableModel.getCourseAt(row);
    }

    private void onAdd() {
        CourseFormDialog dialog = new CourseFormDialog(
                SwingUtilities.getWindowAncestor(this),
                null
        );
        dialog.setVisible(true);
        if (!dialog.isSaved()) {
            return;
        }

        CourseFormDialog.CourseFormData data = dialog.getResult();
        Course c = formDataToCourse(data, null);
        if (c == null) return;
        try {
            courseService.create(c);
            JOptionPane.showMessageDialog(this, "Đã thêm khóa học.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSave() {
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Chọn một khóa học để sửa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        CourseFormDialog.CourseFormData existing = courseToFormData(selectedCourse);
        CourseFormDialog dialog = new CourseFormDialog(
                SwingUtilities.getWindowAncestor(this),
                existing
        );
        dialog.setVisible(true);
        if (!dialog.isSaved()) {
            return;
        }

        CourseFormDialog.CourseFormData data = dialog.getResult();
        Course c = formDataToCourse(data, selectedCourse.getId());
        if (c == null) return;
        try {
            courseService.update(c);
            JOptionPane.showMessageDialog(this, "Đã cập nhật khóa học.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            clearSelection();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private CourseFormDialog.CourseFormData courseToFormData(Course c) {
        CourseFormDialog.CourseFormData data = new CourseFormDialog.CourseFormData();
        data.setName(c.getCourseName());
        data.setDescription(c.getDescription());
        data.setLevel(c.getLevel());
        data.setDuration(c.getDuration() != null ? c.getDuration().toString() : "");
        data.setDurationUnit(c.getDurationUnit());
        data.setFee(c.getFee() != null ? c.getFee().toPlainString() : "");
        data.setStatus(c.getStatus());
        return data;
    }

    private void onDelete() {
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Chọn một khóa học để xóa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa khóa học này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            courseService.delete(selectedCourse.getId());
            JOptionPane.showMessageDialog(this, "Đã xóa khóa học.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            clearSelection();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Course formDataToCourse(CourseFormDialog.CourseFormData data, UUID keepId) {
        String name = data.getName() != null ? data.getName().trim() : "";
        String feeStr = data.getFee() != null ? data.getFee().trim() : "";
        String durationStr = data.getDuration() != null ? data.getDuration().trim() : "";
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên khóa học không được để trống.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        BigDecimal fee = BigDecimal.ZERO;
        if (!feeStr.isEmpty()) {
            try {
                fee = new BigDecimal(feeStr.replace(",", "."));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Mức phí không hợp lệ.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }
        Integer duration = null;
        if (!durationStr.isEmpty()) {
            try {
                duration = Integer.parseInt(durationStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Thời lượng không hợp lệ.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }
        Course c = new Course();
        c.setCourseName(name);
        c.setDescription(data.getDescription());
        c.setLevel(data.getLevel());
        c.setDuration(duration);
        c.setDurationUnit(data.getDurationUnit() != null ? data.getDurationUnit() : DurationUnit.Week);
        c.setFee(fee);
        c.setStatus(data.getStatus());
        return c;
    }

    private Course formToCourse(UUID keepId) {
        String name = txtCourseName.getText().trim();
        String feeStr = txtFee.getText().trim();
        String durationStr = txtDuration.getText().trim();
        CourseLevel level = (CourseLevel) cboLevel.getSelectedItem();
        ActiveStatus status = (ActiveStatus) cboStatus.getSelectedItem();
        DurationUnit durationUnit = (DurationUnit) cboDurationUnit.getSelectedItem();
        String description = txtDescription.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên khóa học không được để trống.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        BigDecimal fee = BigDecimal.ZERO;
        if (!feeStr.isEmpty()) {
            try {
                fee = new BigDecimal(feeStr.replace(",", "."));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Mức phí không hợp lệ.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }
        Integer duration = null;
        if (!durationStr.isEmpty()) {
            try {
                duration = Integer.parseInt(durationStr.trim());
            } catch (NumberFormatException ignored) { }
        }
        Course c = new Course();
        c.setCourseName(name);
        c.setDescription(description.isEmpty() ? null : description);
        c.setLevel(level);
        c.setFee(fee);
        c.setDuration(duration);
        c.setDurationUnit(durationUnit != null ? durationUnit : DurationUnit.Week);
        c.setStatus(status != null ? status : ActiveStatus.Active);
        return c;
    }

    private void clearForm() {
        txtCourseName.setText("");
        txtFee.setText("");
        txtDuration.setText("");
        txtDescription.setText("");
        cboLevel.setSelectedItem(null);
        cboStatus.setSelectedItem(ActiveStatus.Active);
        cboDurationUnit.setSelectedItem(DurationUnit.Week);
        selectedCourse = null;
    }

    private void clearSelection() {
        selectedCourse = null;
        clearForm();
        table.clearSelection();
    }

    // --- Table model ---
    private static class CourseTableModel extends AbstractTableModel {
        private final String[] columns = {
                "Tên khóa học",
                "Mức phí",
                "Thời lượng",
                "Mức độ",
                "Trạng thái",
                "Mô tả"
        };
        private List<Course> data = new ArrayList<>();

        void setData(List<Course> data) {
            this.data = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        Course getCourseAt(int row) {
            if (row < 0 || row >= data.size()) return null;
            return data.get(row);
        }

        @Override
        public int getRowCount() { return data.size(); }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Course c = data.get(row);
            switch (col) {
                case 0:
                    return c.getCourseName();
                case 1:
                    return c.getFee() != null ? c.getFee().toPlainString() : "";
                case 2:
                    return c.getDuration() != null
                            ? c.getDuration() + " " + (c.getDurationUnit() != null ? c.getDurationUnit().name() : "")
                            : "";
                case 3:
                    return c.getLevel() != null ? c.getLevel().name() : "";
                case 4:
                    return c.getStatus() != null ? c.getStatus().name() : "";
                case 5:
                    return c.getDescription() != null ? c.getDescription() : "";
                default:
                    return "";
            }
        }
    }
}
