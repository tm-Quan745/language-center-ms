package vn.edu.ute.productmgmt.service;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.db.Jpa;
import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Notification;
import vn.edu.ute.productmgmt.model.enums.NotificationTargetRole;
import vn.edu.ute.productmgmt.model.enums.UserRole;
import vn.edu.ute.productmgmt.repo.NotificationRepository;

import java.util.List;

public class NotificationService {

    private final NotificationRepository notificationRepo;
    private final TransactionManager tx;

    public NotificationService(NotificationRepository notificationRepo, TransactionManager tx) {
        this.notificationRepo = notificationRepo;
        this.tx = tx;
    }

    public void create(Notification notification) throws Exception {
        validate(notification);
        tx.runInTransaction(em -> {
            notificationRepo.insert(em, notification);
            return null;
        });
    }

    public void update(Notification notification) throws Exception {
        validate(notification);
        tx.runInTransaction(em -> {
            notificationRepo.update(em, notification);
            return null;
        });
    }

    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            notificationRepo.delete(em, id);
            return null;
        });
    }

    public Notification findById(Long id) {
        EntityManager em = Jpa.em();
        try {
            return notificationRepo.findById(em, id);
        } finally {
            em.close();
        }
    }

    public List<Notification> findAll() {
        EntityManager em = Jpa.em();
        try {
            return notificationRepo.findAll(em);
        } finally {
            em.close();
        }
    }

    /**
     * Lấy thông báo dành cho role đăng nhập: All + role tương ứng (Admin chỉ xem All).
     */
    public List<Notification> findForUserRole(UserRole userRole) {
        EntityManager em = Jpa.em();
        try {
            List<NotificationTargetRole> roles = switch (userRole) {
                case Admin -> List.of(NotificationTargetRole.All);
                case Student -> List.of(NotificationTargetRole.All, NotificationTargetRole.Student);
                case Teacher -> List.of(NotificationTargetRole.All, NotificationTargetRole.Teacher);
                case Staff -> List.of(NotificationTargetRole.All, NotificationTargetRole.Staff);
            };
            return notificationRepo.findByTargetRoleIn(em, roles);
        } finally {
            em.close();
        }
    }

    private void validate(Notification n) {
        if (n == null) throw new IllegalArgumentException("Thông báo không được null");
        if (n.getTitle() == null || n.getTitle().isBlank())
            throw new IllegalArgumentException("Tiêu đề không được để trống");
        if (n.getContent() == null || n.getContent().isBlank())
            throw new IllegalArgumentException("Nội dung không được để trống");
    }
}
