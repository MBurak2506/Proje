package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/** Ortak renkler, font ve widget yardımcıları. */
public class AppUI {

    // Renk paleti
    public static final Color PRIMARY    = new Color(67, 97, 238);
    public static final Color SUCCESS    = new Color(40, 167, 69);
    public static final Color DANGER     = new Color(220, 53, 69);
    public static final Color ORANGE     = new Color(255, 152, 0);
    public static final Color BG         = new Color(245, 247, 252);
    public static final Color HEADER_BG  = new Color(36, 48, 110);
    public static final Color TEXT       = new Color(33, 37, 41);
    public static final Color MUTED      = new Color(108, 117, 130);
    public static final Color CARD_BG    = Color.WHITE;
    public static final Color BORDER_CLR = new Color(220, 225, 240);

    // ── Pencere ikonu ────────────────────────────────────────────────────

    public static List<Image> createWindowIconImages() {
        return List.of(drawIcon(16), drawIcon(32), drawIcon(48));
    }

    private static Image drawIcon(int s) {
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(PRIMARY);
        g.fillOval(0, 0, s, s);
        g.setColor(Color.WHITE);
        int p = s / 7;
        g.fillPolygon(new int[]{s/2, p, s-p}, new int[]{p, s/2+1, s/2+1}, 3);
        int bx = p + s/8;
        g.fillRect(bx, s/2+1, s - 2*bx, s/2 - p);
        g.setColor(PRIMARY);
        int dw = s/5, dh = s/4;
        g.fillRect(s/2 - dw/2, s - p - dh, dw, dh);
        g.dispose();
        return img;
    }

    // ── Widget fabrikası ─────────────────────────────────────────────────

    /** Renkli, beyaz yazılı düz buton. */
    public static JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(6, 14, 6, 14));
        return b;
    }

    /** Sidebar sol menü butonu (tam genişlik, sol hizalı). */
    public static JButton navBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(HEADER_BG);
        b.setForeground(new Color(200, 210, 255));
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return b;
    }

    /** navBtn aktif/pasif durumunu günceller. */
    public static void setNavActive(JButton[] btns, int activeIdx) {
        for (int i = 0; i < btns.length; i++) {
            boolean active = i == activeIdx;
            btns[i].setBackground(active ? new Color(80, 100, 200) : HEADER_BG);
            btns[i].setForeground(active ? Color.WHITE : new Color(200, 210, 255));
            btns[i].setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
        }
    }

    /** JTable'a modern stil uygular (font, satır yüksekliği, başlık rengi). */
    public static void styleTable(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(28);
        t.setSelectionBackground(new Color(210, 220, 255));
        t.setSelectionForeground(TEXT);
        t.setGridColor(BORDER_CLR);
        t.setShowGrid(true);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(PRIMARY);
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setReorderingAllowed(false);
    }

    /** Başlık çubuğu — koyu arka plan, beyaz metin. */
    public static JPanel headerBar(String title) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(HEADER_BG);
        bar.setBorder(new EmptyBorder(10, 16, 10, 16));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(Color.WHITE);
        bar.add(lbl, BorderLayout.WEST);
        return bar;
    }

    /**
     * Etiket + alan içeren form satırı.
     * BoxLayout Y ile uyumlu: row'a setAlignmentX + setMaximumSize uygulanır.
     */
    public static JPanel formRow(String label, JComponent field) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        field.setPreferredSize(new Dimension(260, 32));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (field instanceof JTextField tf)
            tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR),
                new EmptyBorder(4, 8, 4, 8)));
        else if (field instanceof JComboBox<?> cb)
            cb.setBackground(Color.WHITE);

        row.add(lbl);
        row.add(Box.createVerticalStrut(3));
        row.add(field);
        return row;
    }

    /** Scroll pane — ince border, beyaz arka plan. */
    public static JScrollPane scroll(JComponent c, Color bg) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        sp.getViewport().setBackground(bg == null ? CARD_BG : bg);
        return sp;
    }
}
