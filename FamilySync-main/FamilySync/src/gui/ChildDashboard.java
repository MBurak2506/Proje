package gui;

import model.*;
import storage.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ChildDashboard extends JFrame {

    private final Child child;
    private final DataManager dm;

    private CardLayout cardLayout;
    private JPanel contentArea;
    private JButton[] navBtns;

    private DefaultTableModel myTaskModel;
    private DefaultTableModel assignedModel;
    private CalendarGridPanel calendarPanel;
    private JLabel pointsLbl;

    public ChildDashboard(Child child, DataManager dm) {
        this.child = child;
        this.dm    = dm;

        setTitle("FamilySync – " + child.getFullName());
        setSize(980, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 500));
        setIconImages(AppUI.createWindowIconImages());

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildSidebar(),  BorderLayout.WEST);
        root.add(buildContent(),  BorderLayout.CENTER);
        add(root);

        AppUI.setNavActive(navBtns, 0);
        setVisible(true);
    }

    // ── Sidebar ──────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(AppUI.HEADER_BG);
        sb.setPreferredSize(new Dimension(195, 0));
        sb.setBorder(new EmptyBorder(24, 0, 16, 0));

        // Uygulama başlığı
        JLabel appLbl = new JLabel("  FamilySync");
        appLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        appLbl.setForeground(Color.WHITE);
        appLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sb.add(appLbl);
        sb.add(Box.createVerticalStrut(4));

        // Kullanıcı adı
        JLabel nameLbl = new JLabel("  " + child.getFullName());
        nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameLbl.setForeground(new Color(160, 175, 230));
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sb.add(nameLbl);

        // Puan
        pointsLbl = new JLabel("  " + child.getPoints() + " puan");
        pointsLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pointsLbl.setForeground(new Color(255, 200, 80));
        pointsLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sb.add(pointsLbl);
        sb.add(Box.createVerticalStrut(16));

        // Ayraç
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(70, 90, 160));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sb.add(sep);
        sb.add(Box.createVerticalStrut(10));

        // Nav butonları
        String[] labels = {"Görevlerim", "Atanan Görevler", "Takvim", "Rozetlerim"};
        String[] cards  = {"myTasks", "assigned", "calendar", "badges"};
        navBtns = new JButton[labels.length];
        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            JButton btn = AppUI.navBtn(labels[i]);
            btn.addActionListener(e -> { cardLayout.show(contentArea, cards[idx]); AppUI.setNavActive(navBtns, idx); });
            navBtns[i] = btn;
            sb.add(btn);
        }

        sb.add(Box.createGlue());
        sb.add(sep = new JSeparator());
        sep.setForeground(new Color(70, 90, 160));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sb.add(Box.createVerticalStrut(8));

        // Çıkış butonu
        JButton logout = AppUI.navBtn("Çıkış Yap");
        logout.setForeground(new Color(255, 120, 120));
        logout.addActionListener(e -> { dispose(); new LoginFrame(dm); });
        sb.add(logout);
        return sb;
    }

    // ── İçerik Alanı ─────────────────────────────────────────────────────

    private JPanel buildContent() {
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(AppUI.BG);
        contentArea.add(buildMyTasksPanel(),  "myTasks");
        contentArea.add(buildAssignedPanel(), "assigned");
        contentArea.add(buildCalendarPanel(), "calendar");
        contentArea.add(buildBadgesPanel(),   "badges");

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(AppUI.BG);
        wrapper.setBorder(new MatteBorder(0, 1, 0, 0, AppUI.BORDER_CLR));
        wrapper.add(contentArea, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Görevlerim ────────────────────────────────────────────────────────

    private JPanel buildMyTasksPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(AppUI.BG);
        panel.setBorder(new EmptyBorder(16, 18, 14, 18));

        JLabel title = new JLabel("Görevlerim");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(AppUI.TEXT);

        String[] cols = {"Başlık", "Açıklama", "Öncelik", "Bitiş Tarihi", "Durum"};
        myTaskModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(myTaskModel);
        AppUI.styleTable(table);
        loadMyTasks();

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnBar.setOpaque(false);
        JButton addBtn  = AppUI.btn("+ Görev Ekle", AppUI.SUCCESS);
        JButton doneBtn = AppUI.btn("✓ Tamamlandı", AppUI.PRIMARY);
        addBtn.addActionListener(e  -> { showAddTaskDialog(); loadMyTasks(); });
        doneBtn.addActionListener(e -> { markMyTaskDone(table); loadMyTasks(); });
        btnBar.add(addBtn); btnBar.add(doneBtn);

        panel.add(title, BorderLayout.NORTH);
        panel.add(AppUI.scroll(table, null), BorderLayout.CENTER);
        panel.add(btnBar, BorderLayout.SOUTH);
        return panel;
    }

    private void loadMyTasks() {
        myTaskModel.setRowCount(0);
        for (Task t : child.getTasks())
            myTaskModel.addRow(new Object[]{
                t.getTitle(), t.getDescription(), t.getPriority().getLabel(),
                t.getDueDate(), t.isCompleted() ? "✓ Tamamlandı" : "● Bekliyor"
            });
    }

    private void markMyTaskDone(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Lütfen bir görev seçin."); return; }
        Task t = child.getTasks().get(row);
        if (t.isCompleted()) { JOptionPane.showMessageDialog(this, "Görev zaten tamamlandı."); return; }
        int earned = child.completeOwnTask(t.getId());
        dm.updateChild(child);
        if (pointsLbl != null) pointsLbl.setText("  " + child.getPoints() + " puan");
        if (earned >= 0) JOptionPane.showMessageDialog(this, "Tebrikler! +" + earned + " puan kazandın.");
    }

    private void showAddTaskDialog() {
        JTextField titleF = new JTextField(), descF = new JTextField();
        JComboBox<String> prioBox = new JComboBox<>(new String[]{"Yüksek","Orta","Düşük"});
        JTextField dateF = new JTextField(LocalDate.now().plusDays(1).toString());
        JPanel form = buildForm(
            new String[]{"Başlık","Açıklama","Öncelik","Bitiş (YYYY-MM-DD)"},
            new JComponent[]{titleF, descF, prioBox, dateF});
        if (JOptionPane.showConfirmDialog(this, form, "Yeni Görev", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            Task.Priority p = switch (prioBox.getSelectedIndex()) { case 0 -> Task.Priority.HIGH; case 1 -> Task.Priority.MEDIUM; default -> Task.Priority.LOW; };
            child.addTask(new Task(DataManager.generateId(), titleF.getText().trim(), descF.getText().trim(), p, LocalDate.parse(dateF.getText().trim())));
            dm.updateChild(child);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Geçersiz tarih formatı!"); }
    }

    // ── Atanan Görevler ───────────────────────────────────────────────────

    private JPanel buildAssignedPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(AppUI.BG);
        panel.setBorder(new EmptyBorder(16, 18, 14, 18));

        JLabel title = new JLabel("Atanan Görevler");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(AppUI.TEXT);

        String[] cols = {"Başlık", "Açıklama", "Öncelik", "Bitiş Tarihi", "Durum"};
        assignedModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(assignedModel);
        AppUI.styleTable(table);
        loadAssigned();

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnBar.setOpaque(false);
        JButton doneBtn = AppUI.btn("✓ Tamamlandı", AppUI.SUCCESS);
        doneBtn.addActionListener(e -> { markAssignedDone(table); loadAssigned(); });
        btnBar.add(doneBtn);

        panel.add(title, BorderLayout.NORTH);
        panel.add(AppUI.scroll(table, null), BorderLayout.CENTER);
        panel.add(btnBar, BorderLayout.SOUTH);
        return panel;
    }

    private void loadAssigned() {
        assignedModel.setRowCount(0);
        for (Task t : child.getAssignedTasks())
            assignedModel.addRow(new Object[]{
                t.getTitle(), t.getDescription(), t.getPriority().getLabel(),
                t.getDueDate(), t.isCompleted() ? "✓ Tamamlandı" : "● Bekliyor"
            });
    }

    private void markAssignedDone(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Lütfen bir görev seçin."); return; }
        Task t = child.getAssignedTasks().get(row);
        if (t.isCompleted()) { JOptionPane.showMessageDialog(this, "Görev zaten tamamlandı."); return; }
        int earned = child.completeAssignedTask(t.getId());
        if (earned >= 0) {
            dm.updateChild(child);
            if (pointsLbl != null) pointsLbl.setText("  " + child.getPoints() + " puan");
            JOptionPane.showMessageDialog(this, "Tebrikler! +" + earned + " puan kazandın.");
        }
    }

    // ── Takvim ────────────────────────────────────────────────────────────

    private JPanel buildCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppUI.BG);
        calendarPanel = new CalendarGridPanel(child.getEvents(), new CalendarGridPanel.EventCallback() {
            @Override public void onAdd() { showAddEventDialog(); }
            @Override public void onDelete(CalendarEvent ev) {
                child.getEvents().remove(ev); dm.updateChild(child);
                calendarPanel.setEvents(child.getEvents());
            }
        });
        panel.add(calendarPanel, BorderLayout.CENTER);
        return panel;
    }

    private void showAddEventDialog() {
        JTextField titleF = new JTextField();
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Toplantı","Randevu","Sınav","Hatırlatıcı","Diğer"});
        JTextField startF = new JTextField(LocalDateTime.now().toString().substring(0, 16));
        JTextField endF   = new JTextField(LocalDateTime.now().plusHours(1).toString().substring(0, 16));
        JPanel form = buildForm(
            new String[]{"Başlık","Tür","Başlangıç (YYYY-MM-DDTHH:MM)","Bitiş (YYYY-MM-DDTHH:MM)"},
            new JComponent[]{titleF, typeBox, startF, endF});
        if (JOptionPane.showConfirmDialog(this, form, "Etkinlik Ekle", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            CalendarEvent.EventType[] types = CalendarEvent.EventType.values();
            child.addEvent(new CalendarEvent(DataManager.generateId(), titleF.getText().trim(), "",
                LocalDateTime.parse(startF.getText().trim()), LocalDateTime.parse(endF.getText().trim()),
                types[typeBox.getSelectedIndex()]));
            dm.updateChild(child);
            calendarPanel.setEvents(child.getEvents());
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Geçersiz tarih formatı!"); }
    }

    // ── Rozetlerim ────────────────────────────────────────────────────────

    private JPanel buildBadgesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(AppUI.BG);
        panel.setBorder(new EmptyBorder(16, 18, 14, 18));

        JLabel title = new JLabel("Rozetlerim  —  " + child.getPoints() + " puan  |  " + child.getCompletedTaskCount() + " görev tamamlandı");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(AppUI.PRIMARY);

        JPanel grid = new JPanel(new GridLayout(0, 4, 10, 10));
        grid.setBackground(AppUI.BG);
        List<Badge> earned = child.getEarnedBadges();
        for (Badge badge : Badge.values()) {
            boolean ok = earned.contains(badge);
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(ok ? new Color(255, 250, 220) : Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ok ? new Color(255, 180, 0) : AppUI.BORDER_CLR),
                new EmptyBorder(10, 8, 10, 8)));
            JLabel lbl = new JLabel("<html><center>" + badge.getLabel() + "<br><small>" +
                (ok ? "✓ Kazanıldı" : "Kilitli") + "</small></center></html>", SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setForeground(ok ? new Color(120, 80, 0) : AppUI.MUTED);
            card.add(lbl, BorderLayout.CENTER);
            grid.add(card);
        }

        panel.add(title, BorderLayout.NORTH);
        panel.add(AppUI.scroll(grid, AppUI.BG), BorderLayout.CENTER);
        return panel;
    }

    // ── Yardımcılar ───────────────────────────────────────────────────────

    /** Etiket-alan çiftlerinden BoxLayout form paneli oluşturur. */
    private JPanel buildForm(String[] labels, JComponent[] fields) {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(8, 8, 8, 8));
        for (int i = 0; i < labels.length; i++) {
            form.add(AppUI.formRow(labels[i], fields[i]));
            if (i < labels.length - 1) form.add(Box.createVerticalStrut(10));
        }
        return form;
    }
}
