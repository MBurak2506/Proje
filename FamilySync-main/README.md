# FamilySync

Modern aile yapısındaki yoğun tempoyu düzenlemek için geliştirilmiş Java Swing tabanlı masaüstü uygulaması.

---

## Proje Yapısı

```
FamilySync/
├── src/
│   ├── model/
│   │   ├── User.java                 ← Soyut temel sınıf (Inheritance)
│   │   ├── Parent.java               ← Ebeveyn sınıfı
│   │   ├── Child.java                ← Çocuk sınıfı
│   │   ├── Task.java                 ← Görev modeli (öncelik, bitiş tarihi)
│   │   ├── CalendarEvent.java        ← Takvim etkinliği modeli
│   │   ├── Badge.java                ← Rozet modeli (gamification)
│   │   ├── RewardRule.java           ← Ödül kuralı modeli
│   │   └── IGamificationManager.java ← Gamification arayüzü (Abstraction)
│   ├── service/
│   │   └── GoogleCalendarService.java ← Google Calendar REST API + OAuth2
│   ├── storage/
│   │   └── DataManager.java          ← El yazımı JSON okuma/yazma (kütüphane yok)
│   ├── gui/
│   │   ├── AppUI.java                ← Ortak UI yardımcıları, programatik ikonlar
│   │   ├── CalendarGridPanel.java    ← Takvim grid bileşeni
│   │   ├── LoginFrame.java           ← Giriş ekranı
│   │   ├── ParentDashboard.java      ← Ebeveyn arayüzü
│   │   └── ChildDashboard.java       ← Çocuk arayüzü
│   └── Main.java                     ← Giriş noktası
└── data/
    └── users.json                    ← Otomatik oluşturulur
```

---

## Kurulum (VS Code)

### Gereksinimler
- **JDK 17+** — https://adoptium.net
- **VS Code** + **Extension Pack for Java** (Microsoft)

### Çalıştırma
1. `File → Open Folder → FamilySync klasörünü seç`
2. `F5` veya `src/Main.java` → sağ üstteki ▶ butonuna tıkla

> Harici kütüphane gerekmez. Tüm JSON işlemleri elle yazılmış parser ile yapılır.

---

## Demo Giriş Bilgileri

| Rol | Kullanıcı Adı | Şifre |
|-----|--------------|-------|
| Ebeveyn | `ebeveyn` | `1234` |
| Çocuk | `cocuk` | `1234` |

> İlk çalıştırmada `data/users.json` otomatik oluşturulur.

---

## Özellikler

### Ebeveyn Paneli
- Görev ekleme, silme, tamamlandı işaretleme
- Çocuklara görev atama
- Takvim etkinliği ekleme/silme
- Çocukların puan ve görev durumunu izleme
- Ödül kuralı tanımlama
- Google Calendar iki yönlü senkronizasyon

### Çocuk Paneli
- Kendi görevlerini yönetme
- Ebeveynden gelen görevleri görme ve tamamlama
- Puan ve rozet sistemi (gamification)
- Takvim etkinliği ekleme/silme
- Google Calendar senkronizasyonu

---

## Google Calendar Entegrasyonu

Sidebar'daki **Google Takvim** butonu ile OAuth2 akışı başlatılır:

1. Google Cloud Console'da proje oluştur
2. Google Calendar API'yi etkinleştir
3. OAuth 2.0 İstemci Kimliği (Masaüstü Uygulaması) oluştur
4. Client ID ve Secret'i uygulamaya gir
5. Tarayıcıda Google hesabıyla yetkilendir

Bağlantı kurulduktan sonra eklenen görev ve etkinlikler otomatik olarak Google Takvim'e senkronize edilir; Google Takvim'deki etkinlikler de uygulamaya çekilir.

---

## OOP Prensipleri

| Prensip | Uygulama |
|---------|----------|
| **Kalıtım** | `User` soyut sınıfı → `Parent` ve `Child` |
| **Polimorfizm** | `getDashboardTitle()`, `addTask()` override |
| **Kapsülleme** | Tüm alanlar `private`, getter/setter ile erişim |
| **Soyutlama** | `IGamificationManager` arayüzü, `User` abstract sınıfı |
| **Singleton** | `DataManager`, `GoogleCalendarService` |

---

## Veri Kalıcılığı

Tüm veriler `data/users.json` dosyasına kaydedilir. Harici JSON kütüphanesi kullanılmaz; okuma/yazma işlemleri `DataManager` içindeki el yazımı parser ile yapılır.
