package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.model.enums.DiscountType;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Calendar;

/**
 * Form nhập liệu cho Promotion, theo mẫu CourseFormDialog.
 * Dùng JSpinner (SpinnerDateModel) để chọn ngày thay vì nhập tay.
 */
public class PromotionFormDialog extends JDialog {

    private static final String DATE_FORMAT = "dd/MM/yyyy";

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
        super(owner, "Khuyến mãi", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

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

        pack();
        setLocationRelativeTo(owner);
    }

    private JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
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

    private void updateDateSpinnersEnabled() {
        spnStartDate.setEnabled(!chkNoStartDate.isSelected());
        spnEndDate.setEnabled(!chkNoEndDate.isSelected());
    }

    /** Đọc ngày từ spinner; gọi commitEdit() trước để giá trị trong ô được ghi vào model. */
    private String getSpinnerDateString(JSpinner spinner) {
        if (!spinner.isEnabled()) return "";
        try {
            spinner.commitEdit();
        } catch (Exception ignored) { /* editor chưa chỉnh sửa */ }
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
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Tên khuyến mãi:"), g);
        g.gridx = 1;
        form.add(txtPromoName, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Loại giảm giá:"), g);
        g.gridx = 1;
        form.add(cboDiscountType, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Giá trị giảm:"), g);
        g.gridx = 1;
        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        valuePanel.add(txtDiscountValue);
        valuePanel.add(new JLabel("(% hoặc số tiền)"));
        form.add(valuePanel, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Ngày bắt đầu:"), g);
        g.gridx = 1;
        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        startPanel.add(spnStartDate);
        chkNoStartDate.addActionListener(e -> updateDateSpinnersEnabled());
        startPanel.add(chkNoStartDate);
        form.add(startPanel, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Ngày kết thúc:"), g);
        g.gridx = 1;
        JPanel endPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        endPanel.add(spnEndDate);
        chkNoEndDate.addActionListener(e -> updateDateSpinnersEnabled());
        endPanel.add(chkNoEndDate);
        form.add(endPanel, g);

        r++;
        g.gridx = 0;
        g.gridy = r;
        form.add(new JLabel("Trạng thái:"), g);
        g.gridx = 1;
        form.add(cboStatus, g);

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
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public PromotionFormData getResult() {
        return result;
    }

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
