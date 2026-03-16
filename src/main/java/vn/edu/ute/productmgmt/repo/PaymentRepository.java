package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Invoice;
import vn.edu.ute.productmgmt.model.Payment;
import vn.edu.ute.productmgmt.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository {

    void insert(EntityManager em, Payment payment);

    void update(EntityManager em, Payment payment);

    void delete(EntityManager em, Long id);

    Payment findById(EntityManager em, Long id);

    List<Payment> findAll(EntityManager em);

    List<Payment> findByInvoiceAndStatus(EntityManager em, Invoice invoice, PaymentStatus status);

    BigDecimal sumCompletedPaymentsByInvoice(EntityManager em, Invoice invoice);
}

