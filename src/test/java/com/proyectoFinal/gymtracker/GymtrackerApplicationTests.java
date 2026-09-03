package com.proyectoFinal.gymtracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop",

		"JWT_SECRET_KEY=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
		"MERCADOPAGO_ACCESS_TOKEN=TEST-dummy-token",
		"MERCADOPAGO_NOTIFICATION= https://dummy",
		"MERCADOPAGO_URL_RETURN=http://localhost:8080/dummy"
})
class GymtrackerApplicationTests {

	@Test
	void contextLoads() {
	}

}
