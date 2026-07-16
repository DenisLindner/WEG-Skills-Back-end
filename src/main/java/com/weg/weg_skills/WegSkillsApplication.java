package com.weg.weg_skills;

import com.weg.weg_skills.config.MinioProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MinioProperties.class)
public class WegSkillsApplication {

	public static void main(String[] args) {
		SpringApplication.run(WegSkillsApplication.class, args);
	}

}
