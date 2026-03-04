package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Teacher;

import java.util.List;

public interface TeacherRepository {

    void insert(EntityManager em, Teacher teacher);

    void update(EntityManager em, Teacher teacher);

    void delete(EntityManager em, Long id);

    Teacher findById(EntityManager em, Long id);

    List<Teacher> findAll(EntityManager em);
}
