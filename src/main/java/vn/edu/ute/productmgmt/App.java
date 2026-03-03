//package vn.edu.ute.productmgmt;
//
//import vn.edu.ute.productmgmt.db.TransactionManager;
//import vn.edu.ute.productmgmt.model.UserAccount;
//import vn.edu.ute.productmgmt.repo.CategoryRepository;
//import vn.edu.ute.productmgmt.repo.ProductRepository;
//import vn.edu.ute.productmgmt.repo.jpa.JpaCategoryRepository;
//import vn.edu.ute.productmgmt.repo.jpa.JpaProductRepository;
//import vn.edu.ute.productmgmt.service.CategoryService;
//import vn.edu.ute.productmgmt.service.ProductService;
//import vn.edu.ute.productmgmt.ui.LcmsMainFrame;
//import vn.edu.ute.productmgmt.ui.UI;
//
//import javax.swing.*;
//
//public class App {
//    public static void main(String[] args) {
//        UI.initLookAndFeel();
//
//        TransactionManager tx = new TransactionManager();
//        ProductRepository productRepo = new JpaProductRepository();
//        CategoryRepository categoryRepo = new JpaCategoryRepository();
//
//        ProductService productService = new ProductService(productRepo, categoryRepo, tx);
//        CategoryService categoryService = new CategoryService(categoryRepo, tx);
//
//
//        SwingUtilities.invokeLater(() -> new LcmsMainFrame(user).setVisible(true));
//    }
//}
