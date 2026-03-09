package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.enums.NotificationTargetRole;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class NotificationFormDialog extends JDialog {

    private final JTextField txtTitle = new JTextField();
    private final JTextArea txtContent = new JTextArea(8, 30);
    private final JComboBox<NotificationTargetRole> cboTargetRole = new JComboBox<>(NotificationTargetRole.values());

    private boolean saved = false;
    private NotificationFormData result;

    public NotificationFormDialog(Window owner, NotificationFormData existing) {
        super(owner, "Soạn thảo thông báo", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setSize(600, 550); // Chiều rộng rộng hơn để Label và Textbox nằm ngang đẹp
        setLayout(new BorderLayout());

        buildUI();

        if (existing != null) {
            txtTitle.setText(existing.getTitle());
            txtContent.setText(existing.getContent());
            if (existing.getTargetRole() != null) cboTargetRole.setSelectedItem(existing.getTargetRole());
            this.result = existing;
        } else {
            this.result = new NotificationFormData();
        }

        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- Header ---
        JLabel lblHeader = new JLabel("Phát hành thông báo");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setBorder(new EmptyBorder(0, 0, 20, 0));
        root.add(lblHeader, BorderLayout.NORTH);

        // --- Form Body (GridBagLayout) ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        // Hàng 1: Tiêu đề
        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0;
        form.add(createLabel("Tiêu đề tin:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 15, 10, 0);
        txtTitle.setPreferredSize(new Dimension(0, 40));
        txtTitle.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập tiêu đề ngắn gọn...");
        txtTitle.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        form.add(txtTitle, gbc);

        // Hàng 2: Đối tượng nhận
        gbc.gridy = 1; gbc.gridx = 0; gbc.weightx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);
        form.add(createLabel("Gửi đến:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 15, 10, 0);
        cboTargetRole.setPreferredSize(new Dimension(0, 40));
        cboTargetRole.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        form.add(cboTargetRole, gbc);

        // Hàng 3: Nội dung (Dùng ScrollPane)
        gbc.gridy = 2; gbc.gridx = 0; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST; // Đẩy label lên góc trên bên trái
        gbc.insets = new Insets(15, 0, 10, 0);
        form.add(createLabel("Nội dung:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 15, 10, 0);

        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        txtContent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(txtContent);
        scroll.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        form.add(scroll, gbc);

        root.add(form, BorderLayout.CENTER);

        // --- Buttons ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(25, 0, 0, 0));

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setPreferredSize(new Dimension(100, 42));
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #f2f2f2; borderWidth: 0");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Gửi thông báo");
        btnSave.setPreferredSize(new Dimension(140, 42));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #0d6efd; foreground: #ffffff; borderWidth: 0");
        btnSave.addActionListener(e -> onSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        root.add(footer, BorderLayout.SOUTH);

        add(root);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        lbl.setForeground(new Color(70, 70, 70));
        return lbl;
    }

    private void onSave() {
        String title = txtTitle.getText().trim();
        String content = txtContent.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tiêu đề và nội dung!", "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        result.setTitle(title);
        result.setContent(content);
        result.setTargetRole((NotificationTargetRole) cboTargetRole.getSelectedItem());
        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public NotificationFormData getResult() { return result; }

    public static class NotificationFormData {
        private String title, content;
        private NotificationTargetRole targetRole;
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public NotificationTargetRole getTargetRole() { return targetRole; }
        public void setTargetRole(NotificationTargetRole targetRole) { this.targetRole = targetRole; }
    }
}