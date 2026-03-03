package vn.edu.ute.productmgmt.service;

import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Product;
import vn.edu.ute.productmgmt.repo.CategoryRepository;
import vn.edu.ute.productmgmt.repo.ProductRepository;

import java.util.List;

public class ProductService {
    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final TransactionManager tx;

    public ProductService(ProductRepository productRepo, CategoryRepository categoryRepo, TransactionManager tx) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.tx = tx;
    }

    public int createProduct(Product p) throws Exception {
        return tx.runInTransaction(em -> {
            int newId = productRepo.insert(em, p);
            categoryRepo.incrementCount(em, p.getCategory().getId(), +1);

            // Demo rollback (teaching):
            // if (true) throw new RuntimeException("Demo rollback");

            return newId;
        });
    }

    public void updateProduct(Product p) throws Exception {
        tx.runInTransaction(em -> {
            Integer oldCid = productRepo.findCategoryIdByProductId(em, p.getId());
            if (oldCid == null) throw new IllegalArgumentException("Product not found: " + p.getId());

            productRepo.update(em, p);

            int newCid = p.getCategory().getId();
            if (oldCid != newCid) {
                categoryRepo.incrementCount(em, oldCid, -1);
                categoryRepo.incrementCount(em, newCid, +1);
            }
            return null;
        });
    }

    public void deleteProduct(int productId) throws Exception {
        tx.runInTransaction(em -> {
            Integer cid = productRepo.findCategoryIdByProductId(em, productId);
            if (cid == null) throw new IllegalArgumentException("Product not found: " + productId);

            productRepo.delete(em, productId);
            categoryRepo.incrementCount(em, cid, -1);
            return null;
        });
    }

    public List<Product> getAll(Integer categoryId) throws Exception {
        return tx.runInTransaction(em -> productRepo.findAll(em, categoryId));
    }
}
