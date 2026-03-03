package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.Category;
import vn.edu.ute.productmgmt.model.Product;
import vn.edu.ute.productmgmt.service.ProductService;
import vn.edu.ute.productmgmt.stream.ProductStreamQueries;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialog demo "15 Stream Queries" để sinh viên chọn từng truy vấn, chạy và xem kết quả.
 *
 * Mục tiêu giảng dạy:
 * - Lấy dữ liệu từ DB bằng JPA (productService.getAll(null))
 * - Chạy phân tích bằng Stream/Lambda trên List<Product>
 * - Hiển thị kết quả bằng JTable + summary text
 */
public class StreamReportsDialog extends JDialog {

    private final ProductService productService;

    private List<Product> products = new ArrayList<>();

    private final JLabel lblInfo = new JLabel("Data: (not loaded)");
    private final DefaultListModel<String> reportListModel = new DefaultListModel<>();
    private final JList<String> lstReports = new JList<>(reportListModel);

    private final JTable table = new JTable();
    private final ProductTableModel productTableModel = new ProductTableModel();
    private final SimpleTableModel simpleTableModel = new SimpleTableModel();

    private final JTextArea txtSummary = new JTextArea();

    public StreamReportsDialog(Frame owner, ProductService productService) {
        super(owner, "Reports - Stream Queries (15)", true);
        this.productService = productService;

        buildUI();
        reloadData();

        setSize(980, 620);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        // ===== Top bar =====
        JButton btnReload = new JButton("Reload data");
        JButton btnRun = new JButton("Run selected");
        JButton btnRunAll = new JButton("Run all (overview)");
        JButton btnClose = new JButton("Close");

        btnReload.addActionListener(e -> reloadData());
        btnRun.addActionListener(e -> runSelectedReport());
        btnRunAll.addActionListener(e -> runAllReportsSummary());
        btnClose.addActionListener(e -> dispose());

        JPanel top = new JPanel(new BorderLayout(8, 8));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(lblInfo);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(btnReload);
        right.add(btnRun);
        right.add(btnRunAll);
        right.add(btnClose);

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        // ===== Reports list =====
        initReportList();
        lstReports.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstReports.addListSelectionListener(this::onReportSelected);
        JScrollPane reportScroll = new JScrollPane(lstReports);
        reportScroll.setPreferredSize(new Dimension(310, 1));

        // ===== Result table =====
        table.setAutoCreateRowSorter(true);
        JScrollPane tableScroll = new JScrollPane(table);

        // ===== Summary =====
        txtSummary.setEditable(false);
        txtSummary.setLineWrap(true);
        txtSummary.setWrapStyleWord(true);
        txtSummary.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane summaryScroll = new JScrollPane(txtSummary);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, summaryScroll);
        split.setResizeWeight(0.70);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(top, BorderLayout.NORTH);
        root.add(reportScroll, BorderLayout.WEST);
        root.add(split, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void initReportList() {
        if (reportListModel.size() > 0) return;

        reportListModel.addElement("1) filterByCategoryId(categoryId)");
        reportListModel.addElement("2) searchByNameContains(keyword)");
        reportListModel.addElement("3) filterByPriceRange(min, max)");
        reportListModel.addElement("4) outOfStock()");
        reportListModel.addElement("5) inventoryValueGreaterThan(threshold)");
        reportListModel.addElement("6) sortByPriceAsc()");
        reportListModel.addElement("7) topNMostExpensive(n)");
        reportListModel.addElement("8) uniqueProductNamesSorted()");
        reportListModel.addElement("9) totalQuantity()");
        reportListModel.addElement("10) totalInventoryValue()");
        reportListModel.addElement("11) priceStatistics()");
        reportListModel.addElement("12) groupByCategoryName()");
        reportListModel.addElement("13) countByCategoryName()");
        reportListModel.addElement("14) totalValueByCategoryName()");
        reportListModel.addElement("15) categoryWithMaxInventoryValue()");
    }

    private void reloadData() {
        try {
            this.products = productService.getAll(null);
            lblInfo.setText("Data loaded: " + products.size() + " products");

            if (lstReports.getSelectedIndex() < 0 && reportListModel.size() > 0) {
                lstReports.setSelectedIndex(0);
            } else {
                runSelectedReport();
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void onReportSelected(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        runSelectedReport();
    }

    private void runSelectedReport() {
        int idx = lstReports.getSelectedIndex();
        if (idx < 0) return;

        try {
            runReport(idx);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    /**
     * Chạy từng report theo index và render ra table + summary.
     */
    private void runReport(int idx) {
        if (products == null) products = new ArrayList<>();

        txtSummary.setText("");

        switch (idx) {
            case 0 -> { // 1) filterByCategoryId
                Integer catId = askCategoryId();
                if (catId == null) return;

                List<Product> result = ProductStreamQueries.filterByCategoryId(products, catId);
                showProducts(result);

                setSummary(
                        "Query: filterByCategoryId\n" +
                        "Code:  ProductStreamQueries.filterByCategoryId(products, categoryId)\n" +
                        "Params: categoryId=" + catId + "\n" +
                        "Result: " + result.size() + " products\n"
                );
            }
            case 1 -> { // 2) searchByNameContains
                String keyword = askString("Keyword (contains):", "a");
                if (keyword == null) return;

                List<Product> result = ProductStreamQueries.searchByNameContains(products, keyword);
                showProducts(result);

                setSummary(
                        "Query: searchByNameContains\n" +
                        "Code:  ProductStreamQueries.searchByNameContains(products, keyword)\n" +
                        "Params: keyword=\"" + keyword + "\"\n" +
                        "Result: " + result.size() + " products\n"
                );
            }
            case 2 -> { // 3) filterByPriceRange
                BigDecimal min = askBigDecimal("Min price:", new BigDecimal("0"));
                if (min == null) return;
                BigDecimal max = askBigDecimal("Max price:", new BigDecimal("1000"));
                if (max == null) return;

                List<Product> result = ProductStreamQueries.filterByPriceRange(products, min, max);
                showProducts(result);

                setSummary(
                        "Query: filterByPriceRange\n" +
                        "Code:  ProductStreamQueries.filterByPriceRange(products, min, max)\n" +
                        "Params: min=" + min + ", max=" + max + "\n" +
                        "Result: " + result.size() + " products\n"
                );
            }
            case 3 -> { // 4) outOfStock
                List<Product> result = ProductStreamQueries.outOfStock(products);
                showProducts(result);

                setSummary(
                        "Query: outOfStock\n" +
                        "Code:  ProductStreamQueries.outOfStock(products)\n" +
                        "Result: " + result.size() + " products (quantity == 0)\n"
                );
            }
            case 4 -> { // 5) inventoryValueGreaterThan
                BigDecimal threshold = askBigDecimal("Inventory value threshold (price*qty) >", new BigDecimal("1000"));
                if (threshold == null) return;

                List<Product> result = ProductStreamQueries.inventoryValueGreaterThan(products, threshold);
                showProducts(result);

                setSummary(
                        "Query: inventoryValueGreaterThan\n" +
                        "Code:  ProductStreamQueries.inventoryValueGreaterThan(products, threshold)\n" +
                        "Params: threshold=" + threshold + "\n" +
                        "Result: " + result.size() + " products\n" +
                        "Note: inventoryValue = price * quantity\n"
                );
            }
            case 5 -> { // 6) sortByPriceAsc
                List<Product> result = ProductStreamQueries.sortByPriceAsc(products);
                showProducts(result);

                setSummary(
                        "Query: sortByPriceAsc\n" +
                        "Code:  ProductStreamQueries.sortByPriceAsc(products)\n" +
                        "Result: " + result.size() + " products (sorted ascending by price)\n"
                );
            }
            case 6 -> { // 7) topNMostExpensive
                Integer n = askInt("Top N:", 5);
                if (n == null) return;

                List<Product> result = ProductStreamQueries.topNMostExpensive(products, n);
                showProducts(result);

                setSummary(
                        "Query: topNMostExpensive\n" +
                        "Code:  ProductStreamQueries.topNMostExpensive(products, n)\n" +
                        "Params: n=" + n + "\n" +
                        "Result: " + result.size() + " products\n"
                );
            }
            case 7 -> { // 8) uniqueProductNamesSorted
                List<String> result = ProductStreamQueries.uniqueProductNamesSorted(products);
                showStrings(result, "Product Name");

                setSummary(
                        "Query: uniqueProductNamesSorted\n" +
                        "Code:  ProductStreamQueries.uniqueProductNamesSorted(products)\n" +
                        "Result: " + result.size() + " unique names\n"
                );
            }
            case 8 -> { // 9) totalQuantity
                int total = ProductStreamQueries.totalQuantity(products);
                showKeyValue(Map.of("totalQuantity", total));

                setSummary(
                        "Query: totalQuantity\n" +
                        "Code:  ProductStreamQueries.totalQuantity(products)\n" +
                        "Result: " + total + "\n"
                );
            }
            case 9 -> { // 10) totalInventoryValue
                BigDecimal total = ProductStreamQueries.totalInventoryValue(products);
                showKeyValue(Map.of("totalInventoryValue", total));

                setSummary(
                        "Query: totalInventoryValue\n" +
                        "Code:  ProductStreamQueries.totalInventoryValue(products)\n" +
                        "Result: " + total + "\n" +
                        "Note: sum(price * quantity)\n"
                );
            }
            case 10 -> { // 11) priceStatistics
                DoubleSummaryStatistics stats = ProductStreamQueries.priceStatistics(products);

                Map<String, Object> map = new LinkedHashMap<>();
                map.put("count", stats.getCount());
                map.put("min", stats.getMin());
                map.put("max", stats.getMax());
                map.put("sum", stats.getSum());
                map.put("avg", stats.getAverage());

                showKeyValue(map);

                setSummary(
                        "Query: priceStatistics\n" +
                        "Code:  ProductStreamQueries.priceStatistics(products)\n" +
                        "Result:\n" +
                        "  count=" + stats.getCount() + "\n" +
                        "  min=" + stats.getMin() + "\n" +
                        "  max=" + stats.getMax() + "\n" +
                        "  sum=" + stats.getSum() + "\n" +
                        "  avg=" + stats.getAverage() + "\n"
                );
            }
            case 11 -> { // 12) groupByCategoryName
                Map<String, List<Product>> grouped = ProductStreamQueries.groupByCategoryName(products);

                Object[][] rows = grouped.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                        .map(e -> new Object[]{e.getKey(), e.getValue().size()})
                        .toArray(Object[][]::new);

                showTable(new String[]{"Category", "Products (#)"}, rows);

                setSummary(
                        "Query: groupByCategoryName\n" +
                        "Code:  ProductStreamQueries.groupByCategoryName(products)\n" +
                        "Result: " + grouped.size() + " groups\n" +
                        "Note: Table shows (category -> product count)\n"
                );
            }
            case 12 -> { // 13) countByCategoryName
                Map<String, Long> counts = ProductStreamQueries.countByCategoryName(products);
                showMap(counts);

                setSummary(
                        "Query: countByCategoryName\n" +
                        "Code:  ProductStreamQueries.countByCategoryName(products)\n" +
                        "Result: " + counts.size() + " categories\n"
                );
            }
            case 13 -> { // 14) totalValueByCategoryName
                Map<String, BigDecimal> totals = ProductStreamQueries.totalValueByCategoryName(products);
                showMap(totals);

                setSummary(
                        "Query: totalValueByCategoryName\n" +
                        "Code:  ProductStreamQueries.totalValueByCategoryName(products)\n" +
                        "Result: " + totals.size() + " categories\n" +
                        "Note: value = sum(price*qty) per category\n"
                );
            }
            case 14 -> { // 15) categoryWithMaxInventoryValue
                var best = ProductStreamQueries.categoryWithMaxInventoryValue(products);

                if (best.isEmpty()) {
                    showKeyValue(Map.of("result", "(no data)"));
                    setSummary(
                            "Query: categoryWithMaxInventoryValue\n" +
                            "Code:  ProductStreamQueries.categoryWithMaxInventoryValue(products)\n" +
                            "Result: (no data)\n"
                    );
                    return;
                }

                var s = best.get();
                Object[][] rows = new Object[][]{
                        {s.category(), s.productCount(), s.totalQuantity(), s.totalInventoryValue()}
                };
                showTable(new String[]{"Category", "Product count", "Total quantity", "Total inventory value"}, rows);

                setSummary(
                        "Query: categoryWithMaxInventoryValue\n" +
                        "Code:  ProductStreamQueries.categoryWithMaxInventoryValue(products)\n" +
                        "Result:\n" +
                        "  category=" + s.category() + "\n" +
                        "  productCount=" + s.productCount() + "\n" +
                        "  totalQuantity=" + s.totalQuantity() + "\n" +
                        "  totalInventoryValue=" + s.totalInventoryValue() + "\n"
                );
            }
            default -> {
                showKeyValue(Map.of("info", "Unknown report index: " + idx));
                setSummary("Unknown report index: " + idx);
            }
        }
    }

    private void runAllReportsSummary() {
        if (products == null || products.isEmpty()) {
            txtSummary.setText("No data loaded.");
            showKeyValue(Map.of("info", "No data loaded."));
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("RUN ALL (overview)\n\n");
        sb.append("Total products: ").append(products.size()).append("\n");
        sb.append("Total quantity: ").append(ProductStreamQueries.totalQuantity(products)).append("\n");
        sb.append("Total inventory value: ").append(ProductStreamQueries.totalInventoryValue(products)).append("\n");

        DoubleSummaryStatistics stats = ProductStreamQueries.priceStatistics(products);
        sb.append("\nPrice statistics\n");
        sb.append("  count=").append(stats.getCount()).append("\n");
        sb.append("  min=").append(stats.getMin()).append("\n");
        sb.append("  max=").append(stats.getMax()).append("\n");
        sb.append("  avg=").append(stats.getAverage()).append("\n");

        var best = ProductStreamQueries.categoryWithMaxInventoryValue(products);
        sb.append("\nBest category by inventory value\n");
        sb.append("  ").append(best.map(b -> b.category() + " | total=" + b.totalInventoryValue()).orElse("(no data)")).append("\n");

        Map<String, Long> byCat = ProductStreamQueries.countByCategoryName(products);
        sb.append("\nCategories: ").append(byCat.size()).append("\n");

        txtSummary.setText(sb.toString());

        // hiển thị Map countByCategoryName như 1 bảng để nhìn rõ
        showMap(byCat);
    }

    // ===== Render helpers =====

    private void showProducts(List<Product> list) {
        table.setModel(productTableModel);
        productTableModel.setData(list);
    }

    private void showStrings(List<String> list, String colName) {
        Object[][] rows = list.stream().map(s -> new Object[]{s}).toArray(Object[][]::new);
        showTable(new String[]{colName}, rows);
    }

    private void showMap(Map<?, ?> map) {
        Object[][] rows = map.entrySet().stream()
                .sorted(Comparator.comparing(e -> String.valueOf(e.getKey()), String.CASE_INSENSITIVE_ORDER))
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toArray(Object[][]::new);
        showTable(new String[]{"Key", "Value"}, rows);
    }

    private void showKeyValue(Map<String, ?> map) {
        Object[][] rows = map.entrySet().stream()
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toArray(Object[][]::new);
        showTable(new String[]{"Metric", "Value"}, rows);
    }

    private void showTable(String[] columns, Object[][] rows) {
        table.setModel(simpleTableModel);
        simpleTableModel.setData(columns, rows);
    }

    private void setSummary(String s) {
        txtSummary.setText(s == null ? "" : s.trim());
    }

    // ===== Input helpers =====

    private String askString(String prompt, String defaultValue) {
        return (String) JOptionPane.showInputDialog(
                this,
                prompt,
                "Input",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                defaultValue
        );
    }

    private Integer askInt(String prompt, Integer defaultValue) {
        String raw = (String) JOptionPane.showInputDialog(
                this,
                prompt,
                "Input",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                defaultValue == null ? "" : defaultValue.toString()
        );
        if (raw == null) return null;
        raw = raw.trim();
        if (raw.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid integer: " + raw);
            return null;
        }
    }

    private BigDecimal askBigDecimal(String prompt, BigDecimal defaultValue) {
        String raw = (String) JOptionPane.showInputDialog(
                this,
                prompt,
                "Input",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                defaultValue == null ? "" : defaultValue.toPlainString()
        );
        if (raw == null) return null;
        raw = raw.trim();
        if (raw.isBlank()) return defaultValue;
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number: " + raw);
            return null;
        }
    }

    private Integer askCategoryId() {
        // Tạo danh sách category từ dữ liệu products (distinct by id)
        Map<Integer, String> cats = products.stream()
                .map(Product::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Category::getId,
                        Category::getName,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        if (cats.isEmpty()) {
            return askInt("Category ID:", null);
        }

        Object[] options = cats.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + " - " + e.getValue())
                .toArray();

        Object chosen = JOptionPane.showInputDialog(
                this,
                "Choose a category:",
                "Category",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (chosen == null) return null;

        String s = String.valueOf(chosen);
        int dash = s.indexOf(" - ");
        if (dash > 0) s = s.substring(0, dash).trim();

        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid category id: " + chosen);
            return null;
        }
    }

    // ===== Error helper =====
    private void showError(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ===== Simple generic table model =====
    private static final class SimpleTableModel extends AbstractTableModel {
        private String[] columns = new String[0];
        private Object[][] rows = new Object[0][0];

        public void setData(String[] columns, Object[][] rows) {
            this.columns = columns == null ? new String[0] : columns;
            this.rows = rows == null ? new Object[0][0] : rows;
            fireTableStructureChanged();
        }

        @Override public int getRowCount() { return rows.length; }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex < 0 || rowIndex >= rows.length) return null;
            if (columnIndex < 0 || columnIndex >= columns.length) return null;
            return rows[rowIndex][columnIndex];
        }
    }
}
