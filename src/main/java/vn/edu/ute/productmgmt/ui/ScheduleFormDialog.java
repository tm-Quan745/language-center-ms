package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.*;
import vn.edu.ute.productmgmt.service.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Form nhập liệu cho Lịch học, chuẩn hóa theo ClassFormDialog
 */
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
        super(owner, "Thông tin Lịch học", ModalityType.APPLICATION_MODAL);

        setSize(580, 680);
        setLayout(new BorderLayout());

        // Load data
        classService.findAll().forEach(cboClass::addItem);
        roomService.findAll().forEach(cboRoom::addItem);

        buildUI();

        if (existing != null) {
            this.result = existing;

            // Fill dữ liệu: tìm item bằng ID thay vì dùng setSelectedItem()
            if (existing.getTeachingClass() != null) {
                for (int i = 0; i < cboClass.getItemCount(); i++) {
                    TeachingClass tc = cboClass.getItemAt(i);
                    if (tc != null && tc.getId().equals(existing.getTeachingClass().getId())) {
                        cboClass.setSelectedIndex(i);
                        break;
                    }
                }
            }

            if (existing.getRoom() != null) {
                for (int i = 0; i < cboRoom.getItemCount(); i++) {
                    Room room = cboRoom.getItemAt(i);
                    if (room != null && room.getId().equals(existing.getRoom().getId())) {
                        cboRoom.setSelectedIndex(i);
                        break;
                    }
                }
            }

            txtDate.setText(existing.getStudyDate().toString());
            txtStart.setText(existing.getStartTime().toString());
            txtEnd.setText(existing.getEndTime().toString());
        } else {
            this.result = new Schedule();
            txtDate.setText(LocalDate.now().toString());
            txtStart.setText("07:30");
            txtEnd.setText("11:30");
        }

        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- Header ---
        JLabel lblHeader = new JLabel("Thông tin Lịch học");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setBorder(new EmptyBorder(0, 0, 25, 0));
        root.add(lblHeader, BorderLayout.NORTH);

        // --- Form Body ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        // Các hàng dữ liệu
        addFormRow(form, gbc, 0, "Lớp học:", cboClass);
        addFormRow(form, gbc, 1, "Phòng học:", cboRoom);
        addFormRow(form, gbc, 2, "Ngày học:", txtDate);
        addFormRow(form, gbc, 3, "Bắt đầu:", txtStart);
        addFormRow(form, gbc, 4, "Kết thúc:", txtEnd);

        root.add(form, BorderLayout.CENTER);

        // --- Buttons ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(25, 0, 0, 0));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(100, 42));
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #f2f2f2; borderWidth: 0");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Lưu Lịch học");
        btnSave.setPreferredSize(new Dimension(140, 42));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #198754; foreground: #ffffff; borderWidth: 0");
        btnSave.addActionListener(e -> onSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        root.add(footer, BorderLayout.SOUTH);

        add(root);
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridy = row;
        gbc.gridx = 0; gbc.weightx = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        p.add(createLabel(label), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 20, 8, 0);
        if (comp instanceof JComboBox || comp instanceof JTextField) {
            comp.setPreferredSize(new Dimension(0, 40));
            comp.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        }
        p.add(comp, gbc);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
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
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Thông báo lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
    public Schedule getResult() { return result; }
}