package vn.edu.ute.productmgmt.model.enums;

/**
 * Trạng thái hóa đơn.
 * Khớp SQL: ENUM('Draft','Issued','Paid','Cancelled')
 */
public enum InvoiceStatus {
    Draft,
    Issued,
    Paid,
    Cancelled
}
