# Gymnasieval

En webbapp för att jämföra gymnasieskolor i hela Sverige. Skolor, program, inriktningar
och antagningspoäng hämtas från Skolverkets öppna API:er. Användaren kan sedan bevaka skolor,
sätta egna betyg per kriterium och planera besök på öppet hus.

## Teknik

- **Backend:** Java 25, Spring Boot 4, Spring Data JPA / Hibernate, Flyway
- **Databas:** MySQL 8.4 (Docker Compose under utveckling)
- **Tester:** JUnit 5, AssertJ, Testcontainers
- **Frontend (planerad):** React, TypeScript, Vite

## Kom igång

Kräver Java 25 och Docker.

```bash
./gradlew bootRun                                 # startar MySQL och appen på http://localhost:8081
./gradlew bootRun --args='skolverket:download'    # hämtar Skolverket-data till data/skolverket.json
./gradlew bootRun --args='skolverket:import'      # läser in filen i databasen
./gradlew build                                   # bygger och kör alla tester
```

## Struktur

Koden är organiserad efter funktion (modulär monolit) under `se.gymnasieval`:

| Paket | Innehåll |
|---|---|
| `school` | Skolor, program, inriktningar och kommuner (entiteter och repositories) |
| `skolverket` | Klient mot Skolverkets API samt kommandona för nedladdning och import |

Databasschemat hanteras med Flyway-migreringar i `src/main/resources/db/migration`.

## Datakälla

Skolverkets öppna API:er (ingen API-nyckel behövs):

- *Planerade utbildningar*: program, inriktningar och antagningspoäng
  (`https://api.skolverket.se/planned-educations/v3`)
- *Skolenhetsregistret*: skolenheternas status
  (`https://api.skolverket.se/skolenhetsregistret/v2`)
