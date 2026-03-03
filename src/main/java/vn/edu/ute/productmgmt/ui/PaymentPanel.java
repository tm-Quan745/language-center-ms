package vn.edu.ute.productmgmt.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Màn hình Thanh toán: form chi tiết phiếu thu, dùng mock data.
 */
public class PaymentPanel extends JPanel {

    private final JTextField txtPaymentId = new JTextField(10);
    private final JTextField txtStudentName = new JTextField(25);
    private final JTextField txtClassName = new JTextField(25);
    private final JTextField txtAmount = new JTextField(12);
    private final JTextField txtDate = new JTextField(10);
    private final JComboBox<String> cboMethod = new JComboBox<>(new String[]{"Cash", "Bank", "Momo"});
    private final JComboBox<String> cboStatus = new JComboBox<>(new String[]{"Paid", "Pending"});

    public PaymentPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        loadMockData();
    }

    private void buildUI() {
        add(buildActionBar(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
    }

    private JComponent buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));

        JButton btnBack = new JButton("Quay lại");
        JButton btnAdd = new JButton("Thêm phiếu");
        JButton btnSave = new JButton("Lưu");
        JButton btnDelete = new JButton("Xóa");

        btnBack.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Back (mock) - chưa xử lý điều hướng."));
        btnAdd.addActionListener(e -> clearForm());
        btnSave.addActionListener(e -> JOptionPane.showMessageDialog(this, "Đã lưu thanh toán (mock)."));
        btnDelete.addActionListener(e -> JOptionPane.showMessageDialog(this, "Đã xóa thanh toán (mock)."));

        bar.add(btnBack);
        bar.add(btnAdd);
        bar.add(btnSave);
        bar.add(btnDelete);

        return bar;
    }

    private JComponent buildForm() {
        JPanel wrapper = new JPanel(new BorderLayout());
        UI.stylePanelBorder(wrapper, "Thông tin thanh toán");

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;

        addField(form, g, r, 0, "Mã phiếu:", txtPaymentId);
        addField(form, g, r, 1, "Học viên:", txtStudentName);

        r++;
        addField(form, g, r, 0, "Lớp học:", txtClassName);
        addField(form, g, r, 1, "Số tiền:", txtAmount);

        r++;
        addField(form, g, r, 0, "Ngày thu:", txtDate);
        addField(form, g, r, 1, "Hình thức:", cboMethod);

        r++;
        addField(form, g, r, 0, "Trạng thái:", cboStatus);

        wrapper.add(form, BorderLayout.CENTER);
        return wrapper;
    }

    private void addField(JPanel form, GridBagConstraints g, int row, int col,
                          String label, JComponent field) {
        int baseGridX = col * 2;
        g.gridy = row;

        g.gridx = baseGridX;
        g.weightx = 0.0;
        form.add(new JLabel(label), g);

        g.gridx = baseGridX + 1;
        g.weightx = 1.0;
        form.add(field, g);
    }

    private void loadMockData() {
        txtPaymentId.setText("P001");
        txtStudentName.setText("Nguyen Van A");
        txtClassName.setText("IELTS Foundation - A");
        txtAmount.setText("5,000,000");
        txtDate.setText("10/03/2026");
        cboMethod.setSelectedItem("Cash");
        cboStatus.setSelectedItem("Paid");
    }

    private void clearForm() {
        txtPaymentId.setText("");
        txtStudentName.setText("");
        txtClassName.setText("");
        txtAmount.setText("");
        txtDate.setText("");
        cboMethod.setSelectedIndex(0);
        cboStatus.setSelectedIndex(0);
    }
}


