package ooioo.ruches.selenium;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class TestUtils {

	// Print pour debug
	public static final void print(String txt) {
		String sep =
			"********************************************************************************************************";
		System.out.println(sep);
		System.out.println(txt);
		System.out.println(sep);
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

	public static final void xpathClick(WebDriver driver, String xpath) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
	}

}
