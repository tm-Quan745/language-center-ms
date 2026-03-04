package vn.edu.ute.productmgmt.service;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.db.Jpa;
import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Teacher;
import vn.edu.ute.productmgmt.repo.TeacherRepository;

import java.util.List;

public class TeacherService {

    private final TeacherRepository teacherRepo;
    private final TransactionManager tx;

    public TeacherService(TeacherRepository teacherRepo, TransactionManager tx) {
        this.teacherRepo = teacherRepo;
        this.tx = tx;
    }

    public void create(Teacher teacher) throws Exception {
        validate(teacher);
        tx.runInTransaction(em -> {
            teacherRepo.insert(em, teacher);
            return null;
        });
    }

    public void update(Teacher teacher) throws Exception {
        validate(teacher);
        tx.runInTransaction(em -> {
            teacherRepo.update(em, teacher);
            return null;
        });
    }

    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            teacherRepo.delete(em, id);
            return null;
        });
    }

    public Teacher getById(Long id) throws Exception {
        return tx.runInTransaction(em -> teacherRepo.findById(em, id));
    }

    public List<Teacher> findAll() {
        EntityManager em = Jpa.em();
        try {
            return teacherRepo.findAll(em);
        } finally {
            em.close();
        }
    }

    private void validate(Teacher teacher) {
        if (teacher == null) {
            throw new IllegalArgumentException("Teacher không được null");
        }
        if (teacher.getFullName() == null || teacher.getFullName().isBlank()) {
            throw new IllegalArgumentException("Tên giáo viên không được để trống");
        }
    }
}
