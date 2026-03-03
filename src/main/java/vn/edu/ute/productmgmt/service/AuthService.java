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

        // Có thể trim để tránh lỗi do khoảng trắng thừa
        final String normalizedUsername = username.trim();
        final String rawPassword = password;

        return tx.runInTransaction(em -> {
            UserAccount account;
            try {
                account = userAccountRepo.findByUsername(em, normalizedUsername);
            } catch (Exception ex) {
                // Không tìm thấy user hoặc lỗi query
                return null;
            }

            if (account == null) {
                return null;
            }

            // Ở đây tạm thời so sánh plain-text cho đơn giản.
            // Nếu sau này lưu password dạng hash, thay bằng logic verify hash.
            if (!rawPassword.equals(account.getPasswordHash())) {
                return null;
            }

            return account;
        });
    }
}

