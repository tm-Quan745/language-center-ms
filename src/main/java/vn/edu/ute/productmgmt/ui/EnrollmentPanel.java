package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Enrollment;
import vn.edu.ute.productmgmt.model.enums.EnrollmentResult;
import vn.edu.ute.productmgmt.model.enums.EnrollmentStatus;
import vn.edu.ute.productmgmt.service.EnrollmentService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentPanel extends JPanel {

    private final EnrollmentService enrollmentService;

    private final JTextField txtSearch = new JTextField(20);
    private final JLabel lblInfo = new JLabel(" ");

    private final EnrollmentTableModel tableModel = new EnrollmentTableModel();
    private final JTable table = new JTable(tableModel);
    private Enrollment selectedEnrollment;

    public EnrollmentPanel(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildActionBar(), BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
        add(lblInfo, BorderLayout.SOUTH);

        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onTableSelection();
        });

        loadTableAll();
    }

    private JComponent buildActionBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 4));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        left.add(new JLabel("Tìm (tên học viên/tên lớp):"));
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
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadTableAll();
        });

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
            List<Enrollment> list = enrollmentService.findAll();
            tableModel.setData(list);
            lblInfo.setText("Tổng số: " + list.size() + " ghi danh.");
            clearSelection();
        } catch (Exception ex) {
            lblInfo.setText("Lỗi: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Không tải được danh sách ghi danh: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSearch() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadTableAll();
            return;
        }
        List<Enrollment> src = tableModel.getRawData();
        List<Enrollment> filtered = new ArrayList<>();
        for (Enrollment e : src) {
            String studentName = (e.getStudent() != null && e.getStudent().getFullName() != null)
                    ? e.getStudent().getFullName().toLowerCase() : "";
            String className = (e.getTeachingClass() != null && e.getTeachingClass().getClassName() != null)
                    ? e.getTeachingClass().getClassName().toLowerCase() : "";
            if (studentName.contains(keyword) || className.contains(keyword)) {
                filtered.add(e);
            }
        }
        tableModel.setData(filtered);
        lblInfo.setText("Kết quả: " + filtered.size() + " ghi danh.");
        clearSelection();
    }

    private void onTableSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            selectedEnrollment = null;
            return;
        }
        selectedEnrollment = tableModel.getEnrollmentAt(row);
    }

    private void onAdd() {
        EnrollmentFormDialog dialog = new EnrollmentFormDialog(
                SwingUtilities.getWindowAncestor(this),
                null
        );
        dialog.setVisible(true);
        if (!dialog.isSaved()) return;

        EnrollmentFormDialog.EnrollmentFormData data = dialog.getResult();
        try {
            Long studentId = Long.parseLong(data.getStudentId().trim());
            Long classId = Long.parseLong(data.getClassId().trim());
            LocalDate enrollDate = parseDateOrToday(data.getEnrollmentDate());

            enrollmentService.createEnrollment(
                    studentId,
                    classId,
                    enrollDate,
                    data.getStatus(),
                    data.getResult()
            );
            JOptionPane.showMessageDialog(this, "Đã thêm ghi danh.", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            loadTableAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEdit() {
        if (selectedEnrollment == null) {
            JOptionPane.showMessageDialog(this, "Chọn một ghi danh để sửa.", "Chưa chọn",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        EnrollmentFormDialog.EnrollmentFormData existing = enrollmentToFormData(selectedEnrollment);
        EnrollmentFormDialog dialog = new EnrollmentFormDialog(
                SwingUtilities.getWindowAncestor(this),
                existing
        );
        dialog.setVisible(true);
        if (!dialog.isSaved()) return;

        EnrollmentFormDialog.EnrollmentFormData data = dialog.getResult();
        try {
            Long studentId = Long.parseLong(data.getStudentId().trim());
            Long classId = Long.parseLong(data.getClassId().trim());
            LocalDate enrollDate = parseDateOrToday(data.getEnrollmentDate());

            enrollmentService.updateEnrollment(
                    selectedEnrollment.getId(),
                    studentId,
                    classId,
                    enrollDate,
                    data.getStatus(),
                    data.getResult()
            );
            JOptionPane.showMessageDialog(this, "Đã cập nhật ghi danh.", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            loadTableAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        if (selectedEnrollment == null) {
            JOptionPane.showMessageDialog(this, "Chọn một ghi danh để xóa.", "Chưa chọn",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa ghi danh này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            enrollmentService.delete(selectedEnrollment.getId());
            JOptionPane.showMessageDialog(this, "Đã xóa ghi danh.", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            loadTableAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private EnrollmentFormDialog.EnrollmentFormData enrollmentToFormData(Enrollment e) {
        EnrollmentFormDialog.EnrollmentFormData data = new EnrollmentFormDialog.EnrollmentFormData();
        data.setStudentId(e.getStudent() != null && e.getStudent().getId() != null
                ? e.getStudent().getId().toString() : "");
        data.setClassId(e.getTeachingClass() != null && e.getTeachingClass().getId() != null
                ? e.getTeachingClass().getId().toString() : "");
        data.setEnrollmentDate(e.getEnrollmentDate() != null ? e.getEnrollmentDate().toString() : "");
        data.setStatus(e.getStatus() != null ? e.getStatus() : EnrollmentStatus.Enrolled);
        data.setResult(e.getResult() != null ? e.getResult() : EnrollmentResult.NA);
        return data;
    }

    private LocalDate parseDateOrToday(String value) {
        String v = value != null ? value.trim() : "";
        if (v.isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ngày ghi danh không hợp lệ. Định dạng đúng: yyyy-MM-dd");
        }
    }

    private void clearSelection() {
        selectedEnrollment = null;
        table.clearSelection();
    }

    // === Table model ===
    private static class EnrollmentTableModel extends AbstractTableModel {
        private final String[] columns = {
                "ID",
                "Học viên",
                "Lớp",
                "Ngày ghi danh",
                "Trạng thái",
                "Kết quả"
        };
        private List<Enrollment> data = new ArrayList<>();

        void setData(List<Enrollment> data) {
            this.data = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        List<Enrollment> getRawData() {
            return data;
        }

        Enrollment getEnrollmentAt(int row) {
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
            Enrollment e = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> e.getId();
                case 1 -> e.getStudent() != null ? e.getStudent().getFullName() : "";
                case 2 -> e.getTeachingClass() != null ? e.getTeachingClass().getClassName() : "";
                case 3 -> e.getEnrollmentDate() != null ? e.getEnrollmentDate().toString() : "";
                case 4 -> e.getStatus() != null ? e.getStatus().name() : "";
                case 5 -> e.getResult() != null ? e.getResult().name() : "";
                default -> "";
            };
        }
    }
}

