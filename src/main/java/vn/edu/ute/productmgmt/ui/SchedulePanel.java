package vn.edu.ute.productmgmt.ui;

import vn.edu.ute.productmgmt.model.*;
import vn.edu.ute.productmgmt.service.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class SchedulePanel extends JPanel {

    // ── Services ──────────────────────────────────────────────────────────────
    private final ClassService    classService;
    private final ScheduleService scheduleService;
    private final RoomService     roomService;

    // ── Form fields ───────────────────────────────────────────────────────────
    private JComboBox<TeachingClass> cboClass;
    private JComboBox<Room>          cboRoom;
    private JTextField               txtDate;
    private JTextField               txtStart;
    private JTextField               txtEnd;

    /** ID of schedule being edited; null = create mode */
    private Long editingId = null;

    // ── Form state ────────────────────────────────────────────────────────────
    private JLabel  lblFormTitle;
    private JButton btnSubmit;
    private JButton btnCancel;

    // ── Table ─────────────────────────────────────────────────────────────────
    private JTable            table;
    private DefaultTableModel tableModel;

    // ── Status bar ────────────────────────────────────────────────────────────
    private JLabel lblStatus;

    // ── Design tokens (same as ClassPanel) ────────────────────────────────────
    private static final Color BG         = new Color(0xF0F4F8);
    private static final Color SURFACE    = Color.WHITE;
    private static final Color PRIMARY    = new Color(0x3B5BDB);
    private static final Color PRIMARY_DK = new Color(0x2F4AC2);
    private static final Color DANGER     = new Color(0xE03131);
    private static final Color DANGER_DK  = new Color(0xC92A2A);
    private static final Color TEXT_H     = new Color(0x1A1D2E);
    private static final Color TEXT_B     = new Color(0x495057);
    private static final Color TEXT_MUTED = new Color(0x868E96);
    private static final Color BORDER_CLR = new Color(0xDEE2E6);
    private static final Color ROW_EVEN   = new Color(0xF8F9FA);
    private static final Color ROW_SEL    = new Color(0xDBE4FF);
    private static final Color HEADER_BG  = new Color(0xF1F3F5);

    private static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD,  14);
    private static final Font FONT_LABEL   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_INPUT   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_TABLE   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_TH      = new Font("Segoe UI", Font.BOLD,  12);
    private static final Font FONT_STATUS  = new Font("Segoe UI", Font.PLAIN, 12);

    // ─────────────────────────────────────────────────────────────────────────
    public SchedulePanel(ClassService classService,
                         ScheduleService scheduleService,
                         RoomService roomService) {
        this.classService    = classService;
        this.scheduleService = scheduleService;
        this.roomService     = roomService;

        initUI();
        loadComboData();
        loadTable();
    }

    // ── Root layout ───────────────────────────────────────────────────────────
    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        add(buildContent(),   BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ── Main content ─────────────────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(BG);
        wrap.setBorder(new EmptyBorder(20, 20, 8, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.gridy   = 0;
        gbc.weighty = 1.0;

        gbc.gridx   = 0;
        gbc.weightx = 0;
        gbc.insets  = new Insets(0, 0, 0, 16);
        wrap.add(buildFormCard(), gbc);

        gbc.gridx   = 1;
        gbc.weightx = 1;
        gbc.insets  = new Insets(0, 0, 0, 0);
        wrap.add(buildTableCard(), gbc);

        return wrap;
    }

    // ── Form card ─────────────────────────────────────────────────────────────
    private JPanel buildFormCard() {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 16));
        card.setPreferredSize(new Dimension(300, 0));

        lblFormTitle = sectionLabel("Add Schedule");
        card.add(lblFormTitle, BorderLayout.NORTH);

        cboClass = styledCombo();
        cboRoom  = styledCombo();
        txtDate  = styledField("2026-03-10");
        txtStart = styledField("08:00");
        txtEnd   = styledField("10:00");

        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.add(fieldBlock("Class",            cboClass));
        fields.add(Box.createVerticalStrut(10));
        fields.add(fieldBlock("Room (optional)",  cboRoom));
        fields.add(Box.createVerticalStrut(10));
        fields.add(fieldBlock("Study Date (yyyy-MM-dd)", txtDate));
        fields.add(Box.createVerticalStrut(10));
        fields.add(fieldBlock("Start Time (HH:mm)", txtStart));
        fields.add(Box.createVerticalStrut(10));
        fields.add(fieldBlock("End Time (HH:mm)",   txtEnd));

        card.add(fields, BorderLayout.CENTER);

        btnSubmit = pillButton("Add Schedule", PRIMARY, PRIMARY_DK);
        btnSubmit.addActionListener(e -> submitForm());

        btnCancel = iconButton("Cancel");
        btnCancel.setVisible(false);
        btnCancel.addActionListener(e -> resetForm());

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnCancel);
        btnRow.add(btnSubmit);

        card.add(btnRow, BorderLayout.SOUTH);
        return card;
    }

    // ── Table card ────────────────────────────────────────────────────────────
    private JPanel buildTableCard() {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 12));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(sectionLabel("Schedule List"), BorderLayout.WEST);

        JButton btnRefresh = iconButton("Refresh");
        btnRefresh.addActionListener(e -> {loadTable(); loadComboData(); showToast("Data refreshed.", false);});
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        toolbar.setOpaque(false);
        toolbar.add(btnRefresh);
        header.add(toolbar, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // col 0 = hidden ID
        String[] cols = {"id", "Class", "Room", "Date", "Start", "End"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(FONT_TABLE);
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(0xEEEEEE));
        table.setSelectionBackground(ROW_SEL);
        table.setSelectionForeground(TEXT_H);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);

        // Hide UUID column
        TableColumn idCol = table.getColumnModel().getColumn(0);
        idCol.setMinWidth(0);
        idCol.setMaxWidth(0);
        idCol.setWidth(0);

        // Alternating row renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setFont(FONT_TABLE);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (!sel) {
                    setBackground(row % 2 == 0 ? SURFACE : ROW_EVEN);
                    setForeground(TEXT_B);
                }
                return this;
            }
        });

        // Table header style
        JTableHeader th = table.getTableHeader();
        th.setFont(FONT_TH);
        th.setBackground(HEADER_BG);
        th.setForeground(TEXT_MUTED);
        th.setBorder(new MatteBorder(0, 0, 2, 0, BORDER_CLR));
        th.setPreferredSize(new Dimension(0, 38));
        ((DefaultTableCellRenderer) th.getDefaultRenderer())
                .setBorder(new EmptyBorder(0, 12, 0, 12));

        // Click row → fill form for editing
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelection();
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        scroll.getViewport().setBackground(SURFACE);
        card.add(scroll, BorderLayout.CENTER);

        // Bottom action bar
        JButton btnDelete = pillButton("Delete Selected", DANGER, DANGER_DK);
        btnDelete.addActionListener(e -> deleteSelected());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.setOpaque(false);
        actions.add(btnDelete);
        card.add(actions, BorderLayout.SOUTH);

        return card;
    }

    // ── Status bar ────────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(SURFACE);
        bar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_CLR),
                new EmptyBorder(7, 20, 7, 20)
        ));
        lblStatus = new JLabel("Ready");
        lblStatus.setFont(FONT_STATUS);
        lblStatus.setForeground(TEXT_MUTED);
        bar.add(lblStatus, BorderLayout.WEST);
        return bar;
    }

    // ── Data operations ───────────────────────────────────────────────────────
    private void loadComboData() {
        cboClass.removeAllItems();
        classService.findAll().forEach(cboClass::addItem);
        cboRoom.removeAllItems();
        roomService.findAll().forEach(cboRoom::addItem);
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        List<Schedule> list = scheduleService.findAll();
        for (Schedule s : list) {
            tableModel.addRow(new Object[]{
                    s.getId(),                                    // col 0 (hidden ID)
                    s.getTeachingClass().getClassName(),          // col 1
                    s.getRoom() != null ? s.getRoom().getRoomName() : "", // col 2
                    s.getStudyDate(),                             // col 3
                    s.getStartTime(),                             // col 4
                    s.getEndTime()                                // col 5
            });
        }
        lblStatus.setText("Total: " + list.size() + " schedules");
    }

    /** Selecting a row fills the form and switches to Edit mode */
    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        Long id = (Long) tableModel.getValueAt(row, 0);
        Schedule s = scheduleService.findById(id);
        if (s == null) return;

        editingId = id;

        selectComboItem(cboClass, s.getTeachingClass());
        selectComboItem(cboRoom,  s.getRoom());
        txtDate.setText(s.getStudyDate() != null ? s.getStudyDate().toString() : "");
        txtStart.setText(s.getStartTime().toString());
        txtEnd.setText(s.getEndTime().toString());

        lblFormTitle.setText("Edit Schedule  —  ID " + id + "...");
        btnSubmit.setText("Save Changes");
        btnCancel.setVisible(true);

        showToast("Editing schedule ID " + id + "... Make changes and click Save.", false);
    }

    /** Create or update depending on editingId */
    private void submitForm() {
        try {
            TeachingClass tc  = (TeachingClass) cboClass.getSelectedItem();
            Room         room = (Room)         cboRoom.getSelectedItem();
            LocalDate     date = LocalDate.parse(txtDate.getText().trim());

            LocalTime start = LocalTime.parse(txtStart.getText().trim());
            LocalTime end   = LocalTime.parse(txtEnd.getText().trim());

            if (!end.isAfter(start))
                throw new IllegalArgumentException("End time must be after start time.");

            if (editingId == null) {
                // ── CREATE ──
                Schedule s = new Schedule();
                s.setTeachingClass(tc);
                s.setRoom(room);
                s.setStudyDate(date);
                s.setStartTime(start);
                s.setEndTime(end);
                scheduleService.createSchedule(s);
                showToast("Schedule added successfully.", false);

            } else {
                // ── UPDATE by UUID ──
                Schedule s = scheduleService.findById(editingId);
                if (s == null) throw new IllegalStateException("Schedule not found (ID " + editingId + ").");
                s.setTeachingClass(tc);
                s.setRoom(room);
                s.setStudyDate(date);
                s.setStartTime(start);
                s.setEndTime(end);
                scheduleService.updateSchedule(s);  // adjust to your actual service method
                showToast("Schedule updated.", false);
            }

            loadTable();
            resetForm();

        } catch (Exception ex) {
            showToast(ex.getMessage(), true);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showToast("Please select a schedule to delete.", true); return; }

        Long id       = (Long) tableModel.getValueAt(row, 0);
        String dayLabel = tableModel.getValueAt(row, 3).toString()
                + " " + tableModel.getValueAt(row, 4)
                + "–" + tableModel.getValueAt(row, 5);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete schedule \"" + dayLabel + "\"?\nThis cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                scheduleService.deleteSchedule(id);  // adjust to your actual service method
                loadTable();
                resetForm();
                showToast("Schedule deleted.", false);
            } catch (Exception ex) {
                showToast("Delete failed: " + ex.getMessage(), true);
            }
        }
    }

    /** Reset back to Add mode */
    private void resetForm() {
        editingId = null;
        lblFormTitle.setText("Add Schedule");
        btnSubmit.setText("Add Schedule");
        btnCancel.setVisible(false);

        if (cboClass.getItemCount() > 0) cboClass.setSelectedIndex(0);
        cboRoom.setSelectedItem(null);
        txtDate.setText("2026-03-10");
        txtStart.setText("08:00");
        txtEnd.setText("10:00");

        table.clearSelection();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private <T> void selectComboItem(JComboBox<T> combo, T target) {
        if (target == null) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).equals(target)) { combo.setSelectedIndex(i); return; }
        }
    }

    private void showToast(String msg, boolean error) {
        lblStatus.setForeground(error ? DANGER : new Color(0x2F9E44));
        lblStatus.setText(msg);
        Timer t = new Timer(4000, e -> {
            lblStatus.setForeground(TEXT_MUTED);
            lblStatus.setText("Ready");
        });
        t.setRepeats(false);
        t.start();
    }

    // ── Widget factories ──────────────────────────────────────────────────────
    private JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(SURFACE);
        p.setBorder(new CompoundBorder(
                new LineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        return p;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SECTION);
        l.setForeground(TEXT_H);
        return l;
    }

    private JPanel fieldBlock(String labelText, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_MUTED);
        p.add(lbl,   BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JTextField styledField(String placeholder) {
        JTextField tf = new JTextField(placeholder) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tf.setFont(FONT_INPUT);
        tf.setForeground(TEXT_B);
        tf.setBackground(new Color(0xF8F9FA));
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(7, 10, 7, 10)
        ));
        tf.setPreferredSize(new Dimension(0, 36));
        tf.setOpaque(false);
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                tf.setBorder(new CompoundBorder(
                        new LineBorder(PRIMARY, 2, true), new EmptyBorder(6, 9, 6, 9)));
            }
            @Override public void focusLost(FocusEvent e) {
                tf.setBorder(new CompoundBorder(
                        new LineBorder(BORDER_CLR, 1, true), new EmptyBorder(7, 10, 7, 10)));
            }
        });
        return tf;
    }

    private <T> JComboBox<T> styledCombo() {
        JComboBox<T> cb = new JComboBox<>();
        cb.setFont(FONT_INPUT);
        cb.setBackground(new Color(0xF8F9FA));
        cb.setBorder(new LineBorder(BORDER_CLR, 1, true));
        cb.setPreferredSize(new Dimension(0, 36));
        return cb;
    }

    private JButton pillButton(String text, Color bg, Color hover) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() || getModel().isPressed() ? hover : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(0, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton iconButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(TEXT_B);
        btn.setBackground(new Color(0xF1F3F5));
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}