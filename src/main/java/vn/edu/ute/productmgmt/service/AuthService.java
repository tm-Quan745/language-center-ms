package vn.edu.ute.productmgmt.service;

import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.UserAccount;
import vn.edu.ute.productmgmt.repo.UserAccountRepository;

public class AuthService {

    private final UserAccountRepository userAccountRepo;
    private final TransactionManager tx;

    public AuthService(UserAccountRepository userAccountRepo, TransactionManager tx) {
        this.userAccountRepo = userAccountRepo;
        this.tx = tx;
    }

    /**
     * Đăng nhập với username/password.
     * Trả về UserAccount (chứa luôn role) nếu thành công, hoặc null nếu sai thông tin.
     */

    public UserAccount login(String username, String password) throws Exception {

        if (username == null || password == null) {
            return null;
        }

        final String normalizedUsername = username.trim();
        final String rawPassword = password;

        return tx.runInTransaction(em -> {

            UserAccount account = userAccountRepo.findByUsername(em, normalizedUsername);

            if (account == null) {
                return null;
            }

            if (!rawPassword.equals(account.getPasswordHash())) {
                return null;
            }

            if (!account.isActive()) {
                throw new RuntimeException("Tài khoản đã bị khóa");
            }

            return account;
        });
    }
}

