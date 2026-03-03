package vn.edu.ute.productmgmt.ui;

import javax.swing.*;
import java.awt.*;

public final class UI {
    private UI() {}

    public static void initLookAndFeel() {
        try {
            // Ưu tiên Nimbus cho giao diện hiện đại hơn, fallback về system L&F nếu không có.
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    public static void styleTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setAutoCreateRowSorter(true);
    }

    public static void stylePanelBorder(JComponent panel, String title) {
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(8, 8, 8, 8),
                title
        ));
    }
}

