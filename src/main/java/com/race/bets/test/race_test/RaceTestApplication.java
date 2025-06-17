package com.race.bets.test.race_test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RaceTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(RaceTestApplication.class, args);
	}

}
