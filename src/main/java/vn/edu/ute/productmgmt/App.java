package vn.edu.ute.productmgmt;

import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.repo.CategoryRepository;
import vn.edu.ute.productmgmt.repo.CourseRepository;
import vn.edu.ute.productmgmt.repo.ProductRepository;
import vn.edu.ute.productmgmt.repo.RoomRepository;
import vn.edu.ute.productmgmt.repo.StudentRepository;
import vn.edu.ute.productmgmt.repo.TeacherRepository;
import vn.edu.ute.productmgmt.repo.StaffRepository;
import vn.edu.ute.productmgmt.repo.UserAccountRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaCategoryRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaCourseRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaProductRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaRoomRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaStudentRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaTeacherRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaStaffRepository;
import vn.edu.ute.productmgmt.repo.jpa.UserAccountRepositoryImpl;
import vn.edu.ute.productmgmt.service.AuthService;
import vn.edu.ute.productmgmt.service.CategoryService;
import vn.edu.ute.productmgmt.service.CourseService;
import vn.edu.ute.productmgmt.service.ProductService;
import vn.edu.ute.productmgmt.service.RoomService;
import vn.edu.ute.productmgmt.service.StudentService;
import vn.edu.ute.productmgmt.service.TeacherService;
import vn.edu.ute.productmgmt.service.StaffService;
import vn.edu.ute.productmgmt.ui.LcmsLoginFrame;
import vn.edu.ute.productmgmt.ui.UI;

import javax.swing.*;

public class App {
   public static void main(String[] args) {
       UI.initLookAndFeel();

       TransactionManager tx = new TransactionManager();
       ProductRepository productRepo = new JpaProductRepository();
       CategoryRepository categoryRepo = new JpaCategoryRepository();

       ProductService productService = new ProductService(productRepo, categoryRepo, tx);
       CategoryService categoryService = new CategoryService(categoryRepo, tx);

       UserAccountRepository userAccountRepo = new UserAccountRepositoryImpl();
       AuthService authService = new AuthService(userAccountRepo, tx);

       CourseRepository courseRepo = new JpaCourseRepository();
       RoomRepository roomRepo = new JpaRoomRepository();
       CourseService courseService = new CourseService(courseRepo, tx);
       RoomService roomService = new RoomService(roomRepo, tx);

       StudentRepository studentRepo = new JpaStudentRepository();
       TeacherRepository teacherRepo = new JpaTeacherRepository();
       StaffRepository staffRepo = new JpaStaffRepository();

       StudentService studentService = new StudentService(studentRepo, tx);
       TeacherService teacherService = new TeacherService(teacherRepo, tx);
       StaffService staffService = new StaffService(staffRepo, tx);

       SwingUtilities.invokeLater(() -> new LcmsLoginFrame(authService, courseService, roomService,
               studentService, teacherService, staffService).setVisible(true));
   }
}
