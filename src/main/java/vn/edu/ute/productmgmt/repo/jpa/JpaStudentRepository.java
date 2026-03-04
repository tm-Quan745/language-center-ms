package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.repo.StudentRepository;

import java.util.List;

public class JpaStudentRepository implements StudentRepository {

    @Override
    public void insert(EntityManager em, Student student) {
        em.persist(student);
    }

    @Override
    public void update(EntityManager em, Student student) {
        em.merge(student);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        Student student = em.find(Student.class, id);
        if (student != null) {
            em.remove(student);
        }
    }

    @Override
    public Student findById(EntityManager em, Long id) {
        return em.find(Student.class, id);
    }

    @Override
    public List<Student> findAll(EntityManager em) {
        return em.createQuery("SELECT s FROM Student s ORDER BY s.fullName", Student.class)
                .getResultList();
    }

    @Override
    public List<Student> findByFullNameOrPhone(EntityManager em, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll(em);
        }
        String pattern = "%" + keyword.trim() + "%";
        return em.createQuery(
                        "SELECT s FROM Student s WHERE LOWER(s.fullName) LIKE LOWER(:kw) OR s.phone LIKE :kw ORDER BY s.fullName",
                        Student.class)
                .setParameter("kw", pattern)
                .getResultList();
    }
}
