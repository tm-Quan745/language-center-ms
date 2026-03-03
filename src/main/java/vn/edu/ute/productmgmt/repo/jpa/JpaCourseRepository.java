package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Course;
import vn.edu.ute.productmgmt.repo.CourseRepository;

import java.util.List;
import java.util.UUID;

public class JpaCourseRepository implements CourseRepository {

    @Override
    public void insert(EntityManager em, Course course) {
        em.persist(course);
    }

    @Override
    public void update(EntityManager em, Course course) {
        em.merge(course);
    }

    @Override
    public void delete(EntityManager em, UUID id) {
        Course course = em.find(Course.class, id);
        if (course != null) {
            em.remove(course);
        }
    }

    @Override
    public Course findById(EntityManager em, UUID id) {
        return em.find(Course.class, id);
    }

    @Override
    public List<Course> findAll(EntityManager em) {
        return em.createQuery("SELECT c FROM Course c", Course.class)
                .getResultList();
    }
}