package vn.edu.ute.productmgmt.service;

import vn.edu.ute.productmgmt.db.Jpa;
import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Schedule;
import vn.edu.ute.productmgmt.repo.ScheduleRepository;

import jakarta.persistence.EntityManager;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class ScheduleService {

    private final ScheduleRepository scheduleRepo;
    private final TransactionManager tx;

    public ScheduleService(ScheduleRepository scheduleRepo,
                           TransactionManager tx) {
        this.scheduleRepo = scheduleRepo;
        this.tx = tx;
    }

    // =================================
    // CREATE SCHEDULE
    // =================================
    public void createSchedule(Schedule s) throws Exception {

        validate(s);

        tx.runInTransaction(em -> {
            scheduleRepo.insert(em, s);
            return null;
        });
    }

    // =================================
    // FIND BY CLASS
    // =================================
    public List<Schedule> findByClass(UUID classId) {

        EntityManager em = Jpa.em();
        try {
            return scheduleRepo.findByClass(em, classId);
        } finally {
            em.close();
        }
    }

    // =================================
    // BUSINESS VALIDATION
    // =================================
    private void validate(Schedule s) {

        if (s.getTeachingClass() == null)
            throw new IllegalArgumentException("Phải chọn lớp");

        if (s.getDayOfWeek() == null)
            throw new IllegalArgumentException("Phải chọn thứ");

        if (s.getStartTime().isAfter(s.getEndTime()))
            throw new IllegalArgumentException("StartTime phải trước EndTime");

        if (s.getStartTime().equals(s.getEndTime()))
            throw new IllegalArgumentException("Giờ bắt đầu và kết thúc không được trùng");

        if (s.getStartTime().isBefore(LocalTime.of(6,0)) ||
                s.getEndTime().isAfter(LocalTime.of(22,0)))
            throw new IllegalArgumentException("Giờ học phải trong khoảng 06:00 - 22:00");
    }
}