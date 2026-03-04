package vn.edu.ute.productmgmt.service;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.db.Jpa;
import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.repo.StudentRepository;

import java.util.List;

public class StudentService {

    private final StudentRepository studentRepo;
    private final TransactionManager tx;

    public StudentService(StudentRepository studentRepo, TransactionManager tx) {
        this.studentRepo = studentRepo;
        this.tx = tx;
    }

    public void create(Student student) throws Exception {
        validate(student);
        tx.runInTransaction(em -> {
            studentRepo.insert(em, student);
            return null;
        });
    }

    public void update(Student student) throws Exception {
        validate(student);
        tx.runInTransaction(em -> {
            studentRepo.update(em, student);
            return null;
        });
    }

    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            studentRepo.delete(em, id);
            return null;
        });
    }

    public Student getById(Long id) throws Exception {
        return tx.runInTransaction(em -> studentRepo.findById(em, id));
    }

    public List<Student> findAll() {
        EntityManager em = Jpa.em();
        try {
            return studentRepo.findAll(em);
        } finally {
            em.close();
        }
    }

    /**
     * Tìm học viên theo tên (fullName) hoặc SĐT (phone).
     * keyword rỗng/null trả về toàn bộ danh sách.
     */
    public List<Student> searchByFullNameOrPhone(String keyword) {
        EntityManager em = Jpa.em();
        try {
            return studentRepo.findByFullNameOrPhone(em, keyword);
        } finally {
            em.close();
        }
    }

    private void validate(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student không được null");
        }
        if (student.getFullName() == null || student.getFullName().isBlank()) {
            throw new IllegalArgumentException("Tên học viên không được để trống");
        }
    }
}
