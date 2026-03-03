package vn.edu.ute.productmgmt.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Form nhập liệu đơn giản cho Course, dùng mock object.
 */
public class CourseFormDialog extends JDialog {

    private final JTextField txtName = new JTextField(25);
    private final JTextArea txtDescription = new JTextArea(4, 25);
    private final JComboBox<String> cboLevel = new JComboBox<>(new String[]{"Beginner", "Intermediate", "Advanced"});
    private final JTextField txtDuration = new JTextField(10);
    private final JTextField txtFee = new JTextField(10);
    private final JComboBox<String> cboStatus = new JComboBox<>(new String[]{"Active", "Inactive"});

    private boolean saved = false;
    private CourseFormData result;

    public CourseFormDialog(Window owner, CourseFormData existing) {
        super(owner, "Course", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();

        if (existing != null) {
            txtName.setText(existing.getName());
            txtDescription.setText(existing.getDescription());
            cboLevel.setSelectedItem(existing.getLevel());
            txtDuration.setText(existing.getDuration());
            txtFee.setText(existing.getFee());
            cboStatus.setSelectedItem(existing.getStatus());
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
        form.add(txtDuration, g);

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
            result.setLevel((String) cboLevel.getSelectedItem());
            result.setDuration(txtDuration.getText().trim());
            result.setFee(txtFee.getText().trim());
            result.setStatus((String) cboStatus.getSelectedItem());

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
        private String level;
        private String duration;
        private String fee;
        private String status;

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

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getFee() {
            return fee;
        }

        public void setFee(String fee) {
            this.fee = fee;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}

