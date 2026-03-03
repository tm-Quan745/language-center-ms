package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Category;
import vn.edu.ute.productmgmt.model.Product;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class ProductFormDialog extends JDialog {
    private final JTextField txtName = new JTextField(25);
    private final JTextField txtPrice = new JTextField(10);
    private final JTextField txtQty = new JTextField(10);
    private final JComboBox<Category> cboCategory = new JComboBox<>();

    private boolean saved = false;
    private Product product; // output

    public ProductFormDialog(Frame owner, String title, List<Category> categories, Product existing) {
        super(owner, title, true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        for (Category c : categories) cboCategory.addItem(c);

        buildUI();

        if (existing != null) {
            txtName.setText(existing.getName());
            txtPrice.setText(existing.getPrice().toPlainString());
            txtQty.setText(String.valueOf(existing.getQuantity()));

            for (int i = 0; i < cboCategory.getItemCount(); i++) {
                if (cboCategory.getItemAt(i).getId() == existing.getCategory().getId()) {
                    cboCategory.setSelectedIndex(i);
                    break;
                }
            }
            this.product = existing;
        } else {
            this.product = new Product();
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;

        int r = 0;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Name:"), g);
        g.gridx = 1; form.add(txtName, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Price:"), g);
        g.gridx = 1; form.add(txtPrice, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Quantity:"), g);
        g.gridx = 1; form.add(txtQty, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Category:"), g);
        g.gridx = 1; form.add(cboCategory, g);

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
            if (name.isEmpty()) throw new IllegalArgumentException("Name is required.");

            BigDecimal price = new BigDecimal(txtPrice.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Price must be >= 0.");

            int qty = Integer.parseInt(txtQty.getText().trim());
            if (qty < 0) throw new IllegalArgumentException("Quantity must be >= 0.");

            Category c = (Category) cboCategory.getSelectedItem();
            if (c == null) throw new IllegalArgumentException("Category is required.");

            product.setName(name);
            product.setPrice(price);
            product.setQuantity(qty);
            product.setCategory(c);

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
    public Product getProduct() { return product; }
}
