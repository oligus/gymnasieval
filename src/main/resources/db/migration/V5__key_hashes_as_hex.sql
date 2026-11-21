-- Nyckelhasharna lagras som läsbar hex (64 tecken) i stället för 32 råa byte.
-- Råa byte visas som skräptecken i databasverktyg och är svåra att jämföra och felsöka.
-- Befintliga hashar konverteras, så att redan utdelade länkar fortsätter att fungera.

ALTER TABLE workspace
    ADD COLUMN edit_key_hex CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL,
    ADD COLUMN view_key_hex CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL;

UPDATE workspace
SET edit_key_hex = LOWER(HEX(edit_key_hash)),
    view_key_hex = LOWER(HEX(view_key_hash));

ALTER TABLE workspace
    DROP INDEX uk_workspace_edit_key,
    DROP INDEX uk_workspace_view_key,
    DROP COLUMN edit_key_hash,
    DROP COLUMN view_key_hash;

ALTER TABLE workspace
    CHANGE COLUMN edit_key_hex edit_key_hash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL AFTER id,
    CHANGE COLUMN view_key_hex view_key_hash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL AFTER edit_key_hash,
    ADD UNIQUE KEY uk_workspace_edit_key (edit_key_hash),
    ADD UNIQUE KEY uk_workspace_view_key (view_key_hash);
