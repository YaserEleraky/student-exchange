package com.eleraky.studentexchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

@SpringBootApplication
public class StudentExchangeApplication {

	public static void main(String[] args) throws Exception {
		YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
		List<PropertySource<?>> sources = loader.load("application.yaml", new ClassPathResource("application.yaml"));
		PropertySource<?> source = sources.get(0);

		String url = (String) source.getProperty("spring.datasource.url");
		String username = (String) source.getProperty("spring.datasource.username");
		String password = (String) source.getProperty("spring.datasource.password");

		DatabaseInitializer.createDatabaseIfNotExists(url, username, password);
		SpringApplication.run(StudentExchangeApplication.class, args);
	}

}
