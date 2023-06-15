package ooioo.ruches.selenium;

import static ooioo.ruches.selenium.TestUtils.baseUrl;
import static ooioo.ruches.selenium.TestUtils.driver;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@TestMethodOrder(OrderAnnotation.class)
public class TestRest {

	static final String comment = "Test selenium chrome";

	static final String commentaire = "commentaire";
	static final String modif = " - modifié";

	static String depotId;

	@BeforeAll
	static void initChrome() {
		TestUtils.initChrome();
	}

	@AfterAll
	static void quitChrome() {
		driver.quit();
	}

	@Test
	@DisplayName("Admin API REST")
	void adminRest() {
		driver.get(baseUrl + "rest");
		// api rest, json test sur lien vers repository recolteHausses
		// https://itecnote.com/tecnote/how-to-get-json-response-by-selenium-webdriver-using-java/
		// on ne voit pas cette balise "pre" avec un curl, est-elle ajoutée par selenium, chrome ?
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode actualObj = mapper.readTree(driver.findElement(By.tagName("pre")).getText());
			System.out.println(actualObj);
			JsonNode jsonNode = actualObj.get("_links").get("recolteHausses").get("href");
			assertTrue(jsonNode.textValue().endsWith("rest/recolteHausses"));
		} catch (JsonProcessingException e) {
			fail(e.getMessage());
		}
	}

	@ParameterizedTest
	@ValueSource(strings = { "distRuchers", "essaims", "recolteHausses", "personnes", "ruchers",
			"recoltes", "evenements", "hausses", "ruches", "rucheTypes", "profile"})
	@DisplayName("Admin API REST essaims")
	void adminRestRuche(String entites) {
		driver.get(baseUrl + "rest/" + entites);
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode actualObj = mapper.readTree(driver.findElement(By.tagName("pre")).getText());
			JsonNode jsonNode = actualObj.get("_links").get("self").get("href");
			assertTrue(jsonNode.textValue().endsWith("rest/" + entites));
		} catch (JsonProcessingException e) {
			fail(e.getMessage());
		}
	}

}
