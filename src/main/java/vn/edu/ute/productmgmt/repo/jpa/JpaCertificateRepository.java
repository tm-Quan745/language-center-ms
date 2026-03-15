package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Certificate;
import vn.edu.ute.productmgmt.repo.CertificateRepository;

import java.util.List;

public class JpaCertificateRepository implements CertificateRepository {

    @Override
    public void insert(EntityManager em, Certificate certificate) {
        em.persist(certificate);
    }

    @Override
    public void update(EntityManager em, Certificate certificate) {
        em.merge(certificate);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        Certificate c = em.find(Certificate.class, id);
        if (c != null) {
            em.remove(c);
        }
    }

    @Override
    public Certificate findById(EntityManager em, Long id) {
        return em.find(Certificate.class, id);
    }

    @Override
    public List<Certificate> findByStudentId(EntityManager em, Long studentId) {
        return em.createQuery(
                "SELECT c FROM Certificate c " +
                        "LEFT JOIN FETCH c.student " +
                        "LEFT JOIN FETCH c.teachingClass " +
                        "WHERE c.student.id = :studentId " +
                        "ORDER BY c.issueDate DESC, c.id",
                Certificate.class
        ).setParameter("studentId", studentId).getResultList();
    }

    @Override
    public List<Certificate> findAll(EntityManager em) {
        return em.createQuery(
                "SELECT c FROM Certificate c " +
                        "LEFT JOIN FETCH c.student " +
                        "LEFT JOIN FETCH c.teachingClass " +
                        "ORDER BY c.issueDate DESC, c.id",
                Certificate.class
        ).getResultList();
    }
}
