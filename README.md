# StokTakip Backend

Migration uygulamasindan bagimsiz, gercek stok takip backend uygulamasidir.

## Gereksinimler

- Java 25
- Maven 3.9+
- Oracle Database 23+

## Docker ile yerel Oracle

Yerel geliştirme veritabanını başlatın:

```bash
docker compose up -d
docker compose ps
```

Compose, `gvenzl/oracle-free:23-slim-faststart` image'ını, kalıcı
`stoktakip-oracle-data` volume'unu ve `FREEPDB1` içindeki `stoktakip` şemasını
hazırlar. Varsayılan JDBC adresi
`jdbc:oracle:thin:@//localhost:1521/FREEPDB1`, kullanıcı `stoktakip`, geliştirme
parolası `stoktakip123` değeridir. Parolayı shell üzerinden değiştirmek için
container ilk kez oluşturulmadan önce `TARGET_DB_PASSWORD` tanımlayın.

Docker profili yalnızca boş yerel şemada Hibernate tablolarını oluşturmak için
`ddl-auto: update` kullanır. Varsayılan profil `validate` olarak kalır:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

İlk Docker profili çalıştırmasından sonra şema hazırdır ve uygulama IntelliJ
üzerinden ek profil tanımlamadan da başlatılabilir. Ana yapılandırma yerel Docker
değerlerini varsayılan olarak kullanır; `TARGET_DB_URL`, `TARGET_DB_USERNAME` ve
`TARGET_DB_PASSWORD` ortam değişkenleri tanımlanırsa bu varsayılanları ezer.

## Ortam degiskenleri

```bash
export TARGET_DB_URL='jdbc:oracle:thin:@//localhost:1521/FREEPDB1'
export TARGET_DB_USERNAME='stoktakip'
export TARGET_DB_PASSWORD='guvenli-parola'
```

## Calistirma

```bash
mvn clean test
mvn spring-boot:run
```

Uygulama migration calistirmaz ve legacy datasource baglantisi icermez. Production
profilinde Hibernate semayi degistirmez; mevcut hedef semayi `validate` eder.
