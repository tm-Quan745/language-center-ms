package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Teacher;
import vn.edu.ute.productmgmt.repo.TeacherRepository;

import java.util.List;

public class JpaTeacherRepository implements TeacherRepository {

    @Override
    public void insert(EntityManager em, Teacher teacher) {
        em.persist(teacher);
    }

    @Override
    public Teacher findById(EntityManager em, Long id) {
        return em.find(Teacher.class, id);
    }

    @Override
    public List<Teacher> findAll(EntityManager em) {
        return em.createQuery(
                "SELECT t FROM Teacher t ORDER BY t.fullName",
                Teacher.class
        ).getResultList();
    }

    @Override
    public List<Teacher> findByStatus(EntityManager em, String status) {
        return em.createQuery(
                        "SELECT t FROM Teacher t WHERE t.status = :status ORDER BY t.fullName",
                        Teacher.class)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public void update(EntityManager em, Teacher teacher) {
        em.merge(teacher);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        Teacher teacher = em.find(Teacher.class, id);
        if (teacher != null) {
            em.remove(teacher);
        }
    }
}