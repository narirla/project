package com.KDT.mosi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
public class MosiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MosiApplication.class, args);
	}
}
