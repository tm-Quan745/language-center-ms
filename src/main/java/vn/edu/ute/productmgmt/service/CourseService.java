package vn.edu.ute.productmgmt.service;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.db.Jpa;
import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Course;
import vn.edu.ute.productmgmt.repo.CourseRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class CourseService {

    private final CourseRepository courseRepo;
    private final TransactionManager tx;

    public CourseService(CourseRepository courseRepo, TransactionManager tx) {
        this.courseRepo = courseRepo;
        this.tx = tx;
    }

    // ========================
    // CREATE
    // ========================
    public void create(Course course) throws Exception {

        validate(course);

        tx.runInTransaction(em -> {
            courseRepo.insert(em, course);
            return null;
        });
    }

    // ========================
    // UPDATE
    // ========================
    public void update(Course course) throws Exception {

        validate(course);

        tx.runInTransaction(em -> {
            courseRepo.update(em, course);
            return null;
        });
    }

    // ========================
    // DELETE
    // ========================
    public void delete(UUID id) throws Exception {
        tx.runInTransaction(em -> {
            courseRepo.delete(em, id);
            return null;
        });
    }

    // ========================
    // FIND ALL
    // ========================
    public List<Course> findAll() {

        EntityManager em = Jpa.em();
        try {
            return courseRepo.findAll(em);
        } finally {
            em.close();
        }
    }

    // ========================
    // VALIDATION
    // ========================
    private void validate(Course course) {

        if (course == null) {
            throw new IllegalArgumentException("Course không được null");
        }

        if (course.getCourseName() == null || course.getCourseName().isBlank()) {
            throw new IllegalArgumentException("Tên khóa học không được để trống");
        }

        if (course.getFee() != null &&
                course.getFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Học phí phải >= 0");
        }
    }
}