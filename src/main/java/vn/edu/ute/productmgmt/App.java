package vn.edu.ute.productmgmt;

import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.repo.BranchRepository;
import vn.edu.ute.productmgmt.repo.CertificateRepository;
import vn.edu.ute.productmgmt.repo.CategoryRepository;
import vn.edu.ute.productmgmt.repo.CourseRepository;
import vn.edu.ute.productmgmt.repo.EnrollmentRepository;
import vn.edu.ute.productmgmt.repo.InvoiceRepository;
import vn.edu.ute.productmgmt.repo.PaymentRepository;
import vn.edu.ute.productmgmt.repo.NotificationRepository;
import vn.edu.ute.productmgmt.repo.PlacementTestRepository;
import vn.edu.ute.productmgmt.repo.PromotionRepository;
import vn.edu.ute.productmgmt.repo.ProductRepository;
import vn.edu.ute.productmgmt.repo.RoomRepository;
import vn.edu.ute.productmgmt.repo.StudentRepository;
import vn.edu.ute.productmgmt.repo.TeacherRepository;
import vn.edu.ute.productmgmt.repo.StaffRepository;
import vn.edu.ute.productmgmt.repo.UserAccountRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaBranchRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaCertificateRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaCategoryRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaCourseRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaEnrollmentRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaInvoiceRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaPaymentRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaPlacementTestRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaPromotionRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaNotificationRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaProductRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaRoomRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaStudentRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaTeacherRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaStaffRepository;
import vn.edu.ute.productmgmt.repo.jpa.UserAccountRepositoryImpl;
import vn.edu.ute.productmgmt.service.AuthService;
import vn.edu.ute.productmgmt.service.BranchService;
import vn.edu.ute.productmgmt.service.CertificateService;
import vn.edu.ute.productmgmt.service.CategoryService;
import vn.edu.ute.productmgmt.service.CourseService;
import vn.edu.ute.productmgmt.service.EnrollmentService;
import vn.edu.ute.productmgmt.service.InvoiceService;
import vn.edu.ute.productmgmt.service.PaymentService;
import vn.edu.ute.productmgmt.service.PlacementTestService;
import vn.edu.ute.productmgmt.service.PromotionService;
import vn.edu.ute.productmgmt.service.NotificationService;
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
       UserAccountRepository userAccountRepo = new UserAccountRepositoryImpl();
       AuthService authService = new AuthService(userAccountRepo, tx);

       CourseRepository courseRepo = new JpaCourseRepository();
       RoomRepository roomRepo = new JpaRoomRepository();
       BranchRepository branchRepo = new JpaBranchRepository();
       CertificateRepository certificateRepo = new JpaCertificateRepository();
       CourseService courseService = new CourseService(courseRepo, tx);
       RoomService roomService = new RoomService(roomRepo, tx);
       BranchService branchService = new BranchService(branchRepo, tx);
       CertificateService certificateService = new CertificateService(certificateRepo, tx);

       StudentRepository studentRepo = new JpaStudentRepository();
       TeacherRepository teacherRepo = new JpaTeacherRepository();
       StaffRepository staffRepo = new JpaStaffRepository();
       EnrollmentRepository enrollmentRepo = new JpaEnrollmentRepository();
       PaymentRepository paymentRepo = new JpaPaymentRepository();
       PromotionRepository promotionRepo = new JpaPromotionRepository();
       InvoiceRepository invoiceRepo = new JpaInvoiceRepository();
       PlacementTestRepository placementTestRepo = new JpaPlacementTestRepository();
       NotificationRepository notificationRepo = new JpaNotificationRepository();

       StudentService studentService = new StudentService(studentRepo, tx);
       TeacherService teacherService = new TeacherService(teacherRepo, tx);
       StaffService staffService = new StaffService(staffRepo, tx);
       EnrollmentService enrollmentService = new EnrollmentService(enrollmentRepo, tx);
       PaymentService paymentService = new PaymentService(paymentRepo, tx);
       PromotionService promotionService = new PromotionService(promotionRepo, tx);
       InvoiceService invoiceService = new InvoiceService(invoiceRepo, promotionService, tx);
       PlacementTestService placementTestService = new PlacementTestService(placementTestRepo, tx);
       NotificationService notificationService = new NotificationService(notificationRepo, tx);

       SwingUtilities.invokeLater(() -> new LcmsLoginFrame(
               authService,
               courseService,
               roomService,
               branchService,
               certificateService,
               studentService,
               teacherService,
               staffService,
               enrollmentService,
               paymentService,
               promotionService,
               invoiceService,
               placementTestService,
               notificationService
       ).setVisible(true));
   }
}
