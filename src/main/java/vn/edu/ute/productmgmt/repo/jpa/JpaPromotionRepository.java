package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Promotion;
import vn.edu.ute.productmgmt.repo.PromotionRepository;

import java.util.List;

public class JpaPromotionRepository implements PromotionRepository {

    @Override
    public void insert(EntityManager em, Promotion promotion) {
        em.persist(promotion);
    }

    @Override
    public void update(EntityManager em, Promotion promotion) {
        em.merge(promotion);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        Promotion promotion = em.find(Promotion.class, id);
        if (promotion != null) {
            em.remove(promotion);
        }
    }

    @Override
    public Promotion findById(EntityManager em, Long id) {
        return em.find(Promotion.class, id);
    }

    @Override
    public List<Promotion> findAll(EntityManager em) {
        return em.createQuery("SELECT p FROM Promotion p", Promotion.class)
                .getResultList();
    }
}
