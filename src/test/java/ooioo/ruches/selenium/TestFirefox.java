package ooioo.ruches.selenium;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;

@Disabled
public class TestFirefox {

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

	static String depotId;

	public static FirefoxDriver getFirefoxDriver() {
		FirefoxOptions options = new FirefoxOptions();
		String osName = System.getProperty("os.name");
		String profileRoot = osName.contains("Linux") && new File("/snap/firefox").exists()
				? createProfileRootInUserHome()
				: null;
		return profileRoot != null ? new FirefoxDriver(createGeckoDriverService(profileRoot), options)
				: new FirefoxDriver(options);
	}

	private static String createProfileRootInUserHome() {
		String userHome = System.getProperty("user.home");
		File profileRoot = new File(userHome, "snap/firefox/common/.firefox-profile-root");
		if (!profileRoot.exists()) {
			if (!profileRoot.mkdirs()) {
				return null;
			}
		}
		return profileRoot.getAbsolutePath();
	}

	private static GeckoDriverService createGeckoDriverService(String tempProfileDir) {
		return new GeckoDriverService.Builder() {
			@Override
			protected List<String> createArgs() {
				List<String> args = new ArrayList<>(super.createArgs());
				args.add(String.format("--profile-root=%s", tempProfileDir));
				return args;
			}
		}.build();
	}

	@BeforeAll
	static void initFirefox() {

		
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("-h", "-p 4444");
        
       
        
        System.setProperty("webdriver.gecko.driver","/snap/bin/firefox.geckodriver");
        		
		// driver = getFirefoxDriver();
		driver = new FirefoxDriver(options);
		

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

}
