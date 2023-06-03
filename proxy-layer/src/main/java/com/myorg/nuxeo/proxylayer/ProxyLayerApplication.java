package com.myorg.nuxeo.proxylayer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableEurekaClient
public class ProxyLayerApplication  extends SpringBootServletInitializer {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ProxyLayerApplication.class, args);

	}

}
