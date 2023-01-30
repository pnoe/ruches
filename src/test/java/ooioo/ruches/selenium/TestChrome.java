package ooioo.ruches.selenium;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestChrome {

	static WebDriver driver;

	// Attention certains tests écrivent en base de donnée
	// ne pas utiliser sur application en production !!!!!!!!!
	static String baseUrl = "http://localhost:8080/ruches/";

	// Initialisation du navigateur Chrome et login
	// Créer un admin, nom : test, prénom : testp, login : test, password : testpwd
	@BeforeAll
	static void initChrome() {
//		System.setProperty("webdriver.chrome.driver","/home/noe/selenium/driver/chrome109/chromedriver");
		System.setProperty("webdriver.chrome.driver", "/snap/bin/chromium.chromedriver");
//		https://github.com/SeleniumHQ/selenium/issues/10969
//		https://github.com/SeleniumHQ/selenium/issues/7788
		driver = new ChromeDriver((ChromeDriverService) (new ChromeDriverService.Builder() {
			@Override
			protected File findDefaultExecutable() {
				if (new File("/snap/bin/chromium.chromedriver").exists()) {
					@SuppressWarnings("serial")
					File f = new File("/snap/bin/chromium.chromedriver") {
						@Override
						public String getCanonicalPath() throws IOException {
							return this.getAbsolutePath();
						}
					};
					return f;
				} else {
					return super.findDefaultExecutable();
				}
			}
		}).build()); // , options);
		
//		https://www.selenium.dev/documentation/webdriver/elements/finders/
//		https://www.selenium.dev/documentation/webdriver/elements/locators/		
		// ******************** Login **************************
		driver.get(baseUrl + "login");
		// Le titre de la page de connexion est "Connexion"
		assertEquals("Connexion", driver.getTitle());
		driver.findElement(By.name("username")).sendKeys("test");
		driver.findElement(By.name("password")).sendKeys("testpwd");
		driver.findElement(By.xpath("//input[@type='submit']")).click();
		// ******************** Logged **************************
		// Le titre de la page après login est "ruches"
		assertEquals("Ruches", driver.getTitle());
		// L'utilisateur est "test" avec un rôle admin
		assertEquals("test", driver.findElement(By.id("login")).getText());
		assertEquals("[ROLE_admin]", driver.findElement(By.id("role")).getText());
	}

	@AfterAll
	static void quitChrome() {
		driver.quit();
	}

	@Test
	void ruches() {
		// ******************** Liste des ruches **************************
		driver.get(baseUrl + "ruche/liste");
		// La table d'id "ruches" est affichée
		assertEquals("table", driver.findElement(By.id("ruches")).getTagName());
		// ******************** Liste des type de ruches **************************
		driver.get(baseUrl + "rucheType/liste");
		// La table d'id "ruchetypes" est affichée
		assertEquals("table", driver.findElement(By.id("ruchetypes")).getTagName());
		// ******************** Liste détaillée des ruches **************************
		driver.get(baseUrl + "ruche/listeplus");
		// La table d'id "ruchesplus" est affichée
		assertEquals("table", driver.findElement(By.id("ruchesplus")).getTagName());
	}

	@Test
	void creeTypeRuche() {
		// Création du type de ruche "TestRucheType" avec 8 cadres max.
		// Attention écriture en base de données
		driver.get(baseUrl + "rucheType/cree");
		assertEquals("form", driver.findElement(By.id("rucheTypeForm")).getTagName());
		driver.findElement(By.name("nom")).sendKeys("TestRucheType" +
				new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
		WebElement nbMax = driver.findElement(By.name("nbCadresMax"));
		nbMax.clear(); // Sinon concaténation avec valeur par défaut ("8" + "10")
		nbMax.sendKeys("8");
		driver.findElement(By.xpath("//input[@type='submit']")).click();
		assertEquals("table", driver.findElement(By.id("ruchetypes")).getTagName());
	}

	@Test
	void creeRuche() {
		// Création d'une ruche
		// Attention écriture en base de données
		driver.get(baseUrl + "ruche/cree");
		// Formulaire de création de ruche
		assertEquals("form", driver.findElement(By.id("rucheForm")).getTagName());
		driver.findElement(By.name("nom")).sendKeys("Test" + 
				new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
		// https://www.lambdatest.com/blog/how-to-deal-with-element-is-not-clickable-at-point-exception-using-selenium/
		// erreur: le bouton submit n'est pas cliquable, car non visible ?
		JavascriptExecutor js = (JavascriptExecutor) driver;
//		js.executeScript("arguments[0].scrollIntoView(true);", btnSubmit);
		js.executeScript("window.scrollBy(0,document.body.scrollHeight)");
		js.executeScript("arguments[0].click();", 
				driver.findElement(By.xpath("//input[@type='submit']")));
//		driver.findElement(By.xpath("//input[@type='submit']")).click();
		// Page détail de la ruche créée
		assertEquals("div", driver.findElement(By.id("ruche")).getTagName());
	}

	@Test
	void hausses() {
		// ******************** Liste des hausses **************************
		driver.get(baseUrl + "hausse/liste");
		// La table d'id "hausses" est affichée
		assertEquals("table", driver.findElement(By.id("hausses")).getTagName());
	}
	
	@Test
	void creeHausse() {
		// Création d'une hausse
		// Attention écriture en base de données
		driver.get(baseUrl + "hausse/cree");
		// Formulaire de création de ruche
		assertEquals("form", driver.findElement(By.id("hausseForm")).getTagName());
		driver.findElement(By.name("nom")).sendKeys("Test" + 
				new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
		// https://www.lambdatest.com/blog/how-to-deal-with-element-is-not-clickable-at-point-exception-using-selenium/
		// erreur: le bouton submit n'est pas cliquable, car non visible ?
		JavascriptExecutor js = (JavascriptExecutor) driver;
//		js.executeScript("arguments[0].scrollIntoView(true);", btnSubmit);
		js.executeScript("window.scrollBy(0,document.body.scrollHeight)");
		js.executeScript("arguments[0].click();", 
				driver.findElement(By.xpath("//input[@type='submit']")));
//		driver.findElement(By.xpath("//input[@type='submit']")).click();
		// Page détail de la ruche créée
		assertEquals("div", driver.findElement(By.id("hausse")).getTagName());
	}

	@Test
	void ruchers() {
		// ******************** Liste des ruchers **************************
		driver.get(baseUrl + "rucher/liste");
		// La table d'id "ruchers" est affichée
		assertEquals("table", driver.findElement(By.id("ruchers")).getTagName());
	}

	@Test
	void personnes() {
		// ******************** Liste des personnes **************************
		driver.get(baseUrl + "personne/liste");
		// La table d'id "personnes" est affichée
		assertEquals("table", driver.findElement(By.id("personnes")).getTagName());
	}

	@Test
	void essaims() {
		// ******************** Liste des essaims **************************
		driver.get(baseUrl + "essaim/liste");
		// La table d'id "essaims" est affichée
		assertEquals("table", driver.findElement(By.id("essaims")).getTagName());
	}

	@Test
	void recoltes() {
		// ******************** Liste des récoltes **************************
		driver.get(baseUrl + "recolte/liste");
		// La table d'id "recoltes" est affichée
		assertEquals("table", driver.findElement(By.id("recoltes")).getTagName());
	}

	@Test
	void evenements() {
		// ******************** Liste des événements **************************
		driver.get(baseUrl + "evenement/liste");
		// La table d'id "evenements" est affichée
		assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
		driver.get(baseUrl + "evenement/essaim/listeSucre");
		// La table d'id "evenementssucre" est affichée
		assertEquals("table", driver.findElement(By.id("evenementssucre")).getTagName());
		driver.get(baseUrl + "evenement/essaim/listeTraitement/false");
		// La table d'id "evenements" est affichée
		assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
		driver.get(baseUrl + "evenement/ruche/listePoidsRuche");
		// La table d'id "evenements" est affichée
		assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
		driver.get(baseUrl + "evenement/ruche/listeCadreRuche");
		// La table d'id "evenements" est affichée
		assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
		driver.get(baseUrl + "evenement/hausse/listeRemplissageHausse");
		// La table d'id "evenements" est affichée
		assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
		driver.get(baseUrl + "evenement/rucher/listeRucheAjout");
		// La table d'id "evenements" est affichée
		assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
		driver.get(baseUrl + "evenement/listeNotif/false");
		// La table d'id "evenementsnotif" est affichée
		assertEquals("table", driver.findElement(By.id("evenementsnotif")).getTagName());
	}

	@Test
	void admin() {
		// ******************** Admin **************************
		driver.get(baseUrl + "parametres");
		// Le formulaire d'id "parametresForm" est affiché
		assertEquals("form", driver.findElement(By.id("parametresForm")).getTagName());
		driver.get(baseUrl + "rest");
		// api rest, json test sur lien vers repository recolteHausses
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode actualObj = mapper.readTree(driver.findElement(By.tagName("pre")).getText());
			JsonNode jsonNode = actualObj.get("_links").get("recolteHausses").get("href");
			assertTrue(jsonNode.textValue().endsWith("rest/recolteHausses"));
		} catch (JsonProcessingException e) {
			fail(e.getMessage());
		}
		driver.get(baseUrl + "admin/logs/logfile");
		// La page de log contient la classe d'authentification
		assertEquals(true, driver.getPageSource().contains("ooioo.ruches.SecurityConfig.onAuthenticationSuccess"));
		driver.get(baseUrl + "infos");
		// La div d'id "info" est présente
		assertEquals("div", driver.findElement(By.id("info")).getTagName());
		driver.get(baseUrl + "tests");
		// La div d'id "tests" est présente
		assertEquals("div", driver.findElement(By.id("tests")).getTagName());
		driver.get(baseUrl + "doc/ruches.html");
		// La page de doc contient "LibreOffice"
		assertEquals(true, driver.getPageSource().contains("LibreOffice"));
	}

}
