package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.enums.PaymentMethod;
import vn.edu.ute.productmgmt.model.enums.PaymentStatus;

import javax.swing.*;
import java.awt.*;

public class PaymentFormDialog extends JDialog {

    private final JTextField txtStudentId = new JTextField(10);
    private final JTextField txtAmount = new JTextField(10);
    private final JTextField txtPaymentDate = new JTextField(16);
    private final JComboBox<PaymentMethod> cboMethod = new JComboBox<>(PaymentMethod.values());
    private final JComboBox<PaymentStatus> cboStatus = new JComboBox<>(PaymentStatus.values());
    private final JTextField txtReferenceCode = new JTextField(20);

    private boolean saved = false;
    private PaymentFormData result;

    public PaymentFormDialog(Window owner, PaymentFormData existing) {
        super(owner, "Thanh toán", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();

        if (existing != null) {
            txtStudentId.setText(existing.getStudentId());
            txtAmount.setText(existing.getAmount());
            txtPaymentDate.setText(existing.getPaymentDate());
            if (existing.getMethod() != null) {
                cboMethod.setSelectedItem(existing.getMethod());
            }
            if (existing.getStatus() != null) {
                cboStatus.setSelectedItem(existing.getStatus());
            }
            txtReferenceCode.setText(existing.getReferenceCode());
            result = existing;
        } else {
            result = new PaymentFormData();
            cboMethod.setSelectedItem(PaymentMethod.Cash);
            cboStatus.setSelectedItem(PaymentStatus.Completed);
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
        form.add(new JLabel("Student ID:"), g);
        g.gridx = 1;
        form.add(txtStudentId, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Số tiền:"), g);
        g.gridx = 1;
        form.add(txtAmount, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Ngày thanh toán (yyyy-MM-dd HH:mm, để trống = now):"), g);
        g.gridx = 1;
        form.add(txtPaymentDate, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Phương thức:"), g);
        g.gridx = 1;
        form.add(cboMethod, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Trạng thái:"), g);
        g.gridx = 1;
        form.add(cboStatus, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Mã tham chiếu:"), g);
        g.gridx = 1;
        form.add(txtReferenceCode, g);

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
            String studentId = txtStudentId.getText().trim();
            if (studentId.isEmpty()) {
                throw new IllegalArgumentException("Student ID không được để trống.");
            }
            Long.parseLong(studentId); // validate parse được

            String amountStr = txtAmount.getText().trim();
            if (amountStr.isEmpty()) {
                throw new IllegalArgumentException("Số tiền không được để trống.");
            }
            new java.math.BigDecimal(amountStr); // validate parse được

            result.setStudentId(studentId);
            result.setAmount(amountStr);
            result.setPaymentDate(txtPaymentDate.getText().trim());
            result.setMethod((PaymentMethod) cboMethod.getSelectedItem());
            result.setStatus((PaymentStatus) cboStatus.getSelectedItem());
            result.setReferenceCode(txtReferenceCode.getText().trim());

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public PaymentFormData getResult() {
        return result;
    }

    public static class PaymentFormData {
        private String studentId;
        private String amount;
        private String paymentDate;
        private PaymentMethod method;
        private PaymentStatus status;
        private String referenceCode;

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getPaymentDate() {
            return paymentDate;
        }

        public void setPaymentDate(String paymentDate) {
            this.paymentDate = paymentDate;
        }

        public PaymentMethod getMethod() {
            return method;
        }

        public void setMethod(PaymentMethod method) {
            this.method = method;
        }

        public PaymentStatus getStatus() {
            return status;
        }

        public void setStatus(PaymentStatus status) {
            this.status = status;
        }

        public String getReferenceCode() {
            return referenceCode;
        }

        public void setReferenceCode(String referenceCode) {
            this.referenceCode = referenceCode;
        }
    }
}

