package com.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:postgresql://localhost:5433/postgres",
		"spring.datasource.username=gateway_app",
		"spring.datasource.password=gateway",
		"app.admin.password=test-admin-password",
		"app.jwt.secret=test-only-secret-key-that-is-at-least-32-bytes"
})
class CustomApiGatewayApplicationTests {

	@Test
	void contextLoads() {
	}
}