package vn.edu.ute.productmgmt.service;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.db.Jpa;
import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Payment;
import vn.edu.ute.productmgmt.model.Student;
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

    public void create(Payment payment) throws Exception {
        validate(payment);
        tx.runInTransaction(em -> {
            paymentRepo.insert(em, payment);
            return null;
        });
    }

    public void update(Payment payment) throws Exception {
        validate(payment);
        tx.runInTransaction(em -> {
            paymentRepo.update(em, payment);
            return null;
        });
    }

    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            paymentRepo.delete(em, id);
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

    /**
     * Tạo Payment từ các primitive/id – tiện cho UI.
     */
    public Payment createPayment(Long studentId,
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

            Payment p = new Payment();
            p.setStudent(studentRef);
            p.setAmount(amount);
            p.setPaymentDate(paymentDate != null ? paymentDate : LocalDateTime.now());
            p.setPaymentMethod(method != null ? method : PaymentMethod.Cash);
            p.setStatus(status != null ? status : PaymentStatus.Completed);
            p.setReferenceCode(referenceCode);

            validate(p);
            paymentRepo.insert(em, p);
            return p;
        });
    }

    /**
     * Cập nhật Payment cơ bản (không bắt buộc đổi student).
     */
    public Payment updatePayment(Long id,
                                 Long studentId,
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
                Student studentRef = em.getReference(Student.class, studentId);
                existing.setStudent(studentRef);
            }
            if (amount != null) existing.setAmount(amount);
            if (paymentDate != null) existing.setPaymentDate(paymentDate);
            if (method != null) existing.setPaymentMethod(method);
            if (status != null) existing.setStatus(status);
            existing.setReferenceCode(referenceCode);

            validate(existing);
            paymentRepo.update(em, existing);
            return existing;
        });
    }

    private void validate(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment không được null");
        }
        if (payment.getStudent() == null) {
            throw new IllegalArgumentException("Payment phải gắn với Student");
        }
        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải > 0");
        }
        if (payment.getPaymentDate() == null) {
            throw new IllegalArgumentException("Payment phải có ngày thanh toán");
        }
    }
}

