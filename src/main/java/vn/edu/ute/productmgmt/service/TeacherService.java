package vn.edu.ute.productmgmt.service;

import vn.edu.ute.productmgmt.db.Jpa;
import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Teacher;
import vn.edu.ute.productmgmt.repo.TeacherRepository;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class TeacherService {

    private final TeacherRepository teacherRepo;
    private final TransactionManager tx;

    public TeacherService(TeacherRepository teacherRepo,
                          TransactionManager tx) {
        this.teacherRepo = teacherRepo;
        this.tx = tx;
    }

    // =============================
    // CREATE
    // =============================
    public void createTeacher(Teacher teacher) throws Exception {

        validate(teacher);

        tx.runInTransaction(em -> {
            teacherRepo.insert(em, teacher);
            return null;
        });
    }

    // =============================
    // UPDATE
    // =============================
    public void updateTeacher(Teacher teacher) throws Exception {

        validate(teacher);

        tx.runInTransaction(em -> {
            teacherRepo.update(em, teacher);
            return null;
        });
    }

    // =============================
    // DELETE
    // =============================
    public void deleteTeacher(UUID id) throws Exception {

        tx.runInTransaction(em -> {
            teacherRepo.delete(em, id);
            return null;
        });
    }

    // =============================
    // FIND ALL
    // =============================
    public List<Teacher> findAll() {

        EntityManager em = Jpa.em();
        try {
            return teacherRepo.findAll(em);
        } finally {
            em.close();
        }
    }

    // =============================
    // FIND ACTIVE
    // =============================
    public List<Teacher> findActive() {

        EntityManager em = Jpa.em();
        try {
            return teacherRepo.findByStatus(em, "ACTIVE");
        } finally {
            em.close();
        }
    }

    // =============================
    // BUSINESS VALIDATION
    // =============================
    private void validate(Teacher t) {

        if (t == null)
            throw new IllegalArgumentException("Teacher không được null");

        if (t.getFullName() == null || t.getFullName().isBlank())
            throw new IllegalArgumentException("Tên giáo viên không được trống");

        if (t.getEmail() != null &&
                !t.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$"))
            throw new IllegalArgumentException("Email không hợp lệ");

        if (t.getHireDate() != null &&
                t.getHireDate().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Hire date không hợp lệ");

        if (t.getStatus() == null || t.getStatus().isBlank())
            t.setStatus("ACTIVE");
    }
}