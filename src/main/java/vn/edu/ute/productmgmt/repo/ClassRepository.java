package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.TeachingClass;

import java.util.List;


public interface ClassRepository {

    void insert(EntityManager em, TeachingClass teachingClass);

    TeachingClass findById(EntityManager em, Long id);

    List<TeachingClass> findAll(EntityManager em);

    /**
     * Tìm các lớp do một giáo viên phụ trách.
     */
    List<TeachingClass> findByTeacherId(EntityManager em, Long teacherId);

    List<TeachingClass> findByStatus(EntityManager em, String status);

    List<TeachingClass> findByStudentId(EntityManager em, Long StudentId);

    void update(EntityManager em, TeachingClass teachingClass);

    void delete(EntityManager em, Long id);
}