package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.ClassStatus;
import vn.edu.ute.productmgmt.model.TeachingClass;
import vn.edu.ute.productmgmt.repo.ClassRepository;

import java.util.List;
import java.util.UUID;

public class JpaClassRepository implements ClassRepository {

    @Override
    public void insert(EntityManager em, TeachingClass teachingClass) {
        em.persist(teachingClass);
    }

    @Override
    public TeachingClass findById(EntityManager em, UUID id) {
        return em.find(TeachingClass.class, id);
    }

    @Override
    public List<TeachingClass> findAll(EntityManager em) {
        return em.createQuery(
                "SELECT DISTINCT tc FROM TeachingClass tc " +
                        "LEFT JOIN FETCH tc.course " +
                        "LEFT JOIN FETCH tc.teacher " +
                        "LEFT JOIN FETCH tc.room " +
                        "LEFT JOIN FETCH tc.branch",
                TeachingClass.class
        ).getResultList();
    }

    @Override
    public List<TeachingClass> findByStatus(EntityManager em, String status) {
        return em.createQuery(
                        "SELECT tc FROM TeachingClass tc WHERE tc.status = :status",
                        TeachingClass.class)
                .setParameter("status", ClassStatus.valueOf(status))
                .getResultList();
    }

    @Override
    public void update(EntityManager em, TeachingClass teachingClass) {
        em.merge(teachingClass);
    }

    @Override
    public void delete(EntityManager em, UUID id) {
        TeachingClass tc = em.find(TeachingClass.class, id);
        if (tc != null) {
            em.remove(tc);
        }
    }
}