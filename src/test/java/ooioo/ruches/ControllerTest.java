package ooioo.ruches;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ooioo.ruches.essaim.EssaimController;
import ooioo.ruches.evenement.EvenementController;
import ooioo.ruches.evenement.EvenementEssaimController;
import ooioo.ruches.evenement.EvenementHausseController;
import ooioo.ruches.evenement.EvenementRucheController;
import ooioo.ruches.evenement.EvenementRucherController;
import ooioo.ruches.hausse.HausseController;
import ooioo.ruches.personne.PersonneController;
import ooioo.ruches.recolte.RecolteController;
import ooioo.ruches.recolte.RecolteHausseController;
import ooioo.ruches.ruche.RucheController;
import ooioo.ruches.ruche.RucheTypeController;
import ooioo.ruches.rucher.RucherController;

@SpringBootTest
class ControllerTest {

	@Autowired
	private EssaimController essaimController;
	@Autowired
	private EvenementController evenementController;
	@Autowired
	private EvenementRucherController evenementRucherController;
	@Autowired
	private EvenementRucheController evenementRucheController;
	@Autowired
	private EvenementEssaimController evenementEssaimController;
	@Autowired
	private EvenementHausseController evenementHausseController;
	@Autowired
	private HausseController hausseController;
	@Autowired
	private PersonneController personneController;
	@Autowired
	private RecolteController recolteController;
	@Autowired
	private RecolteHausseController recolteHausseController;
	@Autowired
	private RucheController rucheController;
	@Autowired
	private RucheTypeController rucheTypeController;
	@Autowired
	private RucherController rucherController;

	@Test
	void controllers() throws Exception {
		assertThat(this.essaimController).isNotNull();
		assertThat(this.evenementController).isNotNull();
		assertThat(this.evenementRucherController).isNotNull();
		assertThat(this.evenementRucheController).isNotNull();
		assertThat(this.evenementEssaimController).isNotNull();
		assertThat(this.evenementHausseController).isNotNull();
		assertThat(this.hausseController).isNotNull();
		assertThat(this.personneController).isNotNull();
		assertThat(this.recolteController).isNotNull();
		assertThat(this.recolteHausseController).isNotNull();
		assertThat(this.rucheController).isNotNull();
		assertThat(this.rucheTypeController).isNotNull();
		assertThat(this.rucherController).isNotNull();
	}
	
}