package se.gymnasieval.skolverket;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

/** Inställningar under {@code skolverket.*} i application.properties. */
@ConfigurationProperties("skolverket")
public record SkolverketProperties(
        String plannedEducationsUrl,
        String registryUrl,
        Path snapshotFile,
        int concurrency) {
}
