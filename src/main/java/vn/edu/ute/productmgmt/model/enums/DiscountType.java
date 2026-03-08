package vn.edu.ute.productmgmt.model.enums;

/**
 * Loại giảm giá: phần trăm hoặc số tiền cố định.
 * Khớp SQL: ENUM('Percent','Amount')
 */
public enum DiscountType {
    Percent,
    Amount
}
