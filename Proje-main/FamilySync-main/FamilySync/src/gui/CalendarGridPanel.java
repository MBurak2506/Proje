package gui;

import model.CalendarEvent;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;

public class CalendarGridPanel extends JPanel {

    public interface EventCallback {
        void onAdd();
        void onDelete(CalendarEvent event);
    }

    private static final Color PRIMARY   = new Color(67, 97, 238);
    private static final Color TODAY_BG  = new Color(67, 97, 238);
    private static final Color SEL_BG    = new Color(220, 228, 255);
    private static final Color HEADER_FG = new Color(100, 110, 130);
    private static final Color TEXT      = new Color(33, 37, 41);
    private static final Color MUTED     = new Color(160, 170, 190);
    private static final Color DANGER    = new Color(220, 53, 69);

    private static final Color[] EVENT_COLORS = {
        new Color(67, 97, 238),
        new Color(40, 167, 69),
        new Color(220, 53, 69),
        new Color(255, 152, 0),
        new Color(108, 117, 130)
    };

    private static final String[] DAY_HEADERS = {"Pzt", "Sal", "Car", "Per", "Cum", "Cmt", "Paz"};

    private final EventCallback callback;
    private List<CalendarEvent> events;

    private YearMonth currentMonth;
    private LocalDate selectedDate;

    private JLabel monthLabel;
    private JPanel gridPanel;
    private JLabel selectedDayLabel;
    private DefaultListModel<CalendarEvent> eventListModel;
    private JList<CalendarEvent> eventJList;

    public CalendarGridPanel(List<CalendarEvent> events, EventCallback callback) {
        this.events       = new ArrayList<>(events);
        this.callback     = callback;
        this.currentMonth = YearMonth.now();
        this.selectedDate = LocalDate.now();
        buildUI();
    }

    public void setEvents(List<CalendarEvent> events) {
        this.events = new ArrayList<>(events);
        rebuildGrid();
        showDayEvents(selectedDate);
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        add(buildNavBar(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
    }

    // Nav bar with month navigation and add button
    private JPanel buildNavBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(220, 225, 240)),
            new EmptyBorder(10, 16, 10, 16)
        ));

        JPanel navLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        navLeft.setOpaque(false);

        JButton prevBtn = navBtn("‹");
        JButton nextBtn = navBtn("›");
        monthLabel = new JLabel();
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        monthLabel.setForeground(TEXT);
        updateMonthLabel();

        prevBtn.addActionListener(e -> { currentMonth = currentMonth.minusMonths(1); updateMonthLabel(); rebuildGrid(); });
        nextBtn.addActionListener(e -> { currentMonth = currentMonth.plusMonths(1); updateMonthLabel(); rebuildGrid(); });

        navLeft.add(prevBtn);
        navLeft.add(monthLabel);
        navLeft.add(nextBtn);

        JButton addBtn = styledBtn("+ Yeni Etkinlik", PRIMARY);
        addBtn.addActionListener(e -> callback.onAdd());

        bar.add(navLeft, BorderLayout.WEST);
        bar.add(addBtn,  BorderLayout.EAST);
        return bar;
    }

    private JSplitPane buildCenter() {
        gridPanel = new JPanel();
        gridPanel.setBackground(Color.WHITE);
        buildGridPanel();

        JScrollPane gridScroll = new JScrollPane(gridPanel);
        gridScroll.setBorder(null);
        gridScroll.getViewport().setBackground(Color.WHITE);
        gridScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel eventDetail = buildEventDetailPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, gridScroll, eventDetail);
        split.setResizeWeight(0.65);
        split.setDividerSize(1);
        split.setBorder(null);
        split.setBackground(Color.WHITE);
        return split;
    }

    // Builds the 6x7 day grid for currentMonth
    private void buildGridPanel() {
        gridPanel.removeAll();
        gridPanel.setLayout(new BorderLayout());

        // Day-of-week header row
        JPanel header = new JPanel(new GridLayout(1, 7));
        header.setBackground(new Color(248, 249, 255));
        header.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 225, 240)));
        for (int i = 0; i < 7; i++) {
            JLabel lbl = new JLabel(DAY_HEADERS[i], SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setForeground(i >= 5 ? new Color(200, 80, 80) : HEADER_FG);
            lbl.setBorder(new EmptyBorder(7, 0, 7, 0));
            header.add(lbl);
        }

        // Day cells (6 weeks x 7 days = 42 cells)
        JPanel days = new JPanel(new GridLayout(6, 7, 1, 1));
        days.setBackground(new Color(225, 228, 240));

        LocalDate first       = currentMonth.atDay(1);
        int       startOffset = first.getDayOfWeek().getValue() - 1;
        int       daysInMonth = currentMonth.lengthOfMonth();
        LocalDate today       = LocalDate.now();
        LocalDate prevEnd     = currentMonth.minusMonths(1).atEndOfMonth();

        for (int i = 0; i < 42; i++) {
            LocalDate date;
            boolean inMonth;
            if (i < startOffset) {
                date    = prevEnd.minusDays(startOffset - i - 1);
                inMonth = false;
            } else if (i - startOffset < daysInMonth) {
                date    = currentMonth.atDay(i - startOffset + 1);
                inMonth = true;
            } else {
                date    = currentMonth.plusMonths(1).atDay(i - startOffset - daysInMonth + 1);
                inMonth = false;
            }
            days.add(buildDayCell(date, inMonth, date.equals(today)));
        }

        gridPanel.add(header, BorderLayout.NORTH);
        gridPanel.add(days,   BorderLayout.CENTER);
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel buildDayCell(LocalDate date, boolean inMonth, boolean isToday) {
        boolean isSelected = date.equals(selectedDate);
        List<CalendarEvent> dayEvs = getEventsForDate(date);

        JPanel cell = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(isToday ? TODAY_BG : isSelected ? SEL_BG : Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        cell.setOpaque(false);
        cell.setPreferredSize(new Dimension(0, 62));
        cell.setBorder(new EmptyBorder(5, 7, 5, 7));

        JLabel num = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.RIGHT);
        num.setFont(new Font("Segoe UI", isToday ? Font.BOLD : Font.PLAIN, 13));
        num.setForeground(isToday                              ? Color.WHITE
                        : !inMonth                            ? MUTED
                        : date.getDayOfWeek().getValue() >= 6 ? new Color(200, 80, 80)
                        : TEXT);

        JPanel dots = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 1));
        dots.setOpaque(false);
        for (int i = 0; i < Math.min(dayEvs.size(), 3); i++) {
            dots.add(dot(isToday ? new Color(255, 255, 255, 160) : eventColor(dayEvs.get(i).getEventType())));
        }
        if (dayEvs.size() > 3) {
            JLabel more = new JLabel("+" + (dayEvs.size() - 3));
            more.setFont(new Font("Segoe UI", Font.BOLD, 8));
            more.setForeground(isToday ? new Color(255, 255, 255, 160) : MUTED);
            dots.add(more);
        }

        cell.add(num,  BorderLayout.NORTH);
        cell.add(dots, BorderLayout.SOUTH);
        cell.setCursor(new Cursor(Cursor.HAND_CURSOR));

        cell.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                selectedDate = date;
                rebuildGrid();
                showDayEvents(date);
            }
            @Override public void mouseEntered(MouseEvent e) {
                if (!isToday && !isSelected) { cell.setBackground(new Color(235, 239, 255)); cell.repaint(); }
            }
            @Override public void mouseExited(MouseEvent e) { cell.repaint(); }
        });
        return cell;
    }

    private JPanel dot(Color c) {
        JPanel d = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.fillOval(0, 1, 6, 6);
                g2.dispose();
            }
        };
        d.setOpaque(false);
        d.setPreferredSize(new Dimension(7, 8));
        return d;
    }

    private void rebuildGrid() { buildGridPanel(); }

    // Right panel: shows events for the selected day
    private JPanel buildEventDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new MatteBorder(0, 1, 0, 0, new Color(220, 225, 240)));

        selectedDayLabel = new JLabel(" ");
        selectedDayLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        selectedDayLabel.setForeground(TEXT);

        JPanel dh = new JPanel(new BorderLayout());
        dh.setBackground(new Color(248, 249, 255));
        dh.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(220, 225, 240)),
            new EmptyBorder(10, 14, 10, 14)
        ));
        dh.add(selectedDayLabel);

        eventListModel = new DefaultListModel<>();
        eventJList     = new JList<>(eventListModel);
        eventJList.setBackground(Color.WHITE);
        eventJList.setBorder(null);
        eventJList.setFixedCellHeight(60);
        eventJList.setCellRenderer(new EventCellRenderer());
        eventJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(eventJList);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(new MatteBorder(1, 0, 0, 0, new Color(220, 225, 240)));
        JButton del = styledBtn("Sil", DANGER);
        del.addActionListener(e -> {
            CalendarEvent sel = eventJList.getSelectedValue();
            if (sel != null) callback.onDelete(sel);
        });
        bottom.add(del);

        panel.add(dh,     BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        showDayEvents(LocalDate.now());
        return panel;
    }

    void showDayEvents(LocalDate date) {
        if (selectedDayLabel == null || eventListModel == null) return;
        String fmt = date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("tr-TR")));
        selectedDayLabel.setText(fmt);
        eventListModel.clear();
        getEventsForDate(date).forEach(eventListModel::addElement);
    }

    private List<CalendarEvent> getEventsForDate(LocalDate date) {
        List<CalendarEvent> result = new ArrayList<>();
        for (CalendarEvent ev : events)
            if (ev.getStartTime().toLocalDate().equals(date)) result.add(ev);
        result.sort(Comparator.comparing(CalendarEvent::getStartTime));
        return result;
    }

    private Color eventColor(CalendarEvent.EventType type) {
        return switch (type) {
            case MEETING     -> EVENT_COLORS[0];
            case APPOINTMENT -> EVENT_COLORS[1];
            case EXAM        -> EVENT_COLORS[2];
            case REMINDER    -> EVENT_COLORS[3];
            default          -> EVENT_COLORS[4];
        };
    }

    private void updateMonthLabel() {
        String m = currentMonth.getMonth()
            .getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("tr-TR"));
        m = Character.toUpperCase(m.charAt(0)) + m.substring(1);
        monthLabel.setText("  " + m + "  " + currentMonth.getYear() + "  ");
    }

    private JButton navBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btn.setForeground(HEADER_FG);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(32, 28));
        return btn;
    }

    private JButton styledBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        return btn;
    }

    // Renders each event row in the detail panel
    private class EventCellRenderer implements ListCellRenderer<CalendarEvent> {
        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");

        @Override
        public Component getListCellRendererComponent(JList<? extends CalendarEvent> list,
                CalendarEvent ev, int idx, boolean sel, boolean foc) {
            JPanel cell = new JPanel(new BorderLayout(8, 0));
            cell.setBackground(sel ? SEL_BG : Color.WHITE);
            cell.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(240, 243, 250)),
                new EmptyBorder(8, 12, 8, 12)
            ));

            Color strip = eventColor(ev.getEventType());
            JPanel sp = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(strip);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                    g2.dispose();
                }
            };
            sp.setOpaque(false);
            sp.setPreferredSize(new Dimension(4, 0));

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            String typeStr = switch (ev.getEventType()) {
                case MEETING     -> "Toplanti";
                case APPOINTMENT -> "Randevu";
                case EXAM        -> "Sinav";
                case REMINDER    -> "Hatirlatici";
                default          -> "Diger";
            };

            JLabel title = new JLabel(ev.getTitle());
            title.setFont(new Font("Segoe UI", Font.BOLD, 12));
            title.setForeground(TEXT);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel sub = new JLabel(ev.getStartTime().format(FMT) + " - " + ev.getEndTime().format(FMT) + "   " + typeStr);
            sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            sub.setForeground(MUTED);
            sub.setAlignmentX(Component.LEFT_ALIGNMENT);

            textPanel.add(title);
            textPanel.add(Box.createVerticalStrut(2));
            textPanel.add(sub);

            cell.add(sp,        BorderLayout.WEST);
            cell.add(textPanel, BorderLayout.CENTER);
            return cell;
        }
    }
}
