package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.enums.SuggestedLevel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class PlacementFormDialog extends JDialog {

    private static final String DATE_FORMAT = "dd/MM/yyyy";

    private final JComboBox<Student> cboStudent;
    private final JSpinner spnTestDate;
    private final JTextField txtScore = new JTextField();
    private final JComboBox<SuggestedLevel> cboSuggestedLevel;
    private final JTextArea txtNote = new JTextArea(6, 30);

    private boolean saved = false;
    private PlacementFormData result;

    public PlacementFormDialog(Window owner,
                               PlacementFormData existing,
                               List<Student> students) {

        super(owner, "Kiểm tra xếp lớp", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setSize(600, 550);
        setLayout(new BorderLayout());

        cboStudent = new JComboBox<>(students != null ? students.toArray(new Student[0]) : new Student[0]);
        cboStudent.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Student s) {
                    setText(s.getFullName() != null ? s.getFullName() : "ID " + s.getId());
                }
                return this;
            }
        });

        spnTestDate = createDateSpinner();

        List<SuggestedLevel> levels = new java.util.ArrayList<>();
        levels.add(null);
        for (SuggestedLevel sl : SuggestedLevel.values()) levels.add(sl);

        cboSuggestedLevel = new JComboBox<>(levels.toArray(new SuggestedLevel[0]));
        cboSuggestedLevel.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "— Không chọn —" : value.toString());
                return this;
            }
        });

        buildUI();

        if (existing != null) {
            setSelectedStudentById(cboStudent, existing.getStudentId());
            setSpinnerFromString(spnTestDate, existing.getTestDate());
            txtScore.setText(existing.getScore() != null ? existing.getScore() : "");
            cboSuggestedLevel.setSelectedItem(existing.getSuggestedLevel());
            txtNote.setText(existing.getNote() != null ? existing.getNote() : "");
            result = existing;
        } else {
            result = new PlacementFormData();
            spnTestDate.setValue(new Date());
        }

        setLocationRelativeTo(owner);
    }

    private JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, DATE_FORMAT);
        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(0, 40));
        return spinner;
    }

    private void buildUI() {

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(30, 30, 30, 30));

        // HEADER
        JLabel lblHeader = new JLabel("Kiểm tra xếp lớp");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setBorder(new EmptyBorder(0, 0, 20, 0));
        root.add(lblHeader, BorderLayout.NORTH);

        // FORM
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        // Học viên
        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0;
        form.add(createLabel("Học viên:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        gbc.insets = new Insets(10, 15, 10, 0);
        cboStudent.setPreferredSize(new Dimension(0, 40));
        cboStudent.putClientProperty(FlatClientProperties.STYLE, "arc:12");
        form.add(cboStudent, gbc);

        // Ngày kiểm tra
        gbc.gridy = 1; gbc.gridx = 0; gbc.weightx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);
        form.add(createLabel("Ngày kiểm tra:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        gbc.insets = new Insets(10, 15, 10, 0);
        spnTestDate.putClientProperty(FlatClientProperties.STYLE, "arc:12");
        form.add(spnTestDate, gbc);

        // Điểm
        gbc.gridy = 2; gbc.gridx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);
        form.add(createLabel("Điểm:"), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(10, 15, 10, 0);
        txtScore.setPreferredSize(new Dimension(0, 40));
        txtScore.putClientProperty(FlatClientProperties.STYLE, "arc:12");
        txtScore.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập điểm...");
        form.add(txtScore, gbc);

        // Cấp độ gợi ý
        gbc.gridy = 3; gbc.gridx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);
        form.add(createLabel("Cấp độ gợi ý:"), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(10, 15, 10, 0);
        cboSuggestedLevel.setPreferredSize(new Dimension(0, 40));
        cboSuggestedLevel.putClientProperty(FlatClientProperties.STYLE, "arc:12");
        form.add(cboSuggestedLevel, gbc);

        // Ghi chú
        gbc.gridy = 4; gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(15, 0, 10, 0);
        form.add(createLabel("Ghi chú:"), gbc);

        gbc.gridx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 15, 10, 0);

        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JScrollPane scroll = new JScrollPane(txtNote);
        scroll.putClientProperty(FlatClientProperties.STYLE, "arc:12");

        form.add(scroll, gbc);

        root.add(form, BorderLayout.CENTER);

        // FOOTER
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(25, 0, 0, 0));

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setPreferredSize(new Dimension(100, 42));
        btnCancel.putClientProperty(FlatClientProperties.STYLE,
                "arc:12; background:#f2f2f2; borderWidth:0");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Lưu");
        btnSave.setPreferredSize(new Dimension(120, 42));
        btnSave.putClientProperty(FlatClientProperties.STYLE,
                "arc:12; background:#0d6efd; foreground:#ffffff; borderWidth:0");
        btnSave.addActionListener(e -> onSave());

        footer.add(btnCancel);
        footer.add(btnSave);

        root.add(footer, BorderLayout.SOUTH);

        add(root);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        lbl.setForeground(new Color(70,70,70));
        return lbl;
    }

    private void onSave() {

        try {

            Student selStudent = (Student) cboStudent.getSelectedItem();
            if (selStudent == null) {
                throw new IllegalArgumentException("Vui lòng chọn học viên.");
            }

            String scoreStr = txtScore.getText().trim();
            if (!scoreStr.isEmpty()) new BigDecimal(scoreStr);

            result.setStudentId(selStudent.getId().toString());
            result.setTestDate(getSpinnerDateString(spnTestDate));
            result.setScore(scoreStr.isEmpty() ? null : scoreStr);
            result.setSuggestedLevel((SuggestedLevel) cboSuggestedLevel.getSelectedItem());
            result.setNote(txtNote.getText().trim());

            saved = true;
            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,"Điểm phải là số.","Lỗi",JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getSpinnerDateString(JSpinner spinner) {
        try {
            Object v = spinner.getValue();
            if (v instanceof Date d) {
                LocalDate ld = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                return ld.toString();
            }
        } catch (Exception ignored) {}
        return "";
    }

    private static void setSelectedStudentById(JComboBox<Student> cbo, String idStr) {
        if (idStr == null) return;
        try {
            Long id = Long.parseLong(idStr);
            for (int i = 0; i < cbo.getItemCount(); i++) {
                Student s = cbo.getItemAt(i);
                if (s != null && id.equals(s.getId())) {
                    cbo.setSelectedIndex(i);
                    return;
                }
            }
        } catch (Exception ignored) {}
    }

    private void setSpinnerFromString(JSpinner spinner, String value) {
        if (value == null) return;
        try {
            LocalDate ld = LocalDate.parse(value);
            Date date = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
            spinner.setValue(date);
        } catch (Exception ignored) {}
    }

    public boolean isSaved() { return saved; }
    public PlacementFormData getResult() { return result; }

    public static class PlacementFormData {
        private String studentId;
        private String testDate;
        private String score;
        private SuggestedLevel suggestedLevel;
        private String note;

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }

        public String getTestDate() { return testDate; }
        public void setTestDate(String testDate) { this.testDate = testDate; }

        public String getScore() { return score; }
        public void setScore(String score) { this.score = score; }

        public SuggestedLevel getSuggestedLevel() { return suggestedLevel; }
        public void setSuggestedLevel(SuggestedLevel suggestedLevel) { this.suggestedLevel = suggestedLevel; }

        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }
}