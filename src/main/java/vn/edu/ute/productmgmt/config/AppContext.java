package vn.edu.ute.productmgmt.config;

import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.repo.EnrollmentRepository;
import vn.edu.ute.productmgmt.repo.jpa.*;
import vn.edu.ute.productmgmt.service.*;


public class AppContext {

    private static final TransactionManager tx = new TransactionManager();

    // ===== Repository =====
    private static final JpaCourseRepository courseRepo = new JpaCourseRepository();
    private static final JpaTeacherRepository teacherRepo = new JpaTeacherRepository();
    private static final JpaRoomRepository roomRepo = new JpaRoomRepository();
    private static final JpaClassRepository classRepo = new JpaClassRepository();
    private static final JpaScheduleRepository scheduleRepo = new JpaScheduleRepository();
    private static final JpaStudentRepository studentRepo = new JpaStudentRepository();
    private static final JpaAttendanceRepository attendanceRepo = new JpaAttendanceRepository();
    private static final JpaResultRepository resultRepo = new JpaResultRepository();
    private static final JpaEnrollmentRepository enrollmentRepo = new JpaEnrollmentRepository();
    private static final JpaBranchRepository branchRepo = new JpaBranchRepository();
    private static final JpaPlacementTestRepository placementTestRepo = new JpaPlacementTestRepository();
    private static final JpaNotificationRepository notificationRepo = new JpaNotificationRepository();

    // ===== Service =====
    public static final CourseService courseService =
            new CourseService(courseRepo, tx);

    public static final TeacherService teacherService =
            new TeacherService(teacherRepo, tx);

    public static final RoomService roomService =
            new RoomService(roomRepo, tx);

    public static final ClassService classService =
            new ClassService(classRepo, tx);

    public static final ScheduleService scheduleService =
            new ScheduleService(scheduleRepo, tx);
    public static final AttendanceService attendanceService =
            new AttendanceService(attendanceRepo, enrollmentRepo,tx);
    public static final ResultService resultService =
            new ResultService(resultRepo, tx);
    public static final EnrollmentService enrollmentService =
            new EnrollmentService(enrollmentRepo, tx);
    public static final BranchService branchService =
            new BranchService(branchRepo, tx);
    public static final PlacementTestService placementTestService =
            new PlacementTestService(placementTestRepo, tx);
    public static final NotificationService notificationService =
            new NotificationService(notificationRepo, tx);
}