package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Course;

import java.util.List;

public interface CourseRepository {

    void insert(EntityManager em, Course course);

    void update(EntityManager em, Course course);

    void delete(EntityManager em, Long id);

    Course findById(EntityManager em, Long id);

    List<Course> findAll(EntityManager em);
}