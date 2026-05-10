import gui.LoginFrame;
import storage.DataManager;

import javax.swing.*;

/**
 * FamilySync uygulamasının giriş noktası.
 * DataManager başlatılır ve giriş ekranı açılır.
 */
public class Main {
    public static void main(String[] args) {
        // Swing bileşenlerini EDT (Event Dispatch Thread) üzerinde başlat
        SwingUtilities.invokeLater(() -> {
            try {
                // Sistem görünümünü kullan (Windows/macOS/Linux'a uygun)
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Sistem teması alınamazsa Swing varsayılanı kullanılır
                System.err.println("Look and Feel ayarlanamadı: " + e.getMessage());
            }

            // Singleton pattern: tek bir DataManager örneği oluşturulur
            DataManager dataManager = DataManager.getInstance();

            // Giriş ekranını aç
            new LoginFrame(dataManager);
        });
    }
}
