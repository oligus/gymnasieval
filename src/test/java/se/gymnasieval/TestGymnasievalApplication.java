package se.gymnasieval;

import org.springframework.boot.SpringApplication;

public class TestGymnasievalApplication {

	public static void main(String[] args) {
		SpringApplication.from(GymnasievalApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
