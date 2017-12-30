package com.rseanking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@EnableAutoConfiguration
@RestController
public class ClientApplication {
	@Value("${hello.greeting}")
	private String greeting;
	
	@RequestMapping("/hello")
	@ResponseBody
	public String greeting() {
		return greeting;
	}

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}
}
