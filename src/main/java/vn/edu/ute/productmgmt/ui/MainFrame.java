package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Category;
import vn.edu.ute.productmgmt.model.Product;
import vn.edu.ute.productmgmt.service.CategoryService;
import vn.edu.ute.productmgmt.service.ProductService;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private final ProductService productService;
    private final CategoryService categoryService;

    private final JComboBox<Object> cboFilterCategory = new JComboBox<>();
    private final ProductTableModel tableModel = new ProductTableModel();
    private final JTable table = new JTable(tableModel);

    private final JLabel lblSelected = new JLabel("Selected: (none)");
    private Product selectedProduct = null;

    private List<Category> cachedCategories = new ArrayList<>();

    public MainFrame(ProductService productService, CategoryService categoryService) {
        super("Product Management (JPA + Swing + SOLID + TX)");
        this.productService = productService;
        this.categoryService = categoryService;

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        buildUI();
        reloadAll();

        setSize(920, 540);
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout(8, 8));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(new JLabel("Category filter:"));
        left.add(cboFilterCategory);
        top.add(left, BorderLayout.WEST);

        JButton btnAdd = new JButton("Add");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");
        JButton btnStream = new JButton("Stream Queries");

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> reloadAll());
        btnStream.addActionListener(e -> onOpenStreamQueries());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(btnAdd);
        right.add(btnEdit);
        right.add(btnDelete);
        right.add(btnRefresh);
        right.add(btnStream);
        top.add(right, BorderLayout.EAST);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this::onRowSelected);
        JScrollPane scroll = new JScrollPane(table);

        cboFilterCategory.addActionListener(e -> refreshTableByCurrentFilter());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(lblSelected, BorderLayout.WEST);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(top, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setJMenuBar(createMenuBar());
        setContentPane(root);
    }

    private void reloadAll() {
        try {
            cachedCategories = categoryService.getAll();

            Object current = cboFilterCategory.getSelectedItem();
            cboFilterCategory.removeAllItems();
            cboFilterCategory.addItem("(All)");
            for (Category c : cachedCategories) cboFilterCategory.addItem(c);

            if (current instanceof Category) {
                int oldId = ((Category) current).getId();
                for (int i = 0; i < cboFilterCategory.getItemCount(); i++) {
                    Object it = cboFilterCategory.getItemAt(i);
                    if (it instanceof Category && ((Category) it).getId() == oldId) {
                        cboFilterCategory.setSelectedIndex(i);
                        break;
                    }
                }
            } else {
                cboFilterCategory.setSelectedIndex(0);
            }

            refreshTableByCurrentFilter();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void refreshTableByCurrentFilter() {
        try {
            Integer catId = null;
            Object sel = cboFilterCategory.getSelectedItem();
            if (sel instanceof Category) catId = ((Category) sel).getId();

            List<Product> products = productService.getAll(catId);
            tableModel.setData(products);

            selectedProduct = null;
            lblSelected.setText("Selected: (none)");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void onRowSelected(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = table.getSelectedRow();
        if (row < 0) {
            selectedProduct = null;
            lblSelected.setText("Selected: (none)");
            return;
        }

        // JTable may use a RowSorter; convert view index -> model index.
        int modelRow = table.convertRowIndexToModel(row);
        selectedProduct = tableModel.getAt(modelRow);
        if (selectedProduct != null) {
            lblSelected.setText("Selected: ID=" + selectedProduct.getId() + " | " + selectedProduct.getName());
        } else {
            lblSelected.setText("Selected: (none)");
        }
    }

    private void onAdd() {
        ProductFormDialog dlg = new ProductFormDialog(this, "Add Product", cachedCategories, null);
        dlg.setVisible(true);
        if (!dlg.isSaved()) return;

        try {
            int newId = productService.createProduct(dlg.getProduct());
            JOptionPane.showMessageDialog(this, "Created product ID=" + newId);
            reloadAll();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void onEdit() {
        if (selectedProduct == null) {
            JOptionPane.showMessageDialog(this, "Please select a product first.");
            return;
        }

        Product copy = new Product(
                selectedProduct.getId(),
                selectedProduct.getName(),
                selectedProduct.getPrice(),
                selectedProduct.getQuantity(),
                selectedProduct.getCategory()
        );

        ProductFormDialog dlg = new ProductFormDialog(this, "Edit Product", cachedCategories, copy);
        dlg.setVisible(true);
        if (!dlg.isSaved()) return;

        try {
            productService.updateProduct(dlg.getProduct());
            JOptionPane.showMessageDialog(this, "Updated!");
            reloadAll();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void onDelete() {
        if (selectedProduct == null) {
            JOptionPane.showMessageDialog(this, "Please select a product first.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Delete product ID=" + selectedProduct.getId() + " ?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            productService.deleteProduct(selectedProduct.getId());
            JOptionPane.showMessageDialog(this, "Deleted!");
            reloadAll();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    /**
     * Menu bar: File/Exit + Reports/Stream Queries.
     */
    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu mFile = new JMenu("File");
        JMenuItem miExit = new JMenuItem("Exit");
        miExit.addActionListener(e -> dispose());
        mFile.add(miExit);

        JMenu mReports = new JMenu("Reports");
        JMenuItem miStream = new JMenuItem("Stream Queries (15)");
        miStream.addActionListener(e -> onOpenStreamQueries());
        mReports.add(miStream);

        bar.add(mFile);
        bar.add(mReports);
        return bar;
    }

    /**
     * Open the demo dialog that showcases 15 Stream/Lambda queries.
     */
    private void onOpenStreamQueries() {
        try {
            StreamReportsDialog dlg = new StreamReportsDialog(this, productService);
            dlg.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showError(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
