package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Branch;
import vn.edu.ute.productmgmt.repo.BranchRepository;

import java.util.List;

public class JpaBranchRepository implements BranchRepository {

    @Override
    public void insert(EntityManager em, Branch branch) {
        em.persist(branch);
    }

    @Override
    public void update(EntityManager em, Branch branch) {
        em.merge(branch);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        Branch b = em.find(Branch.class, id);
        if (b != null) em.remove(b);
    }

    @Override
    public Branch findById(EntityManager em, Long id) {
        return em.find(Branch.class, id);
    }

    @Override
    public List<Branch> findAll(EntityManager em) {
        return em.createQuery("SELECT b FROM Branch b ORDER BY b.branchName", Branch.class)
                .getResultList();
    }
}
