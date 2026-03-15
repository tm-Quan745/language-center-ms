package vn.edu.ute.productmgmt.service;

import vn.edu.ute.productmgmt.db.Jpa;
import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.ClassStatus;
import vn.edu.ute.productmgmt.model.TeachingClass;
import vn.edu.ute.productmgmt.repo.ClassRepository;

import jakarta.persistence.EntityManager;
import java.util.List;

public class ClassService {

    private final ClassRepository classRepo;
    private final TransactionManager tx;

    public ClassService(ClassRepository classRepo, TransactionManager tx) {
        this.classRepo = classRepo;
        this.tx = tx;
    }

    // =============================
    // CREATE CLASS
    // =============================
    public void createClass(TeachingClass teachingClass) throws Exception {

        validate(teachingClass);

        tx.runInTransaction(em -> {
            classRepo.insert(em, teachingClass);
            return null;
        });
    }

    // =============================
    // UPDATE
    // =============================
    public void updateClass(TeachingClass teachingClass) throws Exception {

        validate(teachingClass);

        tx.runInTransaction(em -> {
            classRepo.update(em, teachingClass);
            return null;
        });
    }

    // =============================
    // DELETE
    // =============================
    public void deleteClass(Long id) throws Exception {
        tx.runInTransaction(em -> {
            classRepo.delete(em, id);
            return null;
        });
    }

    public TeachingClass findById(Long id) {

        EntityManager em = Jpa.em();
        try {
            return classRepo.findById(em, id);
        } finally {
            em.close();
        }
    }

    // =============================
    // FIND ALL
    // =============================
    public List<TeachingClass> findAll() {

        EntityManager em = Jpa.em();
        try {
            return classRepo.findAll(em);
        } finally {
            em.close();
        }
    }

    /**
     * Lấy danh sách lớp do một giáo viên phụ trách.
     */
    public List<TeachingClass> findByTeacher(Long teacherId) {
        if (teacherId == null) {
            throw new IllegalArgumentException("teacherId không được null");
        }
        EntityManager em = Jpa.em();
        try {
            return classRepo.findByTeacherId(em, teacherId);
        } finally {
            em.close();
        }
    }

    // =============================
    // FIND ACTIVE
    // =============================
    public List<TeachingClass> findActiveClasses() {

        EntityManager em = Jpa.em();
        try {
            return classRepo.findByStatus(em, ClassStatus.ACTIVE.name());
        } finally {
            em.close();
        }
    }

    public List<TeachingClass> findByStudent(Long studentId) {
        EntityManager em = Jpa.em();
        try {
            return classRepo.findByStudentId(em, studentId);
        } finally {
            em.close();
        }
    }

    // =============================
    // VALIDATION BUSINESS RULE
    // =============================
    private void validate(TeachingClass tc) {

        if (tc == null)
            throw new IllegalArgumentException("TeachingClass không được null");

        if (tc.getClassName() == null || tc.getClassName().isBlank())
            throw new IllegalArgumentException("Tên lớp không được trống");

        if (tc.getStartDate().isAfter(tc.getEndDate()))
            throw new IllegalArgumentException("StartDate phải trước EndDate");

        if (tc.getMaxStudent() <= 0)
            throw new IllegalArgumentException("MaxStudent phải > 0");

        if (tc.getRoom() != null &&
                tc.getMaxStudent() > tc.getRoom().getCapacity())
            throw new IllegalArgumentException(
                    "MaxStudent không được vượt quá sức chứa phòng");
    }
}