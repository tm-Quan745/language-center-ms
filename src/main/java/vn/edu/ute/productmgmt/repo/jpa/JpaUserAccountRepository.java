package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.UserAccount;
import vn.edu.ute.productmgmt.repo.UserAccountRepository;

import java.util.List;

public class JpaUserAccountRepository implements UserAccountRepository{
    @Override
    public UserAccount findByUsername(EntityManager em, String username) throws Exception {
        return em.createQuery("select u from UserAccount u where u.username = :username", UserAccount.class)
                .setParameter("username", username)
                .getSingleResult();
    }

    @Override
    public List<UserAccount> findAll(EntityManager em) throws Exception {
        return em.createQuery("select u from UserAccount u", UserAccount.class)
                .getResultList();
    }

    @Override
    public void insert(EntityManager em, UserAccount userAccount) throws Exception {
        em.persist(userAccount);
    }

    @Override
    public void update(EntityManager em, UserAccount userAccount) throws Exception {
        em.merge(userAccount);
    }

    @Override
    public void delete(EntityManager em, Long id) throws Exception {
        UserAccount userAccount = em.find(UserAccount.class, id);
        if (userAccount != null) {
            em.remove(userAccount);
        }
    }
}
