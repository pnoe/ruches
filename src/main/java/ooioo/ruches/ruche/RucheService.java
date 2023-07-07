package ooioo.ruches.ruche;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpSession;
import ooioo.ruches.Const;
import ooioo.ruches.LatLon;
import ooioo.ruches.Nom;
import ooioo.ruches.Utils;
import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;
import ooioo.ruches.hausse.Hausse;
import ooioo.ruches.hausse.HausseRepository;
import ooioo.ruches.ruche.type.RucheTypeRepository;
import ooioo.ruches.rucher.Rucher;
import ooioo.ruches.rucher.RucherRepository;
import ooioo.ruches.rucher.RucherService;

@Service
public class RucheService {

	private final Logger logger = LoggerFactory.getLogger(RucheService.class);

	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private HausseRepository hausseRepository;
	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private RucheTypeRepository rucheTypeRepository;
	@Autowired
	private RucherRepository rucherRepository;
	@Autowired
	private RucherService rucherService;

	/**
	 * Historique de l'ajout des hausses sur une ruche.
	 */
	boolean historique(Model model, Long rucheId) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			// Les événements ajout/retrait des hausses de la ruche
			List<Evenement> eveRucheHausse = evenementRepository.findEveRucheHausseDesc(rucheId);
			// les noms des hausses présentes sur la ruche en synchro avec eveRucheHausse
			List<String> haussesList = new ArrayList<>();
			// Les hausses actuellement sur la ruche
			List<Hausse> hausses = hausseRepository.findByRucheIdOrderByOrdreSurRuche(rucheId);
			// Liste des noms des hausses (pour affichage et comparaisons)
			List<String> haussesNom = new ArrayList<>();
			for (Hausse hausse : hausses) {
				haussesNom.add(hausse.getNom());
			}
			for (Evenement eve : eveRucheHausse) {
				haussesList.add(String.join(" ", haussesNom));
				if (eve.getHausse() != null) {
					if (eve.getType() == TypeEvenement.HAUSSERETRAITRUCHE) {
						if (haussesNom.contains(eve.getHausse().getNom())) {
							// La hausse est déjà dans la liste
							// erreur
						} else {
							haussesNom.add(eve.getHausse().getNom());
						}
					} else { // eve.getType() est TypeEvenement.HAUSSEPOSERUCHE
						if (!haussesNom.remove(eve.getHausse().getNom())) {
							// La hausse ne peut être enlevée de la liste
							// il y a une erreur dans la pose des hausses
							// erreur
						}
					}
				}
			}
			// if (!hausses.isEmpty()) {
			// erreur
			// }
			model.addAttribute("ruche", ruche);
			model.addAttribute("haussesList", haussesList);
			model.addAttribute("evenements", eveRucheHausse);
			return true;
		}
		return false;
	}

	/*
	 * Ré-ordonne les hausses d'une ruche.
	 * Permet de remettre un ordre 1, 2, 3... après suppression d'une hausse
	 *  par exemple.
	 */
	public void ordonneHaussesRuche(long rucheId) {
		Iterable<Hausse> haussesRuche = hausseRepository.findByRucheIdOrderByOrdreSurRuche(rucheId);
		int i = 1;
		for (Hausse hr : haussesRuche) {
			Integer ordre = hr.getOrdreSurRuche();
			if (ordre == null) {
				logger.error("Ruche {} Ordre hausse {} null.", rucheId, hr.getNom());
			}
			if ((ordre == null) || (ordre != i)) {
				// On corrige l'ordre s'il est null ou s'il est différent de l'ordre
				// renvoyé par findByRucheIdOrderByOrdreSurRuche
				hr.setOrdreSurRuche(i);
				hausseRepository.save(hr);
			}
			i++;
		}
	}

	/*
	 * Liste des ruches.
	 * 
	 * @param plus true pour liste détaillée
	 */
	void liste(HttpSession session, Model model, boolean plus) {
		Object voirInactif = session.getAttribute(Const.VOIRINACTIF);
		Iterable<Ruche> ruches;
		List<Integer> nbHausses = new ArrayList<>();
		List<String> dateAjoutRucher = new ArrayList<>();
		List<Iterable<Evenement>> listeEvensCommentaireEssaim = new ArrayList<>();
		List<Evenement> listeEvenCadre = new ArrayList<>();
		List<List<Hausse>> haussesRuches = new ArrayList<>();
		List<List<Evenement>> evensHaussesRuches = new ArrayList<>();
		List<Evenement> evensPoidsRuches = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		if (voirInactif != null && (boolean) voirInactif) {
			ruches = rucheRepository.findAllByOrderByNom();
		} else {
			ruches = rucheRepository.findByActiveTrueOrderByNom();
		}
		List<String> ruchersNoms = new ArrayList<>(); 
		for (Ruche ruche : ruches) {
			nbHausses.add(hausseRepository.countByRucheId(ruche.getId()));
			if (plus) {
				// Les 3 derniers événements de la ruche (hors HAUSSEPOSERUCHE, RUCHEPESEE et
				// RUCHECADRE).
				listeEvensCommentaireEssaim.add(evenementRepository.find3EveListePlus(ruche));
				// hausses et eve pose hausses en deux listes séparées pour
				// afficher les hausses même si les eves ont été effacés.
				List<Hausse> hausses = hausseRepository.findByRucheIdOrderByOrdreSurRuche(ruche.getId());
				haussesRuches.add(hausses);
				List<Evenement> eveHaussesRuche = new ArrayList<>();
				for (Hausse h : hausses) {
					eveHaussesRuche.add(evenementRepository.findEvePoseHausse(h));
				}
				evensHaussesRuches.add(eveHaussesRuche);
				// Dernier événement pesée de la ruche.
				evensPoidsRuches.add(
						evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche, TypeEvenement.RUCHEPESEE));
				
				Rucher rr = ruche.getRucher();
				if ((rr != null) && (!ruchersNoms.contains(rr.getNom()))) {
					ruchersNoms.add(rr.getNom());
				}
				
			}
			Evenement evenAjoutRucher = evenementRepository.findFirstByRucheAndRucherAndTypeOrderByDateDesc(ruche,
					ruche.getRucher(), TypeEvenement.RUCHEAJOUTRUCHER);
			dateAjoutRucher.add((evenAjoutRucher == null) ? "" : evenAjoutRucher.getDate().format(formatter));
			Evenement evenCadres = evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
					TypeEvenement.RUCHECADRE);
			listeEvenCadre.add(evenCadres);
		}
		model.addAttribute("dateAjoutRucher", dateAjoutRucher);
		model.addAttribute("listeEvenCadre", listeEvenCadre);
		model.addAttribute(Const.NBHAUSSES, nbHausses);
		model.addAttribute(Const.RUCHES, ruches);
		if (plus) {
			model.addAttribute("listeEvensCommentaireEsaim", listeEvensCommentaireEssaim);
			model.addAttribute(Const.HAUSSES, haussesRuches);
			model.addAttribute("evensHaussesRuches", evensHaussesRuches);
			model.addAttribute("evensPoidsRuches", evensPoidsRuches);
			
			Collections.sort(ruchersNoms);
			model.addAttribute("ruchersNoms", ruchersNoms);
			
		}
	}

	/*
	 * Liste des ruches d'un rucher avec ordre de parcours.
	 */
	void listePlusRucher(HttpSession session, Model model, Rucher rucher, List<RucheParcours> chemin, boolean plus) {
		Object voirInactif = session.getAttribute(Const.VOIRINACTIF);
		Iterable<Ruche> ruches;
		List<Integer> nbHausses = new ArrayList<>();
		List<String> dateAjoutRucher = new ArrayList<>();
		List<Iterable<Evenement>> listeEvensCommentaireEssaim = new ArrayList<>();
		List<Evenement> listeEvenCadre = new ArrayList<>();
		List<List<Hausse>> haussesRuches = new ArrayList<>();
		List<List<Evenement>> evensHaussesRuches = new ArrayList<>();
		List<Evenement> evensPoidsRuches = new ArrayList<>();
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
				if (rucheParcours.id().equals(ruche.getId())) {
					break;
				}
				ordre += 1;
			}
			ordreRuche.add(ordre);
			nbHausses.add(hausseRepository.countByRucheId(ruche.getId()));
			if (plus) {
				listeEvensCommentaireEssaim.add(evenementRepository.find3EveListePlus(ruche));
				// hausses et eve pose hausses en deux listes séparées pour
				// afficher les hausses même si les eves ont été effacés.
				List<Hausse> hausses = hausseRepository.findByRucheIdOrderByOrdreSurRuche(ruche.getId());
				haussesRuches.add(hausses);
				List<Evenement> eveHaussesRuche = new ArrayList<>();
				for (Hausse h : hausses) {
					eveHaussesRuche.add(evenementRepository.findEvePoseHausse(h));
				}
				evensHaussesRuches.add(eveHaussesRuche);
				evensPoidsRuches.add(
						evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche, TypeEvenement.RUCHEPESEE));
			}
			Evenement evenAjoutRucher = evenementRepository.findFirstByRucheAndRucherAndTypeOrderByDateDesc(ruche,
					ruche.getRucher(), TypeEvenement.RUCHEAJOUTRUCHER);
			dateAjoutRucher.add((evenAjoutRucher == null) ? "" : evenAjoutRucher.getDate().format(formatter));
			Evenement evenCadres = evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
					TypeEvenement.RUCHECADRE);
			listeEvenCadre.add(evenCadres);
		}
		model.addAttribute("ordreRuche", ordreRuche);
		model.addAttribute("dateAjoutRucher", dateAjoutRucher);
		model.addAttribute("listeEvenCadre", listeEvenCadre);
		model.addAttribute(Const.RUCHES, ruches);
		model.addAttribute(Const.NBHAUSSES, nbHausses);
		model.addAttribute(Const.RUCHER, rucher);
		if (plus) {
			model.addAttribute("listeEvensCommentaireEsaim", listeEvensCommentaireEssaim);
			model.addAttribute(Const.HAUSSES, haussesRuches);
			model.addAttribute("evensHaussesRuches", evensHaussesRuches);
			model.addAttribute("evensPoidsRuches", evensPoidsRuches);
		}
	}

	/**
	 * Appel du formulaire pour la création d'une ruche.
	 */
	void cree(HttpSession session, Model model) {
		List<String> noms = new ArrayList<>();
		for (Nom rucheNom : rucheRepository.findAllProjectedBy()) {
			noms.add(rucheNom.nom());
		}
		model.addAttribute(Const.RUCHENOMS, noms);
		// récupération des coordonnées du dépôt
		Ruche ruche = new Ruche();
		ruche.setDateAcquisition(Utils.dateDecal(session));
		Rucher rucher = rucherRepository.findByDepotTrue();
		LatLon latLon = rucherService.dispersion(rucher.getLatitude(), rucher.getLongitude());
		ruche.setLatitude(latLon.lat());
		ruche.setLongitude(latLon.lon());
		model.addAttribute(Const.RUCHE, ruche);
		model.addAttribute(Const.RUCHETYPES, rucheTypeRepository.findAll());
	}

}