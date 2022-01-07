package ooioo.ruches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;
import ooioo.ruches.hausse.Hausse;
import ooioo.ruches.hausse.HausseRepository;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheRepository;
import ooioo.ruches.rucher.Rucher;
import ooioo.ruches.rucher.RucherRepository;

@Controller
public class AdminController {

	@Autowired
	private RucherRepository rucherRepository;
	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private HausseRepository hausseRepository;

	/**
	 * Tests erreurs
	 *  résultat dans la page tests.html
	 */
	@GetMapping("/tests")
	public String tests(Model model) {
		// Recherche des erreurs dans l'historique des ajouts de ruches dans les ruchers
		Iterable<Rucher> ruchers = rucherRepository.findAll();
		// la liste de tous les événements RUCHEAJOUTRUCHER triés par ordre de date descendante
		List<Evenement> evensListe = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.RUCHEAJOUTRUCHER);
		int levens = evensListe.size();
		List<Evenement> eveRucherRuche = new ArrayList<>();
		List<Rucher> rucherNonVide = new ArrayList<>();
		for (Rucher rucher : ruchers) {
			Long rucherId = rucher.getId();
			// Les nom des ruches présentes dans le rucher
			Collection<Nom> nomRuchesX = rucheRepository.findNomsByRucherId(rucherId);
 			List<String> ruches = new ArrayList<>();
 			for (Nom nomR : nomRuchesX) {
 				ruches.add(nomR.getNom());
 			}
			for (int i = 0; i < levens; i++) {
				Evenement eve = evensListe.get(i);
				if ((eve.getRucher() == null) || (eve.getRuche() == null)) {
					// si événement incorrect, on l'ignore
					continue;
				}
 				if (eve.getRucher().getId().equals(rucherId)) {
					// si l'événement est un ajout dans le rucher
					// on retire la ruche de l'événement 
					//  de la liste des ruches du rucher
					if (!ruches.remove(eve.getRuche().getNom())) {
						// le rucher ne contient pas la ruches désignée par l'événement
						eve.setValeur(rucher.getNom());
						eveRucherRuche.add(eve);
					}
				} else {
					// L'événenemt eve ajoute une ruche dans un autre rucher
					// On cherche l'événement précédent ajout de cette ruche
					Evenement evePrec = null;
					for (int j = i + 1; j < levens; j++) {
						if ((evensListe.get(j).getType() == 
								ooioo.ruches.evenement.TypeEvenement.RUCHEAJOUTRUCHER) &&
								(evensListe.get(j).getRuche() != null) &&
							(evensListe.get(j).getRuche().getId().equals(eve.getRuche().getId()))) {
							evePrec = evensListe.get(j);
							break;
						}
					}
					if (evePrec != null) {
						//   si c'est un ajout dans le rucher rucherId
						if (evePrec.getRucher() == null) {
							continue;
						}
						if (evePrec.getRucher().getId().equals(rucherId)) {
							// si l'événement précédent evePrec était un ajout dans le
							//   rucher, alors eve retire la ruche du rucher
							if (ruches.contains(eve.getRuche().getNom())) {
								// la ruche est déjà dans le rucher !
								//   valeur va indiquer dans quel rucher l'erreur a eu lieu
								eve.setValeur(rucher.getNom());
								eveRucherRuche.add(eve);
							} else {
								ruches.add(eve.getRuche().getNom());
							}
						}
					}
				}
			}
			if (!ruches.isEmpty()) {
				// Après traitement des événements, le rucher n'est pas vide
				rucherNonVide.add(rucher);
			} 			
		}
		model.addAttribute("eveRucherRuche", eveRucherRuche);
		model.addAttribute("rucherNonVide", rucherNonVide);
		List<Evenement> eveInc = new ArrayList<>();
		// événements RUCHEAJOUTRUCHER incomplets
		for (Evenement eve : evensListe) {
			if ((eve.getRucher() == null) || (eve.getRuche() == null)) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.HAUSSEPOSERUCHE);
		for (Evenement eve : evensListe) {
			if (eve.getHausse() == null || eve.getRuche() == null) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.HAUSSERETRAITRUCHE);
		for (Evenement eve : evensListe) {
			if (eve.getHausse() == null || eve.getRuche() == null) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.AJOUTESSAIMRUCHE);
		for (Evenement eve : evensListe) {
			if (eve.getEssaim() == null || eve.getRuche() == null) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.HAUSSEREMPLISSAGE);
		for (Evenement eve : evensListe) {
			if (eve.getHausse() == null || !Utils.isPourCent(eve.getValeur())) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.ESSAIMTRAITEMENT);
		for (Evenement eve : evensListe) {
			if (eve.getEssaim() == null) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.ESSAIMTRAITEMENTFIN);
		for (Evenement eve : evensListe) {
			if (eve.getEssaim() == null) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.ESSAIMSUCRE);
		for (Evenement eve : evensListe) {
			if (eve.getEssaim() == null || !Utils.isNum(eve.getValeur())) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.COMMENTAIRERUCHE);
		for (Evenement eve : evensListe) {
			if (eve.getRuche() == null || eve.getCommentaire().isBlank()) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.COMMENTAIREHAUSSE);
		for (Evenement eve : evensListe) {
			if (eve.getHausse() == null || eve.getCommentaire().isBlank()) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.COMMENTAIREESSAIM);
		for (Evenement eve : evensListe) {
			if (eve.getEssaim() == null || eve.getCommentaire().isBlank()) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.COMMENTAIRERUCHER);
		for (Evenement eve : evensListe) {
			if (eve.getRucher() == null || eve.getCommentaire().isBlank()) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.ESSAIMDISPERSION);
		for (Evenement eve : evensListe) {
			if (eve.getEssaim() == null) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.RUCHEPESEE);
		for (Evenement eve : evensListe) {
			if (eve.getRuche() == null || eve.getEssaim() == null || !Utils.isNum(eve.getValeur())) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.RUCHECADRE);
		for (Evenement eve : evensListe) {
			if (eve.getRuche() == null) {
				eveInc.add(eve);
			}
		}
		model.addAttribute("eveInc", eveInc);
		// Analyse des ajouts/retraits de hausses sur les ruches
		Iterable<Ruche> ruches = rucheRepository.findAll();
		List<Evenement> eveRucheHausseErr = new ArrayList<>();
		List<Ruche> rucheNonVide = new ArrayList<>();
		for (Ruche ruche : ruches) {
			// Les événements ajout/retrait de la ruche
			List<Evenement> eveRucheHausse = evenementRepository.findEveRucheHausseDesc(ruche.getId());
			// Les hausses actuellement sur la ruche
			List<Hausse> hausses = hausseRepository.findByRucheIdOrderByOrdreSurRuche(ruche.getId());
			for (Evenement eve : eveRucheHausse) {
				if (eve.getHausse() != null) {
					if (eve.getType() == TypeEvenement.HAUSSERETRAITRUCHE) {
						if (hausses.contains(eve.getHausse())) {
							// La hausse est déjà dans la liste
							eveRucheHausseErr.add(eve);
						} else {
							hausses.add(eve.getHausse());
						}
					} else { // eve.getType() est TypeEvenement.HAUSSEPOSERUCHE 
						if (!hausses.remove(eve.getHausse())) {
							// La hausse ne peut être enlevée de la liste
							//   il y a une erreur dans la pose des hausses
							eveRucheHausseErr.add(eve);
						}
					}
				}
			}
			if (!hausses.isEmpty()) {
				rucheNonVide.add(ruche);
			}
		}
		model.addAttribute("eveRucheHausseErr", eveRucheHausseErr);
		model.addAttribute("rucheNonVide", rucheNonVide);
		return "tests";
	}

}