package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Schedule;

import java.util.List;
import java.util.UUID;

public interface ScheduleRepository {

    void insert(EntityManager em, Schedule schedule);

    List<Schedule> findByClass(EntityManager em, UUID classId);

    List<Schedule> findAll(EntityManager em);

    Schedule findById(EntityManager em, UUID id);

    void update(EntityManager em, Schedule schedule);

    void delete(EntityManager em, UUID id);
}