package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Course;

import java.util.List;
import java.util.UUID;

public interface CourseRepository {

    void insert(EntityManager em, Course course);

    void update(EntityManager em, Course course);

    void delete(EntityManager em, UUID id);

    Course findById(EntityManager em, UUID id);

    List<Course> findAll(EntityManager em);
}