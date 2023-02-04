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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
	// Créer un admin, nom : xx, prénom : yy, login : test, password : testpwd, role
	// : admin
	// le nom et le prénom peuvent être quelconques
	// Attention à ne pas mettre de mot de passe de production !
	static final String user = "test";
	static final String pwd = "testpwd";
	static final String role = "[ROLE_admin]";

	static final String commentaire = "commentaire";
	static final String modif = " - modifié";

	String depotId;

	@BeforeAll
	static void initChrome() {
		String pathChromeDriver = "/snap/bin/chromium.chromedriver";
//		System.setProperty("webdriver.chrome.driver","/home/noe/selenium/driver/chrome109/chromedriver");
//		System.setProperty("webdriver.chrome.driver", pathChromeDriver");
//		https://github.com/SeleniumHQ/selenium/issues/10969
//		https://github.com/SeleniumHQ/selenium/issues/7788
//		driver = new ChromeDriver();
//		erreur : unknown flag `port'
		driver = new ChromeDriver((ChromeDriverService) (new ChromeDriverService.Builder() {
			@Override
			protected File findDefaultExecutable() {
				if (new File(pathChromeDriver).exists()) {
					@SuppressWarnings("serial")
					File f = new File(pathChromeDriver) {
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
		// Le titre de la page après login est "ruches"
		assertEquals("Ruches", driver.getTitle(),
				() -> "La connexion a échoué, avez-vous créé un utilisateur 'test' ?");
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
	@DisplayName("Ruches liste")
	void listeRuches() {
		driver.get(baseUrl + "ruche/liste");
		// La table d'id "ruches" est affichée
		assertEquals("table", driver.findElement(By.id("ruches")).getTagName());
	}

	@Test
	@DisplayName("Ruche Types liste")
	void listeRucheTypes() {
		driver.get(baseUrl + "rucheType/liste");
		// La table d'id "ruchetypes" est affichée
		assertEquals("table", driver.findElement(By.id("ruchetypes")).getTagName());
	}

	@Test
	@DisplayName("Ruches liste plus")
	void listeRuchesPlus() {
		driver.get(baseUrl + "ruche/listeplus");
		// La table d'id "ruchesplus" est affichée
		assertEquals("table", driver.findElement(By.id("ruchesplus")).getTagName());
	}

	@Test
	@DisplayName("Ruche type création")
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
		// api rest avec méthode dans repository findlastruchetype ?
	}

	@Test
	@DisplayName("Ruche création/modif")
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
	@DisplayName("Hausses liste")
	void listeHausses() {
		driver.get(baseUrl + "hausse/liste");
		// La table d'id "hausses" est affichée
		assertEquals("table", driver.findElement(By.id("hausses")).getTagName());
	}

	@Test
	@DisplayName("Hausse création/modif")
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
	@DisplayName("Ruchers liste")
	void listeRuchers() {
		driver.get(baseUrl + "rucher/liste");
		// La table d'id "ruchers" est affichée
		assertEquals("table", driver.findElement(By.id("ruchers")).getTagName());
	}

	@Test
	@DisplayName("Ruchers carte Gg")
	void mapGgRuchers() {
		driver.get(baseUrl + "rucher/Gg");
		// La div d'id "map" est affichée
		assertEquals("div", driver.findElement(By.id("map")).getTagName());
	}

	@Test
	@DisplayName("Ruchers carte IGN")
	void mapIgnRuchers() {
		driver.get(baseUrl + "rucher/Ign");
		// La div d'id "map" est affichée
		assertEquals("div", driver.findElement(By.id("map")).getTagName());
	}

	@Test
	@DisplayName("Ruchers carte OSM")
	void mapOsmRuchers() {
		driver.get(baseUrl + "rucher/Osm");
		// La div d'id "map" est affichée
		assertEquals("div", driver.findElement(By.id("map")).getTagName());
	}

	@Test
	@DisplayName("Ruchers statistiques")
	void statistiquesRuchers() {
		driver.get(baseUrl + "rucher/statistiques");
		// La table d'id "statistiques" est affichée
		assertEquals("table", driver.findElement(By.id("statistiques")).getTagName());
	}

	@DisplayName("Ruchers Transhumances")
	@ParameterizedTest
	@ValueSource(strings = { "true", "false" })
	void transhumanceRuchers(String groupe) {
		driver.get(baseUrl + "rucher/historiques/" + groupe);
		// La table d'id "transhumances" est affichée
		assertEquals("table", driver.findElement(By.id("transhumances")).getTagName());
	}

	@Test
	@DisplayName("Rucher création/modif")
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

	String getDepotId() {
		// recherche de l'id du dépot avec l'api rest
		driver.get(baseUrl + "rest/ruchers/search/findByDepotIsTrue");
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode actualObj = mapper.readTree(driver.findElement(By.tagName("pre")).getText());
			JsonNode jsonNode = actualObj.get("_links").get("self").get("href");
			String urlDepot = jsonNode.textValue();
			return urlDepot.substring(urlDepot.lastIndexOf("/") + 1);
		} catch (JsonProcessingException e) {
			return "";
		}
	}

	@Test
	@DisplayName("Rucher dépot carte Gg")
	void mapGgDepot() {
		if (depotId == null) {
			depotId = getDepotId();
		}
		if ("".equals(depotId)) {
			fail("Api rest recherche de l'id du dépôt");
		} else {
			driver.get(baseUrl + "rucher/Gg/" + depotId);
			// La div d'id "map" est affichée
			assertEquals("div", driver.findElement(By.id("map")).getTagName());
		}
	}

	@Test
	@DisplayName("Rucher dépot carte IGN")
	void mapIgnDepot() {
		if (depotId == null) {
			depotId = getDepotId();
		}
		if ("".equals(depotId)) {
			fail("Api rest recherche de l'id du dépôt");
		} else {
			driver.get(baseUrl + "rucher/Ign/" + depotId);
			// La div d'id "map" est affichée
			assertEquals("div", driver.findElement(By.id("map")).getTagName());
		}
	}

	@Test
	@DisplayName("Rucher dépot carte OSM")
	void mapOsmDepot() {
		if (depotId == null) {
			depotId = getDepotId();
		}
		if ("".equals(depotId)) {
			fail("Api rest recherche de l'id du dépôt");
		} else {
			driver.get(baseUrl + "rucher/Osm/" + depotId);
			// La div d'id "map" est affichée
			assertEquals("div", driver.findElement(By.id("map")).getTagName());
		}
	}

	@Test
	@DisplayName("Rucher dépot Météo")
	void meteoDepot() {
		if (depotId == null) {
			depotId = getDepotId();
		}
		if ("".equals(depotId)) {
			fail("Api rest recherche de l'id du dépôt");
		} else {
			driver.get(baseUrl + "rucher/meteo/" + depotId);
			// La div d'id "accordion" est affichée
			assertEquals("div", driver.findElement(By.id("accordion")).getTagName());
		}
	}
	
	@DisplayName("Rucher dépôt Transhumances")
	@ParameterizedTest
	@ValueSource(strings = { "true", "false" })
	void transhumDepot(String groupe) {
		if (depotId == null) {
			depotId = getDepotId();
		}
		if ("".equals(depotId)) {
			fail("Api rest recherche de l'id du dépôt");
		} else {
			driver.get(baseUrl + "rucher/historique/" + depotId + "/" + groupe);
			// La table d'id "transhumances" est affichée
			assertEquals("table", driver.findElement(By.id("transhumances")).getTagName());
		}
	}
	
	@DisplayName("Rucher dépôt ajouter ruches")
	@Test
	void rucherRucheDepot() {
		if (depotId == null) {
			depotId = getDepotId();
		}
		if ("".equals(depotId)) {
			fail("Api rest recherche de l'id du dépôt");
		} else {
			driver.get(baseUrl + "rucher/ruches/" + depotId);
			// La table d'id "ajoutRuches" est affichée
			assertEquals("table", driver.findElement(By.id("ajoutRuches")).getTagName());
		}
	}

	@Test
	@DisplayName("Personnes liste")
	void listePersonnes() {
		driver.get(baseUrl + "personne/liste");
		// La table d'id "personnes" est affichée
		assertEquals("table", driver.findElement(By.id("personnes")).getTagName());
	}

	@Test
	@DisplayName("Personne création/modif")
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
	@DisplayName("Essaims liste")
	void listeEssaims() {
		driver.get(baseUrl + "essaim/liste");
		// La table d'id "essaims" est affichée
		assertEquals("table", driver.findElement(By.id("essaims")).getTagName());
	}

	@Test
	@DisplayName("Essaim création/modif")
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
	@DisplayName("Essaims statistiques")
	void essaimStatistiques() {
		driver.get(baseUrl + "essaim/statistiques");
		// La table d'id "statistiques" est affichée
		assertEquals("table", driver.findElement(By.id("statistiques")).getTagName());
	}

	@Test
	@DisplayName("Essaims âge des reines")
	void essaimAgeReines() {
		driver.get(baseUrl + "essaim/statistiquesage");
		// Le canevas d'id "ctx" est affiché
		assertEquals("canvas", driver.findElement(By.id("ctx")).getTagName());
	}

	@Test
	@DisplayName("Récoltes liste")
	void listeRecoltes() {
		driver.get(baseUrl + "recolte/liste");
		// La table d'id "recoltes" est affichée
		assertEquals("table", driver.findElement(By.id("recoltes")).getTagName());
	}

	@Test
	@DisplayName("Récolte création")
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
	@DisplayName("Récoltes statistiques essaim")
	void recoltesStatEssaim() {
		driver.get(baseUrl + "recolte/statistiques/essaim");
		// La tables d'id "recoltes" est affichée
		assertEquals("table", driver.findElement(By.id("recoltes")).getTagName());
	}

	@Test
	@DisplayName("Récoltes statistiques production")
	void recoltesStatProd() {
		driver.get(baseUrl + "recolte/statprod");
		// Le canevas d'id "ctx" est affiché
		assertEquals("canvas", driver.findElement(By.id("ctx")).getTagName());
	}

	@DisplayName("Événements liste")
	@ParameterizedTest
	@ValueSource(strings = { "", "?periode=2" })
	void listeEve(String periode) {
		driver.get(baseUrl + "evenement/liste" + periode);
		// La table d'id "evenements" est affichée
		assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
	}

	@Test
	@DisplayName("Événements liste sucre")
	void listeEveSucre() {
		driver.get(baseUrl + "evenement/essaim/listeSucre");
		// La table d'id "evenementssucre" est affichée
		assertEquals("table", driver.findElement(By.id("evenementssucre")).getTagName());
	}

	@DisplayName("Événements liste traitement")
	@ParameterizedTest
	@ValueSource(strings = { "true", "false" })
	void listeEveTraite(String tous) {
		driver.get(baseUrl + "evenement/essaim/listeTraitement/" + tous);
		// La table d'id "evenements" est affichée
		assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
	}

	@Test
	@DisplayName("Événements liste poids ruche")
	void listeEvePoids() {
		driver.get(baseUrl + "evenement/ruche/listePoidsRuche");
		// La table d'id "evenements" est affichée
		assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
	}

	@Test
	@DisplayName("Événements liste cadre ruche")
	void listeEveCadre() {
		driver.get(baseUrl + "evenement/ruche/listeCadreRuche");
		// La table d'id "evenements" est affichée
		assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
	}

	@Test
	@DisplayName("Événements liste remplissage")
	void listeEveRemplissage() {
		driver.get(baseUrl + "evenement/hausse/listeRemplissageHausse");
		// La table d'id "evenements" est affichée
		assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
	}

	@Test
	@DisplayName("Événements liste ruche ajout")
	void listeEveRucheAjout() {
		driver.get(baseUrl + "evenement/rucher/listeRucheAjout");
		// La table d'id "evenements" est affichée
		assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
	}

	@ParameterizedTest
	@ValueSource(strings = { "true", "false" })
	@DisplayName("Événements liste notifications")
	void listeEveNotif(String tous) {
		driver.get(baseUrl + "evenement/listeNotif/" + tous);
		// La table d'id "evenementsnotif" est affichée
		assertEquals("table", driver.findElement(By.id("evenementsnotif")).getTagName());
	}

	@Test
	@DisplayName("Événements liste rucher dépot")
	void listeEveDepot() {
		if (depotId == null) {
			depotId = getDepotId();
		}
		if ("".equals(depotId)) {
			fail("Api rest recherche de l'id du dépôt");
		} else {
			driver.get(baseUrl + "evenement/rucher/7");
			// La table d'id "evenements" est affichée
			assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
		}
	}
	
	@Test
	@DisplayName("Événement création/modif")
	void creeEtModifEvenement() {
		// Création d'un événement
		// Attention écriture en base de données
		driver.get(baseUrl + "evenement/cree");
		// Formulaire de création de l'événement
		assertEquals("form", driver.findElement(By.id("evenementForm")).getTagName());
		// Pour choisir un autre type d'événement :
		// WebElement typeSelEle = driver.findElement(By.name("type"));
		// Select typeSelect = new Select(typeSelEle);
		// List<WebElement> optionTypeList = typeSelect.getOptions();
		// typeSelect.selectByValue("RUCHEAJOUTRUCHER");
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
	@DisplayName("Admin paramètres")
	void adminParam() {
		driver.get(baseUrl + "parametres");
		// Le formulaire d'id "parametresForm" est affiché
		assertEquals("form", driver.findElement(By.id("parametresForm")).getTagName());
	}

	@Test
	@DisplayName("Admin API REST")
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
	@DisplayName("Admin logs")
	void adminLog() {
		driver.get(baseUrl + "admin/logs/logfile");
		// La page de log contient la classe d'authentification
		assertTrue(driver.getPageSource().contains("ooioo.ruches.SecurityConfig.onAuthenticationSuccess"));
	}

	@Test
	@DisplayName("Admin infos")
	void adminInfos() {
		driver.get(baseUrl + "infos");
		// La div d'id "info" est présente
		assertEquals("div", driver.findElement(By.id("info")).getTagName());
	}

	@Test
	@DisplayName("Admin tests")
	void adminTests() {
		driver.get(baseUrl + "tests");
		// La div d'id "tests" est présente
		assertEquals("div", driver.findElement(By.id("tests")).getTagName());
	}

	@Test
	@DisplayName("Admin documentation")
	void adminDoc() {
		driver.get(baseUrl + "doc/ruches.html");
		// La page de doc contient "LibreOffice"
		assertTrue(driver.getPageSource().contains("LibreOffice"));
	}

}
