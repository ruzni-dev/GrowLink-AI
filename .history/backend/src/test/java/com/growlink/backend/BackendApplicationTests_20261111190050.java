package com.growlink.backend;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.growlink.backend.security.JwtService;

@SpringBootTest(properties = {
	"app.jwt.secret=VGhpcy1pcy1hLXRlc3Qta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLUhTMjU2",
	"spring.datasource.url=jdbc:h2:mem:growlink-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
	"spring.datasource.username=sa",
	"spring.datasource.password=",
	"spring.datasource.driver-class-name=org.h2.Driver"
})
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void rawJwtSecretIsAccepted() {
		assertDoesNotThrow(() -> new JwtService("super-secret-key-for-growlink-ai-1234567890", 900));
	}

}
