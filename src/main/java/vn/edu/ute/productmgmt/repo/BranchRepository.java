package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Branch;

import java.util.List;

public interface BranchRepository {

    void insert(EntityManager em, Branch branch);

    void update(EntityManager em, Branch branch);

    void delete(EntityManager em, Long id);

    Branch findById(EntityManager em, Long id);

    List<Branch> findAll(EntityManager em);
}
