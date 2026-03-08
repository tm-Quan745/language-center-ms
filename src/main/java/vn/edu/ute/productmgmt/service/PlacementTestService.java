package vn.edu.ute.productmgmt.service;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.db.Jpa;
import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.PlacementTest;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.enums.SuggestedLevel;
import vn.edu.ute.productmgmt.repo.PlacementTestRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PlacementTestService {

    private final PlacementTestRepository placementTestRepo;
    private final TransactionManager tx;

    public PlacementTestService(PlacementTestRepository placementTestRepo, TransactionManager tx) {
        this.placementTestRepo = placementTestRepo;
        this.tx = tx;
    }

    public void create(PlacementTest placementTest) throws Exception {
        validate(placementTest);
        tx.runInTransaction(em -> {
            placementTestRepo.insert(em, placementTest);
            return null;
        });
    }

    public void update(PlacementTest placementTest) throws Exception {
        validate(placementTest);
        tx.runInTransaction(em -> {
            placementTestRepo.update(em, placementTest);
            return null;
        });
    }

    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            placementTestRepo.delete(em, id);
            return null;
        });
    }

    public PlacementTest findById(Long id) {
        EntityManager em = Jpa.em();
        try {
            return placementTestRepo.findById(em, id);
        } finally {
            em.close();
        }
    }

    public List<PlacementTest> findAll() {
        EntityManager em = Jpa.em();
        try {
            return placementTestRepo.findAll(em);
        } finally {
            em.close();
        }
    }

    public List<PlacementTest> findByStudentId(Long studentId) {
        EntityManager em = Jpa.em();
        try {
            return placementTestRepo.findByStudentId(em, studentId);
        } finally {
            em.close();
        }
    }

    /**
     * Tạo bài kiểm tra xếp lớp từ studentId và các field còn lại.
     */
    public void createPlacementTest(Long studentId,
                                    LocalDate testDate,
                                    BigDecimal score,
                                    SuggestedLevel suggestedLevel,
                                    String note) throws Exception {
        if (studentId == null) {
            throw new IllegalArgumentException("Placement test phải gắn với học viên");
        }
        tx.runInTransaction(em -> {
            Student studentRef = em.getReference(Student.class, studentId);
            PlacementTest pt = new PlacementTest();
            pt.setStudent(studentRef);
            pt.setTestDate(testDate != null ? testDate : LocalDate.now());
            pt.setScore(score);
            pt.setSuggestedLevel(suggestedLevel);
            pt.setNote(note);
            validate(pt);
            placementTestRepo.insert(em, pt);
            return null;
        });
    }

    /**
     * Cập nhật bài kiểm tra xếp lớp theo id.
     */
    public void updatePlacementTest(Long id,
                                    Long studentId,
                                    LocalDate testDate,
                                    BigDecimal score,
                                    SuggestedLevel suggestedLevel,
                                    String note) throws Exception {
        tx.runInTransaction(em -> {
            PlacementTest existing = placementTestRepo.findById(em, id);
            if (existing == null) {
                throw new IllegalArgumentException("Không tìm thấy placement test id=" + id);
            }
            if (studentId != null) {
                existing.setStudent(em.getReference(Student.class, studentId));
            }
            if (testDate != null) existing.setTestDate(testDate);
            if (score != null) existing.setScore(score);
            existing.setSuggestedLevel(suggestedLevel);
            existing.setNote(note);
            validate(existing);
            placementTestRepo.update(em, existing);
            return null;
        });
    }

    private void validate(PlacementTest pt) {
        if (pt == null) {
            throw new IllegalArgumentException("PlacementTest không được null");
        }
        if (pt.getStudent() == null) {
            throw new IllegalArgumentException("Placement test phải gắn với học viên");
        }
        if (pt.getTestDate() == null) {
            throw new IllegalArgumentException("Placement test phải có ngày kiểm tra");
        }
    }
}
