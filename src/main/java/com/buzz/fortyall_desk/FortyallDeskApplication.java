package com.buzz.fortyall_desk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class FortyallDeskApplication {

	public static void main(String[] args) {
		SpringApplication.run(FortyallDeskApplication.class, args);
	}

}
