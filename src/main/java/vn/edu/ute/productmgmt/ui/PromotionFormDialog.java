package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.model.enums.DiscountType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Calendar;

/**
 * Form nhập liệu cho Khuyến mãi, chuẩn hóa theo ClassFormDialog
 */
public class PromotionFormDialog extends JDialog {

    private final JTextField txtPromoName = new JTextField(25);
    private final JComboBox<DiscountType> cboDiscountType = new JComboBox<>(DiscountType.values());
    private final JTextField txtDiscountValue = new JTextField(10);
    private final JSpinner spnStartDate;
    private final JCheckBox chkNoStartDate = new JCheckBox("Không giới hạn");
    private final JSpinner spnEndDate;
    private final JCheckBox chkNoEndDate = new JCheckBox("Không giới hạn");
    private final JComboBox<ActiveStatus> cboStatus = new JComboBox<>(ActiveStatus.values());

    private boolean saved = false;
    private PromotionFormData result;

    public PromotionFormDialog(Window owner, PromotionFormData existing) {
        super(owner, "Thông tin Khuyến mãi", ModalityType.APPLICATION_MODAL);

        setSize(580, 680);
        setLayout(new BorderLayout());

        spnStartDate = createDateSpinner();
        spnEndDate = createDateSpinner();

        buildUI();

        if (existing != null) {
            txtPromoName.setText(existing.getPromoName());
            if (existing.getDiscountType() != null) cboDiscountType.setSelectedItem(existing.getDiscountType());
            txtDiscountValue.setText(existing.getDiscountValue());
            setSpinnerFromString(spnStartDate, existing.getStartDate());
            setSpinnerFromString(spnEndDate, existing.getEndDate());
            chkNoStartDate.setSelected(existing.getStartDate() == null || existing.getStartDate().isEmpty());
            chkNoEndDate.setSelected(existing.getEndDate() == null || existing.getEndDate().isEmpty());
            updateDateSpinnersEnabled();
            if (existing.getStatus() != null) cboStatus.setSelectedItem(existing.getStatus());
            result = existing;
        } else {
            result = new PromotionFormData();
            chkNoStartDate.setSelected(true);
            chkNoEndDate.setSelected(true);
            updateDateSpinnersEnabled();
        }

        setLocationRelativeTo(owner);
    }

    private JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        spinner.setPreferredSize(new Dimension(0, 40));
        spinner.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
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

    private void updateDateSpinnersEnabled() {
        spnStartDate.setEnabled(!chkNoStartDate.isSelected());
        spnEndDate.setEnabled(!chkNoEndDate.isSelected());
    }

    private String getSpinnerDateString(JSpinner spinner) {
        if (!spinner.isEnabled()) return "";
        try {
            spinner.commitEdit();
        } catch (Exception ignored) { }
        try {
            Object v = spinner.getValue();
            if (v instanceof Date) {
                Date d = (Date) v;
                LocalDate ld = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                return ld.toString();
            }
            if (v instanceof Calendar) {
                LocalDate ld = ((Calendar) v).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                return ld.toString();
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- Header ---
        JLabel lblHeader = new JLabel("Thông tin Khuyến mãi");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setBorder(new EmptyBorder(0, 0, 25, 0));
        root.add(lblHeader, BorderLayout.NORTH);

        // --- Form Body ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        // Các hàng dữ liệu
        addFormRow(form, gbc, 0, "Tên khuyến mãi:", txtPromoName);
        addFormRow(form, gbc, 1, "Loại giảm giá:", cboDiscountType);
        addFormRow(form, gbc, 2, "Giá trị giảm:", txtDiscountValue);
        addFormRowWithCheckbox(form, gbc, 3, "Ngày bắt đầu:", spnStartDate, chkNoStartDate);
        addFormRowWithCheckbox(form, gbc, 4, "Ngày kết thúc:", spnEndDate, chkNoEndDate);
        addFormRow(form, gbc, 5, "Trạng thái:", cboStatus);

        root.add(form, BorderLayout.CENTER);

        // --- Buttons ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(25, 0, 0, 0));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(100, 42));
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #f2f2f2; borderWidth: 0");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Lưu Khuyến mãi");
        btnSave.setPreferredSize(new Dimension(140, 42));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #198754; foreground: #ffffff; borderWidth: 0");
        btnSave.addActionListener(e -> onSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        root.add(footer, BorderLayout.SOUTH);

        add(root);
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridy = row;
        gbc.gridx = 0; gbc.weightx = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        p.add(createLabel(label), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 20, 8, 0);
        if (comp instanceof JComboBox || comp instanceof JSpinner || comp instanceof JTextField) {
            comp.setPreferredSize(new Dimension(0, 40));
            comp.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        }
        p.add(comp, gbc);
    }

    private void addFormRowWithCheckbox(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp, JCheckBox chk) {
        gbc.gridy = row;
        gbc.gridx = 0; gbc.weightx = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        p.add(createLabel(label), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 20, 8, 0);
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        if (comp instanceof JSpinner) {
            comp.setPreferredSize(new Dimension(150, 40));
            comp.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        }
        chk.setOpaque(false);
        chk.addActionListener(e -> updateDateSpinnersEnabled());
        panel.add(comp);
        panel.add(chk);
        p.add(panel, gbc);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private void onSave() {
        try {
            String name = txtPromoName.getText().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Tên khuyến mãi không được để trống.");
            }

            result.setPromoName(name);
            result.setDiscountType((DiscountType) cboDiscountType.getSelectedItem());
            result.setDiscountValue(txtDiscountValue.getText().trim());
            result.setStartDate(chkNoStartDate.isSelected() ? "" : getSpinnerDateString(spnStartDate));
            result.setEndDate(chkNoEndDate.isSelected() ? "" : getSpinnerDateString(spnEndDate));
            result.setStatus((ActiveStatus) cboStatus.getSelectedItem());

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Thông báo lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public PromotionFormData getResult() {
        return result;
    }

    /**
     * DTO đơn giản đại diện thông tin Khuyến mãi cho UI.
     */
    public static class PromotionFormData {
        private String promoName;
        private DiscountType discountType;
        private String discountValue;
        private String startDate;
        private String endDate;
        private ActiveStatus status;

        public String getPromoName() { return promoName; }
        public void setPromoName(String promoName) { this.promoName = promoName; }
        public DiscountType getDiscountType() { return discountType; }
        public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
        public String getDiscountValue() { return discountValue; }
        public void setDiscountValue(String discountValue) { this.discountValue = discountValue; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public ActiveStatus getStatus() { return status; }
        public void setStatus(ActiveStatus status) { this.status = status; }
    }
}
