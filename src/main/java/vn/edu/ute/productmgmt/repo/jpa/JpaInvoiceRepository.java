package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Invoice;
import vn.edu.ute.productmgmt.repo.InvoiceRepository;

import java.util.List;

public class JpaInvoiceRepository implements InvoiceRepository {

    @Override
    public void insert(EntityManager em, Invoice invoice) {
        em.persist(invoice);
    }

    @Override
    public void update(EntityManager em, Invoice invoice) {
        em.merge(invoice);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        Invoice inv = em.find(Invoice.class, id);
        if (inv != null) {
            em.remove(inv);
        }
    }

    @Override
    public Invoice findById(EntityManager em, Long id) {
        return em.find(Invoice.class, id);
    }

    @Override
    public List<Invoice> findAll(EntityManager em) {
        return em.createQuery(
                "SELECT DISTINCT i FROM Invoice i " +
                "LEFT JOIN FETCH i.student " +
                "LEFT JOIN FETCH i.promotion " +
                "ORDER BY i.issueDate DESC, i.id DESC",
                Invoice.class
        ).getResultList();
    }
}
