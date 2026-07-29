package com.weg.weg_skills;

import com.weg.weg_skills.config.minio.MinioProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(MinioProperties.class)
@EnableScheduling
public class WegSkillsApplication {

	public static void main(String[] args) {
		SpringApplication.run(WegSkillsApplication.class, args);
	}

}
