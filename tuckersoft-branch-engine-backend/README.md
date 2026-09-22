# Tuckersoft Branch Engine

Backend de la hackathon DBP: Spring Boot 3.3.4 + Java 21 + PostgreSQL.

## Arranque

1. Levanta PostgreSQL:
```bash
docker run --name bandersnatch-db \
  -e POSTGRES_DB=bandersnatch \
  -e POSTGRES_USER=tuckersoft \
  -e POSTGRES_PASSWORD=colin1984 \
  -p 5432:5432 \
  -d postgres:16
```

2. Copia `.env.example` a `.env` y configura un JWT secret de al menos 32 caracteres.

3. Ejecuta:
```bash
./mvnw spring-boot:run
```

En Windows:
```powershell
mvnw.cmd spring-boot:run
```

## Autotests

Con el backend corriendo:
```powershell
cd autotests
mvnw.cmd test
```

No modificar `autotests/`.
