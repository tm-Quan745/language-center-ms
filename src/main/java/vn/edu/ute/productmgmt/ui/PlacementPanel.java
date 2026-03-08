package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.PlacementTest;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.service.PlacementTestService;
import vn.edu.ute.productmgmt.service.StudentService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel Kiểm tra xếp lớp: bảng danh sách + Thêm / Sửa / Xóa.
 */
public class PlacementPanel extends JPanel {

    private final PlacementTestService placementTestService;
    private final StudentService studentService;

    private final JLabel lblInfo = new JLabel(" ");
    private final PlacementTableModel tableModel = new PlacementTableModel();
    private final JTable table = new JTable(tableModel);
    private PlacementTest selectedPlacement;

    public PlacementPanel(PlacementTestService placementTestService,
                          StudentService studentService) {
        this.placementTestService = placementTestService;
        this.studentService = studentService;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onTableSelection();
        });
        loadTable();
    }

    private void buildUI() {
        add(buildActionBar(), BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
        add(lblInfo, BorderLayout.SOUTH);
    }

    private JComponent buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton btnAdd = new JButton("Thêm mới");
        JButton btnEdit = new JButton("Chỉnh sửa");
        JButton btnDelete = new JButton("Xóa");
        JButton btnRefresh = new JButton("Tải lại");
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> loadTable());
        bar.add(btnAdd);
        bar.add(btnEdit);
        bar.add(btnDelete);
        bar.add(btnRefresh);
        return bar;
    }

    private JComponent buildTableArea() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        UI.stylePanelBorder(wrapper, "Danh sách kiểm tra xếp lớp");
        JScrollPane scroll = new JScrollPane(table);
        UI.styleTable(table);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private void loadTable() {
        try {
            List<PlacementTest> list = placementTestService.findAll();
            tableModel.setData(list);
            lblInfo.setText("Tổng số: " + list.size() + " bài kiểm tra.");
            clearSelection();
        } catch (Exception ex) {
            lblInfo.setText("Lỗi: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Không tải được danh sách: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onTableSelection() {
        int row = table.getSelectedRow();
        selectedPlacement = row < 0 ? null : tableModel.getPlacementAt(row);
    }

    private void onAdd() {
        List<Student> students = studentService.findAll();
        PlacementFormDialog dialog = new PlacementFormDialog(
                SwingUtilities.getWindowAncestor(this),
                null,
                students
        );
        dialog.setVisible(true);
        if (!dialog.isSaved()) return;

        PlacementFormDialog.PlacementFormData data = dialog.getResult();
        try {
            LocalDate testDate = parseDate(data.getTestDate());
            if (testDate == null) testDate = LocalDate.now();
            BigDecimal score = data.getScore() != null && !data.getScore().trim().isEmpty()
                    ? new BigDecimal(data.getScore().trim()) : null;

            placementTestService.createPlacementTest(
                    Long.parseLong(data.getStudentId().trim()),
                    testDate,
                    score,
                    data.getSuggestedLevel(),
                    data.getNote()
            );
            JOptionPane.showMessageDialog(this, "Đã thêm bài kiểm tra xếp lớp.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEdit() {
        if (selectedPlacement == null) {
            JOptionPane.showMessageDialog(this, "Chọn một bài kiểm tra để sửa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Student> students = studentService.findAll();
        PlacementFormDialog.PlacementFormData existing = placementToFormData(selectedPlacement);
        PlacementFormDialog dialog = new PlacementFormDialog(
                SwingUtilities.getWindowAncestor(this),
                existing,
                students
        );
        dialog.setVisible(true);
        if (!dialog.isSaved()) return;

        PlacementFormDialog.PlacementFormData data = dialog.getResult();
        try {
            LocalDate testDate = parseDate(data.getTestDate());
            if (testDate == null) testDate = LocalDate.now();
            BigDecimal score = data.getScore() != null && !data.getScore().trim().isEmpty()
                    ? new BigDecimal(data.getScore().trim()) : null;

            placementTestService.updatePlacementTest(
                    selectedPlacement.getId(),
                    Long.parseLong(data.getStudentId().trim()),
                    testDate,
                    score,
                    data.getSuggestedLevel(),
                    data.getNote()
            );
            JOptionPane.showMessageDialog(this, "Đã cập nhật bài kiểm tra xếp lớp.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            clearSelection();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        if (selectedPlacement == null) {
            JOptionPane.showMessageDialog(this, "Chọn một bài kiểm tra để xóa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa bài kiểm tra này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            placementTestService.delete(selectedPlacement.getId());
            JOptionPane.showMessageDialog(this, "Đã xóa bài kiểm tra.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            clearSelection();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private PlacementFormDialog.PlacementFormData placementToFormData(PlacementTest pt) {
        PlacementFormDialog.PlacementFormData data = new PlacementFormDialog.PlacementFormData();
        data.setStudentId(pt.getStudent() != null && pt.getStudent().getId() != null ? pt.getStudent().getId().toString() : "");
        data.setTestDate(pt.getTestDate() != null ? pt.getTestDate().toString() : "");
        data.setScore(pt.getScore() != null ? pt.getScore().toPlainString() : "");
        data.setSuggestedLevel(pt.getSuggestedLevel());
        data.setNote(pt.getNote());
        return data;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void clearSelection() {
        selectedPlacement = null;
        table.clearSelection();
    }

    private static class PlacementTableModel extends AbstractTableModel {
        private final String[] columns = { "ID", "Học viên", "Ngày kiểm tra", "Điểm", "Cấp độ gợi ý", "Ghi chú" };
        private List<PlacementTest> data = new ArrayList<>();

        void setData(List<PlacementTest> data) {
            this.data = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        PlacementTest getPlacementAt(int row) {
            return (row >= 0 && row < data.size()) ? data.get(row) : null;
        }

        @Override
        public int getRowCount() { return data.size(); }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            PlacementTest p = data.get(row);
            return switch (col) {
                case 0 -> p.getId();
                case 1 -> p.getStudent() != null ? p.getStudent().getFullName() : "";
                case 2 -> p.getTestDate() != null ? p.getTestDate().toString() : "";
                case 3 -> p.getScore() != null ? p.getScore().toPlainString() : "";
                case 4 -> p.getSuggestedLevel() != null ? p.getSuggestedLevel().name() : "";
                case 5 -> p.getNote() != null ? p.getNote() : "";
                default -> "";
            };
        }
    }
}
