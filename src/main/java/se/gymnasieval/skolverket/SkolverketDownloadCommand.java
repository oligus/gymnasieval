package se.gymnasieval.skolverket;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Kommandot {@code skolverket:download}: hämtar Skolverket-data till en JSON-fil.
 * Kör med: {@code ./gradlew bootRun --args='skolverket:download'}
 */
@Component
@EnableConfigurationProperties(SkolverketProperties.class)
class SkolverketDownloadCommand implements ApplicationRunner {

    static final String NAME = "skolverket:download";

    private final SkolverketDownloader downloader;
    private final SkolverketProperties properties;

    SkolverketDownloadCommand(SkolverketDownloader downloader, SkolverketProperties properties) {
        this.downloader = downloader;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (args.getNonOptionArgs().contains(NAME)) {
            downloader.downloadTo(properties.snapshotFile());
        }
    }
}
