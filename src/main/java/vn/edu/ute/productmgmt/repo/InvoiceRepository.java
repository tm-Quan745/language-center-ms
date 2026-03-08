package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Invoice;

import java.util.List;

public interface InvoiceRepository {

    void insert(EntityManager em, Invoice invoice);

    void update(EntityManager em, Invoice invoice);

    void delete(EntityManager em, Long id);

    Invoice findById(EntityManager em, Long id);

    List<Invoice> findAll(EntityManager em);
}
