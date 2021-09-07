package ooioo.ruches.ruche;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import ooioo.ruches.Const;
import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;
import ooioo.ruches.hausse.Hausse;
import ooioo.ruches.hausse.HausseRepository;
import ooioo.ruches.rucher.Rucher;

@Service
public class RucheService {

	private final Logger logger = LoggerFactory.getLogger(RucheService.class);

	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private HausseRepository hausseRepository;
	@Autowired
	private RucheRepository rucheRepository;

	/*
	 * Ré-ordonne les hausses d'une ruche utilisé après retrait ou suppression d'une
	 * hausse
	 */
	public void ordonneHaussesRuche(long rucheId) {

		Iterable<Hausse> haussesRuche = hausseRepository.findByRucheIdOrderByOrdreSurRuche(rucheId);
		int i = 1;
		for (Hausse hausseRuche : haussesRuche) {
			Integer ordre = hausseRuche.getOrdreSurRuche();
			if (ordre == null) {
				logger.error("Ruche {} Ordre hausse {} null.", rucheId, hausseRuche.getNom());
			}
			hausseRuche.setOrdreSurRuche(i++);
			hausseRepository.save(hausseRuche);
		}
	}

	/*
	 * Ajoute au model Spring les chaînes date, valeur et commentaire du
	 * dernier événement de type typeEvenement
	 */
	public void modelAddEvenement(Model model, Ruche ruche, TypeEvenement typeEvenement) {
		Evenement evenement = evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
				typeEvenement);
		String type = typeEvenement.toString();
		if (evenement == null) {
			model.addAttribute(Const.DATE + type, null);
			model.addAttribute(Const.VALEUR + type, null);
			model.addAttribute(Const.COMMENTAIRE + type, null);
		} else {
			model.addAttribute(Const.DATE + type, evenement.getDate());
			model.addAttribute(Const.VALEUR + type, evenement.getValeur());
			model.addAttribute(Const.COMMENTAIRE + type, evenement.getCommentaire());
		}
	}

	/*
	 * Liste des ruches
	 *  @plus à true pour liste détaillée
	 */
	public void liste(HttpSession session, Model model, boolean plus) {
		Object voirInactif = session.getAttribute(Const.VOIRINACTIF);
		Iterable<Ruche> ruches;
		List<Integer> nbHausses = new ArrayList<>();
		List<String> nomHausses = new ArrayList<>();
		List<String> dateAjoutRucher = new ArrayList<>();
		List<Iterable<Evenement>> listeEvensCommentaireEssaim = new ArrayList<>();
		List<Evenement> listeEvenCadre = new ArrayList<>();
		List<Evenement> evensHaussesRuches = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		if (voirInactif != null && (boolean) voirInactif) {
			ruches = rucheRepository.findAllByOrderByNom();
		} else {
			ruches = rucheRepository.findByActiveOrderByNom(true);
		}
		for (Ruche ruche : ruches) {
			nbHausses.add(hausseRepository.countByRucheId(ruche.getId()));
			if (plus) {
				nomHausses.add(hausseRepository.hausseNomsByRucheId(ruche.getId()));
				listeEvensCommentaireEssaim.add(evenementRepository
						.findFirst3ByEssaimAndTypeOrderByDateDesc(ruche.getEssaim(), TypeEvenement.COMMENTAIREESSAIM));
				// il faut trouver le dernier evenement hausseposeruche dont la hausse est effectivement
				//  présente sur la ruche
				// correction spring boot 2.2.0
				List<Hausse> hausses = hausseRepository.findByRucheIdOrderByOrdreSurRuche(ruche.getId());
				evensHaussesRuches.add(evenementRepository.findFirstByRucheAndHausseInAndTypeOrderByDateDesc(ruche, hausses, TypeEvenement.HAUSSEPOSERUCHE));
			}
			Evenement evenAjoutRucher = evenementRepository
					.findFirstByRucheAndRucherAndTypeOrderByDateDesc(ruche, ruche.getRucher(),
							TypeEvenement.RUCHEAJOUTRUCHER);
			dateAjoutRucher.add((evenAjoutRucher == null) ? "" : evenAjoutRucher.getDate().format(formatter));
			Evenement evenCadres = evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
					TypeEvenement.RUCHECADRE);
			listeEvenCadre.add(evenCadres);
		}
		model.addAttribute("dateAjoutRucher", dateAjoutRucher);
		model.addAttribute("listeEvenCadre", listeEvenCadre);
		model.addAttribute("listeEvensCommentaireEsaim", listeEvensCommentaireEssaim);
		model.addAttribute("evensHaussesRuches", evensHaussesRuches);
		model.addAttribute(Const.NBHAUSSES, nbHausses);
		model.addAttribute(Const.HAUSSENOMS, nomHausses);
		model.addAttribute(Const.RUCHES, ruches);
	}

	/*
	 * Liste détaillée des ruches d(un rucher
	 *  avec ordre de parcours
	 */
	public void listePlusRucher(HttpSession session, Model model, Rucher rucher, List<RucheParcours> chemin, boolean plus) {
		Object voirInactif = session.getAttribute(Const.VOIRINACTIF);
		Iterable<Ruche> ruches;
		List<Integer> nbHausses = new ArrayList<>();
		List<String> nomHausses = new ArrayList<>();
		List<String> dateAjoutRucher = new ArrayList<>();
		List<Iterable<Evenement>> listeEvensCommentaireEssaim = new ArrayList<>();
		List<Evenement> listeEvenCadre = new ArrayList<>();
		List<Evenement> evensHaussesRuches = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		if (voirInactif != null && (boolean) voirInactif) {
			ruches = rucheRepository.findByRucherIdOrderById(rucher.getId());
		} else {
			ruches = rucheRepository.findByActiveTrueAndRucherIdOrderById(rucher.getId());
		}
		List<Integer> ordreRuche = new ArrayList<>();
		for (Ruche ruche : ruches) {
			// ajoute l'index de la ruche dans chemin dans ordreRuche
			int ordre = 0;
			for (RucheParcours rucheParcours : chemin) {
				if (rucheParcours.getId().equals(ruche.getId())) {
					break;
				}
				ordre += 1;
			}
			ordreRuche.add(ordre);
			nbHausses.add(hausseRepository.countByRucheId(ruche.getId()));
			if (plus) {
				nomHausses.add(hausseRepository.hausseNomsByRucheId(ruche.getId()));
				listeEvensCommentaireEssaim.add(evenementRepository
						.findFirst3ByEssaimAndTypeOrderByDateDesc(ruche.getEssaim(), TypeEvenement.COMMENTAIREESSAIM));
				// il faut trouver le dernier evenement hausseposeruche dont la hausse est effectivement
				//  présente sur la ruche
				// correction spring boot 2.2.0 iterable -> list
				List<Hausse> hausses = hausseRepository.findByRucheIdOrderByOrdreSurRuche(ruche.getId());
				evensHaussesRuches.add(evenementRepository.findFirstByRucheAndHausseInAndTypeOrderByDateDesc(ruche, hausses, TypeEvenement.HAUSSEPOSERUCHE));
			}
			Evenement evenAjoutRucher = evenementRepository
					.findFirstByRucheAndRucherAndTypeOrderByDateDesc(ruche, ruche.getRucher(),
							TypeEvenement.RUCHEAJOUTRUCHER);
			dateAjoutRucher.add((evenAjoutRucher == null) ? "" : evenAjoutRucher.getDate().format(formatter));
			Evenement evenCadres = evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
					TypeEvenement.RUCHECADRE);
			listeEvenCadre.add(evenCadres);
		}
		model.addAttribute("ordreRuche", ordreRuche);
		model.addAttribute("dateAjoutRucher", dateAjoutRucher);
		model.addAttribute("listeEvenCadre", listeEvenCadre);
		model.addAttribute("listeEvensCommentaireEsaim", listeEvensCommentaireEssaim);
		model.addAttribute("evensHaussesRuches", evensHaussesRuches);
		model.addAttribute(Const.NBHAUSSES, nbHausses);
		model.addAttribute(Const.HAUSSENOMS, nomHausses);
		model.addAttribute(Const.RUCHES, ruches);
		model.addAttribute(Const.RUCHER, rucher);
	}

}