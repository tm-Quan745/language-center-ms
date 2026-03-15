package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Schedule;
import vn.edu.ute.productmgmt.repo.ScheduleRepository;

import java.util.List;

public class JpaScheduleRepository implements ScheduleRepository {

    @Override
    public void insert(EntityManager em, Schedule schedule) {
        em.persist(schedule);
    }

    @Override
    public List<Schedule> findByClass(EntityManager em, Long classId) {
        return em.createQuery(
                        "SELECT s FROM Schedule s " +
                                "WHERE s.teachingClass.id = :classId " +
                                "ORDER BY s.studyDate, s.startTime",
                        Schedule.class)
                .setParameter("classId", classId)
                .getResultList();
    }

    @Override
    public List<Schedule> findAll(EntityManager em) {
        return em.createQuery(
                        "SELECT s FROM Schedule s " +
                                "JOIN FETCH s.teachingClass " +
                                "LEFT JOIN FETCH s.room " +
                                "ORDER BY s.studyDate, s.startTime",
                        Schedule.class)
                .getResultList();
    }

    @Override
    public List<Schedule> findByTeacherId(EntityManager em, Long teacherId) {
        return em.createQuery(
                        "SELECT s FROM Schedule s " +
                                "JOIN FETCH s.teachingClass tc " +
                                "LEFT JOIN FETCH s.room " +
                                "WHERE tc.teacher.id = :teacherId " +
                                "ORDER BY s.studyDate, s.startTime",
                        Schedule.class)
                .setParameter("teacherId", teacherId)
                .getResultList();
    }

    @Override
    public List<Schedule> findByStudentId(EntityManager em, Long studentId) {
        return em.createQuery(
                        "SELECT DISTINCT s FROM Schedule s " +
                                "JOIN FETCH s.teachingClass tc " + // Lấy thông tin lớp học
                                "LEFT JOIN FETCH s.room " +        // Lấy thông tin phòng học
                                "JOIN Enrollment e ON e.teachingClass.id = tc.id " + // Kết nối với bảng đăng ký
                                "WHERE e.student.id = :studentId " +
                                "ORDER BY s.studyDate, s.startTime",
                        Schedule.class)
                .setParameter("studentId", studentId)
                .getResultList();
    }

    @Override
    public Schedule findById(EntityManager em, Long id) {
        return em.find(Schedule.class, id);
    }

    @Override
    public void update(EntityManager em, Schedule schedule) {
        em.merge(schedule);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        Schedule s = em.find(Schedule.class, id);
        if (s != null) {
            em.remove(s);
        }
    }
}