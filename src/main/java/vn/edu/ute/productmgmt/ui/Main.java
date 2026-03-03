package vn.edu.ute.productmgmt.ui;


import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.UserAccount;
import vn.edu.ute.productmgmt.repo.UserAccountRepository;
import vn.edu.ute.productmgmt.repo.jpa.UserAccountRepositoryImpl;
import vn.edu.ute.productmgmt.service.AuthService;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

//            TransactionManager tx = new TransactionManager();
//            UserAccountRepository userRepo = new UserAccountRepositoryImpl();
//
//            AuthService authService = new AuthService(userRepo, tx);
//
//            LcmsLoginFrame loginFrame = new LcmsLoginFrame(authService);
//
            // Tạo user giả để test UI
            UserAccount mockUser = new UserAccount();
            mockUser.setUsername("admin");
            mockUser.setRole("ADMIN");

            LcmsMainFrame mainFrame = new LcmsMainFrame(mockUser);
            mainFrame.setVisible(true); // 🔥 BẮT BUỘC phải có dòng này
        });
    }
}