package vn.edu.ute.productmgmt.service;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.db.Jpa;
import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Invoice;
import vn.edu.ute.productmgmt.model.Promotion;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.enums.InvoiceStatus;
import vn.edu.ute.productmgmt.repo.InvoiceRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InvoiceService {

    private final InvoiceRepository invoiceRepo;
    private final PromotionService promotionService;
    private final TransactionManager tx;

    public InvoiceService(InvoiceRepository invoiceRepo, PromotionService promotionService, TransactionManager tx) {
        this.invoiceRepo = invoiceRepo;
        this.promotionService = promotionService;
        this.tx = tx;
    }

    public void create(Invoice invoice) throws Exception {
        validate(invoice);
        tx.runInTransaction(em -> {
            invoiceRepo.insert(em, invoice);
            return null;
        });
    }

    public void update(Invoice invoice) throws Exception {
        validate(invoice);
        tx.runInTransaction(em -> {
            invoiceRepo.update(em, invoice);
            return null;
        });
    }

    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            invoiceRepo.delete(em, id);
            return null;
        });
    }

    public Invoice findById(Long id) {
        EntityManager em = Jpa.em();
        try {
            return invoiceRepo.findById(em, id);
        } finally {
            em.close();
        }
    }

    public List<Invoice> findAll() {
        EntityManager em = Jpa.em();
        try {
            return invoiceRepo.findAll(em);
        } finally {
            em.close();
        }
    }

    /**
     * Tạo hóa đơn: kiểm tra khuyến mãi còn hiệu lực, tự động tính tổng tiền từ số tiền gốc.
     */
    public void createInvoice(Long studentId,
                              Long promotionId,
                              BigDecimal baseAmount,
                              LocalDate issueDate,
                              InvoiceStatus status,
                              String note) throws Exception {
        if (studentId == null) {
            throw new IllegalArgumentException("Hóa đơn phải gắn với học viên");
        }
        if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số tiền gốc phải >= 0");
        }
        LocalDate effectiveDate = issueDate != null ? issueDate : LocalDate.now();

        Promotion promotion = null;
        if (promotionId != null && promotionService != null) {
            promotion = promotionService.findById(promotionId);
            if (promotion != null && !promotionService.isPromotionValid(promotion, effectiveDate)) {
                throw new IllegalArgumentException("Khuyến mãi không còn hiệu lực (hết hạn hoặc chưa đến ngày áp dụng)");
            }
            if (promotion != null) {
                baseAmount = promotionService.calculateDiscountedAmount(baseAmount, promotion);
            } else {
                promotion = null;
            }
        }

        final BigDecimal totalAmount = baseAmount;
        final Promotion finalPromo = promotion;
        tx.runInTransaction(em -> {
            Student studentRef = em.getReference(Student.class, studentId);
            Promotion promotionRef = finalPromo != null ? em.merge(finalPromo) : null;

            Invoice inv = new Invoice();
            inv.setStudent(studentRef);
            inv.setPromotion(promotionRef);
            inv.setTotalAmount(totalAmount);
            inv.setIssueDate(effectiveDate);
            inv.setStatus(status != null ? status : InvoiceStatus.Issued);
            inv.setNote(note);

            validate(inv);
            invoiceRepo.insert(em, inv);
            return null;
        });
    }

    /**
     * Cập nhật hóa đơn: kiểm tra khuyến mãi còn hiệu lực, tự động tính tổng tiền từ số tiền gốc.
     */
    public void updateInvoice(Long id,
                              Long studentId,
                              Long promotionId,
                              BigDecimal baseAmount,
                              LocalDate issueDate,
                              InvoiceStatus status,
                              String note) throws Exception {
        if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số tiền gốc phải >= 0");
        }
        LocalDate effectiveDate = issueDate != null ? issueDate : LocalDate.now();

        Promotion promotion = null;
        if (promotionId != null && promotionService != null) {
            promotion = promotionService.findById(promotionId);
            if (promotion != null && !promotionService.isPromotionValid(promotion, effectiveDate)) {
                throw new IllegalArgumentException("Khuyến mãi không còn hiệu lực (hết hạn hoặc chưa đến ngày áp dụng)");
            }
            if (promotion != null) {
                baseAmount = promotionService.calculateDiscountedAmount(baseAmount, promotion);
            } else {
                promotion = null;
            }
        }

        final BigDecimal totalAmount = baseAmount;
        final Promotion finalPromo = promotion;
        tx.runInTransaction(em -> {
            Invoice existing = invoiceRepo.findById(em, id);
            if (existing == null) {
                throw new IllegalArgumentException("Không tìm thấy hóa đơn id=" + id);
            }
            if (studentId != null) {
                existing.setStudent(em.getReference(Student.class, studentId));
            }
            existing.setPromotion(finalPromo != null ? em.merge(finalPromo) : null);
            existing.setTotalAmount(totalAmount);
            if (issueDate != null) existing.setIssueDate(issueDate);
            if (status != null) existing.setStatus(status);
            existing.setNote(note);

            validate(existing);
            invoiceRepo.update(em, existing);
            return null;
        });
    }

    private void validate(Invoice inv) {
        if (inv == null) {
            throw new IllegalArgumentException("Invoice không được null");
        }
        if (inv.getStudent() == null) {
            throw new IllegalArgumentException("Hóa đơn phải gắn với học viên");
        }
        if (inv.getTotalAmount() == null || inv.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tổng tiền phải >= 0");
        }
        if (inv.getIssueDate() == null) {
            throw new IllegalArgumentException("Hóa đơn phải có ngày phát hành");
        }
    }
}
