package ooioo.ruches;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheRepository;
import ooioo.ruches.rucher.Rucher;
import ooioo.ruches.rucher.RucherRepository;
import ooioo.ruches.rucher.RucherService;

// https://docs.spring.io/spring-security/reference/servlet/test/method.html

@SpringBootTest
public class TestRucheRepository {

	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private RucherRepository rucherRepository;
	@Autowired
	private RucherService rucherService;
	
	@Test
	@DisplayName("Création et lecture de la ruche 007")
	public void sauveRuche() {
		Ruche ruche = new Ruche();
		ruche.setNom("007");
		Rucher rucher = rucherRepository.findByDepotIsTrue();
		LatLon latLon = rucherService.dispersion(rucher.getLatitude(), rucher.getLongitude());
		ruche.setLatitude(latLon.getLat());
		ruche.setLongitude(latLon.getLon());
		ruche.setRucher(rucher);
		rucheRepository.save(ruche);
		
		String nom = rucheRepository.findByNom("007").getNom();
		assertEquals(nom, "007");
		
	}
	
}
