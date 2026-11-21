package se.gymnasieval.skolverket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Kommandot {@code skolverket:import}: läser JSON-filen från skolverket:download
 * och skriver innehållet till databasen.
 * Kör med: {@code ./gradlew bootRun --args='skolverket:import'}
 */
@Component
class SkolverketImportCommand implements ApplicationRunner {

    static final String NAME = "skolverket:import";

    private static final Logger log = LoggerFactory.getLogger(SkolverketImportCommand.class);

    private final SkolverketImporter importer;
    private final SkolverketProperties properties;
    private final JsonMapper jsonMapper;

    SkolverketImportCommand(SkolverketImporter importer, SkolverketProperties properties, JsonMapper jsonMapper) {
        this.importer = importer;
        this.properties = properties;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!args.getNonOptionArgs().contains(NAME)) {
            return;
        }
        Path file = properties.snapshotFile();
        if (!Files.exists(file)) {
            throw new IllegalStateException("Hittar inte " + file.toAbsolutePath()
                    + ". Kör skolverket:download först.");
        }
        SkolverketSnapshot snapshot = jsonMapper.readValue(file.toFile(), SkolverketSnapshot.class);
        log.info("Läste {} (nedladdad {}), importerar...", file.toAbsolutePath(), snapshot.downloadedAt());
        importer.importSnapshot(snapshot);
    }
}
