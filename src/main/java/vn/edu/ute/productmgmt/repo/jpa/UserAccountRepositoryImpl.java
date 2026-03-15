package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.productmgmt.model.UserAccount;
import vn.edu.ute.productmgmt.repo.UserAccountRepository;

import java.util.List;

public class UserAccountRepositoryImpl implements UserAccountRepository {

    @Override
    public UserAccount findByUsername(EntityManager em, String username) {
        try {
            TypedQuery<UserAccount> query = em.createQuery(
                    "SELECT u FROM UserAccount u WHERE u.username = :username",
                    UserAccount.class
            );
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<UserAccount> findAll(EntityManager em) {
        return em.createQuery(
                "SELECT u FROM UserAccount u " +
                        "LEFT JOIN FETCH u.student " +
                        "LEFT JOIN FETCH u.teacher " +
                        "LEFT JOIN FETCH u.staff",
                UserAccount.class
        ).getResultList();
    }

    @Override
    public void insert(EntityManager em, UserAccount userAccount) {
        em.persist(userAccount);
    }

    @Override
    public void update(EntityManager em, UserAccount userAccount) {
        em.merge(userAccount);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        UserAccount user = em.find(UserAccount.class, id);
        if (user != null) {
            // Xóa mềm: đặt is_active = false, không remove bản ghi
            user.setActive(false);
        }
    }
}