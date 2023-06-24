package ooioo.ruches.selenium;

import static ooioo.ruches.selenium.TestUtils.baseUrl;
import static ooioo.ruches.selenium.TestUtils.driver;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

@TestMethodOrder(OrderAnnotation.class)
public class TestListEve {

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

	private static Stream<Arguments> provideParameters() {
		return Stream.of(Arguments.of("true", "1", "Toute période"), Arguments.of("false", "1", "Toute période"),
				Arguments.of("true", "2", "Moins d'un an"), Arguments.of("false", "2", "Moins d'un an"));
	}

	// https://www.selenium.dev/documentation/webdriver/support_features/select_lists/
	// https://stackoverflow.com/questions/61483452/parameterized-test-with-two-arguments-in-junit-5-jupiter
	@DisplayName("Événements liste traitement")
	@Order(4)
	@ParameterizedTest
	@MethodSource("provideParameters")
	void listeEveTraite(String tous, String periodeVal, String peridodeTxt) {
		driver.get(baseUrl + "evenement/essaim/listeTraitement/" + tous);
		// La table d'id "evenements" est affichée
		assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
		WebElement elSelect = driver.findElement(By.id("periode"));
		Select select = new Select(elSelect);
		select.selectByVisibleText(peridodeTxt);
		assertEquals("table", driver.findElement(By.id("evenements")).getTagName());
		// on vérifie que le select à bien fonctionné
		WebElement el = driver.findElement(By.cssSelector("option[value='" + periodeVal + "']"));
		assertTrue(el.isSelected());
	}

}
