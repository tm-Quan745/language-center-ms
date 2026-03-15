package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Certificate;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.TeachingClass;
import vn.edu.ute.productmgmt.service.CertificateService;
import vn.edu.ute.productmgmt.service.ClassService;
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
 * Panel quản lý chứng chỉ, chuẩn hóa theo NotificationPanel
 */
public class CertificatePanel extends JPanel {

    private final CertificateService certificateService;
    private final StudentService studentService;
    private final ClassService classService;

    private final JTextField txtSearch = new JTextField(18);

    private final JLabel lblInfo = new JLabel(" ");
    private final CertificateTableModel tableModel = new CertificateTableModel();
    private final JTable table = new JTable(tableModel);
    private Certificate selectedCertificate;

    public CertificatePanel(CertificateService certificateService, StudentService studentService, ClassService classService) {
        this.certificateService = certificateService;
        this.studentService = studentService;
        this.classService = classService;

        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        buildUI();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            onTableSelection();
        });

        loadTable();
    }

    private void buildUI() {

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // LEFT: search area
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm tên chứng chỉ hoặc tên học viên...");
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
        btnEdit.addActionListener(e -> onSave());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> loadTable());

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

    private void loadTable() {

        try {

            List<Certificate> list = certificateService.findAll();

            tableModel.setData(list);

            lblInfo.setText("Tổng số: " + list.size() + " chứng chỉ");

            table.clearSelection();

            selectedCertificate = null;

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());

        }
    }

    private void onTableSelection() {

        int row = table.getSelectedRow();

        if (row < 0) {

            selectedCertificate = null;

            return;

        }

        selectedCertificate = tableModel.getCertificateAt(row);

    }

    private void onAdd() {

        List<Student> students = studentService.findAll();
        List<TeachingClass> classes = classService.findAll();

        CertificateFormDialog dialog = new CertificateFormDialog(
                SwingUtilities.getWindowAncestor(this),
                null,
                students,
                classes
        );

        dialog.setVisible(true);

        if (dialog.isSaved()) {

            try {

                CertificateFormDialog.CertificateFormData data = dialog.getResult();
                Certificate c = formDataToCertificate(data, null);
                if (c == null) return;

                certificateService.create(c);

                loadTable();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Lỗi thêm chứng chỉ: " + ex.getMessage());

            }

        }

    }

    private void onSave() {

        if (selectedCertificate == null) {

            JOptionPane.showMessageDialog(this, "Vui lòng chọn chứng chỉ để sửa");

            return;

        }

        CertificateFormDialog.CertificateFormData existing = certificateToFormData(selectedCertificate);

        List<Student> students = studentService.findAll();
        List<TeachingClass> classes = classService.findAll();

        CertificateFormDialog dialog = new CertificateFormDialog(
                SwingUtilities.getWindowAncestor(this),
                existing,
                students,
                classes
        );

        dialog.setVisible(true);

        if (dialog.isSaved()) {

            try {

                CertificateFormDialog.CertificateFormData data = dialog.getResult();
                Certificate c = formDataToCertificate(data, selectedCertificate.getId());
                if (c == null) return;

                certificateService.update(c);

                loadTable();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Lỗi cập nhật: " + ex.getMessage());

            }

        }

    }

    private void onDelete() {

        if (selectedCertificate == null) return;

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Xóa chứng chỉ này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (ok == JOptionPane.YES_OPTION) {

            try {

                certificateService.delete(selectedCertificate.getId());

                loadTable();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Lỗi xóa: " + ex.getMessage());

            }

        }

    }

    private void onSearch() {
        String kw = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        if (kw.isEmpty()) {
            loadTable();
            return;
        }
        List<Certificate> all = certificateService.findAll();
        List<Certificate> filtered = new ArrayList<>();
        for (Certificate c : all) {
            String certName = c.getCertName() != null ? c.getCertName().toLowerCase() : "";
            String studentName = c.getStudent() != null && c.getStudent().getFullName() != null ? c.getStudent().getFullName().toLowerCase() : "";
            if (certName.contains(kw) || studentName.contains(kw)) {
                filtered.add(c);
            }
        }
        tableModel.setData(filtered);
        lblInfo.setText("Tìm thấy: " + filtered.size() + " kết quả");
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

    private static class CertificateTableModel extends AbstractTableModel {

        private final String[] columns = {
                "Tên chứng chỉ",
                "Học viên",
                "Lớp",
                "Ngày cấp",
                "Số seri"
        };

        private List<Certificate> data = new ArrayList<>();

        void setData(List<Certificate> data) {

            this.data = data != null ? data : new ArrayList<>();

            fireTableDataChanged();

        }

        Certificate getCertificateAt(int r) {

            return (r >= 0 && r < data.size()) ? data.get(r) : null;

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

            Certificate cert = data.get(r);

            return switch (c) {

                case 0 -> cert.getCertName() != null ? cert.getCertName() : "";

                case 1 -> cert.getStudent() != null ? cert.getStudent().getFullName() : "";

                case 2 -> cert.getTeachingClass() != null ? cert.getTeachingClass().getClassName() : "";

                case 3 -> cert.getIssueDate() != null ? cert.getIssueDate().toString() : "";

                case 4 -> cert.getSerialNo() != null ? cert.getSerialNo() : "";

                default -> "";

            };

        }

    }

}
