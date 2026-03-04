-- Script tạo tài khoản root admin cho hệ thống LCMS
-- Database: mis_language_center (theo persistence.xml)
-- Bảng: user_accounts (PK BIGINT AUTO_INCREMENT; Admin có teacher_id, student_id, staff_id = NULL)
-- Chạy: mysql -u root -p mis_language_center < src/main/resources/sql/insert_root_admin.sql

USE mis_language_center;

-- Xóa admin cũ nếu đã tồn tại (tránh trùng username)
DELETE FROM user_accounts WHERE username = 'admin';

-- Tạo tài khoản admin (mật khẩu plain-text; AuthService so sánh trực tiếp)
-- Đổi 'admin123' thành mật khẩu bạn muốn; production nên dùng bcrypt/argon2 trong app
INSERT INTO user_accounts (username, password_hash, role, teacher_id, student_id, staff_id, is_active)
VALUES (
    'admin',
    'admin123',
    'Admin',
    NULL,
    NULL,
    NULL,
    1
);

-- Kiểm tra
SELECT user_id, username, role, is_active FROM user_accounts WHERE username = 'admin';
