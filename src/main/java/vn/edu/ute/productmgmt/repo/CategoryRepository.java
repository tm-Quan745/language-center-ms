package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Category;

import java.util.List;

public interface CategoryRepository {
    List<Category> findAll(EntityManager em) throws Exception;
    void incrementCount(EntityManager em, int categoryId, int delta) throws Exception;
}
