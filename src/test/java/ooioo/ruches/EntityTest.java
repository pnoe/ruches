package ooioo.ruches;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.hausse.Hausse;
import ooioo.ruches.personne.Personne;
import ooioo.ruches.recolte.Recolte;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheType;
import ooioo.ruches.rucher.Rucher;

@SpringBootTest
class EntityTest {
	@Test
	void entities() throws Exception {
		assertThat(new Essaim().getReineMarquee()).isFalse();
		assertThat(new Hausse().getActive()).isTrue();
		assertThat(new Personne().getActive()).isTrue();
		assertThat(new Recolte().getIntPoidsMiel()).isZero();
		assertThat(new Ruche().getActive()).isTrue();
		assertThat(new Rucher().getDepot()).isFalse();
		assertThat(new Evenement().getCommentaire()).isEmpty();
		assertThat(new RucheType().getNbCadresMax()).isEqualTo(10);
	}
}