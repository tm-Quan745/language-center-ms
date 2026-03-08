package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Promotion;

import java.util.List;

public interface PromotionRepository {

    void insert(EntityManager em, Promotion promotion);

    void update(EntityManager em, Promotion promotion);

    void delete(EntityManager em, Long id);

    Promotion findById(EntityManager em, Long id);

    List<Promotion> findAll(EntityManager em);
}
