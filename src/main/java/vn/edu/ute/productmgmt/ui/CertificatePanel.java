package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Certificate;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.TeachingClass;
import vn.edu.ute.productmgmt.service.CertificateService;
import vn.edu.ute.productmgmt.service.ClassService;
import vn.edu.ute.productmgmt.service.StudentService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CertificatePanel extends JPanel {

    private final CertificateService certificateService;
    private final StudentService studentService;
    private final ClassService classService;

    private final JLabel lblInfo = new JLabel(" ");
    private final CertificateTableModel tableModel = new CertificateTableModel();
    private final JTable table = new JTable(tableModel);
    private Certificate selectedCertificate;

    public CertificatePanel(CertificateService certificateService, StudentService studentService, ClassService classService) {
        this.certificateService = certificateService;
        this.studentService = studentService;
        this.classService = classService;
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
        add(buildTableArea(), BorderLayout.CENTER);
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

    private JComponent buildTableArea() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        UI.stylePanelBorder(wrapper, "Danh sách chứng chỉ");
        JScrollPane scroll = new JScrollPane(table);
        UI.styleTable(table);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private void loadTable() {
        try {
            List<Certificate> list = certificateService.findAll();
            tableModel.setData(list);
            lblInfo.setText("Tổng số: " + list.size() + " chứng chỉ.");
            clearSelection();
        } catch (Exception ex) {
            lblInfo.setText("Lỗi: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Không tải được danh sách: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onTableSelection() {
        int row = table.getSelectedRow();
        selectedCertificate = row < 0 ? null : tableModel.getCertificateAt(row);
    }

    private void onAdd() {
        List<Student> students = studentService.findAll();
        List<TeachingClass> classes = classService.findAll();
        CertificateFormDialog dialog = new CertificateFormDialog(SwingUtilities.getWindowAncestor(this), null, students, classes);
        dialog.setVisible(true);
        if (!dialog.isSaved()) return;
        CertificateFormDialog.CertificateFormData data = dialog.getResult();
        Certificate c = formDataToCertificate(data, null);
        if (c == null) return;
        try {
            certificateService.create(c);
            JOptionPane.showMessageDialog(this, "Đã thêm chứng chỉ.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSave() {
        if (selectedCertificate == null) {
            JOptionPane.showMessageDialog(this, "Chọn một chứng chỉ để sửa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        CertificateFormDialog.CertificateFormData existing = certificateToFormData(selectedCertificate);
        List<Student> students = studentService.findAll();
        List<TeachingClass> classes = classService.findAll();
        CertificateFormDialog dialog = new CertificateFormDialog(SwingUtilities.getWindowAncestor(this), existing, students, classes);
        dialog.setVisible(true);
        if (!dialog.isSaved()) return;
        CertificateFormDialog.CertificateFormData data = dialog.getResult();
        Certificate c = formDataToCertificate(data, selectedCertificate.getId());
        if (c == null) return;
        try {
            certificateService.update(c);
            JOptionPane.showMessageDialog(this, "Đã cập nhật chứng chỉ.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            clearSelection();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        if (selectedCertificate == null) {
            JOptionPane.showMessageDialog(this, "Chọn một chứng chỉ để xóa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa chứng chỉ này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            certificateService.delete(selectedCertificate.getId());
            JOptionPane.showMessageDialog(this, "Đã xóa chứng chỉ.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            clearSelection();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private CertificateFormDialog.CertificateFormData certificateToFormData(Certificate c) {
        CertificateFormDialog.CertificateFormData data = new CertificateFormDialog.CertificateFormData();
        data.setStudentId(c.getStudent() != null && c.getStudent().getId() != null ? c.getStudent().getId().toString() : "");
        data.setClassId(c.getTeachingClass() != null && c.getTeachingClass().getId() != null ? c.getTeachingClass().getId().toString() : null);
        data.setCertName(c.getCertName() != null ? c.getCertName() : "");
        data.setIssueDate(c.getIssueDate() != null ? c.getIssueDate().toString() : "");
        data.setSerialNo(c.getSerialNo() != null ? c.getSerialNo() : "");
        return data;
    }

    private Certificate formDataToCertificate(CertificateFormDialog.CertificateFormData data, Long keepId) {
        String studentIdStr = data.getStudentId() != null ? data.getStudentId().trim() : "";
        if (studentIdStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn học viên.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String certName = data.getCertName() != null ? data.getCertName().trim() : "";
        if (certName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên chứng chỉ không được để trống.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        LocalDate issueDate = parseDateOrToday(data.getIssueDate());
        Long studentId = Long.parseLong(studentIdStr);
        Student student;
        try {
            student = studentService.getById(studentId);
        } catch (Exception ex) {
            student = null;
        }
        if (student == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy học viên.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Certificate cert = new Certificate();
        if (keepId != null) cert.setId(keepId);
        cert.setStudent(student);
        cert.setCertName(certName);
        cert.setIssueDate(issueDate);
        cert.setSerialNo(data.getSerialNo() != null && !data.getSerialNo().trim().isEmpty() ? data.getSerialNo().trim() : null);

        String classIdStr = data.getClassId() != null ? data.getClassId().trim() : "";
        if (!classIdStr.isEmpty()) {
            try {
                Long classId = Long.parseLong(classIdStr);
                for (TeachingClass tc : classService.findAll()) {
                    if (tc.getId() != null && tc.getId().equals(classId)) {
                        cert.setTeachingClass(tc);
                        break;
                    }
                }
            } catch (Exception ignored) { }
        } else {
            cert.setTeachingClass(null);
        }
        return cert;
    }

    private LocalDate parseDateOrToday(String value) {
        String v = value != null ? value.trim() : "";
        if (v.isEmpty()) return LocalDate.now();
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ngày không hợp lệ. Định dạng: yyyy-MM-dd");
        }
    }

    private void clearSelection() {
        selectedCertificate = null;
        table.clearSelection();
    }

    private static class CertificateTableModel extends AbstractTableModel {
        private final String[] columns = {"Tên chứng chỉ", "Học viên", "Lớp", "Ngày cấp", "Số seri"};
        private List<Certificate> data = new ArrayList<>();

        void setData(List<Certificate> data) {
            this.data = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        Certificate getCertificateAt(int row) {
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
            Certificate c = data.get(row);
            return switch (col) {
                case 0 -> c.getCertName() != null ? c.getCertName() : "";
                case 1 -> c.getStudent() != null ? c.getStudent().getFullName() : "";
                case 2 -> c.getTeachingClass() != null ? c.getTeachingClass().getClassName() : "";
                case 3 -> c.getIssueDate() != null ? c.getIssueDate().toString() : "";
                case 4 -> c.getSerialNo() != null ? c.getSerialNo() : "";
                default -> "";
            };
        }
    }
}
