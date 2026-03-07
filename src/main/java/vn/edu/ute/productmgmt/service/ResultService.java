package vn.edu.ute.productmgmt.service;

import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Result;
import vn.edu.ute.productmgmt.repo.ResultRepository;

import java.math.BigDecimal;

public class ResultService {

    private final ResultRepository resultRepo;
    private final TransactionManager txManager;

    public ResultService(ResultRepository resultRepo,
                         TransactionManager txManager) {

        this.resultRepo = resultRepo;
        this.txManager = txManager;
    }

    public void saveResult(Result r) {

        String grade = calculateGrade(r.getScore());
        r.setGrade(grade);

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