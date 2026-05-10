# 🏠 FamilySync

Modern aile yapısındaki yoğun tempoyu düzenlemek için geliştirilmiş Java Swing tabanlı masaüstü uygulaması.

---

## 📁 Proje Yapısı

```
FamilySync/
├── src/
│   ├── model/
│   │   ├── User.java           ← Soyut temel sınıf (Inheritance)
│   │   ├── Parent.java         ← Ebeveyn sınıfı
│   │   ├── Child.java          ← Çocuk sınıfı
│   │   ├── Task.java           ← Görev modeli
│   │   └── CalendarEvent.java  ← Takvim etkinliği modeli
│   ├── storage/
│   │   └── DataManager.java    ← JSON okuma/yazma (Gson)
│   ├── gui/
│   │   ├── LoginFrame.java     ← Giriş ekranı
│   │   ├── ParentDashboard.java← Ebeveyn arayüzü
│   │   └── ChildDashboard.java ← Çocuk arayüzü
│   └── Main.java               ← Giriş noktası
├── data/
│   └── users.json              ← Otomatik oluşturulur
├── lib/
│   └── gson-2.10.1.jar         ← Manuel indirilmeli (bkz. kurulum)
└── .vscode/
    ├── launch.json
    └── settings.json
```

---

## Kurulum (VS Code)

### 1. Gereksinimler
- **JDK 17+** — https://adoptium.net
- **VS Code** + **Extension Pack for Java** (Microsoft)

### 2. VS Code'da Aç
```
File → Open Folder → FamilySync klasörünü seç
```

### 3. Çalıştır
- `F5` tuşuna bas → **"FamilySync - Çalıştır"** konfigürasyonunu seç
- Veya `src/Main.java` dosyasını aç → sağ üstteki ▶ butonuna tıkla

---

##  Demo Giriş Bilgileri

| Rol | Kullanıcı Adı | Şifre |
|-----|--------------|-------|
| Ebeveyn | `ebeveyn` | `1234` |
| Çocuk | `cocuk` | `1234` |

> İlk çalıştırmada `data/users.json` otomatik oluşturulur.

---

##  OOP Prensipleri

| Prensip | Uygulama |
|---------|----------|
| **Kalıtım** | `User` soyut sınıfı → `Parent` ve `Child` türetiyor |
| **Polimorfizm** | `getDashboardTitle()` ve `addTask()` override ediliyor |
| **Kapsülleme** | Tüm alanlar `private`, getter/setter ile erişim |
| **Soyutlama** | `User` abstract sınıfı doğrudan örneklenemiyor |

---

##  Özellikler

### Ebeveyn
- Kendi görevlerini ekle/sil
-  Takvim etkinliği ekle
- Çocuklara görev ata
- Çocukların görev durumunu izle

### Çocuk
-  Kendi görevlerini ekle
-  Ebeveynden gelen görevleri gör
- Görevleri tamamlandı işaretle
-  Takvim etkinliği ekle

---

## Veri Kalıcılığı

Tüm veriler `data/users.json` dosyasına Gson ile JSON formatında kaydedilir.
Uygulama her kapanışta otomatik kaydeder, açılışta otomatik yükler.
