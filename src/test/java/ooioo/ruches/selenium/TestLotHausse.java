package ooioo.ruches.selenium;

import static ooioo.ruches.selenium.TestUtils.jsExceptionsList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.WebDriver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * Enchainement de tests des traitements par lot
 */
@TestMethodOrder(OrderAnnotation.class)
public class TestLotHausse {

	static WebDriver driver;
	static final String comment = "Test selenium chrome";

	// Attention certains tests écrivent en base de donnée.
	// Ne pas utiliser sur application en production !!!!!!!!!
	// Démarrer l'application correspondant à cette url !!!!
	static String baseUrl;

	static final String commentaire = "commentaire";
	static final String modif = " - modifié";

	static String rucheId;
	static String hausseId;
	static String recolteId;

	@BeforeAll
	static void initChrome() {
		driver = TestUtils.initChrome();
		baseUrl = TestUtils.baseUrl;
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
	@Order(1)
	@DisplayName("Page d'accueil")
	void connecte() {
		driver.get(baseUrl);
		// Le titre de la page d'accueil est "ruches"
		assertEquals("Ruches", driver.getTitle());
		// L'utilisateur est "test" avec un rôle admin
		assertAll("login, rôle", () -> assertEquals(TestUtils.user, driver.findElement(By.id("login")).getText()),
				() -> assertEquals(TestUtils.role, driver.findElement(By.id("role")).getText()));
	}

	@Order(2)
	@RepeatedTest(2)
	@DisplayName("Hausse création")
	void creeHausse() {
		// Création d'une Hausse
		// Attention écriture en base de données
		driver.get(baseUrl + "hausse/cree");
		// Formulaire de création de hausse
		assertEquals("form", driver.findElement(By.id("hausseForm")).getTagName());
		driver.findElement(By.name("nom")).sendKeys(TestUtils.nomMilli());
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		// erreur: le bouton submit n'est pas cliquable, car non visible ?
		TestUtils.submit(driver);
		// Page détail de la hausse créée
		assertEquals("div", driver.findElement(By.id("hausse")).getTagName());
	}

	@Test
	@Order(3)
	@DisplayName("Hausse création commentaires lot")
	void creeCommHausse() {
		driver.get(baseUrl + "hausse/liste");
		// La table liste des hausses d'id "hausses" est affichée
		assertEquals("table", driver.findElement(By.id("hausses")).getTagName());
		// Sélectionner les lignes des deux premières hausses
		TestUtils.xpathClick(driver, "//table/tbody/tr[1]/td[5]");
		TestUtils.xpathClick(driver, "//table/tbody/tr[2]/td[5]");
		driver.findElement(By.id("commentaire")).click();
		assertEquals("form", driver.findElement(By.id("evenementForm")).getTagName());
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		String ids = driver.getCurrentUrl();
		String[] idsT = ids.substring(ids.lastIndexOf("/") + 1).split(",");
		TestUtils.submit(driver);
		// La table liste des hausses d'id "hausses" est affichée
		assertEquals("table", driver.findElement(By.id("hausses")).getTagName());
		for (String str : idsT) {
			assertTrue(typeEveHausseId(str, "COMMENTAIREHAUSSE"));
		}
	}

	/*
	 * Renvoie true si le type eve typeVal de hausse id est trouvé. Appel API REST.
	 * false sinon
	 */
	boolean typeEveHausseId(String id, String typeVal) {
		// recherche des événements essaim avec l'api rest
		driver.get(baseUrl + "rest/evenements/search/findByHausseId?hausseId=" + id);
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode actualObj = mapper.readTree(driver.findElement(By.tagName("pre")).getText());
			for (JsonNode j : actualObj.findValues("type")) {
				if (j.textValue().equals(typeVal)) {
					return true;
				}
			}
			return false;
		} catch (JsonProcessingException e) {
			return false;
		}
	}

}
