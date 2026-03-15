package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Schedule;

import java.util.List;

public interface ScheduleRepository {

    void insert(EntityManager em, Schedule schedule);

    List<Schedule> findByClass(EntityManager em, Long classId);

    List<Schedule> findAll(EntityManager em);

    /**
     * Tìm lịch học theo giáo viên (qua lớp học).
     */
    List<Schedule> findByTeacherId(EntityManager em, Long teacherId);

    List<Schedule> findByStudentId(EntityManager em, Long branchId);

    Schedule findById(EntityManager em, Long id);

    void update(EntityManager em, Schedule schedule);

    void delete(EntityManager em, Long id);
}