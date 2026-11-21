-- Appen har inga användarkonton. Data hör i stället till en arbetsyta som nås via hemliga länkar:
-- en nyckel för redigering och en för läsning. Bara SHA-256-hashen av nycklarna sparas.
-- Användartabellerna från V1 var tomma, så de tas bort och skapas om.

DROP TABLE user_school_score;
DROP TABLE user_school;
DROP TABLE user_weight;
DROP TABLE school_visit;
DROP TABLE app_user;

CREATE TABLE workspace (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    edit_key_hash  BINARY(32)    NOT NULL,
    view_key_hash  BINARY(32)    NOT NULL,
    merit_value    DECIMAL(4, 1),           -- max 340
    home_latitude  DECIMAL(9, 6),
    home_longitude DECIMAL(9, 6),
    created_at     DATETIME      NOT NULL,
    last_used_at   DATETIME      NOT NULL,
    version        INT           NOT NULL DEFAULT 0, -- optimistisk låsning (@Version)
    PRIMARY KEY (id),
    UNIQUE KEY uk_workspace_edit_key (edit_key_hash),
    UNIQUE KEY uk_workspace_view_key (view_key_hash),
    CONSTRAINT chk_workspace_merit CHECK (merit_value BETWEEN 0 AND 340)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_sv_0900_ai_ci;

-- Vikt 0–5 per kriterium (RESTID, INRIKTNING, POANG, RYKTE, PROFIL, MILJO, BESOK).
CREATE TABLE workspace_weight (
    workspace_id BIGINT      NOT NULL,
    criterion    VARCHAR(20) NOT NULL,
    weight       TINYINT     NOT NULL,
    PRIMARY KEY (workspace_id, criterion),
    CONSTRAINT fk_workspace_weight_workspace FOREIGN KEY (workspace_id) REFERENCES workspace (id),
    CONSTRAINT chk_workspace_weight CHECK (weight BETWEEN 0 AND 5)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_sv_0900_ai_ci;

-- Skolor som bevakas i arbetsytan.
CREATE TABLE workspace_school (
    workspace_id     BIGINT       NOT NULL,
    school_unit_code VARCHAR(8)   NOT NULL,
    status           VARCHAR(20),           -- INTRESSANT, BESOK, BESOKT, BORTVALD
    note             TEXT,
    travel_minutes   SMALLINT,
    station          VARCHAR(200),
    PRIMARY KEY (workspace_id, school_unit_code),
    CONSTRAINT fk_workspace_school_workspace FOREIGN KEY (workspace_id)     REFERENCES workspace (id),
    CONSTRAINT fk_workspace_school_school    FOREIGN KEY (school_unit_code) REFERENCES school (school_unit_code)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_sv_0900_ai_ci;

-- Poäng 1–5 per kriterium och skola. Saknad rad = poäng inte satt.
CREATE TABLE workspace_school_score (
    workspace_id     BIGINT      NOT NULL,
    school_unit_code VARCHAR(8)  NOT NULL,
    criterion        VARCHAR(20) NOT NULL,
    score            TINYINT     NOT NULL,
    PRIMARY KEY (workspace_id, school_unit_code, criterion),
    CONSTRAINT fk_workspace_school_score_school FOREIGN KEY (workspace_id, school_unit_code)
        REFERENCES workspace_school (workspace_id, school_unit_code),
    CONSTRAINT chk_workspace_school_score CHECK (score BETWEEN 1 AND 5)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_sv_0900_ai_ci;

-- Inplanerade besök.
CREATE TABLE workspace_visit (
    id               BIGINT     NOT NULL AUTO_INCREMENT,
    workspace_id     BIGINT     NOT NULL,
    school_unit_code VARCHAR(8) NOT NULL,
    visit_date       DATE       NOT NULL,
    PRIMARY KEY (id),
    KEY idx_workspace_visit_date (workspace_id, visit_date),
    CONSTRAINT fk_workspace_visit_workspace FOREIGN KEY (workspace_id)     REFERENCES workspace (id),
    CONSTRAINT fk_workspace_visit_school    FOREIGN KEY (school_unit_code) REFERENCES school (school_unit_code)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_sv_0900_ai_ci;
