-- En skola bevakas för ett visst program (det man sökte på), så att listan kan visa
-- rätt antagningspoäng och inriktningar. Tabellen är tom, så NOT NULL går bra.
ALTER TABLE workspace_school
    ADD COLUMN program_code VARCHAR(10) NOT NULL AFTER school_unit_code,
    ADD CONSTRAINT fk_workspace_school_program FOREIGN KEY (program_code) REFERENCES program (code);
