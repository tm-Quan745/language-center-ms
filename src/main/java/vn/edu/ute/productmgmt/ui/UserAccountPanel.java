package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.UserAccount;
import vn.edu.ute.productmgmt.model.Student;
import vn.edu.ute.productmgmt.model.Teacher;
import vn.edu.ute.productmgmt.model.Staff;
import vn.edu.ute.productmgmt.repo.UserAccountRepository;
import vn.edu.ute.productmgmt.repo.jpa.UserAccountRepositoryImpl;
import vn.edu.ute.productmgmt.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel quản lý tài khoản: danh sách + nút mở dialog tạo tài khoản mới.
 */
public class UserAccountPanel extends JPanel {

    private final UserAccountRepository userRepo = new UserAccountRepositoryImpl();
    private final TransactionManager tx = new TransactionManager();
    private final AuthService authService = new AuthService(userRepo, tx);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Username", "Role", "Liên kết", "Active"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private List<UserAccount> currentAccounts = new ArrayList<>();

    public UserAccountPanel() {
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
        loadAccounts();
    }

    /**
     * Header: tiêu đề + nút mở dialog tạo tài khoản (form ẩn đi, chỉ hiện khi cần).
     */
    private JComponent buildHeader() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(10, 0, 10, 0);
        g.gridx = 0;

        JLabel title = new JLabel("Quản lý tài khoản người dùng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnCreate = new JButton("TẠO TÀI KHOẢN");
        btnCreate.setPreferredSize(new Dimension(150, 36));
        btnCreate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCreate.putClientProperty(
                FlatClientProperties.STYLE,
                "background:#0d6efd;foreground:#fff;arc:10;borderWidth:0"
        );
        btnCreate.addActionListener(e -> showCreateDialog());

        JButton btnEdit = new JButton("CHỈNH SỬA");
        btnEdit.setPreferredSize(new Dimension(120, 36));
        btnEdit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEdit.putClientProperty(
                FlatClientProperties.STYLE,
                "background:#198754;foreground:#fff;arc:10;borderWidth:0"
        );
        btnEdit.addActionListener(e -> showEditDialog());

        JButton btnDelete = new JButton("XÓA");
        btnDelete.setPreferredSize(new Dimension(100, 36));
        btnDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDelete.putClientProperty(
                FlatClientProperties.STYLE,
                "background:#dc3545;foreground:#fff;arc:10;borderWidth:0"
        );
        btnDelete.addActionListener(e -> handleDelete());

        actions.add(btnCreate);
        actions.add(btnEdit);
        actions.add(btnDelete);

        g.gridy = 0;
        g.anchor = GridBagConstraints.WEST;
        panel.add(title, g);

        g.gridx = 1;
        g.anchor = GridBagConstraints.EAST;
        panel.add(actions, g);

        return panel;
    }

    private JComponent buildTableArea() {
        table.setRowHeight(28);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        return scroll;
    }

    private void loadAccounts() {
        try {
            currentAccounts = tx.runInTransaction(userRepo::findAll);
            tableModel.setRowCount(0);
            for (UserAccount ua : currentAccounts) {
                String linkInfo = "";
                if (ua.getStudent() != null) {
                    linkInfo = "Student: " + (ua.getStudent().getFullName() != null
                            ? ua.getStudent().getFullName()
                            : ua.getStudent().getId());
                } else if (ua.getTeacher() != null) {
                    linkInfo = "Teacher: " + (ua.getTeacher().getFullName() != null
                            ? ua.getTeacher().getFullName()
                            : ua.getTeacher().getId());
                } else if (ua.getStaff() != null) {
                    linkInfo = "Staff: " + (ua.getStaff().getFullName() != null
                            ? ua.getStaff().getFullName()
                            : ua.getStaff().getId());
                }
                tableModel.addRow(new Object[]{
                        ua.getId(),
                        ua.getUsername(),
                        ua.getRole(),
                        linkInfo,
                        ua.isActive()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không tải được danh sách tài khoản: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Mở dialog tạo tài khoản mới (form tách riêng, chỉ hiện khi bấm nút).
     */
    private void showCreateDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Tạo tài khoản mới", true);
        dialog.setSize(420, 360);
        dialog.setLocationRelativeTo(this);

        JTextField txtUsername = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JPasswordField txtConfirm = new JPasswordField();
        JComboBox<String> cboRole = new JComboBox<>(new String[]{"STUDENT", "TEACHER", "STAFF"});

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(10, 0, 10, 0);
        g.gridx = 0;

        JLabel title = new JLabel("TẠO TÀI KHOẢN MỚI");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        txtUsername.setPreferredSize(new Dimension(0, 40));
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tên đăng nhập");

        txtPassword.setPreferredSize(new Dimension(0, 40));
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Mật khẩu");

        txtConfirm.setPreferredSize(new Dimension(0, 40));
        txtConfirm.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Xác nhận mật khẩu");

        cboRole.setPreferredSize(new Dimension(0, 40));

        JButton btnCreate = new JButton("TẠO TÀI KHOẢN");
        btnCreate.setPreferredSize(new Dimension(0, 45));
        btnCreate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCreate.putClientProperty(
                FlatClientProperties.STYLE,
                "background:#0d6efd;foreground:#fff;arc:10;borderWidth:0"
        );

        btnCreate.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());
            String confirm = new String(txtConfirm.getPassword());
            String role = (String) cboRole.getSelectedItem();

            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Vui lòng nhập đầy đủ thông tin.",
                        "Thiếu dữ liệu",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog,
                        "Mật khẩu xác nhận không khớp.",
                        "Sai mật khẩu",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (password.length() < 4) {
                JOptionPane.showMessageDialog(dialog,
                        "Mật khẩu phải ít nhất 4 ký tự.",
                        "Mật khẩu yếu",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                authService.register(username, password, role);
                JOptionPane.showMessageDialog(dialog,
                        "Đã tạo tài khoản thành công.",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadAccounts();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Không thể tạo tài khoản: " + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        g.gridy = 0;
        panel.add(title, g);
        g.gridy = 1;
        panel.add(txtUsername, g);
        g.gridy = 2;
        panel.add(txtPassword, g);
        g.gridy = 3;
        panel.add(txtConfirm, g);
        g.gridy = 4;
        panel.add(cboRole, g);
        g.gridy = 5;
        g.insets = new Insets(20, 0, 0, 0);
        panel.add(btnCreate, g);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    /**
     * Mở dialog chỉnh sửa tài khoản hiện có: cho phép đổi mật khẩu, vai trò
     * và gắn liên kết Student/Teacher/Staff theo ID.
     */
    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một tài khoản để chỉnh sửa.",
                    "Chưa chọn dòng",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        UserAccount selectedAccount = currentAccounts.get(row);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Chỉnh sửa tài khoản", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);

        JTextField txtUsername = new JTextField(selectedAccount.getUsername());
        txtUsername.setEditable(false);

        JPasswordField txtPassword = new JPasswordField();
        JPasswordField txtConfirm = new JPasswordField();
        JComboBox<String> cboRole = new JComboBox<>(new String[]{"STUDENT", "TEACHER", "STAFF", "ADMIN"});

        if (selectedAccount.getRole() != null) {
            String currentRole = selectedAccount.getRole().name().toUpperCase();
            cboRole.setSelectedItem(currentRole);
        }

        JComboBox<String> cboLinkType = new JComboBox<>(new String[]{
                "KHÔNG GẮN", "HỌC VIÊN", "GIÁO VIÊN", "NHÂN VIÊN"
        });
        JComboBox<Object> cboLinkObject = new JComboBox<>();

        // Load danh sách Student/Teacher/Staff để chọn từ findAll
        List<Student> studentsLoaded;
        List<Teacher> teachersLoaded;
        List<Staff> staffsLoaded;
        try {
            studentsLoaded = tx.runInTransaction(em ->
                    em.createQuery("SELECT s FROM Student s", Student.class).getResultList()
            );
        } catch (Exception ex) {
            // Nếu lỗi thì để danh sách rỗng, vẫn cho chỉnh sửa phần khác
            studentsLoaded = new ArrayList<>();
        }
        try {
            teachersLoaded = tx.runInTransaction(em ->
                    em.createQuery("SELECT t FROM Teacher t", Teacher.class).getResultList()
            );
        } catch (Exception ex) {
            teachersLoaded = new ArrayList<>();
        }
        try {
            staffsLoaded = tx.runInTransaction(em ->
                    em.createQuery("SELECT s FROM Staff s", Staff.class).getResultList()
            );
        } catch (Exception ex) {
            staffsLoaded = new ArrayList<>();
        }

        final List<Student> students = studentsLoaded;
        final List<Teacher> teachers = teachersLoaded;
        final List<Staff> staffs = staffsLoaded;

        cboLinkObject.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Student) {
                    Student s = (Student) value;
                    setText("[" + s.getId() + "] " + (s.getFullName() != null ? s.getFullName() : ""));
                } else if (value instanceof Teacher) {
                    Teacher t = (Teacher) value;
                    setText("[" + t.getId() + "] " + (t.getFullName() != null ? t.getFullName() : ""));
                } else if (value instanceof Staff) {
                    Staff s = (Staff) value;
                    setText("[" + s.getId() + "] " + (s.getFullName() != null ? s.getFullName() : ""));
                } else if (value == null) {
                    setText("");
                }
                return c;
            }
        });

        // Helper cập nhật combo theo loại liên kết
        Runnable refreshLinkCombo = () -> {
            cboLinkObject.removeAllItems();
            String type = (String) cboLinkType.getSelectedItem();
            if ("HỌC VIÊN".equals(type)) {
                for (Student s : students) {
                    cboLinkObject.addItem(s);
                }
            } else if ("GIÁO VIÊN".equals(type)) {
                for (Teacher t : teachers) {
                    cboLinkObject.addItem(t);
                }
            } else if ("NHÂN VIÊN".equals(type)) {
                for (Staff s : staffs) {
                    cboLinkObject.addItem(s);
                }
            }
        };

        // Set lựa chọn ban đầu theo tài khoản hiện tại
        if (selectedAccount.getStudent() != null) {
            cboLinkType.setSelectedItem("HỌC VIÊN");
            refreshLinkCombo.run();
            Long id = selectedAccount.getStudent().getId();
            for (Student s : students) {
                if (s.getId().equals(id)) {
                    cboLinkObject.setSelectedItem(s);
                    break;
                }
            }
        } else if (selectedAccount.getTeacher() != null) {
            cboLinkType.setSelectedItem("GIÁO VIÊN");
            refreshLinkCombo.run();
            Long id = selectedAccount.getTeacher().getId();
            for (Teacher t : teachers) {
                if (t.getId().equals(id)) {
                    cboLinkObject.setSelectedItem(t);
                    break;
                }
            }
        } else if (selectedAccount.getStaff() != null) {
            cboLinkType.setSelectedItem("NHÂN VIÊN");
            refreshLinkCombo.run();
            Long id = selectedAccount.getStaff().getId();
            for (Staff s : staffs) {
                if (s.getId().equals(id)) {
                    cboLinkObject.setSelectedItem(s);
                    break;
                }
            }
        } else {
            cboLinkType.setSelectedItem("KHÔNG GẮN");
        }

        cboLinkType.addActionListener(e -> refreshLinkCombo.run());

        JCheckBox chkActive = new JCheckBox("Kích hoạt tài khoản", selectedAccount.isActive());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 0, 8, 0);
        g.gridx = 0;

        JLabel title = new JLabel("CHỈNH SỬA TÀI KHOẢN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        txtUsername.setPreferredSize(new Dimension(0, 36));
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tên đăng nhập");

        txtPassword.setPreferredSize(new Dimension(0, 36));
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Mật khẩu mới (bỏ trống nếu không đổi)");

        txtConfirm.setPreferredSize(new Dimension(0, 36));
        txtConfirm.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Xác nhận mật khẩu mới");

        cboRole.setPreferredSize(new Dimension(0, 36));

        cboLinkType.setPreferredSize(new Dimension(0, 36));
        cboLinkObject.setPreferredSize(new Dimension(0, 36));

        JButton btnSave = new JButton("LƯU THAY ĐỔI");
        btnSave.setPreferredSize(new Dimension(0, 45));
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSave.putClientProperty(
                FlatClientProperties.STYLE,
                "background:#198754;foreground:#fff;arc:10;borderWidth:0"
        );

        btnSave.addActionListener(e -> {
            String newPassword = new String(txtPassword.getPassword());
            String confirm = new String(txtConfirm.getPassword());
            String role = (String) cboRole.getSelectedItem();
            String linkType = (String) cboLinkType.getSelectedItem();

            if (!newPassword.isEmpty()) {
                if (!newPassword.equals(confirm)) {
                    JOptionPane.showMessageDialog(dialog,
                            "Mật khẩu xác nhận không khớp.",
                            "Sai mật khẩu",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (newPassword.length() < 6) {
                    JOptionPane.showMessageDialog(dialog,
                            "Mật khẩu phải ít nhất 6 ký tự.",
                            "Mật khẩu yếu",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            try {
                tx.runInTransaction(em -> {
                    UserAccount acc = em.find(UserAccount.class, selectedAccount.getId());
                    if (acc == null) {
                        throw new IllegalStateException("Tài khoản không còn tồn tại.");
                    }

                    if (!newPassword.isEmpty()) {
                        acc.setPasswordHash(newPassword);
                    }

                    if (role != null) {
                        switch (role.toUpperCase()) {
                            case "STUDENT":
                                acc.setRole(vn.edu.ute.productmgmt.model.enums.UserRole.Student);
                                break;
                            case "TEACHER":
                                acc.setRole(vn.edu.ute.productmgmt.model.enums.UserRole.Teacher);
                                break;
                            case "STAFF":
                                acc.setRole(vn.edu.ute.productmgmt.model.enums.UserRole.Staff);
                                break;
                            case "ADMIN":
                                acc.setRole(vn.edu.ute.productmgmt.model.enums.UserRole.Admin);
                                break;
                        }
                    }

                    acc.setActive(chkActive.isSelected());

                    acc.setStudent(null);
                    acc.setTeacher(null);
                    acc.setStaff(null);

                    if (linkType != null && !"KHÔNG GẮN".equals(linkType)) {
                        Object selected = cboLinkObject.getSelectedItem();
                        if (selected == null) {
                            throw new IllegalArgumentException("Vui lòng chọn đối tượng để gắn.");
                        }

                        if (selected instanceof Student) {
                            Long linkId = ((Student) selected).getId();
                            Student student = em.find(Student.class, linkId);
                            if (student == null) {
                                throw new IllegalArgumentException("Không tìm thấy học viên với ID: " + linkId);
                            }
                            acc.setStudent(student);
                        } else if (selected instanceof Teacher) {
                            Long linkId = ((Teacher) selected).getId();
                            Teacher teacher = em.find(Teacher.class, linkId);
                            if (teacher == null) {
                                throw new IllegalArgumentException("Không tìm thấy giáo viên với ID: " + linkId);
                            }
                            acc.setTeacher(teacher);
                        } else if (selected instanceof Staff) {
                            Long linkId = ((Staff) selected).getId();
                            Staff staff = em.find(Staff.class, linkId);
                            if (staff == null) {
                                throw new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + linkId);
                            }
                            acc.setStaff(staff);
                        }
                    }

                    return null;
                });

                JOptionPane.showMessageDialog(dialog,
                        "Đã cập nhật tài khoản thành công.",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadAccounts();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Không thể cập nhật tài khoản: " + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        g.gridy = 0;
        panel.add(title, g);
        g.gridy = 1;
        panel.add(txtUsername, g);
        g.gridy = 2;
        panel.add(txtPassword, g);
        g.gridy = 3;
        panel.add(txtConfirm, g);
        g.gridy = 4;
        panel.add(cboRole, g);
        g.gridy = 5;
        panel.add(cboLinkType, g);
        g.gridy = 6;
        panel.add(cboLinkObject, g);
        g.gridy = 7;
        panel.add(chkActive, g);
        g.gridy = 8;
        g.insets = new Insets(16, 0, 0, 0);
        panel.add(btnSave, g);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    /**
     * Xử lý xóa (khóa) tài khoản: sử dụng delete mềm trong repository.
     */
    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một tài khoản để xóa.",
                    "Chưa chọn dòng",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        UserAccount selectedAccount = currentAccounts.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn khóa tài khoản \"" + selectedAccount.getUsername() + "\"?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            tx.runInTransaction(em -> {
                userRepo.delete(em, selectedAccount.getId());
                return null;
            });
            JOptionPane.showMessageDialog(this,
                    "Đã khóa tài khoản thành công.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            loadAccounts();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể khóa tài khoản: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}