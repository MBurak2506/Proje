package gui;

import model.*;
import storage.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final DataManager    dm;
    private final JTextField     usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);

    public LoginFrame(DataManager dm) {
        this.dm = dm;
        setTitle("FamilySync");
        setSize(420, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setIconImages(AppUI.createWindowIconImages());

        // Gri arka plan — GridBagLayout kartı ortalar
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(AppUI.BG);
        add(root);

        // Beyaz kart
        JPanel card = buildCard();
        card.setPreferredSize(new Dimension(310, 400));
        root.add(card);   // GridBagConstraints varsayılanı = ortala

        setVisible(true);
    }

    private JPanel buildCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppUI.BORDER_CLR),
            new EmptyBorder(32, 36, 28, 36)
        ));

        // Başlık
        card.add(label("FamilySync", 24, Font.BOLD, AppUI.PRIMARY));
        card.add(Box.createVerticalStrut(6));
        card.add(label("Hesabınıza giriş yapın", 13, Font.PLAIN, AppUI.MUTED));
        card.add(Box.createVerticalStrut(28));

        // Kullanıcı adı
        card.add(fieldLabel("Kullanıcı Adı"));
        card.add(Box.createVerticalStrut(4));
        styleField(usernameField);
        card.add(usernameField);
        card.add(Box.createVerticalStrut(14));

        // Şifre
        card.add(fieldLabel("Şifre"));
        card.add(Box.createVerticalStrut(4));
        styleField(passwordField);
        card.add(passwordField);
        card.add(Box.createVerticalStrut(22));

        // Giriş butonu
        JButton loginBtn = AppUI.btn("Giriş Yap", AppUI.PRIMARY);
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        loginBtn.addActionListener(e -> login());
        getRootPane().setDefaultButton(loginBtn);
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(20));

        // Demo bilgisi
        JLabel demo = new JLabel(
            "<html><center><font color='#6c757d'>Demo: <b>ebeveyn</b>/1234 &nbsp;|&nbsp; <b>cocuk</b>/1234</font></center></html>",
            SwingConstants.CENTER);
        demo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        demo.setAlignmentX(Component.LEFT_ALIGNMENT);
        demo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        card.add(demo);

        return card;
    }

    // ── Yardımcılar ──────────────────────────────────────────────────────

    private static JLabel label(String text, int size, int style, Color fg) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", style, size));
        l.setForeground(fg);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, size + 10));
        return l;
    }

    private static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(AppUI.MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static void styleField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppUI.BORDER_CLR),
            new EmptyBorder(5, 10, 5, 10)
        ));
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun.", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        User user = dm.login(username, password);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Kullanıcı adı veya şifre hatalı.", "Hata", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            return;
        }
        dispose();
        if (user instanceof Parent p) new ParentDashboard(p, dm);
        else if (user instanceof Child c) new ChildDashboard(c, dm);
    }
}
