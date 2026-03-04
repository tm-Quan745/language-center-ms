package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.UserAccount;

import java.util.List;

public interface UserAccountRepository {
    UserAccount findByUsername(EntityManager em, String username) throws Exception;
    List<UserAccount> findAll(EntityManager em) throws Exception;
    void insert(EntityManager em, UserAccount userAccount) throws Exception;
    void update(EntityManager em, UserAccount userAccount) throws Exception;
    void delete(EntityManager em, Long id) throws Exception;
}
