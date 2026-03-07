package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Attendance;
import vn.edu.ute.productmgmt.model.TeachingClass;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository {

    void insert(EntityManager em, Attendance attendance);

    void update(EntityManager em, Attendance attendance);

    List<Attendance> findByClassAndDate(EntityManager em,
                                        TeachingClass teachingClass,
                                        LocalDate date);

    List<Attendance> findByClass(EntityManager em,
                                 TeachingClass teachingClass);

    Attendance save(Attendance attendance);

    List<Attendance> findByClassAndDate(TeachingClass teachingClass, LocalDate date);

    List<Attendance> findByClass(TeachingClass teachingClass);
}
