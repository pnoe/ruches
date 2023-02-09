package ooioo.ruches.selenium;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * Enchainement de tests des traitements par lot
 */
@TestMethodOrder(OrderAnnotation.class)
public class TestLotRuche {

	static WebDriver driver;
	static final String comment = "Test selenium chrome";

	// Attention certains tests écrivent en base de donnée.
	// Ne pas utiliser sur application en production !!!!!!!!!
	// Démarrer l'application correspondant à cette url !!!!
	static final String baseUrl = "http://localhost:8080/ruches/";

	// Initialisation du navigateur Chrome et login
	// Créer un admin, nom : xx, prénom : yy, login : test, password : testpwd, role
	// : admin
	// le nom et le prénom peuvent être quelconques
	// Attention à ne pas mettre de mot de passe de production !
	static final String user = "test";
	static final String pwd = "testpwd";
	static final String role = "[ROLE_admin]";

	static final String commentaire = "commentaire";
	static final String modif = " - modifié";

	static String rucheId;
	static String hausseId;
	static String recolteId;

	@BeforeAll
	static void initChrome() {
		TestChrome.initChrome();
		driver = TestChrome.driver;
	}

	@AfterAll
	static void quitChrome() {
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
		assertAll("login, rôle", () -> assertEquals(user, driver.findElement(By.id("login")).getText()),
				() -> assertEquals(role, driver.findElement(By.id("role")).getText()));
	}

	@Test
	@Order(2)
	@RepeatedTest(1)
	@DisplayName("Ruche création")
	void creeRuche() {
		// Création d'une ruche
		// Attention écriture en base de données
		driver.get(baseUrl + "ruche/cree");
		// Formulaire de création de ruche
		assertEquals("form", driver.findElement(By.id("rucheForm")).getTagName());
		driver.findElement(By.name("nom")).sendKeys(TestUtils.nomMilli());
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		// Date d'acquisition (LocalDate) = hier pour événement commentaire plus récent
		TestUtils.clearSend(driver, "dateAcquisition",
				LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		TestUtils.submit(driver);
		// Page détail de la ruche créée
		assertEquals("div", driver.findElement(By.id("ruche")).getTagName());
	}

	@Test
	@Order(3)
	@DisplayName("Ruche création commentaires lot")
	void creeCommRuche() {
		driver.get(baseUrl + "ruche/liste");
		// La table liste des ruches d'id "ruches" est affichée
		assertEquals("table", driver.findElement(By.id("ruches")).getTagName());
		// Sélectionner les lignes des deux premières ruches
		TestUtils.xpathClick(driver, "//table/tbody/tr[1]/td[4]");
		TestUtils.xpathClick(driver, "//table/tbody/tr[2]/td[4]");
		driver.findElement(By.id("commentaire")).click();
		assertEquals("form", driver.findElement(By.id("evenementForm")).getTagName());
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		String ids = driver.getCurrentUrl();
		String[] idsT = ids.substring(ids.lastIndexOf("/") + 1).split(",");
		TestUtils.submit(driver);
		// La table liste des ruches d'id "ruches" est affichée
		assertEquals("table", driver.findElement(By.id("ruches")).getTagName());
		for (String str : idsT) {
			assertTrue("COMMENTAIRERUCHE".equals(typeEveRucheId(str)));
		}
	}

	/*
	 * Renvoie le type eve du dernier événement de la ruche id. Appel API REST.
	 */
	String typeEveRucheId(String id) {
		// recherche des événements ruche avec l'api rest
		driver.get(baseUrl + "rest/evenements/search/findByRucheId?rucheId=" + id);
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode actualObj = mapper.readTree(driver.findElement(By.tagName("pre")).getText());
			// On prend le type du deuxième événement (le premier est RUCHEAJOUTRUCHER dans
			// le dépot
			JsonNode jsonNode = actualObj.get("_embedded").get("evenementRepository").get(1).get("type");
			return jsonNode.textValue();
		} catch (JsonProcessingException e) {
			return "";
		}
	}

}
