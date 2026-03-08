package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Notification;
import vn.edu.ute.productmgmt.model.enums.NotificationTargetRole;
import vn.edu.ute.productmgmt.repo.NotificationRepository;

import java.util.List;

public class JpaNotificationRepository implements NotificationRepository {

    @Override
    public void insert(EntityManager em, Notification notification) {
        em.persist(notification);
    }

    @Override
    public void update(EntityManager em, Notification notification) {
        em.merge(notification);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        Notification n = em.find(Notification.class, id);
        if (n != null) em.remove(n);
    }

    @Override
    public Notification findById(EntityManager em, Long id) {
        return em.find(Notification.class, id);
    }

    @Override
    public List<Notification> findAll(EntityManager em) {
        return em.createQuery("SELECT n FROM Notification n ORDER BY n.createdAt DESC", Notification.class)
                .getResultList();
    }

    @Override
    public List<Notification> findByTargetRoleIn(EntityManager em, List<NotificationTargetRole> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        return em.createQuery(
                        "SELECT n FROM Notification n WHERE n.targetRole IN :roles ORDER BY n.createdAt DESC",
                        Notification.class)
                .setParameter("roles", roles)
                .getResultList();
    }
}
