package gui;

import model.*;
import service.GoogleCalendarService;
import storage.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ParentDashboard extends JFrame {

    private final Parent parent;
    private final DataManager dm;

    private CardLayout cardLayout;
    private JPanel contentArea;
    private JButton[] navBtns;

    private DefaultTableModel taskModel;
    private DefaultTableModel childrenModel;
    private DefaultTableModel rewardModel;
    private CalendarGridPanel calendarPanel;

    public ParentDashboard(Parent parent, DataManager dm) {
        this.parent = parent;
        this.dm     = dm;

        setTitle("FamilySync – Ebeveyn Paneli");
        setSize(1060, 680);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(860, 520));
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
        sb.setPreferredSize(new Dimension(200, 0));
        sb.setBorder(new EmptyBorder(24, 0, 16, 0));

        JLabel appLbl = new JLabel("  FamilySync");
        appLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        appLbl.setForeground(Color.WHITE);
        appLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sb.add(appLbl);
        sb.add(Box.createVerticalStrut(4));

        JLabel nameLbl = new JLabel("  " + parent.getFullName());
        nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameLbl.setForeground(new Color(160, 175, 230));
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sb.add(nameLbl);

        JLabel roleLbl = new JLabel("  Ebeveyn");
        roleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roleLbl.setForeground(new Color(120, 140, 200));
        roleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sb.add(roleLbl);
        sb.add(Box.createVerticalStrut(16));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(70, 90, 160));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sb.add(sep);
        sb.add(Box.createVerticalStrut(10));

        String[] labels = {"Görevlerim", "Takvim", "Çocuklarım"};
        String[] cards  = {"tasks", "calendar", "children"};
        navBtns = new JButton[labels.length];
        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            JButton btn = AppUI.navBtn(labels[i]);
            btn.addActionListener(e -> { cardLayout.show(contentArea, cards[idx]); AppUI.setNavActive(navBtns, idx); });
            navBtns[i] = btn;
            sb.add(btn);
        }

        sb.add(Box.createVerticalStrut(16));
        sep = new JSeparator();
        sep.setForeground(new Color(70, 90, 160));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sb.add(sep);
        sb.add(Box.createVerticalStrut(8));

        // Google Takvim butonu
        JButton gcalBtn = AppUI.navBtn("Google Takvim");
        gcalBtn.setForeground(new Color(120, 230, 150));
        gcalBtn.addActionListener(e -> showGoogleCalendarDialog());
        sb.add(gcalBtn);

        sb.add(Box.createGlue());

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
        contentArea.add(buildTasksPanel(),    "tasks");
        contentArea.add(buildCalendarPanel(), "calendar");
        contentArea.add(buildChildrenPanel(), "children");

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(new MatteBorder(0, 1, 0, 0, AppUI.BORDER_CLR));
        wrapper.add(contentArea, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Görevlerim ────────────────────────────────────────────────────────

    private JPanel buildTasksPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(AppUI.BG);
        panel.setBorder(new EmptyBorder(16, 18, 14, 18));

        JLabel title = new JLabel("Görevlerim");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(AppUI.TEXT);

        String[] cols = {"Başlık", "Açıklama", "Öncelik", "Bitiş Tarihi", "Durum"};
        taskModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(taskModel);
        AppUI.styleTable(table);
        loadTasks();

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnBar.setOpaque(false);
        JButton addBtn  = AppUI.btn("+ Yeni Görev",           AppUI.SUCCESS);
        JButton doneBtn = AppUI.btn("✓ Tamamlandı İşaretle",  AppUI.PRIMARY);
        JButton delBtn  = AppUI.btn("✕ Sil",                  AppUI.DANGER);
        addBtn.addActionListener(e  -> { showAddTaskDialog(); loadTasks(); });
        doneBtn.addActionListener(e -> { markTaskDone(table); loadTasks(); });
        delBtn.addActionListener(e  -> { deleteTask(table);   loadTasks(); });
        btnBar.add(addBtn); btnBar.add(doneBtn); btnBar.add(delBtn);

        panel.add(title, BorderLayout.NORTH);
        panel.add(AppUI.scroll(table, null), BorderLayout.CENTER);
        panel.add(btnBar, BorderLayout.SOUTH);
        return panel;
    }

    private void loadTasks() {
        taskModel.setRowCount(0);
        for (Task t : parent.getTasks())
            taskModel.addRow(new Object[]{
                t.getTitle(), t.getDescription(), t.getPriority().getLabel(),
                t.getDueDate(), t.isCompleted() ? "✓ Tamamlandı" : "● Bekliyor"
            });
    }

    private void markTaskDone(JTable t) {
        int row = t.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Lütfen bir görev seçin."); return; }
        Task task = parent.getTasks().get(row);
        if (task.isCompleted()) { JOptionPane.showMessageDialog(this, "Görev zaten tamamlandı."); return; }
        task.setCompleted(true); dm.updateParent(parent);
    }

    private void deleteTask(JTable t) {
        int row = t.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Lütfen bir görev seçin."); return; }
        parent.getTasks().remove(row); dm.updateParent(parent);
    }

    private void showAddTaskDialog() {
        JTextField titleF = new JTextField(), descF = new JTextField();
        JComboBox<String> prioBox = new JComboBox<>(new String[]{"Yüksek","Orta","Düşük"});
        JTextField dateF = new JTextField(LocalDate.now().plusDays(1).toString());
        JPanel form = buildForm(
            new String[]{"Başlık","Açıklama","Öncelik","Bitiş (YYYY-MM-DD)"},
            new JComponent[]{titleF, descF, prioBox, dateF});
        if (JOptionPane.showConfirmDialog(this, form, "Yeni Görev Ekle", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            Task.Priority p = switch (prioBox.getSelectedIndex()) { case 0 -> Task.Priority.HIGH; case 1 -> Task.Priority.MEDIUM; default -> Task.Priority.LOW; };
            parent.addTask(new Task(DataManager.generateId(), titleF.getText().trim(), descF.getText().trim(), p, LocalDate.parse(dateF.getText().trim())));
            dm.updateParent(parent);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Geçersiz tarih formatı!"); }
    }

    // ── Takvim ────────────────────────────────────────────────────────────

    private JPanel buildCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppUI.BG);
        calendarPanel = new CalendarGridPanel(parent.getEvents(), new CalendarGridPanel.EventCallback() {
            @Override public void onAdd() { showAddEventDialog(); }
            @Override public void onDelete(CalendarEvent ev) {
                parent.getEvents().remove(ev); dm.updateParent(parent);
                calendarPanel.setEvents(parent.getEvents());
            }
        });
        panel.add(calendarPanel, BorderLayout.CENTER);
        return panel;
    }

    private void showAddEventDialog() {
        JTextField titleF = new JTextField(), descF = new JTextField();
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Toplantı","Randevu","Sınav","Hatırlatıcı","Diğer"});
        JTextField startF = new JTextField(LocalDateTime.now().toString().substring(0, 16));
        JTextField endF   = new JTextField(LocalDateTime.now().plusHours(1).toString().substring(0, 16));
        JPanel form = buildForm(
            new String[]{"Başlık","Açıklama","Tür","Başlangıç (YYYY-MM-DDTHH:MM)","Bitiş (YYYY-MM-DDTHH:MM)"},
            new JComponent[]{titleF, descF, typeBox, startF, endF});
        if (JOptionPane.showConfirmDialog(this, form, "Etkinlik Ekle", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            CalendarEvent.EventType[] types = CalendarEvent.EventType.values();
            parent.addEvent(new CalendarEvent(DataManager.generateId(), titleF.getText().trim(), descF.getText().trim(),
                LocalDateTime.parse(startF.getText().trim()), LocalDateTime.parse(endF.getText().trim()),
                types[typeBox.getSelectedIndex()]));
            dm.updateParent(parent);
            calendarPanel.setEvents(parent.getEvents());
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Geçersiz tarih formatı!"); }
    }

    // ── Çocuklarım ────────────────────────────────────────────────────────

    private JPanel buildChildrenPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(AppUI.BG);
        panel.setBorder(new EmptyBorder(16, 18, 14, 18));

        // Çocuklar tablosu
        String[] childCols = {"Ad Soyad", "Kullanıcı Adı", "Yaş", "Puan", "Bekleyen Görev"};
        childrenModel = new DefaultTableModel(childCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable childTable = new JTable(childrenModel);
        AppUI.styleTable(childTable);
        loadChildren();

        JPanel childBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        childBtns.setOpaque(false);
        JButton assignBtn  = AppUI.btn("Görev Ata", AppUI.ORANGE);
        JButton refreshBtn = AppUI.btn("Yenile",    AppUI.PRIMARY);
        assignBtn.addActionListener(e  -> { showAssignDialog(); loadChildren(); });
        refreshBtn.addActionListener(e -> loadChildren());
        childBtns.add(assignBtn); childBtns.add(refreshBtn);

        JPanel childSection = new JPanel(new BorderLayout(0, 6));
        childSection.setBackground(AppUI.BG);
        JLabel childTitle = sectionTitle("Çocuklarım");
        childSection.add(childTitle, BorderLayout.NORTH);
        childSection.add(AppUI.scroll(childTable, null), BorderLayout.CENTER);
        childSection.add(childBtns, BorderLayout.SOUTH);

        // Ödül kuralları
        String[] rewardCols = {"Ödül Açıklaması", "Gerekli Puan"};
        rewardModel = new DefaultTableModel(rewardCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable rewardTable = new JTable(rewardModel);
        AppUI.styleTable(rewardTable);
        loadRewards();

        JPanel rewardBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rewardBtns.setOpaque(false);
        JButton addRuleBtn = AppUI.btn("+ Kural Ekle", AppUI.SUCCESS);
        JButton delRuleBtn = AppUI.btn("✕ Sil",         AppUI.DANGER);
        addRuleBtn.addActionListener(e -> { showAddRewardDialog(); loadRewards(); });
        delRuleBtn.addActionListener(e -> {
            int row = rewardTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Bir kural seçin."); return; }
            parent.getRewardRules().remove(row); dm.updateParent(parent); loadRewards();
        });
        rewardBtns.add(addRuleBtn); rewardBtns.add(delRuleBtn);

        JPanel rewardSection = new JPanel(new BorderLayout(0, 6));
        rewardSection.setBackground(AppUI.BG);
        rewardSection.add(sectionTitle("Ödül Kuralları"), BorderLayout.NORTH);
        rewardSection.add(AppUI.scroll(rewardTable, null), BorderLayout.CENTER);
        rewardSection.add(rewardBtns, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, childSection, rewardSection);
        split.setResizeWeight(0.55);
        split.setDividerSize(5);
        split.setBorder(null);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void loadChildren() {
        childrenModel.setRowCount(0);
        for (Child c : dm.getChildrenByParentId(parent.getId()))
            childrenModel.addRow(new Object[]{
                c.getFullName(), c.getUsername(), c.getAge(),
                c.getPoints() + " puan", c.getPendingAssignedTaskCount() + " bekliyor"
            });
    }

    private void loadRewards() {
        rewardModel.setRowCount(0);
        for (RewardRule r : parent.getRewardRules())
            rewardModel.addRow(new Object[]{r.getDescription(), r.getPointThreshold()});
    }

    private void showAssignDialog() {
        List<Child> children = dm.getChildrenByParentId(parent.getId());
        if (children.isEmpty()) { JOptionPane.showMessageDialog(this, "Kayıtlı çocuğunuz yok."); return; }
        JComboBox<String> childBox = new JComboBox<>(children.stream()
            .map(c -> c.getFullName() + " (" + c.getUsername() + ")").toArray(String[]::new));
        JTextField titleF = new JTextField(), descF = new JTextField();
        JComboBox<String> prioBox = new JComboBox<>(new String[]{"Yüksek","Orta","Düşük"});
        JTextField dateF  = new JTextField(LocalDate.now().plusDays(1).toString());
        JPanel form = buildForm(
            new String[]{"Çocuk","Görev Başlığı","Açıklama","Öncelik","Bitiş (YYYY-MM-DD)"},
            new JComponent[]{childBox, titleF, descF, prioBox, dateF});
        if (JOptionPane.showConfirmDialog(this, form, "Görev Ata", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            Task.Priority p = switch (prioBox.getSelectedIndex()) { case 0 -> Task.Priority.HIGH; case 1 -> Task.Priority.MEDIUM; default -> Task.Priority.LOW; };
            Child sel = children.get(childBox.getSelectedIndex());
            Task task = new Task(DataManager.generateId(), titleF.getText().trim(), descF.getText().trim(), p, LocalDate.parse(dateF.getText().trim()));
            parent.assignTaskToChild(task, sel.getId());
            sel.receiveAssignedTask(task);
            dm.updateChild(sel);
            JOptionPane.showMessageDialog(this, "Görev atandı: " + sel.getFullName());
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Geçersiz tarih formatı!"); }
    }

    private void showAddRewardDialog() {
        JTextField descF = new JTextField(), pointF = new JTextField("50");
        JPanel form = buildForm(new String[]{"Ödül Açıklaması","Gerekli Puan"}, new JComponent[]{descF, pointF});
        if (JOptionPane.showConfirmDialog(this, form, "Ödül Kuralı Ekle", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            int pts = Integer.parseInt(pointF.getText().trim());
            if (pts <= 0) throw new NumberFormatException();
            String desc = descF.getText().trim();
            if (desc.isEmpty()) { JOptionPane.showMessageDialog(this, "Açıklama boş olamaz."); return; }
            parent.addRewardRule(new RewardRule(DataManager.generateId(), desc, pts));
            dm.updateParent(parent);
        } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Geçersiz puan değeri!"); }
    }

    // ── Google Takvim ─────────────────────────────────────────────────────

    private void showGoogleCalendarDialog() {
        GoogleCalendarService gcal = GoogleCalendarService.getInstance();
        if (gcal.isAuthenticated()) {
            if (JOptionPane.showConfirmDialog(this, "Google Takvim bağlı. Bağlantıyı kesmek istiyor musunuz?",
                    "Google Takvim", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                gcal.disconnect(); JOptionPane.showMessageDialog(this, "Bağlantı kesildi.");
            }
            return;
        }
        if (!gcal.isConfigured()) {
            JTextField cidF = new JTextField(32), csF = new JTextField(32);
            JPanel form = new JPanel(new GridLayout(2, 2, 6, 6));
            form.add(new JLabel("Client ID:")); form.add(cidF);
            form.add(new JLabel("Client Secret:")); form.add(csF);
            if (JOptionPane.showConfirmDialog(this, form, "Google Takvim", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
            gcal.setCredentials(cidF.getText().trim(), csF.getText().trim());
        }
        JOptionPane.showMessageDialog(this, "Tarayıcı açılıyor — Google hesabınızla yetkilendirin.");
        new Thread(() -> {
            try { gcal.authenticate(); SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Google Takvim bağlandı!")); }
            catch (Exception ex) { SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Bağlantı hatası: " + ex.getMessage())); }
        }).start();
    }

    // ── Yardımcılar ───────────────────────────────────────────────────────

    private JLabel sectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(AppUI.TEXT);
        return lbl;
    }

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
