package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Category;
import vn.edu.ute.productmgmt.repo.CategoryRepository;

import java.util.List;

public class JpaCategoryRepository implements CategoryRepository {

    @Override
    public List<Category> findAll(EntityManager em) {
        return em.createQuery("select c from Category c order by c.name", Category.class)
                .getResultList();
    }

    @Override
    public void incrementCount(EntityManager em, int categoryId, int delta) {
        Category c = em.find(Category.class, categoryId);
        if (c == null) throw new IllegalArgumentException("Category not found: " + categoryId);
        c.setProductCount(c.getProductCount() + delta);
    }
}
