package vn.edu.ute.productmgmt.service;

import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Attendance;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.repo.AttendanceRepository;
import vn.edu.ute.productmgmt.repo.EnrollmentRepository;

import java.util.List;
import java.util.UUID;

public class AttendanceService {

    private final AttendanceRepository attendanceRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final TransactionManager txManager;

    public AttendanceService(
            AttendanceRepository attendanceRepo,
            EnrollmentRepository enrollmentRepo,
            TransactionManager txManager) {

        this.attendanceRepo = attendanceRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.txManager = txManager;
    }

    // lấy danh sách học viên của lớp
    public List<Student> getStudentsByClass(UUID classId) {

        try {
            return txManager.runInTransaction(
                    em -> enrollmentRepo.findStudentsByClassId(em, classId)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // lưu hàng loạt điểm danh
    public void saveAttendanceBatch(List<Attendance> list) {

        try {
            txManager.runInTransaction(em -> {

                for (Attendance a : list) {
                    attendanceRepo.insert(em, a);
                }

                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}