package com.example.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 后端项目启动类。
 */
@MapperScan("com.example.backend.mapper")
@SpringBootApplication
public class BackendApplication {

	/**
	 * 启动 Spring Boot 应用。
	 *
	 * @param args 启动参数
	 */
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
