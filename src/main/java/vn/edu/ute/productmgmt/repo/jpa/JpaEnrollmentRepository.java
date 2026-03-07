package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Enrollment;
import vn.edu.ute.productmgmt.repo.EnrollmentRepository;

import java.util.List;

public class JpaEnrollmentRepository implements EnrollmentRepository {

    @Override
    public void insert(EntityManager em, Enrollment enrollment) {
        em.persist(enrollment);
    }

    @Override
    public void update(EntityManager em, Enrollment enrollment) {
        em.merge(enrollment);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        Enrollment e = em.find(Enrollment.class, id);
        if (e != null) {
            em.remove(e);
        }
    }

    @Override
    public Enrollment findById(EntityManager em, Long id) {
        return em.find(Enrollment.class, id);
    }

    @Override
    public List<Enrollment> findAll(EntityManager em) {
        // join fetch student & class để UI đọc được tên sau khi EntityManager đóng
        return em.createQuery(
                        "SELECT e FROM Enrollment e " +
                                "JOIN FETCH e.student " +
                                "JOIN FETCH e.teachingClass",
                        Enrollment.class)
                .getResultList();
    }
}

