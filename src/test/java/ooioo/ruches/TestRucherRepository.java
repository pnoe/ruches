package ooioo.ruches;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ooioo.ruches.rucher.Rucher;
import ooioo.ruches.rucher.RucherRepository;

// https://docs.spring.io/spring-security/reference/servlet/test/method.html

@SpringBootTest
public class TestRucherRepository {

	@Autowired
	private RucherRepository rucherRepository;

	@Test
	@DisplayName("Lecture du rucher dépot et lecture de son nom")
	// @WithMockUser(username="admin",roles={"admin"})
	public void getMessageWithMockUser() {
		Rucher rucher = rucherRepository.findByDepotIsTrue();
		assertEquals(rucher.getNom(), "Dépôt");
	}
}
