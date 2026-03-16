package vn.edu.ute.productmgmt.service;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.db.Jpa;
import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.*;
import vn.edu.ute.productmgmt.model.enums.InvoiceStatus;
import vn.edu.ute.productmgmt.model.enums.PaymentMethod;
import vn.edu.ute.productmgmt.model.enums.PaymentStatus;
import vn.edu.ute.productmgmt.repo.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final TransactionManager tx;

    public PaymentService(PaymentRepository paymentRepo, TransactionManager tx) {
        this.paymentRepo = paymentRepo;
        this.tx = tx;
    }

    // =============================
    // CRUD
    // =============================

    public void create(Payment payment) throws Exception {
        validate(payment);

        tx.runInTransaction(em -> {

            validateInvoicePayment(em, payment);

            paymentRepo.insert(em, payment);

            updateInvoiceStatus(em, payment.getInvoice());

            return null;
        });
    }

    public void update(Payment payment) throws Exception {
        validate(payment);

        tx.runInTransaction(em -> {

            validateInvoicePayment(em, payment);

            paymentRepo.update(em, payment);

            updateInvoiceStatus(em, payment.getInvoice());

            return null;
        });
    }

    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {

            Payment p = paymentRepo.findById(em, id);

            paymentRepo.delete(em, id);

            if(p != null) {
                updateInvoiceStatus(em, p.getInvoice());
            }

            return null;
        });
    }

    public Payment getById(Long id) throws Exception {
        return tx.runInTransaction(em -> paymentRepo.findById(em, id));
    }

    public List<Payment> findAll() {
        EntityManager em = Jpa.em();
        try {
            return paymentRepo.findAll(em);
        } finally {
            em.close();
        }
    }

    // =============================
    // CREATE FROM UI
    // =============================

    public Payment createPayment(Long studentId,
                                 Long enrollmentId,
                                 Long invoiceId,
                                 BigDecimal amount,
                                 LocalDateTime paymentDate,
                                 PaymentMethod method,
                                 PaymentStatus status,
                                 String referenceCode) throws Exception {

        return tx.runInTransaction(em -> {

            if (studentId == null) {
                throw new IllegalArgumentException("Payment phải gắn với Student");
            }

            Student studentRef = em.getReference(Student.class, studentId);

            Enrollment enrollmentRef =
                    enrollmentId != null ? em.getReference(Enrollment.class, enrollmentId) : null;

            Invoice invoiceRef =
                    invoiceId != null ? em.getReference(Invoice.class, invoiceId) : null;

            Payment p = new Payment();

            p.setStudent(studentRef);
            p.setEnrollment(enrollmentRef);
            p.setInvoice(invoiceRef);
            p.setAmount(amount);
            p.setPaymentDate(paymentDate != null ? paymentDate : LocalDateTime.now());
            p.setPaymentMethod(method != null ? method : PaymentMethod.Cash);
            p.setStatus(status != null ? status : PaymentStatus.Completed);
            p.setReferenceCode(referenceCode);

            validate(p);

            validateInvoicePayment(em, p);

            paymentRepo.insert(em, p);

            updateInvoiceStatus(em, invoiceRef);


            return p;
        });
    }

    // =============================
    // UPDATE FROM UI
    // =============================

    public Payment updatePayment(Long id,
                                 Long studentId,
                                 Long enrollmentId,
                                 Long invoiceId,
                                 BigDecimal amount,
                                 LocalDateTime paymentDate,
                                 PaymentMethod method,
                                 PaymentStatus status,
                                 String referenceCode) throws Exception {

        return tx.runInTransaction(em -> {

            Payment existing = paymentRepo.findById(em, id);

            if (existing == null) {
                throw new IllegalArgumentException("Không tìm thấy payment id=" + id);
            }

            if (studentId != null) {
                existing.setStudent(em.getReference(Student.class, studentId));
            }

            existing.setEnrollment(
                    enrollmentId != null ? em.getReference(Enrollment.class, enrollmentId) : null
            );

            existing.setInvoice(
                    invoiceId != null ? em.getReference(Invoice.class, invoiceId) : null
            );

            if (amount != null) existing.setAmount(amount);
            if (paymentDate != null) existing.setPaymentDate(paymentDate);
            if (method != null) existing.setPaymentMethod(method);
            if (status != null) existing.setStatus(status);

            existing.setReferenceCode(referenceCode);

            validate(existing);

            validateInvoicePayment(em, existing);

            paymentRepo.update(em, existing);

            updateInvoiceStatus(em, existing.getInvoice());

            return existing;
        });
    }

    // =============================
    // BUSINESS LOGIC
    // =============================

    /**
     * Tính tổng tiền đã thu của 1 invoice
     */
    private BigDecimal getPaidAmount(EntityManager em, Invoice invoice){

        if(invoice == null) return BigDecimal.ZERO;

        List<Payment> payments =
                paymentRepo.findByInvoiceAndStatus(em, invoice, PaymentStatus.Completed);

        BigDecimal total = BigDecimal.ZERO;

        for(Payment p : payments){

            if(p.getAmount() != null){
                total = total.add(p.getAmount());
            }

        }

        return total;
    }

    /**
     * Tính số tiền còn nợ
     */
    private BigDecimal getRemainingAmount(EntityManager em, Invoice invoice){

        if(invoice == null) return BigDecimal.ZERO;

        BigDecimal total =
                invoice.getTotalAmount() != null
                        ? invoice.getTotalAmount()
                        : BigDecimal.ZERO;

        BigDecimal paid = getPaidAmount(em, invoice);

        return total.subtract(paid);
    }

    /**
     * Không cho thanh toán vượt tiền hóa đơn
     */
    private void validateInvoicePayment(EntityManager em, Payment payment){

        Invoice invoice = payment.getInvoice();

        if(invoice == null) return;

        if(payment.getStatus() != PaymentStatus.Completed) return;

        BigDecimal remaining = getRemainingAmount(em, invoice);

        if(payment.getAmount().compareTo(remaining) > 0){

            throw new IllegalArgumentException(
                    "Số tiền thanh toán vượt số tiền còn nợ (" + remaining + ")"
            );

        }
    }

    /**
     * Cập nhật trạng thái invoice
     */
    private void updateInvoiceStatus(EntityManager em, Invoice invoice){

        if(invoice == null) return;

        BigDecimal total =
                invoice.getTotalAmount() != null
                        ? invoice.getTotalAmount()
                        : BigDecimal.ZERO;

        BigDecimal paid = getPaidAmount(em, invoice);

        if(paid.compareTo(BigDecimal.ZERO) == 0){

            invoice.setStatus(InvoiceStatus.Issued);

        }
        else if(paid.compareTo(total) < 0){

            invoice.setStatus(InvoiceStatus.Issued);

        }
        else{

            invoice.setStatus(InvoiceStatus.Paid);

        }

        em.merge(invoice);
    }

    // =============================
    // VALIDATION
    // =============================

    private void validate(Payment payment) {

        if (payment == null) {
            throw new IllegalArgumentException("Payment không được null");
        }

        if (payment.getStudent() == null) {
            throw new IllegalArgumentException("Payment phải gắn với Student");
        }

        if (payment.getAmount() == null ||
                payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException("Số tiền thanh toán phải > 0");
        }

        if (payment.getPaymentDate() == null) {
            throw new IllegalArgumentException("Payment phải có ngày thanh toán");
        }
    }
}