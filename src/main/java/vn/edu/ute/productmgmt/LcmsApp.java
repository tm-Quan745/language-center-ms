package vn.edu.ute.productmgmt;

import vn.edu.ute.productmgmt.ui.LcmsMainFrame;
import vn.edu.ute.productmgmt.ui.UI;

import javax.swing.SwingUtilities;

/**
 * Entry point riêng để chạy giao diện Language Center Management (LCMS)
 * dùng mock data, song song với ứng dụng product hiện tại.
 */
public class LcmsApp {

    public static void main(String[] args) {
        UI.initLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            LcmsMainFrame frame = new LcmsMainFrame();
            frame.setVisible(true);
        });
    }
}

