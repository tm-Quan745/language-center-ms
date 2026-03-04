package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.UserAccount;
import vn.edu.ute.productmgmt.model.enums.UserRole;
import vn.edu.ute.productmgmt.repo.CourseRepository;
import vn.edu.ute.productmgmt.repo.RoomRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaCourseRepository;
import vn.edu.ute.productmgmt.repo.jpa.JpaRoomRepository;
import vn.edu.ute.productmgmt.service.CourseService;
import vn.edu.ute.productmgmt.service.RoomService;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        UI.initLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            // Tạo user giả để test UI
            UserAccount mockUser = new UserAccount();
            mockUser.setUsername("admin");
            mockUser.setRole(UserRole.Admin);

            TransactionManager tx = new TransactionManager();
            CourseRepository courseRepo = new JpaCourseRepository();
            RoomRepository roomRepo = new JpaRoomRepository();
            CourseService courseService = new CourseService(courseRepo, tx);
            RoomService roomService = new RoomService(roomRepo, tx);

            LcmsMainFrame mainFrame = new LcmsMainFrame(mockUser, courseService, roomService);
            mainFrame.setVisible(true);
        });
    }
}