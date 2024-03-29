package ooioo.ruches.selenium;

//https://docs.oracle.com/javase/7/docs/technotes/guides/language/static-import.html

import static ooioo.ruches.selenium.TestUtils.baseUrl;
import static ooioo.ruches.selenium.TestUtils.driver;
import static ooioo.ruches.selenium.TestUtils.jsExceptionsList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;

/*
 * Enchainement de tests :
 *  création ruche, création rucher, ajout ruche dans rucher,
 *  création essaim, ajout essaim dans ruche
 */

@TestMethodOrder(OrderAnnotation.class)
public class TestEssaim {

	// private final Logger logger = LoggerFactory.getLogger(TestEssaim.class);

	static final String comment = "Test selenium chrome";

	static final String commentaire = "commentaire";
	static final String modif = " - modifié";

	static String rucheId;
	static String rucherId;
	static String hausseId;
	static String essaimId;
	static String essaimIdNew;

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
	@DisplayName("Rucher création")
	void creeRucher() {
		// Création d'un rucher
		// Attention écriture en base de données
		driver.get(baseUrl + "rucher/cree");
		// Formulaire de création de ruche
		assertEquals("form", driver.findElement(By.id("rucherForm")).getTagName());
		driver.findElement(By.name("nom")).sendKeys(TestUtils.nomMilli());
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		TestUtils.submit(driver);
		// Page détail du rucher créé
		assertEquals("div", driver.findElement(By.id("detailRucher")).getTagName());
		String urlDetail = driver.getCurrentUrl();
		rucherId = urlDetail.substring(urlDetail.lastIndexOf("/") + 1);
	}

	@Test
	@Order(4)
	@DisplayName("Ajout ruche dans rucher")
	void rucheRucher() {
		// On pourrait aussi cliquer sur la ruche dans la liste des ruchers
		driver.get(baseUrl + "rucher/" + rucherId);
		driver.findElement(By.id("ajoutRuches")).click();
		assertEquals("table", driver.findElement(By.id("ajoutRuches")).getTagName());
		TestUtils.xpathClick(driver, "//table[@id='ajoutRuches']//td[contains(., " + rucheId + ")]");
		driver.findElement(By.id("actionAjoutRuches")).click();
		assertEquals("form", driver.findElement(By.id("evenementForm")).getTagName());
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		// On augmente la date du formulaire de 2 minutes
		// sinon elle est rejetée par les controles javascript
		String dateS = driver.findElement(By.name("date")).getAttribute("value");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
		LocalDateTime dateTime = LocalDateTime.parse(dateS, formatter);
		dateTime = dateTime.plusMinutes(2);
		TestUtils.clearSend(driver, "date", dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
		TestUtils.submit(driver);
		assertEquals("table", driver.findElement(By.id("ajoutRuches")).getTagName());
	}

	@Test
	@Order(5)
	@DisplayName("Essaim création")
	void creeEssaim() {
		// Création d'un essaim
		// Attention écriture en base de données
		driver.get(baseUrl + "essaim/cree");
		// Formulaire de création de l'essaim
		assertEquals("form", driver.findElement(By.id("essaimForm")).getTagName());
		driver.findElement(By.name("nom")).sendKeys(TestUtils.nomMilli());
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		TestUtils.submit(driver);
		// Page détail de l'essaim créé
		assertEquals("div", driver.findElement(By.id("detailEssaim")).getTagName());
		String urlDetail = driver.getCurrentUrl();
		essaimId = urlDetail.substring(urlDetail.lastIndexOf("/") + 1);
	}

	@Test
	@Order(6)
	@DisplayName("Essaim mis en ruche")
	void essaimRuche() {
		// Attention écriture en base de données
		driver.get(baseUrl + "essaim/" + essaimId);
		assertEquals("div", driver.findElement(By.id("detailEssaim")).getTagName());
		driver.findElement(By.id("assoRuche")).click();
		assertEquals("table", driver.findElement(By.id("associerRucheEssaim")).getTagName());
		TestUtils.xpathClick(driver, "//a[contains(@href, '/" + rucheId + "/')]");
		// On valide le formulaire pour l'eve essaim mis dans ruche
		TestUtils.submit(driver);
		assertEquals("div", driver.findElement(By.id("detailEssaim")).getTagName());
	}

	@Test
	@Order(7)
	@DisplayName("Essaimage")
	void essaimage() {
		// Attention écriture en base de données
		driver.get(baseUrl + "essaim/" + essaimId);
		assertEquals("div", driver.findElement(By.id("detailEssaim")).getTagName());
		driver.findElement(By.id("essaime")).click();
		assertEquals("form", driver.findElement(By.id("essaimeForm")).getTagName());
		driver.findElement(By.name(commentaire)).sendKeys(" - " + comment);
		TestUtils.submit(driver);
		String urlDetail = driver.getCurrentUrl();
		essaimIdNew = urlDetail.substring(urlDetail.lastIndexOf("/") + 1);
		assertEquals("div", driver.findElement(By.id("detailEssaim")).getTagName());
	}

	@Test
	@Order(8)
	@DisplayName("Arbre de descendance")
	void desc() {
		driver.get(baseUrl + "essaim/" + essaimIdNew);
		assertEquals("div", driver.findElement(By.id("detailEssaim")).getTagName());
		driver.findElement(By.id("descend")).click();
		assertEquals("div", driver.findElement(By.id("graph")).getTagName());
	}

	@Test
	@Order(9)
	@DisplayName("Dispersion essaim")
	void dispersion() {
		driver.get(baseUrl + "essaim/" + essaimIdNew);
		assertEquals("div", driver.findElement(By.id("detailEssaim")).getTagName());
		driver.findElement(By.id("dispersion")).click();
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		// On augmente la date du formulaire de 5 minutes
		// sinon elle est rejetée par les controles javascript
		String dateS = driver.findElement(By.name("date")).getAttribute("value");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
		LocalDateTime dateTime = LocalDateTime.parse(dateS, formatter);
		dateTime = dateTime.plusMinutes(5);
		TestUtils.clearSend(driver, "date", dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
		assertEquals("form", driver.findElement(By.id("evenementForm")).getTagName());
		TestUtils.submit(driver);
		assertEquals("table", driver.findElement(By.id("essaims")).getTagName());
	}

}
