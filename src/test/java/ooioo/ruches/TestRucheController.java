package ooioo.ruches;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import ooioo.ruches.ruche.RucheRepository;
import ooioo.ruches.rucher.RucherRepository;
import ooioo.ruches.rucher.RucherService;

// https://spring.io/guides/gs/testing-web/
// https://www.baeldung.com/integration-testing-in-spring

@Disabled("login à faire")
// @WebMvcTest(RucheController.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestRucheController {

	// @Autowired
	// private MockMvc mockMvc;

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private RucherRepository rucherRepository;
	@Autowired
	private RucherService rucherService;
	@Autowired
	private WebApplicationContext context;

	private MockMvc mvc;
	
	@Test
	@WithMockUser(username="admin", password="admin", roles={"admin"})
	public void sauve() throws Exception {
		

		
		
		// TODO sauve est un post avec les données de la ruche à enregistrer
		// !!!!!!!!!!!!!!!!!!!
		// this.mockMvc.perform(post("/ruche/sauve")).andDo(print()).andExpect(status().isOk());
		// .andExpect(content().string(containsString("Hello, World")));

		// avec debug as junit, result contient un header location
		//   de redirection vers /login et un status 302 (redirection)
		//  idem avec @WithMockUser
		
		// https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/test/web/client/TestRestTemplate.html#postForObject-java.lang.String-java.lang.Object-java.lang.Class-java.util.Map-
		// https://howtodoinjava.com/spring-boot2/testing/testresttemplate-post-example/
		
		/*
		String url = "http://localhost:" + port + "/ruche/sauve/";
		
		Ruche ruche = new Ruche();
		ruche.setNom("001");
		Rucher rucher = rucherRepository.findByDepotIsTrue();
		LatLon latLon = rucherService.dispersion(rucher.getLatitude(), rucher.getLongitude());
		ruche.setLatitude(latLon.getLat());
		ruche.setLongitude(latLon.getLon());
		ruche.setRucher(rucher);

		HttpHeaders headers = new HttpHeaders();
		headers.set("X-COM-PERSIST", "true");

		HttpEntity<Ruche> request = new HttpEntity<>(ruche, headers);

		ResponseEntity<String> result = this.restTemplate.postForEntity(url, request, String.class);

		assertEquals(302, result.getStatusCodeValue());
		String nom = rucheRepository.findByNom("001").getNom();
		assertEquals(nom, "001");
		*/
	}
}
