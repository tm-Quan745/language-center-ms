package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Notification;
import vn.edu.ute.productmgmt.model.enums.NotificationTargetRole;

import java.util.List;

public interface NotificationRepository {

    void insert(EntityManager em, Notification notification);

    void update(EntityManager em, Notification notification);

    void delete(EntityManager em, Long id);

    Notification findById(EntityManager em, Long id);

    List<Notification> findAll(EntityManager em);

    /** Lấy thông báo theo các target_role (vd: All, Student). */
    List<Notification> findByTargetRoleIn(EntityManager em, List<NotificationTargetRole> roles);
}
