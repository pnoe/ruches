package ooioo.ruches.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class TestUtils {

	static ChromeDriver driver;
	static List<JavascriptException> jsExceptionsList = new ArrayList<>();

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
	static final String role = "Admin";

	static final WebDriver initChrome() {
		String pathChromeDriver = "/snap/bin/chromium.chromedriver";
//		System.setProperty("webdriver.chrome.driver","/home/noe/selenium/driver/chrome109/chromedriver");
//		System.setProperty("webdriver.chrome.driver", pathChromeDriver");
//		https://github.com/SeleniumHQ/selenium/issues/10969
//		https://github.com/SeleniumHQ/selenium/issues/7788
//		driver = new ChromeDriver();
//		erreur : unknown flag `port'

//      logs voir commentaires dans :
//		https://github.com/SeleniumHQ/selenium/blob/trunk/java/src/org/openqa/selenium/logging/LoggingPreferences.java
//      dans application.properties, ne marche pas non plus :
//		logging.level.org.openqa.selenium=INFO
//		logging.level.org.junit.jupiter=INFO
		ChromeOptions options = new ChromeOptions();
//		https://groups.google.com/g/chromedriver-users/c/xL5-13_qGaA
		options.addArguments("--remote-allow-origins=*");
		// Pour tests sans interface graphique de Chrome
		// options.addArguments("headless");
		// Pas d'effet sur les logs :
		// options.setLogLevel(ChromeDriverLogLevel.OFF);

		/*
		 * driver = new ChromeDriver((new ChromeDriverService.Builder() {
		 * 
		 * @Override protected File findDefaultExecutable() { if (new
		 * File(pathChromeDriver).exists()) {
		 * 
		 * @SuppressWarnings("serial") File f = new File(pathChromeDriver) {
		 * 
		 * @Override public String getCanonicalPath() throws IOException { return
		 * this.getAbsolutePath(); } }; return f; } else { return
		 * super.findDefaultExecutable(); } } }).build(), options);
		 */
		// driver = new ChromeDriver();

		// https://www.selenium.dev/documentation/webdriver/drivers/service/
		@SuppressWarnings("serial")
		ChromeDriverService service = new ChromeDriverService.Builder()
				.usingDriverExecutable(new File(pathChromeDriver) {
					@Override
					public String getCanonicalPath() throws IOException {
						return this.getAbsolutePath();
					}
				}).build();
		driver = new ChromeDriver(service);

		// listen to js exceptions
		DevTools devTools = driver.getDevTools();
		devTools.createSession();
		// List<JavascriptException> jsExceptionsList = new ArrayList<>();
		Consumer<JavascriptException> addEntry = jsExceptionsList::add;
		devTools.getDomains().events().addJavascriptExceptionListener(addEntry);

		driver.get(baseUrl + "login");
		// Le titre de la page de connexion est "Connexion"
		assertEquals("Ruches", driver.getTitle(), () -> "Avez-vous démarré l'application Ruches ?");
		driver.findElement(By.name("username")).sendKeys(user);
		driver.findElement(By.name("password")).sendKeys(pwd);
		driver.findElement(By.xpath("//input[@type='submit']")).click();
		// Le titre de la page après login est "ruches"
		assertEquals("Ruches", driver.getTitle(),
				() -> "La connexion a échoué, avez-vous créé un utilisateur 'test' ?");
		return driver;
	}

	// Renvoie un nom pour nommer les ruches, hausses et ruchers créés lors des
	// tests.
	// Les noms les plus récents apparaissent en premier dans les listes.
	public static final String nomMilli() {
		return "!" + Instant.now().until(Instant.ofEpochSecond(1900000000), ChronoUnit.MILLIS);
	}

	// Scroll down et click avec javascript
	public static final void submit(WebDriver driver) {
		// https://www.lambdatest.com/blog/how-to-deal-with-element-is-not-clickable-at-point-exception-using-selenium/
		// driver.findElement(By.xpath("//input[@type='submit']")).click();
		// ne fonctionne pas si le bouton submit n'est pas visible
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("window.scrollBy(0,document.body.scrollHeight)");
		js.executeScript("arguments[0].click();", driver.findElement(By.xpath("//input[@type='submit']")));
	}

	public static final void clearSend(WebDriver driver, String key, String val) {
		WebElement we = driver.findElement(By.name(key));
		we.clear();
		we.sendKeys(val);
	}

	public static final String getDepotId(WebDriver driver, String baseUrl) {
		// recherche de l'id du dépot avec l'api rest
		driver.get(baseUrl + "rest/ruchers/search/findByDepotTrue");
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

	public static final void xpathClick(WebDriver driver, String xpath) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
	}

	private TestUtils() {
		throw new IllegalStateException("Constant class");
	}

}
