package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Teacher;

import java.util.List;
import java.util.UUID;

public interface TeacherRepository {

    void insert(EntityManager em, Teacher teacher);

    Teacher findById(EntityManager em, UUID id);

    List<Teacher> findAll(EntityManager em);

    List<Teacher> findByStatus(EntityManager em, String status);

    void update(EntityManager em, Teacher teacher);

    void delete(EntityManager em, UUID id);
}