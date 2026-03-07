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
        System.out.println("Creating schedule: " + s);
        tx.runInTransaction(em -> {
            scheduleRepo.insert(em, s);
            return null;
        });
        System.out.println("Created schedule: " + s);
    }

    public void updateSchedule(Schedule s) throws Exception {

        validate(s);

        tx.runInTransaction(em -> {
            scheduleRepo.update(em, s);
            return null;
        });
    }

    public Schedule findById(UUID id) {
        EntityManager em = Jpa.em();
        try {
            return scheduleRepo.findById(em, id);
        } finally {
            em.close();
        }
    }

    public Schedule findById(String id) {
        UUID uuid = UUID.fromString(id);
        EntityManager em = Jpa.em();
        try {
            return scheduleRepo.findById(em, uuid);
        } finally {
            em.close();
        }
    }

    public void deleteSchedule(UUID id) throws Exception {

        tx.runInTransaction(em -> {
            scheduleRepo.delete(em, id);
            return null;
        });
    }

    public List<Schedule> findAll() {

        EntityManager em = Jpa.em();
        try {
            return scheduleRepo.findAll(em);
        } finally {
            em.close();
        }
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

        LocalTime start = s.getStartTime();
        LocalTime end = s.getEndTime();

        if (start == null || end == null)
            throw new IllegalArgumentException("Phải nhập giờ học");

        if (!start.isBefore(end))
            throw new IllegalArgumentException("Giờ bắt đầu phải trước giờ kết thúc");

        if (start.isBefore(LocalTime.of(6,0)) || end.isAfter(LocalTime.of(22,0)))
            throw new IllegalArgumentException("Giờ học phải trong khoảng 06:00 - 22:00");
    }
}