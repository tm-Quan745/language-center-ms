package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Payment;
import vn.edu.ute.productmgmt.repo.PaymentRepository;

import java.util.List;

public class JpaPaymentRepository implements PaymentRepository {

    @Override
    public void insert(EntityManager em, Payment payment) {
        em.persist(payment);
    }

    @Override
    public void update(EntityManager em, Payment payment) {
        em.merge(payment);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        Payment p = em.find(Payment.class, id);
        if (p != null) {
            em.remove(p);
        }
    }

    @Override
    public Payment findById(EntityManager em, Long id) {
        return em.find(Payment.class, id);
    }

    @Override
    public List<Payment> findAll(EntityManager em) {
        return em.createQuery(
                "SELECT DISTINCT p FROM Payment p " +
                "LEFT JOIN FETCH p.student " +
                "LEFT JOIN FETCH p.enrollment " +
                "LEFT JOIN FETCH p.invoice",
                Payment.class
        ).getResultList();
    }
}


