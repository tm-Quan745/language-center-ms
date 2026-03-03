package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Product;

import java.util.List;

public interface ProductRepository {
    List<Product> findAll(EntityManager em, Integer categoryId) throws Exception;
    int insert(EntityManager em, Product p) throws Exception;
    void update(EntityManager em, Product p) throws Exception;
    void delete(EntityManager em, int productId) throws Exception;
    Integer findCategoryIdByProductId(EntityManager em, int productId) throws Exception;
}
