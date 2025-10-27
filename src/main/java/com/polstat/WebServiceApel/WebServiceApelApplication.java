package com.polstat.WebServiceApel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.polstat.WebServiceApel.entity")
@EnableJpaRepositories("com.polstat.WebServiceApel.repository")
public class WebServiceApelApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebServiceApelApplication.class, args);
	}

}
