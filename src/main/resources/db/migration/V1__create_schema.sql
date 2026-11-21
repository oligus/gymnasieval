-- =====================================================================
-- Skolverket-data: importeras och skrivs över vid varje import.
-- Nycklar är Skolverkets egna koder så att användardata överlever omimport.
-- =====================================================================

CREATE TABLE municipality (
    code VARCHAR(4)   NOT NULL,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (code)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_sv_0900_ai_ci;

CREATE TABLE school (
    school_unit_code  VARCHAR(8)    NOT NULL,
    name              VARCHAR(200)  NOT NULL,
    organizer_name    VARCHAR(200),
    organizer_type    VARCHAR(20),          -- KOMMUNAL, ENSKILD, REGION, STATLIG
    municipality_code VARCHAR(4),
    street            VARCHAR(200),
    zip_code          VARCHAR(10),
    city              VARCHAR(100),
    latitude          DECIMAL(9, 6),
    longitude         DECIMAL(9, 6),
    web_url           VARCHAR(500),
    status            VARCHAR(20)   NOT NULL, -- AKTIV, VILANDE, UPPHORT, PLANERAD
    imported_at       DATETIME      NOT NULL,
    PRIMARY KEY (school_unit_code),
    CONSTRAINT fk_school_municipality FOREIGN KEY (municipality_code) REFERENCES municipality (code)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_sv_0900_ai_ci;

CREATE TABLE program (
    code VARCHAR(10)  NOT NULL,             -- t.ex. TE25
    name VARCHAR(200) NOT NULL,             -- Teknikprogrammet
    PRIMARY KEY (code)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_sv_0900_ai_ci;

-- Ett program som en viss skola erbjuder, med antagningspoäng.
CREATE TABLE school_program (
    school_unit_code     VARCHAR(8)    NOT NULL,
    program_code         VARCHAR(10)   NOT NULL,
    admission_points_min DECIMAL(5, 1),     -- null om inget tal finns
    admission_points_avg DECIMAL(5, 1),
    admission_year       SMALLINT,
    admission_note       VARCHAR(100),      -- t.ex. "alla antagna", "antagningsprov"
    PRIMARY KEY (school_unit_code, program_code),
    CONSTRAINT fk_school_program_school  FOREIGN KEY (school_unit_code) REFERENCES school (school_unit_code),
    CONSTRAINT fk_school_program_program FOREIGN KEY (program_code)     REFERENCES program (code)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_sv_0900_ai_ci;

-- Inriktning inom ett program på en skola.
CREATE TABLE study_path (
    school_unit_code VARCHAR(8)   NOT NULL,
    program_code     VARCHAR(10)  NOT NULL,
    code             VARCHAR(10)  NOT NULL, -- t.ex. TEINF
    name             VARCHAR(200) NOT NULL,
    PRIMARY KEY (school_unit_code, program_code, code),
    CONSTRAINT fk_study_path_school_program FOREIGN KEY (school_unit_code, program_code)
        REFERENCES school_program (school_unit_code, program_code)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_sv_0900_ai_ci;

-- =====================================================================
-- Delad data som matas in i appen (inte från Skolverket).
-- =====================================================================

CREATE TABLE open_house (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    school_unit_code VARCHAR(8)   NOT NULL,
    event_date       DATE         NOT NULL,
    start_time       TIME,
    end_time         TIME,
    certainty        VARCHAR(20)  NOT NULL, -- CONFIRMED, UNCONFIRMED, GUESS
    comment          VARCHAR(500),
    PRIMARY KEY (id),
    KEY idx_open_house_date (event_date),
    CONSTRAINT fk_open_house_school FOREIGN KEY (school_unit_code) REFERENCES school (school_unit_code)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_sv_0900_ai_ci;

-- =====================================================================
-- Användardata: skrivs aldrig över av importen.
-- =====================================================================

CREATE TABLE app_user (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    name         VARCHAR(100)  NOT NULL,
    email        VARCHAR(200),
    merit_value  DECIMAL(4, 1),             -- max 340
    home_latitude  DECIMAL(9, 6),
    home_longitude DECIMAL(9, 6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_user_email (email),
    CONSTRAINT chk_app_user_merit CHECK (merit_value BETWEEN 0 AND 340)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_sv_0900_ai_ci;

-- Vikt 0–5 per kriterium (RESTID, INRIKTNING, POANG, RYKTE, PROFIL, MILJO, BESOK).
CREATE TABLE user_weight (
    user_id   BIGINT      NOT NULL,
    criterion VARCHAR(20) NOT NULL,
    weight    TINYINT     NOT NULL,
    PRIMARY KEY (user_id, criterion),
    CONSTRAINT fk_user_weight_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT chk_user_weight CHECK (weight BETWEEN 0 AND 5)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_sv_0900_ai_ci;

-- Skolor som användaren bevakar.
CREATE TABLE user_school (
    user_id          BIGINT       NOT NULL,
    school_unit_code VARCHAR(8)   NOT NULL,
    status           VARCHAR(20),           -- INTRESSANT, BESOK, BESOKT, BORTVALD
    note             TEXT,
    travel_minutes   SMALLINT,
    station          VARCHAR(200),
    PRIMARY KEY (user_id, school_unit_code),
    CONSTRAINT fk_user_school_user   FOREIGN KEY (user_id)          REFERENCES app_user (id),
    CONSTRAINT fk_user_school_school FOREIGN KEY (school_unit_code) REFERENCES school (school_unit_code)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_sv_0900_ai_ci;

-- Poäng 1–5 per kriterium och skola. Saknad rad = poäng inte satt.
CREATE TABLE user_school_score (
    user_id          BIGINT      NOT NULL,
    school_unit_code VARCHAR(8)  NOT NULL,
    criterion        VARCHAR(20) NOT NULL,
    score            TINYINT     NOT NULL,
    PRIMARY KEY (user_id, school_unit_code, criterion),
    CONSTRAINT fk_user_school_score_user_school FOREIGN KEY (user_id, school_unit_code)
        REFERENCES user_school (user_id, school_unit_code),
    CONSTRAINT chk_user_school_score CHECK (score BETWEEN 1 AND 5)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_sv_0900_ai_ci;

-- Inplanerade besök.
CREATE TABLE school_visit (
    id               BIGINT     NOT NULL AUTO_INCREMENT,
    user_id          BIGINT     NOT NULL,
    school_unit_code VARCHAR(8) NOT NULL,
    visit_date       DATE       NOT NULL,
    PRIMARY KEY (id),
    KEY idx_school_visit_user_date (user_id, visit_date),
    CONSTRAINT fk_school_visit_user   FOREIGN KEY (user_id)          REFERENCES app_user (id),
    CONSTRAINT fk_school_visit_school FOREIGN KEY (school_unit_code) REFERENCES school (school_unit_code)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_sv_0900_ai_ci;
