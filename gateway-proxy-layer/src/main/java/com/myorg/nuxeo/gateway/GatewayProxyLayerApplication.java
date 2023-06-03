package com.myorg.nuxeo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableAutoConfiguration
@EnableEurekaClient
public class GatewayProxyLayerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayProxyLayerApplication.class, args);
	}


}
