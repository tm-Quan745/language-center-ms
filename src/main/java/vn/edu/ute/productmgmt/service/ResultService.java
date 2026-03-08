package vn.edu.ute.productmgmt.service;

import vn.edu.ute.productmgmt.db.Jpa;
import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Result;
import vn.edu.ute.productmgmt.model.TeachingClass;
import vn.edu.ute.productmgmt.repo.ResultRepository;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

public class ResultService {

    private final ResultRepository resultRepo;
    private final TransactionManager txManager;

    public ResultService(ResultRepository resultRepo,
                         TransactionManager txManager) {

        this.resultRepo = resultRepo;
        this.txManager = txManager;
    }

    public List<Result> findResultsByClass(TeachingClass teachingClass) {
        EntityManager em = Jpa.em();
        try {
            return resultRepo.findByClass(em, teachingClass);
        } finally {
            em.close();
        }
    }

    public void saveResult(Result r) {
        // Chỉ tự tính xếp loại nếu user chưa nhập
        if (r.getGrade() == null || r.getGrade().isBlank()) {
            r.setGrade(calculateGrade(r.getScore()));
        }

        try {
            txManager.runInTransaction(em -> {

                if (r.getId() == null) {
                    resultRepo.insert(em, r);
                } else {
                    resultRepo.update(em, r);
                }

                return null;
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String calculateGrade(BigDecimal score) {

        double s = score.doubleValue();

        if (s >= 8.5) return "A";
        if (s >= 7) return "B";
        if (s >= 5.5) return "C";
        if (s >= 4) return "D";
        return "F";
    }
}