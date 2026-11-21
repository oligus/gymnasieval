# CLAUDE.md

Gymnasieval: en webbapp för att jämföra gymnasieskolor i hela Sverige, med data från Skolverket.
Projektet är också ett CV-projekt i Java. Välj därför etablerade och välkända verktyg.

## Arbetssätt
- Svara på **svenska**.
- Ta **ett litet steg i taget**. Gör bara det steg som efterfrågas och föreslå nästa efteråt.
- Användaren kommer från PHP/Symfony/Doctrine. Förklara gärna Java- och Spring-begrepp genom att jämföra med dem.
- Committa och pusha bara när användaren ber om det.
- Starta, stoppa eller döda aldrig användarens processer (t.ex. appen i IntelliJ eller Docker-containrar). Be användaren göra det.
- Committa aldrig personuppgifter (namn, hemort, meritvärden för verkliga personer).

## Stack
- Java 25 (Temurin), Spring Boot 4.2, Gradle med Kotlin DSL (`build.gradle.kts`)
- MySQL 8.4 via Docker Compose (`compose.yaml`). Spring startar containern automatiskt.
- Spring Data JPA / Hibernate 7, Flyway, Jackson 3 (`tools.jackson.*`), RestClient
- Tester: JUnit 5, AssertJ, Testcontainers (riktig MySQL, inte H2)
- Planerat: React + TypeScript + Vite i `frontend/`, byggt in i Spring Boot-jaren (ett repo, en Docker-image)

## Kommandon
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 25)   # om Gradle inte hittar Java 25

./gradlew build                                   # kompilera + alla tester (kräver Docker)
./gradlew bootRun                                 # webbappen på http://localhost:8081
./gradlew bootRun --args='skolverket:download'    # Skolverkets API → data/skolverket.json
./gradlew bootRun --args='skolverket:import'      # data/skolverket.json → databasen
```
Kommandon (argument utan `--`) körs utan webbserver och avslutar processen när de är klara (se `GymnasievalApplication.main`).
Varje kommando är en `ApplicationRunner` som själv kontrollerar sitt namn.

## Portar
- App: **8081**, eftersom 8080 används av en annan Docker-container på datorn
- MySQL: **3308** (`gymnasieval` / `secret`, databas `gymnasieval`). 3307 och 3311 används av andra projekt.

## Arkitektur
Package-by-feature (modulär monolit) under `se.gymnasieval`:
- `school/`: Skolverket-data som entiteter (`School`, `Program`, `SchoolProgram`, `StudyPath`, `Municipality`) och repositories
- `skolverket/`: API-klient, nedladdning och import. Skolverkets JSON-format stannar i den här modulen (anti-corruption layer).
- `workspace/`: arbetsytor i stället för användarkonton (se nedan)
- Planerat: `shortlist/` (status, poäng, totalpoäng, antagningschans), `visit/` (öppet hus, besök)

Arbetsytor (inga konton):
- All användardata hör till en `Workspace`, som nås via två hemliga nycklar: redigering (EDIT) och läsning (VIEW).
- Nyckeln består av 128 slumpade bitar som base64url (22 tecken). Databasen sparar bara SHA-256-hashen, och klartexten returneras bara när arbetsytan skapas.
- Nyckeln ligger i URL-fragmentet (`#k=…`), som aldrig skickas till servern. Frontend skickar den i headern `X-Workspace-Key`.
- `WorkspaceService.resolve(key)` ger `WorkspaceAccess` (arbetsyta + nivå). Skrivningar kräver `canEdit()`.

Gränssnitt (beslutat):
- Startsidan är **alltid sökningen** (program, kommun, valfritt meritvärde → skollista med antagningschans). "Min lista" och "Dela" nås via headern.
- En arbetsyta skapas **automatiskt vid första sparning** (t.ex. ☆ Bevaka), aldrig vid besök. Efteråt visas redigerings- och läslänken en gång, och därefter alltid via "Dela".
- När en länk öppnas sparas `#k=` i localStorage och tas bort ur adressfältet (`history.replaceState`). Har webbläsaren redan en annan lista frågar appen innan den byter. Med en läsnyckel visas en banner om att listan är skrivskyddad, och ändringsknapparna döljs.

Principer:
- Skolverkets koder är primärnycklar (`school_unit_code`, `program_code`), så att omimport inte påverkar användardata.
- Importen gör upsert och tar aldrig bort skolor.
- Värden som kan räknas fram (totalpoäng, antagningschans, krockar i besöksschemat) sparas inte. De räknas i Java, utom när de måste uppdateras direkt vid inmatning. Antagningschansen räknas därför i frontend (`frontend/src/lib/chance.ts`).
- Inga Lombok-annotationer. Records för värdeobjekt, nycklar och DTO:er.

## Databas och migreringar
- Flyway äger schemat. Hibernate kör bara `ddl-auto=validate`.
- Nya ändringar läggs i en ny fil `src/main/resources/db/migration/V<n>__<beskrivning>.sql`.
- **Ändra aldrig en migrering som redan har körts.** Då vägrar Flyway starta (checksum mismatch).
- Tabeller använder `utf8mb4_sv_0900_ai_ci` för svensk sortering.
- Enum-fält mappas med `@Enumerated(STRING)` + `@JdbcTypeCode(SqlTypes.VARCHAR)`. Annars förväntar sig Hibernate MySQL:s ENUM-typ.

## Fallgropar
- `bootRun` skriver `BUILD SUCCESSFUL` även när appen kraschar, eftersom devtools kör appen i en egen tråd. Läs Spring-loggen.
- Skolverkets API `planned-educations/v3` kräver headern `Accept: application/vnd.skolverket.plannededucations.api.v3.hal+json`. `school-units` tillåter högst 100 per sida.
- Antagningspoäng finns bara per program, inte per inriktning.
