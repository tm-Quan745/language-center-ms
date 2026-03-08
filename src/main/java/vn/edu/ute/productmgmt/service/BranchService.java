package vn.edu.ute.productmgmt.service;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.db.Jpa;
import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Branch;
import vn.edu.ute.productmgmt.repo.BranchRepository;

import java.util.List;

public class BranchService {

    private final BranchRepository branchRepo;
    private final TransactionManager tx;

    public BranchService(BranchRepository branchRepo, TransactionManager tx) {
        this.branchRepo = branchRepo;
        this.tx = tx;
    }

    public void create(Branch branch) throws Exception {
        validate(branch);
        tx.runInTransaction(em -> {
            branchRepo.insert(em, branch);
            return null;
        });
    }

    public void update(Branch branch) throws Exception {
        validate(branch);
        tx.runInTransaction(em -> {
            branchRepo.update(em, branch);
            return null;
        });
    }

    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            branchRepo.delete(em, id);
            return null;
        });
    }

    public Branch findById(Long id) {
        EntityManager em = Jpa.em();
        try {
            return branchRepo.findById(em, id);
        } finally {
            em.close();
        }
    }

    public List<Branch> findAll() {
        EntityManager em = Jpa.em();
        try {
            return branchRepo.findAll(em);
        } finally {
            em.close();
        }
    }

    private void validate(Branch b) {
        if (b == null) throw new IllegalArgumentException("Branch không được null");
        if (b.getBranchName() == null || b.getBranchName().isBlank())
            throw new IllegalArgumentException("Tên chi nhánh không được để trống");
    }
}
