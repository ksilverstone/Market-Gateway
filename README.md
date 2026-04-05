# Modern Yazılım Geliştirme: Mikroservis API Gateway (Dispatcher) Projesi

**Proje Ekibi:** Kerem Emre Gümüştaş - Eren Ceylan  
**Tarih:** 5 Nisan 2026

## 1. Proje Amacı ve Kapsamı
Bu projenin amacı, dış dünyaya tamamen kapalı uçtan-uca izole bir Mikroservis mimarisi geliştirmek; bu servisler arasındaki trafik yönetimini modüler, loglanabilir ve güvenli bir **Dispatcher (API Gateway)** üzerinden geçirmektir.

Sistemde Dispatcher birimi **TDD (Test-Driven Development)** yaklaşımları gözetilerek inşa edilmiş olup; Auth, User ve Product olmak üzere tam 3 adet bağımsız mikroservisin haberleşmesi JSON ve RESTful konseptleri (Richardson Maturity Model - RMM) kullanılarak kodlanmıştır.

## 2. Richardson Olgunluk Modeli (RMM) ve RESTful Kavramları
Projeye özel geliştirilen mikroservislerdeki API tasarımlarında **RMM Seviye 2 standartları** titizlikle uygulanmıştır. 
Klasik URL yapılarında yapılan eylemleri belirten `.../deleteUser` veya `.../addProduct` formundaki isimlerden (Verb) kesinlikle kaçınılmıştır. Zira Restful'un kalbi Kaynaklara (Resources) odaklanmaktır.
Sistemde sadece `/api/users` ve `/api/products` şeklinde URL kaynakları bulunmaktadır. İlgili CRUD eylemleri doğru **HTTP Metotları (GET, POST, PUT, DELETE)** çağrılarak yapılmış ve operasyon sonucuna göre anlamlandırılmış durum kodları (201 Created, 204 No Content, 404 Not Found vb.) döndürülmüştür. 

## 3. Sistem Mimarisi ve Ağ İzolasyonu
Projemizde güvenlik esastır. Dispatcher dışındaki hiçbir mikroservis dışarı (host) port açmaz. Servislerin Dispatcher dışından gelen istekleri reddetmesini garantileyen "Network Isolation" prensibi tam uyumlulukla hayata geçirilmiştir.

```mermaid
graph TD
    Client((Sanal / Gerçek Kullanıcı)) -->|HTTP 8080| Dispatcher[Dispatcher API Gateway]
    
    subgraph Dışarıya Tamamen Kapalı 'gateway_net' Ağı
        Dispatcher -->|Proxy: Yönlendir| Auth[Auth Service :8081]
        Dispatcher -->|Proxy: JWT Onaylıysa| UserSvc[User Service :8082]
        Dispatcher -->|Proxy: JWT Onaylıysa| ProdSvc[Product Service :8083]
        
        Prometheus[Prometheus] -.->|/actuator Kazıması| Dispatcher
        Grafana[Grafana] -.->|Analiz ve Oku| Prometheus
    end

    Dispatcher -->|İzole Ağ| Redis[(Dispatcher JWT & Önbellek Redis)]
    Auth -->|İzole Ağ| MongoAuth[(Auth DB Mongo)]
    UserSvc -->|İzole Ağ| MongoUser[(User DB Mongo)]
    ProdSvc -->|İzole Ağ| MongoProd[(Product DB Mongo)]
```

## 4. Yetkilendirme (JWT) ve İstek Akışı (Sequence Diagram)
Sistemdeki tüm Auth JWT doğrulama yükü ve trafik loglama mekanizması (SLF4J kullanılarak) tamamen Dispatcher Service'in Filter/Interceptor katmanına bindirilmiş, mikroservislerin sadece kendi iş bağlamlarına konsantre olmaları sağlanmıştır (Spagetti mimariden kaçış). 

Aşağıdaki süreçte bu JWT akışı yer almaktadır:

```mermaid
sequenceDiagram
    participant User as Client/Locust
    participant Gateway as Dispatcher (Log/Auth)
    participant Auth as Auth Mikroservis
    participant Svc as User/Product Servis

    Note over User, Gateway: Adım 1: Login Olma
    User->>Gateway: POST /api/auth/login
    Gateway-->>Auth: İstek İletilir (Proxy)
    Auth-->>Gateway: Başarılı & JSON Web Token üretimi
    Gateway-->>User: HTTP 200 OK Token Teslimi

    Note over User, Gateway: Adım 2: Güvenlik Testi
    User->>Gateway: GET /api/users (Header: Bearer Token)
    Gateway->>Gateway: Interceptor JWT imzasını çözer
    alt Geçersiz JWT / Süresi Dolmuş
        Gateway-->>User: HTTP 401 Unauthorized (Dışarı Atılır)
    else Geçerli JWT (Trafik Geçişi Verilir)
        Gateway->>Svc: Doğrulanmış İsteği İlet
        Svc-->>Gateway: HTTP 200 (JSON Verisi)
        Gateway->>Gateway: İşlem ve Gecikme Süresini Logla
        Gateway-->>User: Sonucu Client'a Aktar
    end
```

## 5. TDD Aşaması (Red-Green-Refactor)
Dispatcher Service geliştirilirken TDD prensipleri asla çiğnenmemiştir. Önce başarısız (Fail) olması beklenen test sınıfları kodlanmış ve mock URL'ler yaratılmıştır. 

<div align="center">
  <img src="images/TDD_Red_Phase_404.png" alt="TDD Red Phase (Fail)"/>
  <p><i>TDD Red Phase: Test kodlarının çalışıp, henüz fonkisyon yazılmadığı için 404/Fail durumuna düşme aşaması</i></p>
</div>

<div align="center">
  <img src="images/TDD_Green_Phase.png" alt="TDD Green Phase (Success)"/>
  <p><i>TDD Green Phase: Interceptor ve Controller kodlanıp feyk-token eklenmesiyle başarı aşamasına (Success) geçiş</i></p>
</div>

Ardından Dispatcher Controller kodlanarak servis yönlendirmeleri yaratılmış, böylece test başarıya (Green Phase) dönüştürülmüştür. Kod refactor edilerek OOP'ye tam uyumlu modüler helper metotlar yazılmıştır. Proje commit zaman damgaları incelendiğinde bu disiplinin sırası görülebilmektedir. 

## 6. Yük Performans Testleri (Locust)
Sistemin yoğun isteklere karşı direncini test edebilmek amacıyla Locust ile Python tabanlı senkron yük testleri gerçekleştirilmiştir. Yazılan script öncelikle dinamik bir token alıp ardından User ve Product uç noktalarını ablukaya (eş zamanlı vuruşa) almaktadır.
Testler esnasında Grafana ekranından Dispatcher gecikme payları anlık (Real-time) monitor edilmiştir. 
* *Eşzamanlı Yük Performans Sonuçları (Locust & Grafana İstatistikleri):*

<div align="center">
  <img src="images/locust.png" alt="Locust RPS ve İstatistik Grafiği"/>
</div>

<br>

<div align="center">
  <img src="images/grafana.png" alt="Grafana 0.001ms Başarı Oranı Göstergesi"/>
</div>
## 7. Sonuç ve Sınırlılıklar
* **Başarılar:** Tamamen izole Docker konteyner ağı kuruldu, gateway prensibi başarıldı, RMM seviye 2 limitlerine uyuldu. JWT ile Stateless mimari benimsendi.
* **Olası Geliştirmeler:** Ön bellekleme, JWT refresh token yapıları ileride entegre edilebilir. Rate-Limiting fonksiyonları Redis vasıtasıyla kodlanabilir.
