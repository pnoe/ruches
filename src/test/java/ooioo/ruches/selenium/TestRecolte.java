package ooioo.ruches.selenium;

import static ooioo.ruches.selenium.TestUtils.jsExceptionsList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.WebDriver;

/*
 * Enchainement de tests :
 *  création ruche, création hausse, ajout hausse sur ruche,
 *    création récolte, ajout hausse récolte
 */
@TestMethodOrder(OrderAnnotation.class)
public class TestRecolte {

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

	@Test
	@Order(2)
	@DisplayName("Ruche création")
	void creeRuche() {
		// Création d'une ruche
		// Attention écriture en base de données
		driver.get(baseUrl + "ruche/cree");
		// Formulaire de création de ruche
		assertEquals("form", driver.findElement(By.id("rucheForm")).getTagName());
		driver.findElement(By.name("nom")).sendKeys(TestUtils.nomMilli());
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		// erreur: le bouton submit n'est pas cliquable, car non visible ?
		TestUtils.submit(driver);
		// Page détail de la ruche créée
		assertEquals("div", driver.findElement(By.id("ruche")).getTagName());
		String urlDetail = driver.getCurrentUrl();
		rucheId = urlDetail.substring(urlDetail.lastIndexOf("/") + 1);
	}

	@Test
	@Order(3)
	@DisplayName("Hausse création")
	void creeHausse() {
		// Création d'une hausse
		// Attention écriture en base de données
		driver.get(baseUrl + "hausse/cree");
		// Formulaire de création de ruche
		assertEquals("form", driver.findElement(By.id("hausseForm")).getTagName());
		driver.findElement(By.name("nom")).sendKeys(TestUtils.nomMilli());
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		TestUtils.submit(driver);
		// Page détail de la hausse créée
		assertEquals("div", driver.findElement(By.id("hausse")).getTagName());
		String urlDetail = driver.getCurrentUrl();
		hausseId = urlDetail.substring(urlDetail.lastIndexOf("/") + 1);
	}

	@Test
	@Order(4)
	@DisplayName("Ruche ajout hausse")
	void ajoutHausse() {
		// Ajout hausse hausseId sur la ruche rucheId
		// Attention écriture en base de données
		driver.get(baseUrl + "ruche/hausses/" + rucheId);
		// table liste des hausses à ajouter
		assertEquals("table", driver.findElement(By.id("ajoutHausses")).getTagName());
		// rechercher href dont le texte est hausseId
		// puis click, ou appel direct
		String urlForm = "/ruches/evenement/ruche/hausse/ajout/" + rucheId + "/" + hausseId;
		// WebElement link = driver.findElement(By.xpath("//a[@href=\"" + urlForm +
		// "\"]"));
		TestUtils.xpathClick(driver, "//a[@href=\"" + urlForm + "\"]");
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		assertEquals("form", driver.findElement(By.id("evenementForm")).getTagName());
		TestUtils.submit(driver);
		assertEquals("table", driver.findElement(By.id("ajoutHausses")).getTagName());
	}

	@Test
	@Order(5)
	@DisplayName("Récolte création")
	void creeRecolte() {
		// Création d'une récolte
		// Attention écriture en base de données
		driver.get(baseUrl + "recolte/cree");
		// Formulaire de création de la récolte
		assertEquals("form", driver.findElement(By.id("recolteForm")).getTagName());
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		TestUtils.submit(driver);
		// Page détail de la récolte créée
		assertEquals("div", driver.findElement(By.id("detailRecolte")).getTagName());
		String urlDetail = driver.getCurrentUrl();
		recolteId = urlDetail.substring(urlDetail.lastIndexOf("/") + 1);
	}

	@Test
	@Order(6)
	@DisplayName("Récolte ajout hausse")
	void recolteAjoutHausse() {
		// Création d'une hausse de récolte.
		// Attention écriture en base de données
		driver.get(baseUrl + "recolte/choixHausses/" + recolteId);
		// table liste des hausses à ajouter
		assertEquals("table", driver.findElement(By.id("ajoutHausseRecolte")).getTagName());
		TestUtils.xpathClick(driver, "//table[@id='ajoutHausseRecolte']//td[contains(., " + hausseId + ")]");
		driver.findElement(By.id("ajouterHausses")).click();
		// la table retraitHausseRecolte contient maintenant la hausse d'id hausseId
		assertEquals("td",
				driver.findElement(By.xpath("//table[@id='retraitHausseRecolte']//td[contains(., " + hausseId + ")]"))
						.getTagName());
	}

	@Test
	@Order(7)
	@DisplayName("Récolte saisie tabulaire poids de miel")
	void modifHausseRecolte() {
		// Affichage du formulaitre de saisie tabulaire poids de miel
		// Attention écriture en base de données
		driver.get(baseUrl + "recolte/" + recolteId);
		assertEquals("div", driver.findElement(By.id("detailRecolte")).getTagName());
		driver.get(baseUrl + "recolte/haussesMiel/" + recolteId);
		assertEquals("table", driver.findElement(By.id("recolteMielTable")).getTagName());
	}

	@Test
	@Order(8)
	@DisplayName("Récolte statistiques poids de miel")
	void statRecoltePoids() {
		// Attention écriture en base de données
		driver.get(baseUrl + "recolte/" + recolteId);
		assertEquals("div", driver.findElement(By.id("detailRecolte")).getTagName());
		driver.get(baseUrl + "recolte/statistiques/essaim/" + recolteId);
		assertEquals("canvas", driver.findElement(By.id("ctx")).getTagName());
	}

}
