package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Schedule;
import vn.edu.ute.productmgmt.repo.ScheduleRepository;

import java.util.List;
import java.util.UUID;

public class JpaScheduleRepository implements ScheduleRepository {

    @Override
    public void insert(EntityManager em, Schedule schedule) {
        em.persist(schedule);
    }

    @Override
    public List<Schedule> findByClass(EntityManager em, UUID classId) {
        return em.createQuery(
                        "SELECT s FROM Schedule s " +
                                "WHERE s.teachingClass.id = :classId " +
                                "ORDER BY s.dayOfWeek, s.startTime",
                        Schedule.class)
                .setParameter("classId", classId)
                .getResultList();
    }

    @Override
    public void delete(EntityManager em, UUID id) {
        Schedule s = em.find(Schedule.class, id);
        if (s != null) {
            em.remove(s);
        }
    }
}