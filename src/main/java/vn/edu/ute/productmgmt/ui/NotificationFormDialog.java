package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.enums.NotificationTargetRole;

import javax.swing.*;
import java.awt.*;

public class NotificationFormDialog extends JDialog {

    private final JTextField txtTitle = new JTextField(30);
    private final JTextArea txtContent = new JTextArea(6, 30);
    private final JComboBox<NotificationTargetRole> cboTargetRole = new JComboBox<>(NotificationTargetRole.values());

    private boolean saved = false;
    private NotificationFormData result;

    public NotificationFormDialog(Window owner, NotificationFormData existing) {
        super(owner, "Thông báo", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();

        if (existing != null) {
            txtTitle.setText(existing.getTitle());
            txtContent.setText(existing.getContent());
            if (existing.getTargetRole() != null) cboTargetRole.setSelectedItem(existing.getTargetRole());
            result = existing;
        } else {
            result = new NotificationFormData();
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
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Tiêu đề:"), g);
        g.gridx = 1;
        form.add(txtTitle, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Nội dung:"), g);
        g.gridx = 1;
        JScrollPane contentScroll = new JScrollPane(txtContent);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        form.add(contentScroll, g);

        r++;
        g.gridx = 0; g.gridy = r;
        form.add(new JLabel("Đối tượng:"), g);
        g.gridx = 1;
        form.add(cboTargetRole, g);

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
            String title = txtTitle.getText().trim();
            if (title.isEmpty()) {
                throw new IllegalArgumentException("Tiêu đề không được để trống.");
            }
            String content = txtContent.getText().trim();
            if (content.isEmpty()) {
                throw new IllegalArgumentException("Nội dung không được để trống.");
            }
            result.setTitle(title);
            result.setContent(content);
            result.setTargetRole((NotificationTargetRole) cboTargetRole.getSelectedItem());
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
    public NotificationFormData getResult() { return result; }

    public static class NotificationFormData {
        private String title;
        private String content;
        private NotificationTargetRole targetRole;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public NotificationTargetRole getTargetRole() { return targetRole; }
        public void setTargetRole(NotificationTargetRole targetRole) { this.targetRole = targetRole; }
    }
}
