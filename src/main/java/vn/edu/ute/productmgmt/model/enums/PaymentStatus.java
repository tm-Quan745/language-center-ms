package vn.edu.ute.productmgmt.model.enums;

/**
 * Trạng thái thanh toán.
 * Khớp SQL: ENUM('Pending','Completed','Failed','Refunded')
 */
public enum PaymentStatus {
    Pending,
    Completed,
    Failed,
    Refunded
}
