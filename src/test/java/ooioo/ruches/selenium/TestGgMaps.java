package ooioo.ruches.selenium;

import static ooioo.ruches.selenium.TestUtils.baseUrl;
import static ooioo.ruches.selenium.TestUtils.driver;
import static ooioo.ruches.selenium.TestUtils.jsExceptionsList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;

@Disabled
@TestMethodOrder(OrderAnnotation.class)
public class TestGgMaps {

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
		for (JavascriptException jsException : jsExceptionsList) {
			System.out.println("JS exception message: " + jsException.getMessage());
			System.out.println("JS exception system information: " + jsException.getSystemInformation());
			jsException.printStackTrace();
		}
		driver.quit();
	}

	@Test
	@Order(1000)
	@DisplayName("Ruchers carte Gg")
	void mapGgRuchers() {
		driver.get(baseUrl + "rucher/Gg");
		// La div d'id "map" est affichée
		assertEquals("div", driver.findElement(By.id("map")).getTagName());
	}

	@Test
	@Order(160)
	@DisplayName("Rucher dépot carte Gg")
	void mapGgDepot() {
		if (depotId == null) {
			depotId = TestUtils.getDepotId(driver, baseUrl);
		}
		if ("".equals(depotId)) {
			fail("Api rest recherche de l'id du dépôt");
		} else {
			driver.get(baseUrl + "rucher/Gg/" + depotId);
			// La div d'id "map" est affichée
			assertEquals("div", driver.findElement(By.id("map")).getTagName());
		}
	}

}
