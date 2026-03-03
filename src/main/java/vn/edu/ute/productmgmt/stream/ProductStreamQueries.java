package vn.edu.ute.productmgmt.stream;

import vn.edu.ute.productmgmt.model.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 15 ví dụ "truy vấn" (query/analytics) dùng Java Stream + Lambda trên List<Product>.
 *
 * Gợi ý dùng trong dự án:
 *     List<Product> products = productService.getAll(null); // lấy tất cả (repo đã join fetch category)
 *     var top = ProductStreamQueries.topNMostExpensive(products, 5);
 *
 * Lưu ý BigDecimal:
 * - Cộng/nhân nên dùng reduce/Collectors.reducing để tránh lỗi số thực.
 * - Khi cần average, có thể chuyển sang double (thống kê) hoặc tự chia BigDecimal có scale.
 */
public final class ProductStreamQueries {

    private ProductStreamQueries() {}

    /** DTO view (projection) để hiển thị/report, không phụ thuộc UI. */
    public record ProductView(
            int id,
            String name,
            String category,
            BigDecimal price,
            int quantity,
            BigDecimal inventoryValue
    ) {}

    /** Tóm tắt theo Category (đếm, tổng qty, tổng value). */
    public record CategorySummary(
            String category,
            long productCount,
            int totalQuantity,
            BigDecimal totalInventoryValue
    ) {}

    // ===== Helpers =====

    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

    private static BigDecimal inventoryValue(Product p) {
        // price * quantity
        return p.getPrice().multiply(BigDecimal.valueOf(p.getQuantity()));
    }

    // ======================================================================
    // 1) Lọc theo categoryId (filter)
    // ======================================================================
    public static List<Product> filterByCategoryId(List<Product> products, int categoryId) {
        return products.stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getId() == categoryId)
                .collect(Collectors.toList());
    }

    // ======================================================================
    // 2) Tìm theo keyword trong tên (case-insensitive)
    // ======================================================================
    public static List<Product> searchByNameContains(List<Product> products, String keyword) {
        String k = safeLower(keyword).trim();
        return products.stream()
                .filter(p -> safeLower(p.getName()).contains(k))
                .collect(Collectors.toList());
    }

    // ======================================================================
    // 3) Lọc theo khoảng giá [min, max]
    // ======================================================================
    public static List<Product> filterByPriceRange(List<Product> products, BigDecimal min, BigDecimal max) {
        BigDecimal lo = min == null ? BigDecimal.ZERO : min;
        BigDecimal hi = max == null ? new BigDecimal("999999999999") : max;
        return products.stream()
                .filter(p -> p.getPrice() != null
                        && p.getPrice().compareTo(lo) >= 0
                        && p.getPrice().compareTo(hi) <= 0)
                .collect(Collectors.toList());
    }

    // ======================================================================
    // 4) Sản phẩm hết hàng (quantity == 0)
    // ======================================================================
    public static List<Product> outOfStock(List<Product> products) {
        return products.stream()
                .filter(p -> p.getQuantity() == 0)
                .collect(Collectors.toList());
    }

    // ======================================================================
    // 5) Lọc theo "giá trị tồn kho" (price * quantity) > threshold
    // ======================================================================
    public static List<Product> inventoryValueGreaterThan(List<Product> products, BigDecimal threshold) {
        BigDecimal th = threshold == null ? BigDecimal.ZERO : threshold;
        return products.stream()
                .filter(p -> inventoryValue(p).compareTo(th) > 0)
                .collect(Collectors.toList());
    }

    // ======================================================================
    // 6) Sắp xếp theo giá tăng dần
    // ======================================================================
    public static List<Product> sortByPriceAsc(List<Product> products) {
        return products.stream()
                .sorted(Comparator.comparing(Product::getPrice))
                .collect(Collectors.toList());
    }

    // ======================================================================
    // 7) Top N sản phẩm đắt nhất (sort desc + limit)
    // ======================================================================
    public static List<Product> topNMostExpensive(List<Product> products, int n) {
        return products.stream()
                .sorted(Comparator.comparing(Product::getPrice).reversed())
                .limit(Math.max(n, 0))
                .collect(Collectors.toList());
    }

    // ======================================================================
    // 8) Danh sách tên sản phẩm unique (distinct) + sort
    // ======================================================================
    public static List<String> uniqueProductNamesSorted(List<Product> products) {
        return products.stream()
                .map(Product::getName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    // ======================================================================
    // 9) Tổng số lượng tồn (sum quantity)
    // ======================================================================
    public static int totalQuantity(List<Product> products) {
        return products.stream()
                .mapToInt(Product::getQuantity)
                .sum();
    }

    // ======================================================================
    // 10) Tổng giá trị tồn kho (sum price * qty) - BigDecimal reduce
    // ======================================================================
    public static BigDecimal totalInventoryValue(List<Product> products) {
        return products.stream()
                .map(ProductStreamQueries::inventoryValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ======================================================================
    // 11) Thống kê giá (min/max/avg) - DoubleSummaryStatistics
    // ======================================================================
    public static DoubleSummaryStatistics priceStatistics(List<Product> products) {
        return products.stream()
                .filter(p -> p.getPrice() != null)
                .collect(Collectors.summarizingDouble(p -> p.getPrice().doubleValue()));
    }

    // ======================================================================
    // 12) Group sản phẩm theo categoryName -> Map<String, List<Product>>
    // ======================================================================
    public static Map<String, List<Product>> groupByCategoryName(List<Product> products) {
        return products.stream()
                .collect(Collectors.groupingBy(p -> p.getCategory() == null ? "(none)" : p.getCategory().getName()));
    }

    // ======================================================================
    // 13) Đếm số sản phẩm theo categoryName -> Map<String, Long>
    // ======================================================================
    public static Map<String, Long> countByCategoryName(List<Product> products) {
        return products.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategory() == null ? "(none)" : p.getCategory().getName(),
                        Collectors.counting()
                ));
    }

    // ======================================================================
    // 14) Tổng giá trị tồn kho theo categoryName -> Map<String, BigDecimal>
    // ======================================================================
    public static Map<String, BigDecimal> totalValueByCategoryName(List<Product> products) {
        return products.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategory() == null ? "(none)" : p.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, ProductStreamQueries::inventoryValue, BigDecimal::add)
                ));
    }

    // ======================================================================
    // 15) Category có tổng giá trị tồn kho lớn nhất -> Optional<CategorySummary>
    //     (kết hợp grouping + mapping + reducing + max)
    // ======================================================================
    public static Optional<CategorySummary> categoryWithMaxInventoryValue(List<Product> products) {
        Map<String, List<Product>> grouped = groupByCategoryName(products);

        return grouped.entrySet().stream()
                .map(e -> {
                    String cat = e.getKey();
                    List<Product> ps = e.getValue();

                    long count = ps.size();
                    int totalQty = ps.stream().mapToInt(Product::getQuantity).sum();
                    BigDecimal totalVal = ps.stream()
                            .map(ProductStreamQueries::inventoryValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new CategorySummary(cat, count, totalQty, totalVal);
                })
                .max(Comparator.comparing(CategorySummary::totalInventoryValue));
    }

    // ===== Bonus (thường dùng khi hiển thị trên UI / export CSV) =====

    /** Projection: Product -> ProductView (map). */
    public static List<ProductView> toProductViews(List<Product> products) {
        return products.stream()
                .map(p -> new ProductView(
                        p.getId(),
                        p.getName(),
                        p.getCategory() == null ? "(none)" : p.getCategory().getName(),
                        p.getPrice(),
                        p.getQuantity(),
                        inventoryValue(p)
                ))
                .collect(Collectors.toList());
    }

    /** Join tên sản phẩm thành 1 chuỗi CSV-style (map + joining). */
    public static String joinNames(List<Product> products, String delimiter) {
        String d = delimiter == null ? ", " : delimiter;
        return products.stream()
                .map(Product::getName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(d));
    }

    /** Average tồn kho theo giá trị (BigDecimal) = totalValue / numberOfProducts (ví dụ chia có scale). */
    public static BigDecimal averageInventoryValuePerProduct(List<Product> products, int scale) {
        if (products == null || products.isEmpty()) return BigDecimal.ZERO;
        BigDecimal total = totalInventoryValue(products);
        return total.divide(BigDecimal.valueOf(products.size()), scale, RoundingMode.HALF_UP);
    }
}
