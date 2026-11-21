package se.gymnasieval;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

@SpringBootApplication
public class GymnasievalApplication {

	/**
	 * Startar webbappen, eller kör ett kommando om ett sådant anges, t.ex.
	 * {@code ./gradlew bootRun --args='skolverket:download'}.
	 * Kommandon körs utan webbserver och avslutar processen när de är klara.
	 */
	public static void main(String[] args) {
		if (isCommand(args)) {
			ConfigurableApplicationContext context = new SpringApplicationBuilder(GymnasievalApplication.class)
					.web(WebApplicationType.NONE)
					.run(args);
			System.exit(SpringApplication.exit(context));
		}
		SpringApplication.run(GymnasievalApplication.class, args);
	}

	private static boolean isCommand(String[] args) {
		return Arrays.stream(args).anyMatch(arg -> !arg.startsWith("--"));
	}

}
