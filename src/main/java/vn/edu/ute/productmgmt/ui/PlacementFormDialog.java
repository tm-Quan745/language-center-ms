package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.enums.SuggestedLevel;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Form nhập/sửa bài kiểm tra xếp lớp: học viên, ngày kiểm tra, điểm, cấp độ gợi ý, ghi chú.
 */
public class PlacementFormDialog extends JDialog {

    private static final String DATE_FORMAT = "dd/MM/yyyy";

    private final JComboBox<Student> cboStudent;
    private final JSpinner spnTestDate;
    private final JTextField txtScore = new JTextField(10);
    private final JComboBox<SuggestedLevel> cboSuggestedLevel;
    private final JTextArea txtNote = new JTextArea(3, 25);

    private boolean saved = false;
    private PlacementFormData result;

    public PlacementFormDialog(Window owner,
                               PlacementFormData existing,
                               List<Student> students) {
        super(owner, "Kiểm tra xếp lớp", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

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
        levels.add(null); // "— Không chọn —"
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
            if (existing.getSuggestedLevel() != null) cboSuggestedLevel.setSelectedItem(existing.getSuggestedLevel());
            else cboSuggestedLevel.setSelectedIndex(0);
            txtNote.setText(existing.getNote() != null ? existing.getNote() : "");
            result = existing;
        } else {
            result = new PlacementFormData();
            spnTestDate.setValue(new Date());
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, DATE_FORMAT);
        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(120, spinner.getPreferredSize().height));
        return spinner;
    }

    private void setSpinnerFromString(JSpinner spinner, String value) {
        if (value == null || value.trim().isEmpty()) return;
        try {
            LocalDate ld = LocalDate.parse(value.trim());
            Date date = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
            spinner.setValue(date);
        } catch (Exception ignored) { }
    }

    private static void setSelectedStudentById(JComboBox<Student> cbo, String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) return;
        try {
            Long id = Long.parseLong(idStr.trim());
            for (int i = 0; i < cbo.getItemCount(); i++) {
                Student s = cbo.getItemAt(i);
                if (s != null && id.equals(s.getId())) {
                    cbo.setSelectedIndex(i);
                    return;
                }
            }
        } catch (Exception ignored) { }
    }

    private String getSpinnerDateString(JSpinner spinner) {
        try {
            spinner.commitEdit();
        } catch (Exception ignored) { }
        try {
            Object v = spinner.getValue();
            if (v instanceof Date d) {
                LocalDate ld = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                return ld.toString();
            }
            if (v instanceof java.util.Calendar c) {
                LocalDate ld = c.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                return ld.toString();
            }
        } catch (Exception ignored) { }
        return "";
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Học viên:"), g);
        g.gridx = 1;
        form.add(cboStudent, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Ngày kiểm tra:"), g);
        g.gridx = 1;
        form.add(spnTestDate, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Điểm:"), g);
        g.gridx = 1;
        form.add(txtScore, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Cấp độ gợi ý:"), g);
        g.gridx = 1;
        form.add(cboSuggestedLevel, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Ghi chú:"), g);
        g.gridx = 1;
        form.add(new JScrollPane(txtNote), g);

        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnSave);
        actions.add(btnCancel);

        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(actions, BorderLayout.SOUTH);
    }

    private void onSave() {
        try {
            Student selStudent = (Student) cboStudent.getSelectedItem();
            if (selStudent == null) {
                throw new IllegalArgumentException("Chọn học viên.");
            }
            String scoreStr = txtScore.getText().trim();
            if (!scoreStr.isEmpty()) {
                new BigDecimal(scoreStr); // validate number
            }

            result.setStudentId(selStudent.getId().toString());
            result.setTestDate(getSpinnerDateString(spnTestDate));
            result.setScore(scoreStr.isEmpty() ? null : scoreStr);
            result.setSuggestedLevel((SuggestedLevel) cboSuggestedLevel.getSelectedItem());
            result.setNote(txtNote.getText().trim());

            saved = true;
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Điểm phải là số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
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
