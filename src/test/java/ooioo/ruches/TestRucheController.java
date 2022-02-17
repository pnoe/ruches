package ooioo.ruches;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import ooioo.ruches.ruche.RucheController;

// https://spring.io/guides/gs/testing-web/
// https://www.baeldung.com/integration-testing-in-spring
	
@Disabled("post à terminer...")
@WebMvcTest(RucheController.class)
public class TestRucheController {

	@Autowired
	private MockMvc mockMvc;
	
	@Test
	public void sauve() throws Exception {
		// TODO sauve est un post avec les données de la ruche à enregistrer !!!!!!!!!!!!!!!!!!!
		this.mockMvc.perform(post("/sauve")).andDo(print()).andExpect(status().isOk());
		//	.andExpect(content().string(containsString("Hello, World")));
	}
}
