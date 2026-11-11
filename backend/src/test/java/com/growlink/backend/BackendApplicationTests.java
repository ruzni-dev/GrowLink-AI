package com.growlink.backend;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.growlink.backend.security.JwtService;

@SpringBootTest(properties = "app.jwt.secret=VGhpcy1pcy1hLXRlc3Qta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLUhTMjU2")
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void rawJwtSecretIsAccepted() {
		assertDoesNotThrow(() -> new JwtService("super-secret-key-for-growlink-ai-1234567890", 900));
	}

}
