package ooioo.ruches.rucher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ooioo.ruches.Nom;
import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.ruche.RucheRepository;

@Controller
@RequestMapping("/rucher")
public class RucherControllerMAJ {

	private static final String HISTO = "histo";

	private final Logger logger = LoggerFactory.getLogger(RucherControllerMAJ.class);

	@Autowired
	private RucherRepository rucherRepository;
	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private EvenementRepository evenementRepository;

	/**
	 * Mise à jour eve ruche ajout rucher avec id des ruchers de provenance
	 */
	@GetMapping("/majValeurEveRAR")
	public String historiques(Model model) {
		boolean group = false;
		List<Rucher> ruchers = rucherRepository.findByActif(true);
		List<Transhumance> histoAll = new ArrayList<>();
		for (Rucher rucher : ruchers) {
			// la liste de tous les événements RUCHEAJOUTRUCHER triés par ordre de date
			// descendante avec les champs ruche et rucher non null
			// On ne peut exclure le dépôt qui sert pour trouver les retraits d'un
			// rucher vers le dépôt
			List<Evenement> evensRucheAjout = evenementRepository.findAjoutRucheOK();
			// Les nom des ruches présentes dans le rucher
			Collection<Nom> nomRuchesX = rucheRepository.findNomsByRucherId(rucher.getId());
			List<String> ruches = new ArrayList<>();
			for (Nom nomR : nomRuchesX) {
				ruches.add(nomR.nom());
			}
			List<Transhumance> histo = new ArrayList<>();
			for (int i = 0, levens = evensRucheAjout.size(); i < levens; i++) {
				Evenement eve = evensRucheAjout.get(i);
				if (eve.getRucher().getId().equals(rucher.getId())) {
					// si l'événement est un ajout dans le rucher
					// on retire après l'affichage la ruche de l'événement
					// de la liste des ruches du rucher
					// On cherche l'événement précédent ajout de cette ruche
					// pour indication de sa provenance
					Evenement evePrec = null;
					for (int j = i + 1; j < levens; j++) {
						if ((evensRucheAjout.get(j).getRuche().getId().equals(eve.getRuche().getId()))
								&& !(evensRucheAjout.get(j).getRuche().getId().equals(rucher.getId()))) {
							// si (evensRucheAjout.get(j).getRuche().getId().equals(rucherId))
							// c'est une erreur, deux ajouts successifs dans le même rucher
							evePrec = evensRucheAjout.get(j);
							break;
						}
					}
					histo.add(new Transhumance(rucher, true, // type = true Ajout
							eve.getDate(),
							Collections.singleton(evePrec == null ? "Inconnue" : evePrec.getRucher().getNom()),
							Arrays.asList(eve.getRuche().getNom()), new ArrayList<>(ruches), eve.getId()));
					if (!ruches.remove(eve.getRuche().getNom())) {
						logger.error("Événement {} le rucher {} ne contient pas la ruche {}", eve.getDate(),
								eve.getRucher().getNom(), eve.getRuche().getNom());
					}
				} else {
					// l'événenemt eve ajoute une ruche dans un autre rucher
					// On cherche l'événement précédent ajout de cette ruche
					for (int j = i + 1; j < levens; j++) {
						Evenement eveJ = evensRucheAjout.get(j);
						if (eveJ.getRuche().getId().equals(eve.getRuche().getId())) {
							if (eveJ.getRucher().getId().equals(rucher.getId())) {
								// si l'événement précédent evePrec était un ajout dans le
								// rucher, alors eve retire la ruche du rucher
								if (!ruches.contains(eve.getRuche().getNom())) {
									// si l'événement précédent evePrec était un ajout dans le
									// rucher, alors eve retire la ruche du rucher
									histo.add(new Transhumance(rucher, false, // type = false Retrait
											eve.getDate(), Collections.singleton(eve.getRucher().getNom()),
											Arrays.asList(eve.getRuche().getNom()), new ArrayList<>(ruches),
											eve.getId()));
									ruches.add(eve.getRuche().getNom());
								}
								break;
							} else {
								// c'est un événement ajout dans la ruche mais
								// dans un autre rucher. IL y a deux événements
								// successifs ajout de la ruche dans un autre rucher
								// on revient à la boucle principale qui traitera
								// ce deuxième événement
								break;
							}
						}
					}
				}
			}
			histoAll.addAll(histo);
			if (!ruches.isEmpty()) {
				logger.error(
						"Historique : après traitement des événements en reculant dans le temps, le rucher n'est pas vide");
			}
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
	 * transhumances de tous les ruchers. En commentaire mise de l'id du rucher dans
	 * le champ valeur mais abandonné car gestion de l'affichage lourde
	 * (transformation ids en noms)
	 *
	 * @param histoAll les transhumances de tous les ruchers
	 */
	private void majEve(List<Transhumance> histoAll) {
		for (Transhumance tr : histoAll) {
			if (tr.type()) { // true : Ajout dans rucher
				Optional<Evenement> eveOpt = evenementRepository.findById(tr.eveid());
				if (eveOpt.isPresent()) {
					Evenement eve = eveOpt.get();
					// Lire le rucher de provenance dans la transhumance
					// set contenant un rucher unique
					Rucher ruTr = rucherRepository.findByNom(tr.destProv().iterator().next());
					if (ruTr == null) {
						logger.error("Pas de rucher de nom ---{}---", tr.destProv().iterator().next());
						// deuxième chance, est-ce que le nom du rucher est déjà dans l'eve
						Rucher ruEve = rucherRepository.findByNom(eve.getValeur());
						if (ruEve != null) {
							eve.setValeur(ruEve.getNom());
							evenementRepository.save(eve);
						} else {
							// en fait null ou blanc
							logger.error("Caramba rien dans l'eve {}", eve.getValeur());
						}
					} else {
						// lire dans la transhumance l'événement tr.eveId()
						// On met dans valeur le nom du rucher
						eve.setValeur(ruTr.getNom());
						evenementRepository.save(eve);
					}
				} else {
					logger.error("Eve transhumance null !");
				}
			}
		}
	}

}
