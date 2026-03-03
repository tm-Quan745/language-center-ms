package vn.edu.ute.productmgmt.service;

import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Category;
import vn.edu.ute.productmgmt.repo.CategoryRepository;

import java.util.List;

public class CategoryService {
    private final CategoryRepository categoryRepo;
    private final TransactionManager tx;

    public CategoryService(CategoryRepository categoryRepo, TransactionManager tx) {
        this.categoryRepo = categoryRepo;
        this.tx = tx;
    }

    public List<Category> getAll() throws Exception {
        return tx.runInTransaction(em -> categoryRepo.findAll(em));
    }
}
