package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.*;
import vn.edu.ute.productmgmt.service.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleFormDialog extends JDialog {
    private final JComboBox<TeachingClass> cboClass = new JComboBox<>();
    private final JComboBox<Room> cboRoom = new JComboBox<>();
    private final JTextField txtDate = new JTextField(10);
    private final JTextField txtStart = new JTextField(10);
    private final JTextField txtEnd = new JTextField(10);

    private boolean saved = false;
    private final Schedule result;

    public ScheduleFormDialog(Window owner, Schedule existing,
                              ClassService classService, RoomService roomService) {
        super(owner, existing == null ? "Thêm lịch học" : "Sửa lịch học", ModalityType.APPLICATION_MODAL);

        // Load data
        classService.findAll().forEach(cboClass::addItem);
        roomService.findAll().forEach(cboRoom::addItem);

        if (existing != null) {
            this.result = existing;
            cboClass.setSelectedItem(existing.getTeachingClass());
            cboRoom.setSelectedItem(existing.getRoom());
            txtDate.setText(existing.getStudyDate().toString());
            txtStart.setText(existing.getStartTime().toString());
            txtEnd.setText(existing.getEndTime().toString());
        } else {
            this.result = new Schedule();
            txtDate.setText(LocalDate.now().toString());
            txtStart.setText("07:30");
            txtEnd.setText("11:30");
        }

        buildUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;
        addFormRow(p, "Lớp học:", cboClass, g, r++);
        addFormRow(p, "Phòng học:", cboRoom, g, r++);
        addFormRow(p, "Ngày học (yyyy-MM-dd):", txtDate, g, r++);
        addFormRow(p, "Bắt đầu (HH:mm):", txtStart, g, r++);
        addFormRow(p, "Kết thúc (HH:mm):", txtEnd, g, r++);

        JButton btnSave = new JButton("Lưu lại");
        btnSave.addActionListener(e -> onSave());
        JButton btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnSave);
        actions.add(btnCancel);

        add(p, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel p, String label, JComponent comp, GridBagConstraints g, int row) {
        g.gridx = 0; g.gridy = row; g.weightx = 0; p.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 1.0; p.add(comp, g);
    }

    private void onSave() {
        try {
            result.setTeachingClass((TeachingClass) cboClass.getSelectedItem());
            result.setRoom((Room) cboRoom.getSelectedItem());
            result.setStudyDate(LocalDate.parse(txtDate.getText().trim()));
            result.setStartTime(LocalTime.parse(txtStart.getText().trim()));
            result.setEndTime(LocalTime.parse(txtEnd.getText().trim()));

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Dữ liệu không hợp lệ: " + ex.getMessage());
        }
    }

    public boolean isSaved() { return saved; }
    public Schedule getResult() { return result; }
}