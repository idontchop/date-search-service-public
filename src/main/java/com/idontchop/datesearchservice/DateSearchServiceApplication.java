package com.idontchop.datesearchservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class DateSearchServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DateSearchServiceApplication.class, args);
	}

}
