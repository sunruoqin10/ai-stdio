package com.example.oa_system_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OaSystemBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(OaSystemBackendApplication.class, args);
	}

}
