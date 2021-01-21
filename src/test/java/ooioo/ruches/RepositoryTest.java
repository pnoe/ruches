package ooioo.ruches;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ooioo.ruches.essaim.EssaimRepository;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.hausse.HausseRepository;
import ooioo.ruches.personne.PersonneRepository;
import ooioo.ruches.recolte.RecolteHausseRepository;
import ooioo.ruches.recolte.RecolteRepository;
import ooioo.ruches.ruche.RucheRepository;
import ooioo.ruches.ruche.RucheTypeRepository;
import ooioo.ruches.rucher.RucherRepository;

@SpringBootTest
class RepositoryTest {

	@Autowired
	private EssaimRepository essaimRepository;
	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private HausseRepository hausseRepository;
	@Autowired
	private PersonneRepository personneRepository;
	@Autowired
	private RecolteRepository recolteRepository;
	@Autowired
	private RecolteHausseRepository recolteHausseRepository;
	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private RucheTypeRepository rucheTypeRepository;
	@Autowired
	private RucherRepository rucherRepository;

	@Test
	public void repositories() throws Exception {
		assertThat(this.essaimRepository).isNotNull();
		assertThat(this.evenementRepository).isNotNull();
		assertThat(this.hausseRepository).isNotNull();
		assertThat(this.personneRepository).isNotNull();
		assertThat(this.recolteRepository).isNotNull();
		assertThat(this.recolteHausseRepository).isNotNull();
		assertThat(this.rucheRepository).isNotNull();
		assertThat(this.rucheTypeRepository).isNotNull();
		assertThat(this.rucherRepository).isNotNull();
	}

}