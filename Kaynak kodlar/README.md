# E-Commerce Spring Boot Application

Bu proje, Spring Boot framework'ü kullanılarak geliştirilmiş kapsamlı bir e-ticaret web uygulamasıdır. Kullanıcıların ürün inceleyebileceği, sepete ekleyebileceği, sipariş verebileceği ve hesap yönetimi yapabileceği modern bir platform sunar.

## 📋 Proje Özeti

Bu e-ticaret uygulaması aşağıdaki temel özellikleri içermektedir:

### 🔐 Kullanıcı Yönetimi ve Güvenlik
- **JWT tabanlı kimlik doğrulama**: Güvenli oturum yönetimi
- **Kullanıcı kayıt sistemi**: E-posta doğrulama ile hesap oluşturma
- **Profil yönetimi**: Kullanıcı bilgilerini güncelleme
- **Spring Security**: Güvenli erişim kontrolü

### 🛍️ E-ticaret Özellikleri
- **Ürün katalog sistemi**: Kategorilere göre ürün listeleme
- **Ürün detay sayfaları**: Ürün bilgileri ve fotoğrafları
- **Alışveriş sepeti**: Ürün ekleme/çıkarma, miktar güncelleme
- **Favoriler sistemi**: Beğenilen ürünleri kaydetme
- **Sipariş yönetimi**: Checkout işlemi ve sipariş geçmişi
- **Ürün değerlendirme**: Kullanıcı yorumları ve puanlama sistemi

### 📊 İleri Düzey Özellikler
- **İndirim sistemi**: Kategori bazlı indirimler
- **Kullanıcı etkileşim takibi**: Kullanıcı davranışlarını analiz etme
- **E-posta servisi**: Doğrulama ve bildirim e-postaları
- **Sipariş geçmişi**: Geçmiş siparişleri görüntüleme

## 🛠️ Teknoloji Stack

### Backend
- **Spring Boot 3.4.4**: Ana framework
- **Spring Security**: Güvenlik ve kimlik doğrulama
- **Spring Data JPA**: Veritabanı işlemleri
- **JWT (jsonwebtoken)**: Token tabanlı kimlik doğrulama
- **Spring Mail**: E-posta gönderimi
- **Lombok**: Code generation için

### Frontend
- **Thymeleaf**: Server-side template engine
- **HTML/CSS/JavaScript**: Frontend teknolojileri
- **Bootstrap**: Responsive tasarım

### Veritabanı
- **MySQL**: Ana veritabanı
- **Hibernate**: ORM (Object-Relational Mapping)

### Build Tool
- **Maven**: Proje yönetimi ve build

## 🚀 Kurulum ve Çalıştırma

### Gereksinimler
- Java 17 veya üzeri
- Maven 3.6+
- MySQL 8.0+
- Git

### 1. Projeyi İndirin
```bash
git clone <repository-url>
cd Projectt
```

### 2. Veritabanı Kurulumu
MySQL'de yeni bir veritabanı oluşturun:
```sql
CREATE DATABASE e_commerce;
```

### 3. Yapılandırma
`src/main/resources/application.properties` dosyasındaki veritabanı bağlantı bilgilerini güncelleyin:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/e_commerce
spring.datasource.username=your_username
spring.datasource.password=your_password
```

E-posta servisi için SMTP ayarlarını yapılandırın:
```properties
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
```

### 4. Uygulamayı Çalıştırın

#### Maven Wrapper ile:
```bash
./mvnw spring-boot:run
```

#### Windows için:
```bash  
mvnw.cmd spring-boot:run
```

#### Maven yüklü ise:
```bash
mvn spring-boot:run
```

### 5. Uygulamaya Erişim
Uygulama başlatıldıktan sonra tarayıcınızda şu adresi ziyaret edin:
```
http://localhost:8080
```

## 📁 Proje Yapısı

```
src/
├── main/
│   ├── java/com/example/e_commerce/
│   │   ├── controller/          # REST Controller sınıfları
│   │   ├── entity/              # JPA Entity sınıfları
│   │   ├── repository/          # Repository interface'leri
│   │   ├── service/             # Business logic sınıfları
│   │   ├── dto/                 # Data Transfer Object'leri
│   │   ├── security/            # Güvenlik yapılandırmaları
│   │   ├── enums/               # Enum sınıfları
│   │   └── ECommerceApplication.java
│   └── resources/
│       ├── application.properties
│       └── templates/           # Thymeleaf template'leri
└── test/                        # Test sınıfları
```

## 🔧 API Endpoints

### Kimlik Doğrulama
- `POST /login` - Kullanıcı girişi
- `POST /register` - Kullanıcı kaydı
- `POST /logout` - Kullanıcı çıkışı

### Ürün Yönetimi
- `GET /products` - Ürün listeleme
- `GET /products/{id}` - Ürün detayları
- `GET /categories` - Kategori listesi

### Sepet İşlemleri
- `GET /cart` - Sepet görüntüleme
- `POST /cart/add` - Sepete ürün ekleme
- `PUT /cart/update` - Sepet güncelleme
- `DELETE /cart/remove` - Sepetten ürün çıkarma

### Sipariş İşlemleri
- `POST /checkout` - Sipariş verme
- `GET /orders` - Sipariş geçmişi

## 🤝 Katkıda Bulunanlar

Bu proje aşağıdaki geliştiriciler tarafından hazırlanmıştır:

- **170421008 - Bora KIRARSLAN**
- **170421013 - Mehmet Ali GÜLYURDU**

## 📄 Lisans

Bu proje, Marmara Üniversitesi Bilgisayar Mühendisliği bölümüne ait bitirme projesidir.

- Not:  spring.mail.username= (e-posta adresi)
        spring.mail.password= (e-posta adresinin uygulama anahtarı)