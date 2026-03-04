package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.model.enums.CourseLevel;
import vn.edu.ute.productmgmt.model.enums.DurationUnit;

import javax.swing.*;
import java.awt.*;

/**
 * Form nhập liệu đơn giản cho Course, dùng mock object.
 */
public class CourseFormDialog extends JDialog {

    private final JTextField txtName = new JTextField(25);
    private final JTextArea txtDescription = new JTextArea(4, 25);
    private final JComboBox<CourseLevel> cboLevel = new JComboBox<>(CourseLevel.values());
    private final JTextField txtDuration = new JTextField(10);
    private final JComboBox<DurationUnit> cboDurationUnit = new JComboBox<>(DurationUnit.values());
    private final JTextField txtFee = new JTextField(10);
    private final JComboBox<ActiveStatus> cboStatus = new JComboBox<>(ActiveStatus.values());

    private boolean saved = false;
    private CourseFormData result;

    public CourseFormDialog(Window owner, CourseFormData existing) {
        super(owner, "Course", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();

        if (existing != null) {
            txtName.setText(existing.getName());
            txtDescription.setText(existing.getDescription());
            if (existing.getLevel() != null) cboLevel.setSelectedItem(existing.getLevel());
            txtDuration.setText(existing.getDuration());
            if (existing.getDurationUnit() != null) cboDurationUnit.setSelectedItem(existing.getDurationUnit());
            txtFee.setText(existing.getFee());
            if (existing.getStatus() != null) cboStatus.setSelectedItem(existing.getStatus());
            result = existing;
        } else {
            result = new CourseFormData();
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Course name:"), g);
        g.gridx = 1;
        form.add(txtName, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Description:"), g);
        g.gridx = 1;
        JScrollPane descScroll = new JScrollPane(txtDescription);
        form.add(descScroll, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Level:"), g);
        g.gridx = 1;
        form.add(cboLevel, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Duration:"), g);
        g.gridx = 1;
        JPanel durationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        durationPanel.add(txtDuration);
        durationPanel.add(cboDurationUnit);
        form.add(durationPanel, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Fee:"), g);
        g.gridx = 1;
        form.add(txtFee, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Status:"), g);
        g.gridx = 1;
        form.add(cboStatus, g);

        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");

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
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Course name is required.");
            }

            result.setName(name);
            result.setDescription(txtDescription.getText().trim());
            result.setLevel((CourseLevel) cboLevel.getSelectedItem());
            result.setDuration(txtDuration.getText().trim());
            result.setDurationUnit((DurationUnit) cboDurationUnit.getSelectedItem());
            result.setFee(txtFee.getText().trim());
            result.setStatus((ActiveStatus) cboStatus.getSelectedItem());

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public CourseFormData getResult() {
        return result;
    }

    /**
     * DTO đơn giản đại diện thông tin Course cho UI.
     */
    public static class CourseFormData {
        private String name;
        private String description;
        private CourseLevel level;
        private String duration;
        private DurationUnit durationUnit;
        private String fee;
        private ActiveStatus status;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public CourseLevel getLevel() {
            return level;
        }

        public void setLevel(CourseLevel level) {
            this.level = level;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public DurationUnit getDurationUnit() {
            return durationUnit;
        }

        public void setDurationUnit(DurationUnit durationUnit) {
            this.durationUnit = durationUnit;
        }

        public String getFee() {
            return fee;
        }

        public void setFee(String fee) {
            this.fee = fee;
        }

        public ActiveStatus getStatus() {
            return status;
        }

        public void setStatus(ActiveStatus status) {
            this.status = status;
        }
    }
}

