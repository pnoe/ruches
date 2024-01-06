package ooioo.ruches.rucher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;

@Controller
@RequestMapping("/rucher")
public class RucherControllerMAJ {

	private static final String HISTO = "histo";

	private final Logger logger = LoggerFactory.getLogger(RucherControllerMAJ.class);

	private final RucherRepository rucherRepository;
	private final EvenementRepository evenementRepository;
	private final RucherService rucherService;

	public RucherControllerMAJ(RucherRepository rucherRepository, EvenementRepository evenementRepository,
			RucherService rucherService) {
		this.rucherRepository = rucherRepository;
		this.evenementRepository = evenementRepository;
		this.rucherService = rucherService;
	}

	/**
	 * Mise à jour eve "ruche ajout rucher" avec noms des ruchers de provenance.
	 */
	@GetMapping("/majValeurEveRAR")
	public String historiques(Model model) {
		boolean group = false;
		List<Rucher> ruchers = rucherRepository.findByActifTrue();
		List<Transhumance> histoAll = new ArrayList<>();
		for (Rucher rucher : ruchers) {
			// la liste de tous les événements RUCHEAJOUTRUCHER triés par ordre de date
			// descendante avec les champs ruche et rucher non null
			// On ne peut exclure le dépôt qui sert pour trouver les retraits d'un
			// rucher vers le dépôt
			List<Evenement> evensRucheAjout = evenementRepository.findAjoutRucheOK();
			List<Transhumance> histo = new ArrayList<>();
			rucherService.transhum(rucher, evensRucheAjout, false, histo, null);
			histoAll.addAll(histo);
		}
		// On trie la liste à afficher par date et on l'ajoute au model
		Collections.sort(histoAll, (b, a) -> a.date().compareTo(b.date()));
		model.addAttribute(HISTO, histoAll);
		model.addAttribute("group", group);
		majEve(histoAll);
		return "rucher/ruchersHisto";
	}

	/**
	 * Mise à jour des événements RUCHEAJOUTRUCHER avec le champ valeur contenant le
	 * nom du rucher de provenance. Ces noms sont récupérés dans le calcul des
	 * transhumances de tous les ruchers.
	 *
	 * @param histoAll les transhumances de tous les ruchers
	 */
	private void majEve(List<Transhumance> histoAll) {
		int i = 0;
		for (Transhumance tr : histoAll) {
			if (tr.type()) { // true : Ajout dans rucher
				Optional<Evenement> eveOpt = evenementRepository.findById(tr.eveid());
				if (eveOpt.isPresent()) {
					Evenement eve = eveOpt.get();
					// Lire le rucher de provenance dans la transhumance
					// set contenant un rucher unique
					Rucher ruTr = rucherRepository.findByNom(tr.destProv().iterator().next());
					if (ruTr != null) {
						// On met dans la valeur de l'événmeent ajout, le nom du rucher si différent
						// de celui trouvé par l'analyse des transhumances.
						// Mémo eve.getValeur avant save eve pour log.
						String val = eve.getValeur();
						if (val == null || !val.equals(ruTr.getNom())) {
							eve.setValeur(ruTr.getNom());
							evenementRepository.save(eve);
							logger.info("{} provenance {} corrigée", eve, val);
							i++;
						}
					}
				} else {
					logger.error("Eve transhumance null !");
				}
			}
		}
		logger.info("{} provenance(s) corrigée(s)", i);
	}

}
