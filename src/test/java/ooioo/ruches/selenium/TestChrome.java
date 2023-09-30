package ooioo.ruches.selenium;

import static ooioo.ruches.selenium.TestUtils.baseUrl;
import static ooioo.ruches.selenium.TestUtils.driver;
import static ooioo.ruches.selenium.TestUtils.jsExceptionsList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

@TestMethodOrder(OrderAnnotation.class)
public class TestChrome {

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
	@Order(100)
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
	@Order(200)
	@DisplayName("Ruches liste")
	void listeRuches() {
		driver.get(baseUrl + "ruche/liste");
		// La table d'id "ruches" est affichée
		assertEquals("table", driver.findElement(By.id("ruches")).getTagName());
	}

	@Test
	@Order(300)
	@DisplayName("Ruche Types liste")
	void listeRucheTypes() {
		driver.get(baseUrl + "rucheType/liste");
		// La table d'id "ruchetypes" est affichée
		assertEquals("table", driver.findElement(By.id("ruchetypes")).getTagName());
	}

	@Test
	@Order(400)
	@DisplayName("Ruches liste plus")
	void listeRuchesPlus() {
		driver.get(baseUrl + "ruche/listeplus");
		// La table d'id "ruchesplus" est affichée
		assertEquals("table", driver.findElement(By.id("ruchesplus")).getTagName());
	}

	@Test
	@Order(500)
	@DisplayName("Ruche type création")
	void creeTypeRuche() {
		// Création du type de ruche "TestRucheType" avec 8 cadres max.
		// Attention écriture en base de données
		driver.get(baseUrl + "rucheType/cree");
		assertEquals("form", driver.findElement(By.id("rucheTypeForm")).getTagName());
		driver.findElement(By.name("nom")).sendKeys(TestUtils.nomMilli());
		// Pas de champ commentaire sur TypeRuche
		driver.findElement(By.xpath("//input[@type='submit']")).click();
		// Page détail du type de ruche créé
		assertEquals("div", driver.findElement(By.id("ruchetype")).getTagName());
		// Modification du type de ruche
		String urlDetail = driver.getCurrentUrl();
		driver.get(baseUrl + "rucheType/modifie" + urlDetail.substring(urlDetail.lastIndexOf("/")));
		driver.findElement(By.name("nbCadresMax")).sendKeys("1000");
		TestUtils.submit(driver);
		// Page détail du type de ruche modifiée
		assertEquals("div", driver.findElement(By.id("ruchetype")).getTagName());
	}

	@Test
	@Order(600)
	@DisplayName("Ruche création/modif")
	void creeModifRuche() {
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
		// Modification de la ruche
		String urlDetail = driver.getCurrentUrl();
		driver.get(baseUrl + "ruche/modifie" + urlDetail.substring(urlDetail.lastIndexOf("/")));
		driver.findElement(By.name(commentaire)).sendKeys(modif);
		TestUtils.submit(driver);
		// Page détail de la ruche modifiée
		assertEquals("div", driver.findElement(By.id("ruche")).getTagName());
	}

	@Test
	@Order(700)
	@DisplayName("Hausses liste")
	void listeHausses() {
		driver.get(baseUrl + "hausse/liste");
		// La table d'id "hausses" est affichée
		assertEquals("table", driver.findElement(By.id("hausses")).getTagName());
	}

	@Test
	@Order(800)
	@DisplayName("Hausse création/modif")
	void creeModifHausse() {
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
		// Modification de la hausse
		String urlDetail = driver.getCurrentUrl();
		driver.get(baseUrl + "hausse/modifie" + urlDetail.substring(urlDetail.lastIndexOf("/")));
		driver.findElement(By.name(commentaire)).sendKeys(modif);
		TestUtils.submit(driver);
		// Page détail de la hausse modifiée
		assertEquals("div", driver.findElement(By.id("hausse")).getTagName());
	}

	@Test
	@Order(900)
	@DisplayName("Ruchers liste")
	void listeRuchers() {
		driver.get(baseUrl + "rucher/liste");
		// La table d'id "ruchers" est affichée
		assertEquals("table", driver.findElement(By.id("ruchers")).getTagName());
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
	@Order(1100)
	@DisplayName("Ruchers carte IGN")
	void mapIgnRuchers() {
		driver.get(baseUrl + "rucher/Ign");
		// La div d'id "map" est affichée
		assertEquals("div", driver.findElement(By.id("map")).getTagName());
	}

	@Test
	@Order(1200)
	@DisplayName("Ruchers carte OSM")
	void mapOsmRuchers() {
		driver.get(baseUrl + "rucher/Osm");
		// La div d'id "map" est affichée
		assertEquals("div", driver.findElement(By.id("map")).getTagName());
	}

	@Test
	@Order(1300)
	@DisplayName("Ruchers statistiques")
	void statistiquesRuchers() {
		driver.get(baseUrl + "rucher/statistiques");
		// La table d'id "statistiques" est affichée
		assertEquals("table", driver.findElement(By.id("statistiques")).getTagName());
	}

	@DisplayName("Ruchers Transhumances")
	@ParameterizedTest
	@Order(140)
	@ValueSource(strings = { "true", "false" })
	void transhumanceRuchers(String groupe) {
		driver.get(baseUrl + "rucher/historiques/" + groupe);
		// La table d'id "transhumances" est affichée
		assertEquals("table", driver.findElement(By.id("transhumances")).getTagName());
	}

	@Test
	@Order(150)
	@DisplayName("Rucher création/modif")
	void creeModifRucher() {
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
		// Modification du rucher
		String urlDetail = driver.getCurrentUrl();
		driver.get(baseUrl + "rucher/modifie" + urlDetail.substring(urlDetail.lastIndexOf("/")));
		TestUtils.clearSend(driver, "altitude", "1000");
		TestUtils.submit(driver);
		// Page détail du rucher modifiée
		assertEquals("div", driver.findElement(By.id("detailRucher")).getTagName());
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

	@Test
	@Order(170)
	@DisplayName("Rucher dépot carte IGN")
	void mapIgnDepot() {
		if (depotId == null) {
			depotId = TestUtils.getDepotId(driver, baseUrl);
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
	@Order(180)
	@DisplayName("Rucher dépot carte OSM")
	void mapOsmDepot() {
		if (depotId == null) {
			depotId = TestUtils.getDepotId(driver, baseUrl);
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
	@Order(190)
	@DisplayName("Rucher dépot Météo")
	void meteoDepot() {
		if (depotId == null) {
			depotId = TestUtils.getDepotId(driver, baseUrl);
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
	@Order(200)
	@ParameterizedTest
	@ValueSource(strings = { "true", "false" })
	void transhumDepot(String groupe) {
		if (depotId == null) {
			depotId = TestUtils.getDepotId(driver, baseUrl);
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
	@Order(210)
	@Test
	void rucherRucheDepot() {
		if (depotId == null) {
			depotId = TestUtils.getDepotId(driver, baseUrl);
		}
		if ("".equals(depotId)) {
			fail("Api rest recherche de l'id du dépôt");
		} else {
			driver.get(baseUrl + "rucher/ruches/" + depotId);
			// La table d'id "ajoutRuches" est affichée
			assertEquals("table", driver.findElement(By.id("ajoutRuches")).getTagName());
		}
	}
	
	@DisplayName("Rucher dépôt saisie des poids des ruches")
	@Order(211)
	@Test
	void rucherRuchePoids() {
		if (depotId == null) {
			depotId = TestUtils.getDepotId(driver, baseUrl);
		}
		if ("".equals(depotId)) {
			fail("Api rest recherche de l'id du dépôt");
		} else {
			driver.get(baseUrl + "rucher/poidsruches/" + depotId);
			// La table d'id "ruchesPoidsTbl" est affichée
			assertEquals("table", driver.findElement(By.id("ruchesPoidsTbl")).getTagName());
		}
	}
	

	@Test
	@Order(220)
	@DisplayName("Personnes liste")
	void listePersonnes() {
		driver.get(baseUrl + "personne/liste");
		// La table d'id "personnes" est affichée
		assertEquals("table", driver.findElement(By.id("personnes")).getTagName());
	}

	@Test
	@Order(230)
	@DisplayName("Personne création/modif")
	void creeModifPersonne() {
		// Création d'une personne
		// Attention écriture en base de données
		driver.get(baseUrl + "personne/cree");
		// Formulaire de création de la personne
		assertEquals("form", driver.findElement(By.id("personneForm")).getTagName());
		driver.findElement(By.name("nom")).sendKeys(TestUtils.nomMilli());
		driver.findElement(By.name("prenom")).sendKeys("test");
		// Pas de champ commentaire sur Personne
		TestUtils.submit(driver);
		// Page détail de la personne créée
		assertEquals("div", driver.findElement(By.id("detailPersonne")).getTagName());
		// Modification de la personne
		String urlDetail = driver.getCurrentUrl();
		driver.get(baseUrl + "personne/modifie" + urlDetail.substring(urlDetail.lastIndexOf("/")));
		driver.findElement(By.name("adresse")).sendKeys("La croix - Sainte Victoire");
		TestUtils.submit(driver);
		// Page détail de la personne modifiée
		assertEquals("div", driver.findElement(By.id("detailPersonne")).getTagName());
	}

	@Test
	@Order(240)
	@DisplayName("Essaims liste")
	void listeEssaims() {
		driver.get(baseUrl + "essaim/liste");
		// La table d'id "essaims" est affichée
		assertEquals("table", driver.findElement(By.id("essaims")).getTagName());
	}

	@Test
	@Order(250)
	@DisplayName("Essaim création/modif")
	void creeModifEssaim() {
		// Création d'un essaim
		// Attention écriture en base de données
		driver.get(baseUrl + "essaim/cree");
		// Formulaire de création de ruche
		assertEquals("form", driver.findElement(By.id("essaimForm")).getTagName());
		driver.findElement(By.name("nom")).sendKeys(TestUtils.nomMilli());
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		TestUtils.submit(driver);
		// Page détail du rucher créé
		assertEquals("div", driver.findElement(By.id("detailEssaim")).getTagName());
		// Modification de l'essaim
		String urlDetail = driver.getCurrentUrl();
		driver.get(baseUrl + "essaim/modifie" + urlDetail.substring(urlDetail.lastIndexOf("/")));
		driver.findElement(By.name(commentaire)).sendKeys(modif);
		TestUtils.submit(driver);
		// Page détail de l'essaim modifiée
		assertEquals("div", driver.findElement(By.id("detailEssaim")).getTagName());

	}

	@DisplayName("Essaims statistiques")
	@Order(260)
	@ParameterizedTest
	@ValueSource(strings = { "", "?masquerInactif=on", "depot" })
	void essaimStatistiques(String param) {
		if (param.equals("depot")) {
			if (depotId == null) {
				depotId = TestUtils.getDepotId(driver, baseUrl);
			}
			param = "?rucherId=" + depotId;
		}
		driver.get(baseUrl + "essaim/statistiques" + param);
		// La table d'id "statistiques" est affichée
		assertEquals("table", driver.findElement(By.id("statistiques")).getTagName());
	}

	@Test
	@Order(270)
	@DisplayName("Essaims âge des reines")
	void essaimAgeReines() {
		driver.get(baseUrl + "essaim/statistiquesage");
		// Le canevas d'id "ctx" est affiché
		assertEquals("canvas", driver.findElement(By.id("ctx")).getTagName());
	}

	@Test
	@Order(280)
	@DisplayName("Récoltes liste")
	void listeRecoltes() {
		driver.get(baseUrl + "recolte/liste");
		// La table d'id "recoltes" est affichée
		assertEquals("table", driver.findElement(By.id("recoltes")).getTagName());
	}

	@Test
	@Order(290)
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
		// Modification de la récolte
		String urlDetail = driver.getCurrentUrl();
		driver.get(baseUrl + "recolte/modifie" + urlDetail.substring(urlDetail.lastIndexOf("/")));
		driver.findElement(By.name(commentaire)).sendKeys(modif);
		TestUtils.submit(driver);
		// Page détail de la récolte modifiée
		assertEquals("div", driver.findElement(By.id("detailRecolte")).getTagName());
	}

	@Test
	@Order(300)
	@DisplayName("Récoltes statistiques essaim")
	void recoltesStatEssaim() {
		driver.get(baseUrl + "recolte/stat/essaim/false");
		// La tables d'id "recoltes" est affichée
		assertEquals("table", driver.findElement(By.id("recoltes")).getTagName());
		// TODO Tester afficher inactifs on puis clic sur filter
		// ci-dessous test sans clic en envoyant directement les bons paramètres
		driver.get(baseUrl + "recolte/statistiques/essaim?rucherId=0&masquerInactif=on");
	}

	@Test
	@Order(310)
	@DisplayName("Récoltes statistiques production")
	void recoltesStatProd() {
		driver.get(baseUrl + "recolte/statprod");
		// Le canevas d'id "ctx" est affiché

		// !!!!!!!!!! erreur sur base min
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

		assertEquals("canvas", driver.findElement(By.id("ctx")).getTagName());
	}

	@DisplayName("Événements liste")
	@Order(320)
	@ParameterizedTest
	@ValueSource(strings = { "", "?periode=2" })
	void listeEve(String periode) {
		driver.get(baseUrl + "evenement/liste" + periode);
		// La table d'id "evenements" est affichée
		assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
	}

	@Test
	@Order(330)
	@DisplayName("Événements liste sucre")
	void listeEveSucre() {
		driver.get(baseUrl + "evenement/essaim/listeSucre");
		// La table d'id "evenementssucre" est affichée
		assertEquals("table", driver.findElement(By.id("evenementssucre")).getTagName());
	}

	@DisplayName("Événements liste traitement")
	@Order(340)
	@ParameterizedTest
	@ValueSource(strings = { "true", "false" })
	void listeEveTraite(String tous) {
		driver.get(baseUrl + "evenement/essaim/listeTraitement/" + tous);
		// La table d'id "evenements" est affichée
		assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
	}

	@Test
	@Order(350)
	@DisplayName("Événements liste poids ruche")
	void listeEvePoids() {
		driver.get(baseUrl + "evenement/ruche/listePoidsRuche");
		// La table d'id "evenements" est affichée
		assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
	}

	@Test
	@Order(360)
	@DisplayName("Événements liste cadre ruche")
	void listeEveCadre() {
		driver.get(baseUrl + "evenement/ruche/listeCadreRuche");
		// La table d'id "evenements" est affichée
		assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
	}

	@Test
	@Order(370)
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
	@Order(380)
	@ValueSource(strings = { "true", "false" })
	@DisplayName("Événements liste notifications")
	void listeEveNotif(String tous) {
		driver.get(baseUrl + "evenement/listeNotif/" + tous);
		// La table d'id "evenementsnotif" est affichée
		assertEquals("table", driver.findElement(By.id("evenementsnotif")).getTagName());
	}

	@Test
	@Order(390)
	@DisplayName("Événements liste rucher dépot")
	void listeEveDepot() {
		if (depotId == null) {
			depotId = TestUtils.getDepotId(driver, baseUrl);
		}
		if ("".equals(depotId)) {
			fail("Api rest recherche de l'id du dépôt");
		} else {
			driver.get(baseUrl + "evenement/rucher/" + depotId);
			// La table d'id "evenements" est affichée
			assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
		}
	}

	@Test
	@Order(400)
	@DisplayName("Événements commentaire création rucher dépot")
	void creeEveCommDepot() {
		if (depotId == null) {
			depotId = TestUtils.getDepotId(driver, baseUrl);
		}
		if ("".equals(depotId)) {
			fail("Api rest recherche de l'id du dépôt");
		} else {
			driver.get(baseUrl + "evenement/rucher/commentaire/cree/" + depotId);
			LocalDateTime date = LocalDateTime.now().minusDays(20);
			TestUtils.clearSend(driver, "date", date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
			driver.findElement(By.name(commentaire)).sendKeys(comment);
			driver.findElement(By.name("valeur")).sendKeys("5");
			TestUtils.submit(driver);
			// La div d'id "detailRucher" est affichée
			assertEquals("div", driver.findElement(By.id("detailRucher")).getTagName());
		}
	}

	@Test
	@Order(410)
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
		rucheSelect.selectByIndex(10);
		WebElement rucherSelEle = driver.findElement(By.name("rucher"));
		Select rucherSelect = new Select(rucherSelEle);
		rucherSelect.selectByIndex(10);
		driver.findElement(By.name(commentaire)).sendKeys(comment);
		TestUtils.submit(driver);
		// Page détail de l'événement créé
		assertEquals("div", driver.findElement(By.id("detailEvenement")).getTagName());
		// Modification de la valeur de l'événement
		String urlDetail = driver.getCurrentUrl();
		driver.get(baseUrl + "evenement/modifie" + urlDetail.substring(urlDetail.lastIndexOf("/")));
		driver.findElement(By.name("valeur")).sendKeys("007");
		TestUtils.submit(driver);
		// Page détail de l'événement modifié
		assertEquals("div", driver.findElement(By.id("detailEvenement")).getTagName());
	}

	@Test
	@Order(420)
	@DisplayName("Admin paramètres")
	void adminParam() {
		driver.get(baseUrl + "parametres");
		// Le formulaire d'id "parametresForm" est affiché
		assertEquals("form", driver.findElement(By.id("parametresForm")).getTagName());
	}

	// 43 déplacée dans TestRest.java

	@Test
	@Order(440)
	@DisplayName("Admin logs")
	void adminLog() {
		driver.get(baseUrl + "admin/logs/logfile");
		// La page de log contient la classe d'authentification
		assertTrue(driver.getPageSource().contains("ooioo.ruches.SecurityConfig.onAuthenticationSuccess"));
	}

	@Test
	@Order(450)
	@DisplayName("Admin infos")
	void adminInfos() {
		driver.get(baseUrl + "infos");
		// La div d'id "info" est présente
		assertEquals("div", driver.findElement(By.id("info")).getTagName());
	}

	@Test
	@DisplayName("Admin tests")
	@Order(460)
	void adminTests() {
		driver.get(baseUrl + "tests");
		// La div d'id "tests" est présente
		assertEquals("div", driver.findElement(By.id("accordion")).getTagName());
	}

	@Test
	@Order(470)
	@DisplayName("Admin documentation")
	void adminDoc() {
		driver.get(baseUrl + "doc/ruches.html");
		// La page de doc contient "LibreOffice"
		assertTrue(driver.getPageSource().contains("LibreOffice"));
	}
	
	@Test
	@Order(480)
	@DisplayName("Rucher dépot distances ruchers")
	void distsDepot() {
		if (depotId == null) {
			depotId = TestUtils.getDepotId(driver, baseUrl);
		}
		if ("".equals(depotId)) {
			fail("Api rest recherche de l'id du dépôt");
		} else {
			driver.get(baseUrl + "rucher/dists/" + depotId);
			// La talbe d'id "distsRuchers" est affichée
			assertEquals("table", driver.findElement(By.id("distsRuchers")).getTagName());
		}
	}

}
