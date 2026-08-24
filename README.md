# StokTakip Backend

Migration uygulamasindan bagimsiz, gercek stok takip backend uygulamasidir.

## Gereksinimler

- Java 25
- Maven 3.9+
- Oracle Database 23+

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
