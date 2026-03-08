package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Certificate;

import java.util.List;

public interface CertificateRepository {

    void insert(EntityManager em, Certificate certificate);

    void update(EntityManager em, Certificate certificate);

    void delete(EntityManager em, Long id);

    Certificate findById(EntityManager em, Long id);

    List<Certificate> findAll(EntityManager em);
}
