package vn.edu.ute.productmgmt.service;

import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Attendance;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.TeachingClass;
import vn.edu.ute.productmgmt.repo.AttendanceRepository;
import vn.edu.ute.productmgmt.repo.EnrollmentRepository;

import java.time.LocalDate;
import java.util.List;

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
    public List<Student> getStudentsByClass(Long classId) {

        try {
            return txManager.runInTransaction(
                    em -> enrollmentRepo.findStudentsByClassId(em, classId)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // lấy danh sách attendance theo lớp + ngày (để load lại form điểm danh)
    public List<Attendance> findByClassAndDate(TeachingClass teachingClass, LocalDate date) {
        try {
            return txManager.runInTransaction(em ->
                    attendanceRepo.findByClassAndDate(em, teachingClass, date)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Student getStudentById(Long studentId) {

        try {
            return txManager.runInTransaction(em ->
                    attendanceRepo.getStudentById(em, studentId)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // lưu hàng loạt điểm danh (upsert theo student + class + date)
    public void saveAttendanceBatch(List<Attendance> list) {

        if (list == null || list.isEmpty()) {
            return;
        }

        try {
            txManager.runInTransaction(em -> {
                // Giả định toàn bộ list cùng lớp + cùng ngày
                TeachingClass cls = list.get(0).getTeachingClass();
                LocalDate date = list.get(0).getAttendDate();

                // Lấy các bản ghi đã tồn tại để tránh lỗi duplicate unique
                List<Attendance> existing = attendanceRepo.findByClassAndDate(em, cls, date);

                java.util.Map<Long, Attendance> existingByStudentId = new java.util.HashMap<>();
                for (Attendance a : existing) {
                    if (a.getStudent() != null && a.getStudent().getId() != null) {
                        existingByStudentId.put(a.getStudent().getId(), a);
                    }
                }

                for (Attendance incoming : list) {
                    if (incoming.getStudent() == null || incoming.getStudent().getId() == null) {
                        continue;
                    }
                    Long studentId = incoming.getStudent().getId();
                    Attendance target = existingByStudentId.get(studentId);
                    if (target != null) {
                        // cập nhật bản ghi cũ
                        target.setStatus(incoming.getStatus());
                        target.setNote(incoming.getNote());
                        attendanceRepo.update(em, target);
                    } else {
                        // thêm mới nếu chưa có
                        attendanceRepo.insert(em, incoming);
                    }
                }

                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}