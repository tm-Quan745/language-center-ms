package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Student;

import java.util.List;

public interface StudentRepository {

    void insert(EntityManager em, Student student);

    void update(EntityManager em, Student student);

    void delete(EntityManager em, Long id);

    Student findById(EntityManager em, Long id);

    List<Student> findAll(EntityManager em);

    /**
     * Tìm học viên theo tên (fullName chứa keyword) hoặc SĐT (phone chứa keyword).
     * keyword rỗng/null trả về findAll.
     */
    List<Student> findByFullNameOrPhone(EntityManager em, String keyword);
}
