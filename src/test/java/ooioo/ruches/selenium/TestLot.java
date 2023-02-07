package ooioo.ruches.selenium;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/*
 * Enchainement de tests des traitements par lot
 */
@TestMethodOrder(OrderAnnotation.class)
public class TestLot {

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

	// Click sur bouton submit
	void submit() {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		// scroll en bas de page pour click sur bouton submit
		js.executeScript("window.scrollBy(0,document.body.scrollHeight)");
		js.executeScript("arguments[0].click();", driver.findElement(By.xpath("//input[@type='submit']")));
	}

	void scrollToTop() {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("window.scrollTo(0,0)");
	}

	void xpathClick(String xpath) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		// js.executeScript("window.scrollBy(0,document.body.scrollHeight)");
		js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
	}

	void clearSend(String key, String val) {
		WebElement we = driver.findElement(By.name(key));
		we.clear();
		we.sendKeys(val);
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
		driver.findElement(By.name("nom")).sendKeys("!" + new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date()));
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		// erreur: le bouton submit n'est pas cliquable, car non visible ?
		submit();
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
		xpathClick("//table/tbody/tr[1]/td[4]");
		xpathClick("//table/tbody/tr[2]/td[4]");
		driver.findElement(By.id("commentaire")).click();
		assertEquals("form", driver.findElement(By.id("evenementForm")).getTagName());
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		submit();
		// La table liste des ruches d'id "ruches" est affichée
		assertEquals("table", driver.findElement(By.id("ruches")).getTagName());
	}

}
