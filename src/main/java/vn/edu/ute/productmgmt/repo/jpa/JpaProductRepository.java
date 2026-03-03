package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.productmgmt.model.Product;
import vn.edu.ute.productmgmt.repo.ProductRepository;

import java.util.List;

public class JpaProductRepository implements ProductRepository {

    @Override
    public List<Product> findAll(EntityManager em, Integer categoryId) {
        String jpql =
                "select p from Product p join fetch p.category c " +
                (categoryId == null ? "" : "where c.id = :cid ") +
                "order by p.id desc";

        TypedQuery<Product> q = em.createQuery(jpql, Product.class);
        if (categoryId != null) q.setParameter("cid", categoryId);
        return q.getResultList();
    }

    @Override
    public int insert(EntityManager em, Product p) {
        em.persist(p);
        em.flush();
        return p.getId();
    }

    @Override
    public void update(EntityManager em, Product p) {
        em.merge(p);
    }

    @Override
    public void delete(EntityManager em, int productId) {
        Product found = em.find(Product.class, productId);
        if (found == null) throw new IllegalArgumentException("Product not found: " + productId);
        em.remove(found);
    }

    @Override
    public Integer findCategoryIdByProductId(EntityManager em, int productId) {
        return em.createQuery(
                        "select p.category.id from Product p where p.id = :pid",
                        Integer.class
                )
                .setParameter("pid", productId)
                .getSingleResult();
    }
}
