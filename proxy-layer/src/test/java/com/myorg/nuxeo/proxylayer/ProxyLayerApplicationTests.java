package com.myorg.nuxeo.proxylayer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ProxyLayerApplicationTests {

	private final ApplicationContext applicationContext;

	public ProxyLayerApplicationTests(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Test
	void contextLoads() {
		assertNotNull(applicationContext);
	}

}
