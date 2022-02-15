package ooioo.ruches;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// https://spring.io/guides/gs/testing-web/
//  voir src/test/resources/application.properties
//    mise en commentaire postgresql jndi (sinon erreur datasource)
//    et ajout base h2 pour tests intégration...
//    et pom.xml pour dependency h2 en scope test

@SpringBootTest
public class TestContext {

	@Autowired
	private AccueilController accueilController;

	@Test
	public void contextLoads() throws Exception {
		assertThat(accueilController).isNotNull();
	}
}
