package ooioo.ruches.html;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// Validation de pages html.
//  https://validator.w3.org/docs/api.html
//            Est ce qu'il peut y avoir des erreurs html après traitement par thymeleaf ??????????????????????????????????????
public class Test1 {

//	https://docs.oracle.com/en/java/javase/17/docs/api/java.net.http/java/net/http/package-summary.html
//	https://www.baeldung.com/java-9-http-client
//	https://www.baeldung.com/java-httpclient-post
//	http://www.mastertheboss.com/java/top-solutions-for-java-http-clients/

	private final Logger logger = LoggerFactory.getLogger(Test1.class);
	
	static final String w3cVal = "https://validator.nu/";

	static final String baseUrl = "http://localhost:8080/ruches/";
	static final String user = "test";
	static final String pwd = "testpwd";

	public HttpClient cli = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
			.cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();

	void w3c(String html, String nomPage) throws IOException, InterruptedException {
		// https://github.com/validator/validator/wiki/Service-%C2%BB-Common-params
		HttpRequest reqW3c = HttpRequest.newBuilder().uri(URI.create(w3cVal + "?out=json"))
				.headers("Content-type", "text/html; charset=utf-8").POST(HttpRequest.BodyPublishers.ofString(html))
				.build();
		HttpResponse<String> respW3c = cli.send(reqW3c, HttpResponse.BodyHandlers.ofString());
		// https://github.com/validator/validator/wiki/Output-%C2%BB-JSON
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(respW3c.body());
		for (JsonNode j : actualObj.findValues("type")) {
			if (j.textValue().equals("error")) {
				logger.error(actualObj.toPrettyString());
				fail("Erreur " + nomPage);
				return;
			}
		}
		assertEquals(200, respW3c.statusCode());
	}

	@Test
	void login() {
		try {
			HttpRequest reqRuche = HttpRequest.newBuilder().uri(URI.create(baseUrl + "login")).GET().build();
			HttpResponse<String> respRuche = cli.send(reqRuche, HttpResponse.BodyHandlers.ofString());
			String rb = respRuche.body();
			assertEquals(200, respRuche.statusCode());
			w3c(rb, "login.html");
			// Connexion à l'application Ruches.
			// Récupérer le csrf du formulaire de login
			// <form action="/ruchestest/login" method="post"><input type="hidden"
			// name="_csrf" value="2cf67483-a65e-46d3-b18c-c5a01bdbe11f"/>
			String csrf = rb.substring(rb.indexOf("value=\"") + 7);
			csrf = csrf.substring(0, csrf.indexOf("\"/>"));
			HttpRequest login = HttpRequest.newBuilder()
					.uri(URI.create(baseUrl + "login?username=" + user + "&password=" + pwd + "&_csrf=" + csrf))
					.headers("Content-type", "application/x-www-form-urlencoded")
					.POST(HttpRequest.BodyPublishers.noBody()).build();
			HttpResponse<String> respLogin = cli.send(login, HttpResponse.BodyHandlers.ofString());
			// Validation html de la page de connexion
			assertEquals(200, respLogin.statusCode());
			w3c(respLogin.body(), "index.html");
			
		} catch (IOException | InterruptedException e) {
			System.out.println(e.getMessage());
		}
	}

}
