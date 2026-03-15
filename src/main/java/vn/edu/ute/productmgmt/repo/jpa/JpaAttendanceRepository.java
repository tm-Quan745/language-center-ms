package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.productmgmt.model.Attendance;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.TeachingClass;
import vn.edu.ute.productmgmt.repo.AttendanceRepository;

import java.time.LocalDate;
import java.util.List;

public class JpaAttendanceRepository implements AttendanceRepository {

    @Override
    public void insert(EntityManager em, Attendance attendance) {
        em.persist(attendance);
    }

    @Override
    public void update(EntityManager em, Attendance attendance) {
        em.merge(attendance);
    }

    @Override
    public List<Attendance> findByClassAndDate(EntityManager em,
                                               TeachingClass teachingClass,
                                               LocalDate date) {

        TypedQuery<Attendance> query = em.createQuery(
                "SELECT a FROM Attendance a " +
                        "WHERE a.teachingClass = :cls AND a.attendDate = :date",
                Attendance.class
        );

        query.setParameter("cls", teachingClass);
        query.setParameter("date", date);

        return query.getResultList();
    }

    @Override
    public List<Attendance> findByClass(EntityManager em,
                                        TeachingClass teachingClass) {

        TypedQuery<Attendance> query = em.createQuery(
                "SELECT a FROM Attendance a WHERE a.teachingClass = :cls",
                Attendance.class
        );

        query.setParameter("cls", teachingClass);

        return query.getResultList();
    }

    @Override
    public Attendance save(Attendance attendance) {
        return null;
    }

    @Override
    public Student getStudentById(EntityManager em, Long studentId) {
        // Tìm sinh viên theo ID để phục vụ việc điểm danh
        return em.find(Student.class, studentId);
    }

    @Override
    public List<Attendance> findByClassAndDate(TeachingClass teachingClass, LocalDate date) {
        return List.of();
    }

    @Override
    public List<Attendance> findByClass(TeachingClass teachingClass) {
        return List.of();
    }
}