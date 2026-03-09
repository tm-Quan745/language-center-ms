package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Enrollment;
import vn.edu.ute.productmgmt.service.ClassService;
import vn.edu.ute.productmgmt.service.EnrollmentService;
import vn.edu.ute.productmgmt.service.StudentService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentPanel extends JPanel {

    private final EnrollmentService enrollmentService;
    private final StudentService studentService; // Thêm Service
    private final ClassService classService;     // Thêm Service

    private final JTextField txtSearch = new JTextField(20);
    private final JLabel lblInfo = new JLabel(" ");
    private final EnrollmentTableModel tableModel = new EnrollmentTableModel();
    private final JTable table = new JTable(tableModel);
    private Enrollment selectedEnrollment;

    public EnrollmentPanel(EnrollmentService enrollmentService,
                           StudentService studentService,
                           ClassService classService) {
        this.enrollmentService = enrollmentService;
        this.studentService = studentService;
        this.classService = classService;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        buildUI();
        loadTableAll();
    }

    private void buildUI() {
        add(buildActionBar(), BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onTableSelection();
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(lblInfo, BorderLayout.SOUTH);
    }

    private JComponent buildActionBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 4));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        left.add(new JLabel("Tìm kiếm:"));
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

        right.add(btnAdd); right.add(btnEdit);
        right.add(btnDelete); right.add(btnRefresh);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void loadTableAll() {
        try {
            List<Enrollment> list = enrollmentService.findAll();
            tableModel.setData(list);
            lblInfo.setText("Tổng số: " + list.size() + " ghi danh.");
            clearSelection();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }

    private void onAdd() {
        // Truyền trực tiếp Service vào Dialog
        EnrollmentFormDialog dialog = new EnrollmentFormDialog(
                SwingUtilities.getWindowAncestor(this),
                null,
                studentService,
                classService
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            try {
                EnrollmentFormDialog.EnrollmentFormData data = dialog.getResult();
                // Service xử lý tạo mới với các tham số từ DTO
                enrollmentService.createEnrollment(
                        data.getStudentId(),
                        data.getClassId(),
                        java.time.LocalDate.parse(data.getEnrollmentDate()),
                        data.getStatus(),
                        data.getResult()
                );
                loadTableAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi thêm mới: " + ex.getMessage());
            }
        }
    }

    private void onEdit() {
        if (selectedEnrollment == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi!");
            return;
        }

        EnrollmentFormDialog dialog = new EnrollmentFormDialog(
                SwingUtilities.getWindowAncestor(this),
                enrollmentToFormData(selectedEnrollment),
                studentService,
                classService
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            try {
                EnrollmentFormDialog.EnrollmentFormData data = dialog.getResult();
                enrollmentService.updateEnrollment(
                        selectedEnrollment.getId(),
                        data.getStudentId(),
                        data.getClassId(),
                        java.time.LocalDate.parse(data.getEnrollmentDate()),
                        data.getStatus(),
                        data.getResult()
                );
                loadTableAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi cập nhật: " + ex.getMessage());
            }
        }
    }

    private void onDelete() {
        if (selectedEnrollment == null) return;
        int ok = JOptionPane.showConfirmDialog(this, "Xác nhận xóa ghi danh này?", "Xóa", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            try {
                enrollmentService.delete(selectedEnrollment.getId());
                loadTableAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa: " + ex.getMessage());
            }
        }
    }

    private EnrollmentFormDialog.EnrollmentFormData enrollmentToFormData(Enrollment e) {
        EnrollmentFormDialog.EnrollmentFormData data = new EnrollmentFormDialog.EnrollmentFormData();
        data.setStudentId(e.getStudent().getId());
        data.setClassId(e.getTeachingClass().getId());
        data.setEnrollmentDate(e.getEnrollmentDate().toString());
        data.setStatus(e.getStatus());
        data.setResult(e.getResult());
        return data;
    }

    private void onSearch() {
        String kw = txtSearch.getText().trim().toLowerCase();
        List<Enrollment> filtered = enrollmentService.findAll().stream()
                .filter(e -> e.getStudent().getFullName().toLowerCase().contains(kw) ||
                        e.getTeachingClass().getClassName().toLowerCase().contains(kw))
                .toList();
        tableModel.setData(filtered);
        lblInfo.setText("Tìm thấy: " + filtered.size());
    }

    private void onTableSelection() {
        int row = table.getSelectedRow();
        if (row >= 0) selectedEnrollment = tableModel.getEnrollmentAt(row);
    }

    private void clearSelection() {
        selectedEnrollment = null;
        table.clearSelection();
    }

    // === Table Model (Giữ nguyên cấu trúc AbstractTableModel) ===
    private static class EnrollmentTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Học viên", "Lớp", "Ngày ghi danh", "Trạng thái", "Kết quả"};
        private List<Enrollment> data = new ArrayList<>();

        void setData(List<Enrollment> data) { this.data = data; fireTableDataChanged(); }
        Enrollment getEnrollmentAt(int r) { return data.get(r); }
        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int c) { return columns[c]; }
        @Override public Object getValueAt(int r, int c) {
            Enrollment e = data.get(r);
            return switch (c) {
                case 0 -> e.getId();
                case 1 -> e.getStudent().getFullName();
                case 2 -> e.getTeachingClass().getClassName();
                case 3 -> e.getEnrollmentDate();
                case 4 -> e.getStatus();
                case 5 -> e.getResult();
                default -> "";
            };
        }
    }
}