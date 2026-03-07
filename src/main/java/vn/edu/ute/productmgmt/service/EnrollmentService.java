package vn.edu.ute.productmgmt.service;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.db.Jpa;
import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Enrollment;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.TeachingClass;
import vn.edu.ute.productmgmt.model.enums.EnrollmentResult;
import vn.edu.ute.productmgmt.model.enums.EnrollmentStatus;
import vn.edu.ute.productmgmt.repo.EnrollmentRepository;

import java.time.LocalDate;
import java.util.List;

public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepo;
    private final TransactionManager tx;

    public EnrollmentService(EnrollmentRepository enrollmentRepo, TransactionManager tx) {
        this.enrollmentRepo = enrollmentRepo;
        this.tx = tx;
    }

    public void create(Enrollment enrollment) throws Exception {
        validate(enrollment);
        tx.runInTransaction(em -> {
            enrollmentRepo.insert(em, enrollment);
            return null;
        });
    }

    public void update(Enrollment enrollment) throws Exception {
        validate(enrollment);
        tx.runInTransaction(em -> {
            enrollmentRepo.update(em, enrollment);
            return null;
        });
    }

    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            enrollmentRepo.delete(em, id);
            return null;
        });
    }

    public Enrollment getById(Long id) throws Exception {
        return tx.runInTransaction(em -> enrollmentRepo.findById(em, id));
    }

    public List<Enrollment> findAll() {
        EntityManager em = Jpa.em();
        try {
            return enrollmentRepo.findAll(em);
        } finally {
            em.close();
        }
    }

    /**
     * Tạo Enrollment từ id học viên + id lớp – tiện cho UI.
     */
    public Enrollment createEnrollment(Long studentId,
                                       Long classId,
                                       LocalDate enrollmentDate,
                                       EnrollmentStatus status,
                                       EnrollmentResult result) throws Exception {
        return tx.runInTransaction(em -> {
            if (studentId == null) {
                throw new IllegalArgumentException("Enrollment phải có Student");
            }
            if (classId == null) {
                throw new IllegalArgumentException("Enrollment phải có Class");
            }
            Student studentRef = em.getReference(Student.class, studentId);
            TeachingClass classRef = em.getReference(TeachingClass.class, classId);

            Enrollment e = new Enrollment();
            e.setStudent(studentRef);
            e.setTeachingClass(classRef);
            e.setEnrollmentDate(enrollmentDate != null ? enrollmentDate : LocalDate.now());
            e.setStatus(status != null ? status : EnrollmentStatus.Enrolled);
            e.setResult(result != null ? result : EnrollmentResult.NA);

            validate(e);
            enrollmentRepo.insert(em, e);
            return e;
        });
    }

    /**
     * Cập nhật Enrollment cơ bản (có thể đổi lớp/kết quả).
     */
    public Enrollment updateEnrollment(Long id,
                                       Long studentId,
                                       Long classId,
                                       LocalDate enrollmentDate,
                                       EnrollmentStatus status,
                                       EnrollmentResult result) throws Exception {
        return tx.runInTransaction(em -> {
            Enrollment existing = enrollmentRepo.findById(em, id);
            if (existing == null) {
                throw new IllegalArgumentException("Không tìm thấy enrollment id=" + id);
            }
            if (studentId != null) {
                Student studentRef = em.getReference(Student.class, studentId);
                existing.setStudent(studentRef);
            }
            if (classId != null) {
                TeachingClass classRef = em.getReference(TeachingClass.class, classId);
                existing.setTeachingClass(classRef);
            }
            if (enrollmentDate != null) existing.setEnrollmentDate(enrollmentDate);
            if (status != null) existing.setStatus(status);
            if (result != null) existing.setResult(result);

            validate(existing);
            enrollmentRepo.update(em, existing);
            return existing;
        });
    }

    private void validate(Enrollment enrollment) {
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment không được null");
        }
        if (enrollment.getStudent() == null) {
            throw new IllegalArgumentException("Enrollment phải có Student");
        }
        if (enrollment.getTeachingClass() == null) {
            throw new IllegalArgumentException("Enrollment phải có Class");
        }
        if (enrollment.getEnrollmentDate() == null) {
            throw new IllegalArgumentException("Enrollment phải có ngày đăng ký");
        }
    }
}

