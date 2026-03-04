package vn.edu.ute.productmgmt.service;

import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.UserAccount;
import vn.edu.ute.productmgmt.model.enums.UserRole;
import vn.edu.ute.productmgmt.repo.UserAccountRepository;

public class AuthService {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final String[] ALLOWED_REGISTER_ROLES = { "STUDENT", "TEACHER", "STAFF" };

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

    /**
     * Đăng ký tài khoản mới.
     *
     * @param username tên đăng nhập (không trùng, không rỗng)
     * @param password mật khẩu (plain text; độ dài tối thiểu 6)
     * @param role     vai trò: STUDENT, TEACHER, STAFF (không cho phép đăng ký ADMIN)
     * @return UserAccount vừa tạo
     */
    public UserAccount register(String username, String password, String role) throws Exception {
        String normalizedUsername = username == null ? "" : username.trim();
        validateRegister(normalizedUsername, password, role);

        return tx.runInTransaction(em -> {
            UserAccount existing = userAccountRepo.findByUsername(em, normalizedUsername);
            if (existing != null) {
                throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng: " + normalizedUsername);
            }
            UserAccount account = new UserAccount();
            account.setUsername(normalizedUsername);
            account.setPasswordHash(password);
            account.setRole(parseRoleFromInput(role));
            account.setTeacher(null);
            account.setStudent(null);
            account.setStaff(null);
            account.setActive(true);
            userAccountRepo.insert(em, account);
            return account;
        });
    }

    /**
     * Kiểm tra username đã tồn tại chưa (dùng cho form đăng ký).
     */
    public boolean isUsernameTaken(String username) throws Exception {
        if (username == null || username.isBlank()) {
            return false;
        }
        return tx.runInTransaction(em ->
                userAccountRepo.findByUsername(em, username.trim()) != null
        );
    }

    private static UserRole parseRoleFromInput(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Vai trò không được để trống.");
        }
        String r = role.trim().toUpperCase();
        switch (r) {
            case "STUDENT": return UserRole.Student;
            case "TEACHER": return UserRole.Teacher;
            case "STAFF": return UserRole.Staff;
            case "ADMIN": return UserRole.Admin;
            default: return UserRole.valueOf(role.trim());
        }
    }

    private void validateRegister(String username, String password, String role) {
        if (username.isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        }
        if (username.length() > 50) {
            throw new IllegalArgumentException("Tên đăng nhập tối đa 50 ký tự.");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự.");
        }
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Vai trò không được để trống.");
        }
        String r = role.toUpperCase();
        for (String a : ALLOWED_REGISTER_ROLES) {
            if (a.equals(r)) return;
        }
        throw new IllegalArgumentException("Vai trò không hợp lệ. Chọn: " + String.join(", ", ALLOWED_REGISTER_ROLES));
    }
}

