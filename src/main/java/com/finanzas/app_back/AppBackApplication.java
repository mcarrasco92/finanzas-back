package com.finanzas.app_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AppBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppBackApplication.class, args);
	}

}
