-- Skolverkets inriktningskoder kan vara upp till 15 tecken (t.ex. lärlingsvarianter).
ALTER TABLE study_path MODIFY code VARCHAR(20) NOT NULL;