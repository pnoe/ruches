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
	 * 
	 * @return true rucheId OK, false KO
	 */
	boolean historique(Model model, Long rucheId) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			// Les événements ajout/retrait des hausses de la ruche
			List<Evenement> eveRucheHausse = evenementRepository.findEveRucheHausseDesc(rucheId);
			// les noms des hausses présentes sur la ruche en synchro avec eveRucheHausse
			List<String> haussesList = new ArrayList<>(eveRucheHausse.size());
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
	 * Ré-ordonne les hausses d'une ruche. Permet de remettre un ordre 1, 2, 3...
	 * après suppression d'une hausse par exemple.
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
		List<Ruche> ruches;
		Object voirInactif = session.getAttribute(Const.VOIRINACTIF);
		if (voirInactif != null && (boolean) voirInactif) {
			ruches = rucheRepository.findAllByOrderByNom();
		} else {
			ruches = rucheRepository.findByActiveTrueOrderByNom();
		}
		int nbr = ruches.size();
		List<Integer> nbHausses = new ArrayList<>(nbr);
		List<String> dateAjoutRucher = new ArrayList<>(nbr);
		List<Evenement> listeEvenCadre = new ArrayList<>(nbr);
		// Liste des noms de ruchers pour filtre sur colonne rucher
		List<String> ruchersNoms = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		for (Ruche ruche : ruches) {
			nbHausses.add(hausseRepository.countByRucheId(ruche.getId()));
			// On ajoute le nom du rucher pour la liste déroulante du filtre nom de rucher
			Rucher rr = ruche.getRucher();
			if ((rr != null) && (!ruchersNoms.contains(rr.getNom()))) {
				ruchersNoms.add(rr.getNom());
			}
			Evenement evenAjoutRucher = evenementRepository.findFirstByRucheAndRucherAndTypeOrderByDateDesc(ruche,
					ruche.getRucher(), TypeEvenement.RUCHEAJOUTRUCHER);
			dateAjoutRucher.add((evenAjoutRucher == null) ? "" : evenAjoutRucher.getDate().format(formatter));
			listeEvenCadre.add(evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
					TypeEvenement.RUCHECADRE));
		}
		model.addAttribute("dateAjoutRucher", dateAjoutRucher);
		model.addAttribute("listeEvenCadre", listeEvenCadre);
		model.addAttribute(Const.NBHAUSSES, nbHausses);
		model.addAttribute(Const.RUCHES, ruches);
		Collections.sort(ruchersNoms);
		model.addAttribute("ruchersNoms", ruchersNoms);
		if (plus) {
			List<Iterable<Evenement>> listeEvensCommentaireEssaim = new ArrayList<>(nbr);
			List<List<Hausse>> haussesRuches = new ArrayList<>(nbr);
			List<List<Evenement>> evensHaussesRuches = new ArrayList<>(nbr);
			List<Evenement> evensPoidsRuches = new ArrayList<>(nbr);
			for (Ruche ruche : ruches) {
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
			}
			model.addAttribute("listeEvensCommentaireEsaim", listeEvensCommentaireEssaim);
			model.addAttribute(Const.HAUSSES, haussesRuches);
			model.addAttribute("evensHaussesRuches", evensHaussesRuches);
			model.addAttribute("evensPoidsRuches", evensPoidsRuches);
		}
	}

	/*
	 * Liste des ruches d'un rucher avec ordre de parcours.
	 *
	 * @param parcours liste des ids des ruches ordonnés selon le parcours le plus court.
	 * @param plus true pour liste détaillée.
	 */
	void listePlusRucher(HttpSession session, Model model, Rucher rucher, List<Long> parcours, boolean plus) {
		List<Ruche> ruches;
		Object voirInactif = session.getAttribute(Const.VOIRINACTIF);
		if (voirInactif != null && (boolean) voirInactif) {
			ruches = rucheRepository.findByRucherIdOrderById(rucher.getId());
		} else {
			ruches = rucheRepository.findByActiveTrueAndRucherIdOrderById(rucher.getId());
		}
		int nbr = ruches.size();
		List<Integer> nbHausses = new ArrayList<>(nbr);
		List<String> dateAjoutRucher = new ArrayList<>(nbr);
		List<Iterable<Evenement>> listeEvensCommentaireEssaim = new ArrayList<>();
		List<Evenement> listeEvenCadre = new ArrayList<>(nbr);
		List<List<Hausse>> haussesRuches = new ArrayList<>();
		List<List<Evenement>> evensHaussesRuches = new ArrayList<>();
		List<Evenement> evensPoidsRuches = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		List<Integer> ordreRuche = new ArrayList<>(nbr);
		for (Ruche ruche : ruches) {
			// Ajoute l'index de la ruche dans le parcours dans ordreRuche.
			ordreRuche.add(parcours.indexOf(ruche.getId()));
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
			listeEvenCadre.add(evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
					TypeEvenement.RUCHECADRE));
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
		List<Nom> nomsRecords = rucheRepository.findAllProjectedBy();
		List<String> noms = new ArrayList<>(nomsRecords.size());
		for (Nom rucheNom : nomsRecords) {
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