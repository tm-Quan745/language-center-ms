package vn.edu.ute.productmgmt.config;

import vn.edu.ute.productmgmt.db.TransactionManager;
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
}