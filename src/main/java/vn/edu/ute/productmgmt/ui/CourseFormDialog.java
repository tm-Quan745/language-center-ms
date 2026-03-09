package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.model.enums.CourseLevel;
import vn.edu.ute.productmgmt.model.enums.DurationUnit;
import vn.edu.ute.productmgmt.model.Course;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

public class CourseFormDialog extends JDialog {

    private final JTextField txtName = new JTextField();
    private final JTextArea txtDescription = new JTextArea(4, 25);
    private final JComboBox<CourseLevel> cboLevel = new JComboBox<>(CourseLevel.values());
    private final JTextField txtDuration = new JTextField();
    private final JComboBox<DurationUnit> cboDurationUnit = new JComboBox<>(DurationUnit.values());
    private final JTextField txtFee = new JTextField();
    private final JComboBox<ActiveStatus> cboStatus = new JComboBox<>(ActiveStatus.values());

    private boolean saved = false;
    private CourseFormData result;

    public CourseFormDialog(Window owner, CourseFormData existing) {
        super(owner, "Thông tin khóa học", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setSize(600, 650);
        setLayout(new BorderLayout());

        buildUI();

        if (existing != null) {
            txtName.setText(existing.getName());
            txtDescription.setText(existing.getDescription());
            cboLevel.setSelectedItem(existing.getLevel());
            txtDuration.setText(existing.getDuration());
            cboDurationUnit.setSelectedItem(existing.getDurationUnit());
            txtFee.setText(existing.getFee());
            cboStatus.setSelectedItem(existing.getStatus());
            this.result = existing;
        } else {
            this.result = new CourseFormData();
            cboStatus.setSelectedItem(ActiveStatus.Active);
        }

        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- Header ---
        JLabel lblHeader = new JLabel("Chi tiết chương trình đào tạo");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setBorder(new EmptyBorder(0, 0, 25, 0));
        root.add(lblHeader, BorderLayout.NORTH);

        // --- Form Body ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        // 1. Tên khóa học
        addLabel(form, gbc, 0, "Tên khóa học:");
        addEditor(form, gbc, 0, txtName, "Nhập tên khóa học...", " 📘 ");

        // 2. Mức độ (Level)
        addLabel(form, gbc, 1, "Cấp độ:");
        styleCombo(cboLevel);
        addCustomEditor(form, gbc, 1, cboLevel);

        // 3. Thời lượng (Số + Đơn vị trên 1 hàng)
        addLabel(form, gbc, 2, "Thời lượng:");
        JPanel durationWrapper = new JPanel(new BorderLayout(10, 0));
        durationWrapper.setOpaque(false);
        txtDuration.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Số...");
        txtDuration.putClientProperty(FlatClientProperties.STYLE, "arc:12");
        txtDuration.setPreferredSize(new Dimension(80, 40));
        styleCombo(cboDurationUnit);
        durationWrapper.add(txtDuration, BorderLayout.WEST);
        durationWrapper.add(cboDurationUnit, BorderLayout.CENTER);
        addCustomEditor(form, gbc, 2, durationWrapper);

        // 4. Học phí
        addLabel(form, gbc, 3, "Học phí (VNĐ):");
        addEditor(form, gbc, 3, txtFee, "Ví dụ: 5000000", " 💰 ");

        // 5. Trạng thái
        addLabel(form, gbc, 4, "Trạng thái:");
        styleCombo(cboStatus);
        addCustomEditor(form, gbc, 4, cboStatus);

        // 6. Mô tả (TextArea chiếm diện tích lớn)
        gbc.gridy = 5; gbc.gridx = 0; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(12, 0, 8, 0);
        form.add(createLabel("Mô tả khóa học:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(8, 20, 8, 0);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(txtDescription);
        scroll.putClientProperty(FlatClientProperties.STYLE, "arc:12");
        form.add(scroll, gbc);

        root.add(form, BorderLayout.CENTER);

        // --- Buttons ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(25, 0, 0, 0));

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setPreferredSize(new Dimension(100, 42));
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc:12; background:#f2f2f2; borderWidth:0");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Lưu khóa học");
        btnSave.setPreferredSize(new Dimension(140, 42));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc:12; background:#0d6efd; foreground:#fff; borderWidth:0");
        btnSave.addActionListener(e -> onSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        root.add(footer, BorderLayout.SOUTH);

        add(root);
    }

    // --- Helper Methods cho Layout ---
    private void addLabel(JPanel p, GridBagConstraints g, int y, String text) {
        g.gridy = y; g.gridx = 0; g.weightx = 0;
        g.insets = new Insets(8, 0, 8, 0);
        p.add(createLabel(text), g);
    }

    private void addEditor(JPanel p, GridBagConstraints g, int y, JTextField f, String ph, String icon) {
        g.gridy = y; g.gridx = 1; g.weightx = 1.0;
        g.insets = new Insets(8, 20, 8, 0);
        f.setPreferredSize(new Dimension(0, 40));
        f.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, ph);
        f.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new JLabel(icon));
        f.putClientProperty(FlatClientProperties.STYLE, "arc:12");
        p.add(f, g);
    }

    private void addCustomEditor(JPanel p, GridBagConstraints g, int y, JComponent c) {
        g.gridy = y; g.gridx = 1; g.weightx = 1.0;
        g.insets = new Insets(8, 20, 8, 0);
        p.add(c, g);
    }

    private void styleCombo(JComboBox<?> c) {
        c.setPreferredSize(new Dimension(0, 40));
        c.putClientProperty(FlatClientProperties.STYLE, "arc:12");
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    // --- Logic & Mapping ---
    public Course getResultEntity() {
        Course c = new Course();
        c.setCourseName(result.getName());
        c.setDescription(result.getDescription());
        c.setLevel(result.getLevel());
        c.setStatus(result.getStatus());
        c.setDurationUnit(result.getDurationUnit());

        try {
            c.setDuration(Integer.parseInt(result.getDuration()));
            c.setFee(new BigDecimal(result.getFee().replace(",", "")));
        } catch (Exception ignored) {}

        return c;
    }

    private void onSave() {
        if (txtName.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Tên khóa học không được để trống!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        result.setName(txtName.getText().trim());
        result.setDescription(txtDescription.getText().trim());
        result.setLevel((CourseLevel) cboLevel.getSelectedItem());
        result.setDuration(txtDuration.getText().trim());
        result.setDurationUnit((DurationUnit) cboDurationUnit.getSelectedItem());
        result.setFee(txtFee.getText().trim());
        result.setStatus((ActiveStatus) cboStatus.getSelectedItem());

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public CourseFormData getResult() { return result; }

    public static class CourseFormData {
        private String name, description, duration, fee;
        private CourseLevel level;
        private DurationUnit durationUnit;
        private ActiveStatus status;
        // Getters & Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public CourseLevel getLevel() { return level; }
        public void setLevel(CourseLevel level) { this.level = level; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        public DurationUnit getDurationUnit() { return durationUnit; }
        public void setDurationUnit(DurationUnit durationUnit) { this.durationUnit = durationUnit; }
        public String getFee() { return fee; }
        public void setFee(String fee) { this.fee = fee; }
        public ActiveStatus getStatus() { return status; }
        public void setStatus(ActiveStatus status) { this.status = status; }
    }
}