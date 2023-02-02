package ooioo.ruches.selenium;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.support.ui.Select;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestChrome {

	static WebDriver driver;
	static final String comment = "Test selenium chrome";

	// Attention certains tests écrivent en base de donnée.
	// Ne pas utiliser sur application en production !!!!!!!!!
	// Démarrer l'application correspondant à cette url !!!!
	static final String baseUrl = "http://localhost:8080/ruches/";

	// Initialisation du navigateur Chrome et login
	// Créer un admin, nom : test, prénom : testp, login : test, password : testpwd
	// Attention à ne pas mettre de mot de passe de production !
	static final String user = "test";
	static final String pwd = "testpwd";
	static final String role = "[ROLE_admin]";

	static final String commentaire = "commentaire";
	static final String modif = " - modifié";

	@BeforeAll
	static void initChrome() {
//		System.setProperty("webdriver.chrome.driver","/home/noe/selenium/driver/chrome109/chromedriver");
		System.setProperty("webdriver.chrome.driver", "/snap/bin/chromium.chromedriver");
//		https://github.com/SeleniumHQ/selenium/issues/10969
//		https://github.com/SeleniumHQ/selenium/issues/7788
//		driver = new ChromeDriver();
//		erreur : unknown flag `port'
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
		driver.findElement(By.name("username")).sendKeys(user);
		driver.findElement(By.name("password")).sendKeys(pwd);
		driver.findElement(By.xpath("//input[@type='submit']")).click();
		// ******************** Logged **************************
		// Le titre de la page après login est "ruches"
		assertEquals("Ruches", driver.getTitle(),
				() -> "La connexion a échoué, avez-vous créé un utilisateur 'test' ?");
		// L'utilisateur est "test" avec un rôle admin
		assertAll("login, rôle", () -> assertEquals(user, driver.findElement(By.id("login")).getText()),
				() -> assertEquals(role, driver.findElement(By.id("role")).getText()));
	}

	@AfterAll
	static void quitChrome() {
		driver.quit();
	}

	void submit() {
		// https://www.lambdatest.com/blog/how-to-deal-with-element-is-not-clickable-at-point-exception-using-selenium/
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("window.scrollBy(0,document.body.scrollHeight)");
		js.executeScript("arguments[0].click();", driver.findElement(By.xpath("//input[@type='submit']")));
	}

	void clearSend(String key, String val) {
		WebElement we = driver.findElement(By.name(key));
		we.clear();
		we.sendKeys(val);
	}

	@Test
	void listeRuches() {
		driver.get(baseUrl + "ruche/liste");
		// La table d'id "ruches" est affichée
		assertEquals("table", driver.findElement(By.id("ruches")).getTagName());
	}

	@Test
	void listeRucheTypes() {
		driver.get(baseUrl + "rucheType/liste");
		// La table d'id "ruchetypes" est affichée
		assertEquals("table", driver.findElement(By.id("ruchetypes")).getTagName());
	}

	@Test
	void listeRuchesPlus() {
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
		driver.findElement(By.name("nom")).sendKeys("#" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
		// Pas de champ commentaire sur TypeRuche
		driver.findElement(By.xpath("//input[@type='submit']")).click();
		assertEquals("table", driver.findElement(By.id("ruchetypes")).getTagName());
		// Modification de la valeur de l'événement
		// pas de page détail après création d'un type de ruche
		// le code suivant ne récupère pas l'id du nouveau type créé
		// A corriger !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// api rest avec méthode dans repository findlastruchetype ?
		/*
		 * String urlDetail = driver.getCurrentUrl(); driver.get(baseUrl +
		 * "rucheType/modifie" + urlDetail.substring(urlDetail.lastIndexOf("/")));
		 * WebElement nbMax = driver.findElement(By.name("nbCadresMax")); nbMax.clear();
		 * // Sinon concaténation avec valeur par défaut ("8" + "10")
		 * nbMax.sendKeys("8");
		 * driver.findElement(By.xpath("//input[@type='submit']")).click(); // Page
		 * détail de l'événement modifié assertEquals("div",
		 * driver.findElement(By.id("ruchetypes")).getTagName());
		 */
	}

	@Test
	void creeModifRuche() {
		// Création d'une ruche
		// Attention écriture en base de données
		driver.get(baseUrl + "ruche/cree");
		// Formulaire de création de ruche
		assertEquals("form", driver.findElement(By.id("rucheForm")).getTagName());
		driver.findElement(By.name("nom")).sendKeys("#" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		// erreur: le bouton submit n'est pas cliquable, car non visible ?
		submit();
		// Page détail de la ruche créée
		assertEquals("div", driver.findElement(By.id("ruche")).getTagName());
		// Modification de la ruche
		String urlDetail = driver.getCurrentUrl();
		driver.get(baseUrl + "ruche/modifie" + urlDetail.substring(urlDetail.lastIndexOf("/")));
		driver.findElement(By.name(commentaire)).sendKeys(modif);
		submit();
		// Page détail de la ruche modifiée
		assertEquals("div", driver.findElement(By.id("ruche")).getTagName());
	}

	@Test
	void listeHausses() {
		// ******************** Liste des hausses **************************
		driver.get(baseUrl + "hausse/liste");
		// La table d'id "hausses" est affichée
		assertEquals("table", driver.findElement(By.id("hausses")).getTagName());
	}

	@Test
	void creeModifHausse() {
		// Création d'une hausse
		// Attention écriture en base de données
		driver.get(baseUrl + "hausse/cree");
		// Formulaire de création de ruche
		assertEquals("form", driver.findElement(By.id("hausseForm")).getTagName());
		driver.findElement(By.name("nom")).sendKeys("#" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		submit();
		// Page détail de la hausse créée
		assertEquals("div", driver.findElement(By.id("hausse")).getTagName());
		// Modification de la hausse
		String urlDetail = driver.getCurrentUrl();
		driver.get(baseUrl + "hausse/modifie" + urlDetail.substring(urlDetail.lastIndexOf("/")));
		driver.findElement(By.name(commentaire)).sendKeys(modif);
		submit();
		// Page détail de la hausse modifiée
		assertEquals("div", driver.findElement(By.id("hausse")).getTagName());
	}

	@Test
	void listeRuchers() {
		driver.get(baseUrl + "rucher/liste");
		// La table d'id "ruchers" est affichée
		assertEquals("table", driver.findElement(By.id("ruchers")).getTagName());
	}
	
	@Test
	void mapGgRuchers() {
		driver.get(baseUrl + "rucher/Gg");
		// La div d'id "map" est affichée
		assertEquals("div", driver.findElement(By.id("map")).getTagName());
	}
	
	@Test
	void mapIgnRuchers() {
		driver.get(baseUrl + "rucher/Ign");
		// La div d'id "map" est affichée
		assertEquals("div", driver.findElement(By.id("map")).getTagName());
	}
	
	@Test
	void mapOsmRuchers() {
		driver.get(baseUrl + "rucher/Osm");
		// La div d'id "map" est affichée
		assertEquals("div", driver.findElement(By.id("map")).getTagName());
	}
	
	@Test
	void statistiquesRuchers() {
		driver.get(baseUrl + "rucher/statistiques");
		// La table d'id "statistiques" est affichée
		assertEquals("table", driver.findElement(By.id("statistiques")).getTagName());
	}

	@Test
	@DisplayName("Transhumances des ruchers, groupées")
	void transhumGroupeRuchers() {
		driver.get(baseUrl + "rucher/historiques/true");
		// La table d'id "transhumances" est affichée
		assertEquals("table", driver.findElement(By.id("transhumances")).getTagName());
	}

	@Test
	@DisplayName("Transhumances des ruchers, non groupées")
	void transhumRuchers() {
		driver.get(baseUrl + "rucher/historiques/false");
		// La table d'id "transhumances" est affichée
		assertEquals("table", driver.findElement(By.id("transhumances")).getTagName());
	}
	
	@Test
	void creeModifRucher() {
		// Création d'un rucher
		// Attention écriture en base de données
		driver.get(baseUrl + "rucher/cree");
		// Formulaire de création de ruche
		assertEquals("form", driver.findElement(By.id("rucherForm")).getTagName());
		driver.findElement(By.name("nom")).sendKeys("#" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		submit();
		// Page détail du rucher créé
		assertEquals("div", driver.findElement(By.id("detailRucher")).getTagName());
		// Modification du rucher
		String urlDetail = driver.getCurrentUrl();
		driver.get(baseUrl + "rucher/modifie" + urlDetail.substring(urlDetail.lastIndexOf("/")));
		clearSend("altitude", "1000");
		submit();
		// Page détail du rucher modifiée
		assertEquals("div", driver.findElement(By.id("detailRucher")).getTagName());
	}

	@Test
	void listePersonnes() {
		driver.get(baseUrl + "personne/liste");
		// La table d'id "personnes" est affichée
		assertEquals("table", driver.findElement(By.id("personnes")).getTagName());
	}

	@Test
	void creeModifPersonne() {
		// Création d'une personne
		// Attention écriture en base de données
		driver.get(baseUrl + "personne/cree");
		// Formulaire de création de la personne
		assertEquals("form", driver.findElement(By.id("personneForm")).getTagName());
		driver.findElement(By.name("nom")).sendKeys("#" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
		driver.findElement(By.name("prenom")).sendKeys("test");
		// Pas de champ commentaire sur Personne
		submit();
		// Page détail de la personne créée
		assertEquals("div", driver.findElement(By.id("detailPersonne")).getTagName());
		// Modification de la personne
		String urlDetail = driver.getCurrentUrl();
		driver.get(baseUrl + "personne/modifie" + urlDetail.substring(urlDetail.lastIndexOf("/")));
		driver.findElement(By.name("adresse")).sendKeys("La croix - Sainte Victoire");
		submit();
		// Page détail de la personne modifiée
		assertEquals("div", driver.findElement(By.id("detailPersonne")).getTagName());
	}

	@Test
	void listeEssaims() {
		driver.get(baseUrl + "essaim/liste");
		// La table d'id "essaims" est affichée
		assertEquals("table", driver.findElement(By.id("essaims")).getTagName());
	}

	@Test
	void creeModifEssaim() {
		// Création d'un essaim
		// Attention écriture en base de données
		driver.get(baseUrl + "essaim/cree");
		// Formulaire de création de ruche
		assertEquals("form", driver.findElement(By.id("essaimForm")).getTagName());
		driver.findElement(By.name("nom")).sendKeys("#" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		submit();
		// Page détail du rucher créé
		assertEquals("div", driver.findElement(By.id("detailEssaim")).getTagName());
		// Modification de l'essaim
		String urlDetail = driver.getCurrentUrl();
		driver.get(baseUrl + "essaim/modifie" + urlDetail.substring(urlDetail.lastIndexOf("/")));
		driver.findElement(By.name(commentaire)).sendKeys(modif);
		submit();
		// Page détail de l'essaim modifiée
		assertEquals("div", driver.findElement(By.id("detailEssaim")).getTagName());

	}

	@Test
	void listeRecoltes() {
		// ******************** Liste des récoltes **************************
		driver.get(baseUrl + "recolte/liste");
		// La table d'id "recoltes" est affichée
		assertEquals("table", driver.findElement(By.id("recoltes")).getTagName());
	}

	@Test
	void creeRecolte() {
		// Création d'une récolte
		// Attention écriture en base de données
		driver.get(baseUrl + "recolte/cree");
		// Formulaire de création de la récolte
		assertEquals("form", driver.findElement(By.id("recolteForm")).getTagName());
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		submit();
		// Page détail de la récolte créée
		assertEquals("div", driver.findElement(By.id("detailRecolte")).getTagName());
		// Modification de la récolte
		String urlDetail = driver.getCurrentUrl();
		driver.get(baseUrl + "recolte/modifie" + urlDetail.substring(urlDetail.lastIndexOf("/")));
		driver.findElement(By.name(commentaire)).sendKeys(modif);
		submit();
		// Page détail de la récolte modifiée
		assertEquals("div", driver.findElement(By.id("detailRecolte")).getTagName());
	}

	@Test
	void listeEvenements() {
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
	void creeEtModifEvenement() {
		// Création d'un événement
		// Attention écriture en base de données
		driver.get(baseUrl + "evenement/cree");
		// Formulaire de création de l'événement
		assertEquals("form", driver.findElement(By.id("evenementForm")).getTagName());
		/*
		 * // Pour choisir un autre type d'événement WebElement typeSelEle =
		 * driver.findElement(By.name("type")); Select typeSelect = new
		 * Select(typeSelEle); //List<WebElement> optionTypeList =
		 * typeSelect.getOptions(); typeSelect.selectByValue("RUCHEAJOUTRUCHER");
		 */
		WebElement rucheSelEle = driver.findElement(By.name("ruche"));
		Select rucheSelect = new Select(rucheSelEle);
		rucheSelect.selectByIndex(1);
		WebElement rucherSelEle = driver.findElement(By.name("rucher"));
		Select rucherSelect = new Select(rucherSelEle);
		rucherSelect.selectByIndex(1);
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		submit();
		// Page détail de l'événement créé
		assertEquals("div", driver.findElement(By.id("detailEvenement")).getTagName());
		// Modification de la valeur de l'événement
		String urlDetail = driver.getCurrentUrl();
		driver.get(baseUrl + "evenement/modifie" + urlDetail.substring(urlDetail.lastIndexOf("/")));
		driver.findElement(By.name("valeur")).sendKeys("007");
		submit();
		// Page détail de l'événement modifié
		assertEquals("div", driver.findElement(By.id("detailEvenement")).getTagName());
	}

	@Test
	void adminParam() {
		driver.get(baseUrl + "parametres");
		// Le formulaire d'id "parametresForm" est affiché
		assertEquals("form", driver.findElement(By.id("parametresForm")).getTagName());
	}

	@Test
	void adminRest() {
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
	}

	@Test
	void adminLog() {
		driver.get(baseUrl + "admin/logs/logfile");
		// La page de log contient la classe d'authentification
		assertEquals(true, driver.getPageSource().contains("ooioo.ruches.SecurityConfig.onAuthenticationSuccess"));
	}

	@Test
	void adminInfos() {
		driver.get(baseUrl + "infos");
		// La div d'id "info" est présente
		assertEquals("div", driver.findElement(By.id("info")).getTagName());
	}

	@Test
	void adminTests() {
		driver.get(baseUrl + "tests");
		// La div d'id "tests" est présente
		assertEquals("div", driver.findElement(By.id("tests")).getTagName());
	}

	@Test
	void adminDoc() {
		driver.get(baseUrl + "doc/ruches.html");
		// La page de doc contient "LibreOffice"
		assertEquals(true, driver.getPageSource().contains("LibreOffice"));
	}

}
