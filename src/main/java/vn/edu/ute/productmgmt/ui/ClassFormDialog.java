package vn.edu.ute.productmgmt.ui;

import com.formdev.flatlaf.FlatClientProperties;
import vn.edu.ute.productmgmt.model.*;
import vn.edu.ute.productmgmt.service.*;
import vn.edu.ute.productmgmt.model.enums.ClassStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class ClassFormDialog extends JDialog {
    private final RoomService roomService;
    private final JComboBox<Course> cboCourse = new JComboBox<>();
    private final JComboBox<Teacher> cboTeacher = new JComboBox<>();
    private final JComboBox<Room> cboRoom = new JComboBox<>();
    private final JComboBox<Branch> cboBranch = new JComboBox<>();
    private final JTextField txtMaxStudent = new JTextField();

    private final JSpinner spnStart;
    private final JSpinner spnEnd;

    private boolean saved = false;
    private final TeachingClass result;

    public ClassFormDialog(Window owner, TeachingClass existing,
                           CourseService cs, TeacherService ts, RoomService rs, BranchService bs) {
        super(owner, "Thiết lập lớp học", ModalityType.APPLICATION_MODAL);
        this.roomService = rs;

        setSize(580, 680);
        setLayout(new BorderLayout());

        // Khởi tạo Spinner trước khi build UI
        spnStart = createDateSpinner(new Date());
        spnEnd = createDateSpinner(addMonths(new Date(), 3));

        // Load Data vào ComboBox
        try {
            cs.findAll().forEach(cboCourse::addItem);
            ts.findAll().forEach(cboTeacher::addItem);
            cboBranch.addItem(null);
            bs.findAll().forEach(cboBranch::addItem);
        } catch (Exception ignored) {}

        if (existing != null) {
            this.result = existing;

            cboCourse.setSelectedItem(existing.getCourse());
            cboTeacher.setSelectedItem(existing.getTeacher());
            cboBranch.setSelectedItem(existing.getBranch());

            // LOAD ROOM ĐÚNG CHI NHÁNH
            if(existing.getBranch() != null){
                loadRoomsByBranch(existing.getBranch());
                cboRoom.setSelectedItem(existing.getRoom());
            }

            txtMaxStudent.setText(String.valueOf(existing.getMaxStudent()));
            spnStart.setValue(toDate(existing.getStartDate()));
            spnEnd.setValue(toDate(existing.getEndDate()));
        } else {
            this.result = new TeachingClass();
        }
        cboBranch.addActionListener(e -> {

            Branch branch = (Branch) cboBranch.getSelectedItem();

            if(branch != null){
                loadRoomsByBranch(branch);
            }

        });
        buildUI();
        setLocationRelativeTo(owner);
    }

    private void loadRoomsByBranch(Branch branch){

        cboRoom.removeAllItems();

        if(branch == null){
            cboRoom.setEnabled(false);
            return;
        }

        try{

            // Lấy danh sách phòng theo chi nhánh
            roomService.findByBranch(branch.getId())
                    .forEach(cboRoom::addItem);
            cboRoom.setEnabled(true);

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,
                    "Không tải được phòng học: " + ex.getMessage());

        }
    }

    private JSpinner createDateSpinner(Date defaultDate) {
        SpinnerDateModel model = new SpinnerDateModel(defaultDate, null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        spinner.setPreferredSize(new Dimension(0, 40));
        spinner.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        return spinner;
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- Header ---
        JLabel lblHeader = new JLabel("Thông tin lớp học");
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
        addFormRow(form, gbc, 0, "Khóa học đào tạo:", cboCourse);
        addFormRow(form, gbc, 1, "Giảng viên phụ trách:", cboTeacher);
        addFormRow(form, gbc, 2, "Chi nhánh quản lý:", cboBranch);
        cboBranch.setRenderer((list, value, index, isSelected, cellHasFocus) -> {

            JLabel label = new JLabel();

            if (value == null) {
                label.setText("---- Chọn chi nhánh ----");
            } else {
                label.setText(((Branch) value).getBranchName());
            }

            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
                label.setOpaque(true);
            }

            return label;
        });
        addFormRow(form, gbc, 3, "Phòng học:", cboRoom);
        cboRoom.setEnabled(false);

        // Sĩ số
        gbc.gridy = 4; gbc.gridx = 0; gbc.weightx = 0;
        form.add(createLabel("Sĩ số tối đa:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.insets = new Insets(8, 20, 8, 0);
        txtMaxStudent.setPreferredSize(new Dimension(0, 40));
        txtMaxStudent.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        txtMaxStudent.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ví dụ: 30");
        txtMaxStudent.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new JLabel(" 👥 "));
        form.add(txtMaxStudent, gbc);

        // Ngày tháng
        gbc.insets = new Insets(8, 0, 8, 0);
        addFormRow(form, gbc, 5, "Ngày bắt đầu:", spnStart);
        addFormRow(form, gbc, 6, "Ngày kết thúc:", spnEnd);

        root.add(form, BorderLayout.CENTER);

        // --- Buttons ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(25, 0, 0, 0));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(100, 42));
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #f2f2f2; borderWidth: 0");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Lưu lớp học");
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
        if (comp instanceof JComboBox) {
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
            spnStart.commitEdit();
            spnEnd.commitEdit();

            LocalDate start = toLocalDate((Date) spnStart.getValue());
            LocalDate end = toLocalDate((Date) spnEnd.getValue());

            if (start.isAfter(end)) {
                throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc!");
            }

            Course selectedCourse = (Course) cboCourse.getSelectedItem();
            Branch selectedBranch = (Branch) cboBranch.getSelectedItem();
            if (selectedCourse == null) throw new IllegalArgumentException("Vui lòng chọn khóa học!");

            result.setCourse(selectedCourse);
            result.setTeacher((Teacher) cboTeacher.getSelectedItem());
            result.setRoom((Room) cboRoom.getSelectedItem());
            result.setBranch(selectedBranch);
            result.setMaxStudent(Integer.parseInt(txtMaxStudent.getText().trim()));
            result.setStartDate(start);
            result.setEndDate(end);

            if (result.getId() == null) {
                String datePart = start.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String branchPart = (selectedBranch != null) ? selectedBranch.getBranchName() : "Gen";
                String autoName = String.format("%s_%s_%s",
                        selectedCourse.getCourseName().replaceAll("\\s+", ""),
                        branchPart,
                        datePart);

                result.setClassName(autoName);
                result.setStatus(ClassStatus.Open);
            }

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Thông báo lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Date toDate(LocalDate ld) {
        return ld == null ? new Date() : Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date addMonths(Date date, int months) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, months);
        return cal.getTime();
    }

    public boolean isSaved() { return saved; }
    public TeachingClass getResult() { return result; }
}