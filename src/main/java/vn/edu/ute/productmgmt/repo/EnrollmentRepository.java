package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Enrollment;
import vn.edu.ute.productmgmt.model.Student;

import java.util.List;
import java.util.UUID;

public interface EnrollmentRepository {

    void insert(EntityManager em, Enrollment enrollment);

    void update(EntityManager em, Enrollment enrollment);

    void delete(EntityManager em, Long id);

    Enrollment findById(EntityManager em, Long id);

    List<Enrollment> findAll(EntityManager em);

    List<Student> findStudentsByClassId(EntityManager em, UUID classId);
}

