package ooioo.ruches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import ooioo.ruches.essaim.EssaimRepository;
import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;
import ooioo.ruches.hausse.Hausse;
import ooioo.ruches.hausse.HausseRepository;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheRepository;
import ooioo.ruches.rucher.Rucher;
import ooioo.ruches.rucher.RucherRepository;

@Service
public class AdminService {

	@Autowired
	private RucherRepository rucherRepository;
	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private HausseRepository hausseRepository;
	@Autowired
	private EssaimRepository essaimRepository;

	/**
	 * Tests2 recherche les erreurs de l'ajout et du retrait des ruches dans les
	 * ruchers En partant du rucher vide et en ajoutant et retirant les ruches en
	 * parcourant les événements RUCHEAJOUTRUCHER dans l'orde ascendant des dates
	 * (plus ancien au plus récent) résultat dans la page tests.html Test2
	 * (contrairement à Tests ci-dessous) n'est pas appelé par un menu et doit être
	 * appelé en tapant son url
	 */
	public void tests2(Model model) {
		// Recherche des erreurs dans l'historique des ajouts de ruches dans les ruchers
		Iterable<Rucher> ruchers = rucherRepository.findAll();
		// la liste de tous les événements RUCHEAJOUTRUCHER triés par ordre de date
		// ascendante
		Iterable<Evenement> evesRucheAjoutRucher = evenementRepository
				.findByTypeOrderByDateAsc(TypeEvenement.RUCHEAJOUTRUCHER);
		List<Evenement> evesErrRucherRuche = new ArrayList<>();
		List<Rucher> rucherRuchesDiff = new ArrayList<>();
		for (Rucher rucher : ruchers) {
			Set<Ruche> ruches = new HashSet<>();
			for (Evenement eve : evesRucheAjoutRucher) {
				if ((eve.getRucher() == null) || (eve.getRuche() == null)) {
					// si événement incorrect, on l'ignore
					continue;
				}
				if (eve.getRucher().getId().equals(rucher.getId())) {
					// si l'événement est un ajout dans le rucher on ajoute la ruche de l'événement
					// à la liste des ruches du rucher
					if (!ruches.add(eve.getRuche())) {
						// erreur le rucher contient la ruche désignée par l'événement
						eve.setValeur(rucher.getNom());
						evesErrRucherRuche.add(eve);
					}
				} else {
					// l'événement est un ajout dans un autre rucher
					// si cette ruche est dans le rucher, on la retire
					ruches.remove(eve.getRuche());
				}
			}
			// Les nom des ruches présentes dans le rucher
			Collection<Ruche> ruchesRucher = rucheRepository.findCollByRucherId(rucher.getId());
			Set<Ruche> ruchesRucherSet = new HashSet<>(ruchesRucher);
			if (!ruches.equals(ruchesRucherSet)) {
				// Après traitement des événements, le rucher ne contient pas
				// les ruches en accord avec les événements
				rucherRuchesDiff.add(rucher);
			}
		}
		model.addAttribute("eveRucherRuche", evesErrRucherRuche);
		model.addAttribute("rucherNonVide", rucherRuchesDiff);
	}

	/**
	 * Tests recherche les erreurs de l'ajout et du retrait des ruches dans les
	 * ruchers. En partant du rucher dans son état actuel et en ajoutant et retirant
	 * les ruches en parcourant les événements RUCHEAJOUTRUCHER dans l'orde
	 * descendant des dates (plus récent au plus ancien) résultat dans la page
	 * tests.html. Tests de la validité des événements Tests erreurs ajout/retrait
	 * des hausses sur les ruches.
	 */
	public void tests(Model model) {
		// Recherche des erreurs dans l'historique des ajouts de ruches dans les ruchers
		// La liste de tous les ruches même inactifs
		Iterable<Rucher> ruchers = rucherRepository.findAll();
		// la liste de tous les événements RUCHEAJOUTRUCHER
		// ayant une ruche et un rucher non nuls
		// triés par ordre de date descendante
		List<Evenement> evensListe = evenementRepository.findAjoutRucheOK();
		int levens = evensListe.size();
		// liste des événements générant des erreurs dans l'historique
		List<Evenement> eveRucherRuche = new ArrayList<>();
		// les noms des ruchers analysés en synchro avec la liste eveRucherRuche
		List<String> ruchersErr = new ArrayList<>();
		// les codes d'erreur en synchro avec la liste eveRucherRuche
		List<String> errsRucher = new ArrayList<>();
		List<Rucher> rucherNonVide = new ArrayList<>();
		for (Rucher rucher : ruchers) {
			// Les noms des ruches présentes dans le rucher
			Collection<Nom> nomRuchesX = rucheRepository.findNomsByRucherId(rucher.getId());
			List<String> ruches = new ArrayList<>();
			for (Nom nomR : nomRuchesX) {
				ruches.add(nomR.nom());
			}
			for (int i = 0; i < levens; i++) {
				Evenement eve = evensListe.get(i);
				if (eve.getRucher().getId().equals(rucher.getId())) {
					// si l'événement est un ajout dans le rucher
					// on retire la ruche de l'événement
					// de la liste des ruches du rucher
					if (!ruches.remove(eve.getRuche().getNom())) {
						// erreur le rucher ne contient pas la ruche désignée par l'événement
						ruchersErr.add(rucher.getNom());
						errsRucher.add("1");
						eveRucherRuche.add(eve);
					} // sinon, la ruche a été enlevée de la liste des noms
						// On parcours les événements suivants à la recherche
						// de deux ajouts successifs (erreur)
						// donc même ruche que eve et même rucher : rucherId
					for (int j = i + 1; j < levens; j++) {
						Evenement eveJ = evensListe.get(j);
						if (eveJ.getRuche().getId().equals(eve.getRuche().getId())) {
							// si ajout de la ruche dans le même rucher
							// noe 5 juillet 2023 correction getRucher et non getRuche
							if (eveJ.getRucher().getId().equals(rucher.getId())) {
								// si même rucher
								// c'est une erreur, deux ajouts successifs dans le même rucher
								ruchersErr.add(rucher.getNom());
								errsRucher.add("2");
								eveRucherRuche.add(eveJ);
							} // sinon pas d'erreur, la ruche a été ajouté dans un autre rucher
								// Une fois un ajout de cette ruche dans un rucher trouvé on arrête
								// la recherche de deux ajouts consécutifs
							break;
						}
					}
				} else {
					// L'événenemt eve ajoute une ruche dans un autre rucher
					// On cherche l'événement précédent ajout de cette ruche
					// dans le rucher
					for (int j = i + 1; j < levens; j++) {
						Evenement eveJ = evensListe.get(j);
						if (eveJ.getRuche().getId().equals(eve.getRuche().getId())) {
							if (eveJ.getRucher().getId().equals(rucher.getId())) {
								// si l'événement précédent evePrec était un ajout dans le
								// rucher, alors eve retire la ruche du rucher
								if (ruches.contains(eve.getRuche().getNom())) {
									// la ruche est déjà dans le rucher !
									// valeur va indiquer dans quel rucher l'erreur a eu lieu
									ruchersErr.add(rucher.getNom());
									errsRucher.add("3");
									eveRucherRuche.add(eve);
								} else {
									ruches.add(eve.getRuche().getNom());
								}
								break;
							} else {
								// c'est un événement ajout de la ruche mais
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
			if (!ruches.isEmpty()) {
				// Après traitement des événements, le rucher n'est pas vide
				rucherNonVide.add(rucher);
			}
		}
		model.addAttribute("ruchersErr", ruchersErr);
		model.addAttribute("errsRucher", errsRucher);
		model.addAttribute("eveRucherRuche", eveRucherRuche);
		model.addAttribute("rucherNonVide", rucherNonVide);
		List<Evenement> eveInc = new ArrayList<>();
		evensListe = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.RUCHEAJOUTRUCHER);
		for (Evenement eve : evensListe) {
			if ((eve.getRucher() == null) || (eve.getRuche() == null)) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.HAUSSEPOSERUCHE);
		for (Evenement eve : evensListe) {
			if (eve.getHausse() == null || eve.getRuche() == null || !Utils.isIntInf10(eve.getValeur())) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.HAUSSERETRAITRUCHE);
		for (Evenement eve : evensListe) {
			if (eve.getHausse() == null || eve.getRuche() == null || !Utils.isIntInf10(eve.getValeur())) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.AJOUTESSAIMRUCHE);
		for (Evenement eve : evensListe) {
			if (eve.getEssaim() == null || eve.getRuche() == null) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.HAUSSEREMPLISSAGE);
		for (Evenement eve : evensListe) {
			if (eve.getHausse() == null || !Utils.isPourCent(eve.getValeur())) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.ESSAIMTRAITEMENT);
		for (Evenement eve : evensListe) {
			if (eve.getEssaim() == null) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.ESSAIMTRAITEMENTFIN);
		for (Evenement eve : evensListe) {
			if (eve.getEssaim() == null) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.ESSAIMSUCRE);
		for (Evenement eve : evensListe) {
			if (eve.getEssaim() == null || !Utils.isNum(eve.getValeur())) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.COMMENTAIRERUCHE);
		for (Evenement eve : evensListe) {
			if (eve.getRuche() == null || eve.getCommentaire().isBlank()) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.COMMENTAIREHAUSSE);
		for (Evenement eve : evensListe) {
			if (eve.getHausse() == null || eve.getCommentaire().isBlank()) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.COMMENTAIREESSAIM);
		for (Evenement eve : evensListe) {
			if (eve.getEssaim() == null || eve.getCommentaire().isBlank()) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.COMMENTAIRERUCHER);
		for (Evenement eve : evensListe) {
			if (eve.getRucher() == null || eve.getCommentaire().isBlank()) {
				eveInc.add(eve);
			}
		}
		
		
//		evensListe = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.ESSAIMDISPERSION);
//		for (Evenement eve : evensListe) {
//			if (eve.getEssaim() == null) {
//				eveInc.add(eve);
//			}
//		}
		
		
		evensListe = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.RUCHEPESEE);
		for (Evenement eve : evensListe) {
			if (eve.getRuche() == null || eve.getEssaim() == null || !Utils.isNum(eve.getValeur())) {
				eveInc.add(eve);
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.RUCHECADRE);
		for (Evenement eve : evensListe) {
			if (eve.getRuche() == null) {
				eveInc.add(eve);
			}
		}
		model.addAttribute("eveInc", eveInc);
		// Analyse des ajouts/retraits de hausses sur les ruches
		Iterable<Ruche> ruches = rucheRepository.findAll();
		List<Evenement> eveRucheHausseErr = new ArrayList<>();
		// les codes d'erreur en synchro avec la liste eveRucheHausseErr
		List<Ruche> rucheNonVide = new ArrayList<>();
		for (Ruche ruche : ruches) {
			// Les événements ajout/retrait des hausses de la ruche
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
							// il y a une erreur dans la pose des hausses
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

		// Essaims actifs et dispersés ou inactifs et non dispersés
//		model.addAttribute("eveDisperseActifs", evenementRepository.findEssaimActifDisperse());
//		model.addAttribute("essaimInactifsNonDisp",  essaimRepository.findEssaimInactifPasDipserse());

	}
}
