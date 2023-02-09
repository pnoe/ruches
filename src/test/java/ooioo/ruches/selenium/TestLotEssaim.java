package ooioo.ruches.selenium;

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
import org.openqa.selenium.WebDriver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * Enchainement de tests des traitements par lot
 */
@TestMethodOrder(OrderAnnotation.class)
public class TestLotEssaim {

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
	static final String valeur = "valeur";
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
	@DisplayName("Essaim création")
	void creeEssaim() {
		// Création d'une essaim
		// Attention écriture en base de données
		driver.get(baseUrl + "essaim/cree");
		// Formulaire de création de essaim
		assertEquals("form", driver.findElement(By.id("essaimForm")).getTagName());
		driver.findElement(By.name("nom")).sendKeys(TestUtils.nomMilli());
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		TestUtils.submit(driver);
		// Page détail de l'essaim créée
		assertEquals("div", driver.findElement(By.id("detailEssaim")).getTagName());
	}

	@Test
	@Order(3)
	@DisplayName("Essaim création commentaires lot")
	void creeCommEssaim() {
		driver.get(baseUrl + "essaim/liste");
		// La table liste des essaims d'id "essaims" est affichée
		assertEquals("table", driver.findElement(By.id("essaims")).getTagName());
		// Sélectionner les lignes des deux premiers essaims
		TestUtils.xpathClick(driver, "//table/tbody/tr[1]/td[4]");
		TestUtils.xpathClick(driver, "//table/tbody/tr[2]/td[4]");
		driver.findElement(By.id("commentaire")).click();
		assertEquals("form", driver.findElement(By.id("evenementForm")).getTagName());
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		String ids = driver.getCurrentUrl();
		String[] idsT = ids.substring(ids.lastIndexOf("/") + 1).split(",");
		TestUtils.submit(driver);
		// La table liste des essaims d'id "essaims" est affichée
		assertEquals("table", driver.findElement(By.id("essaims")).getTagName());
		for (String str : idsT) {
			assertTrue("COMMENTAIREESSAIM".equals(typeEveEssaimId(str)));
		}
	}

	@Test
	@Order(4)
	@DisplayName("Essaim création sucre lot")
	void creeSucreEssaim() {
		driver.get(baseUrl + "essaim/liste");
		// La table liste des essaims d'id "essaims" est affichée
		assertEquals("table", driver.findElement(By.id("essaims")).getTagName());
		// Sélectionner les lignes des deux premiers essaims

		// TODO en fait on ne récupère pas les derniers essaims, le tri est dans le
		// mauvais sens

		TestUtils.xpathClick(driver, "//table/tbody/tr[1]/td[4]");
		TestUtils.xpathClick(driver, "//table/tbody/tr[2]/td[4]");
		driver.findElement(By.id("sucre")).click();
		assertEquals("form", driver.findElement(By.id("evenementForm")).getTagName());
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		driver.findElement(By.name(valeur)).sendKeys("0,5");
		String ids = driver.getCurrentUrl();
		String[] idsT = ids.substring(ids.lastIndexOf("/") + 1).split(",");
		TestUtils.submit(driver);
		// La table liste des essaims d'id "essaims" est affichée
		assertEquals("table", driver.findElement(By.id("essaims")).getTagName());

		// TODO erreur la date des événements est identique (même minute)
		// on récupère les événements commentaire, le test suivant plante

		for (String str : idsT) {
			assertTrue("ESSAIMSUCRE".equals(typeEveEssaimId(str)));
		}
	}

	/*
	 * Renvoie le type eve du dernier événement de l'essaim id. Appel API REST.
	 */
	String typeEveEssaimId(String id) {
		// recherche des événements essaim avec l'api rest
		driver.get(baseUrl + "rest/evenements/search/findByEssaimId?essaimId=" + id);
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode actualObj = mapper.readTree(driver.findElement(By.tagName("pre")).getText());
			// On prend le type du premier événement
			JsonNode jsonNode = actualObj.get("_embedded").get("evenementRepository").get(1).get("type");
			return jsonNode.textValue();
		} catch (JsonProcessingException e) {
			return "";
		}
	}

}
