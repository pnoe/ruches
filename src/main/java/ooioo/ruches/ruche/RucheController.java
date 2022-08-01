package ooioo.ruches.ruche;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ooioo.ruches.Const;
import ooioo.ruches.LatLon;
import ooioo.ruches.Nom;
import ooioo.ruches.Utils;
import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;
import ooioo.ruches.hausse.Hausse;
import ooioo.ruches.hausse.HausseRepository;
import ooioo.ruches.recolte.RecolteHausse;
import ooioo.ruches.recolte.RecolteHausseRepository;
import ooioo.ruches.rucher.Rucher;
import ooioo.ruches.rucher.RucherRepository;
import ooioo.ruches.rucher.RucherService;

@Controller
@RequestMapping("/ruche")
public class RucheController {

	private static final String RUCHE_RUCHEFORM = "ruche/rucheForm";
	private static final String RUCHE_RUCHESLISTE = "ruche/ruchesListe";

	private final Logger logger = LoggerFactory.getLogger(RucheController.class);

	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private HausseRepository hausseRepository;
	@Autowired
	private RucheTypeRepository rucheTypeRepository;
	@Autowired
	private RucherRepository rucherRepository;
	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private RecolteHausseRepository recolteHausseRepository;

	@Autowired
	private RucheService rucheService;
	@Autowired
	private RucherService rucherService;

	@Autowired
	MessageSource messageSource;

	/**
	 * Historique de l'ajout des hausses sur une ruche
	 */
	@GetMapping("/historique/{rucheId}")
	public String historique(Model model, @PathVariable long rucheId) {
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
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "ruche/rucheHisto";
	}

	/**
	 * Clonage multiple d'une ruche (appel XMLHttpRequest)
	 */
	@PostMapping("/clone/{rucheId}")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody String clone(HttpSession session, Model model, @PathVariable long rucheId,
			@RequestParam String nomclones) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			List<String> noms = new ArrayList<>();
			for (Nom rucheNom : rucheRepository.findAllProjectedBy()) {
				noms.add(rucheNom.nom());
			}
			Float longitude = ruche.getLongitude();
			String[] nomarray = nomclones.split(",");
			String commentaire = "Clône ruche " + ruche.getNom();
			List<String> nomsCrees = new ArrayList<>();
			for (String nom : nomarray) {
				if (noms.contains(nom)) {
					logger.error("Clone d'une ruche : {} nom existant", nom);
				} else {
					longitude += 0.00004f;
					Ruche clone = new Ruche(ruche, nom, longitude);
					rucheRepository.save(clone);
					Evenement eveAjout = new Evenement(Utils.dateTimeDecal(session), TypeEvenement.RUCHEAJOUTRUCHER,
							clone, null, ruche.getRucher(), null, null, commentaire);
					evenementRepository.save(eveAjout);
					nomsCrees.add(nom);
					// pour éviter clone "a,a" : 2 fois le même nom dans la liste
					noms.add(nom);
				}
			}
			String nomsJoin = String.join(",", nomsCrees);
			logger.info("Ruches {} créée(s)", nomsJoin);
			return messageSource.getMessage("cloneruchecreees", new Object[] { nomsJoin },
					LocaleContextHolder.getLocale());
		}
		logger.error(Const.IDRUCHEXXINCONNU, rucheId);
		return "Erreur : id ruche inconnu";
	}

	/**
	 * Liste des ruches
	 */
	@GetMapping("/liste")
	public String liste(HttpSession session, Model model) {
		rucheService.liste(session, model, false);
		return RUCHE_RUCHESLISTE;
	}

	/**
	 * Liste plus détaillée des ruches
	 */
	@GetMapping("/listeplus")
	public String listePlus(HttpSession session, Model model) {
		rucheService.liste(session, model, true);
		return "ruche/ruchesListePlus";
	}

	/**
	 * Liste des ruches d'un rucher paramètres parcours : pour l'ordre des ruches
	 * plus : liste détaillée si true
	 */
	@GetMapping("/liste/{rucherId}")
	public String listeRucher(HttpSession session, Model model, @PathVariable long rucherId, @RequestParam boolean plus,
			@RequestParam String parcours) {
		ObjectMapper objectMapper = new ObjectMapper();
		List<RucheParcours> chemin;
		try {
			chemin = objectMapper.readValue(parcours, new TypeReference<List<RucheParcours>>() {
			});
		} catch (IOException e) {
			logger.error("Paramètre parcours incorrect");
			model.addAttribute(Const.MESSAGE, "Paramètre incorect");
			return Const.INDEX;
		}
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			rucheService.listePlusRucher(session, model, rucher, chemin, plus);
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		if (plus) {
			return "ruche/ruchesListePlusRucher";
		}
		return "ruche/ruchesListeRucher";

	}

	/**
	 * Appel du formulaire pour la création d'une ruche
	 */
	@GetMapping("/cree")
	public String cree(HttpSession session, Model model) {
		List<String> noms = new ArrayList<>();
		for (Nom rucheNom : rucheRepository.findAllProjectedBy()) {
			noms.add(rucheNom.nom());
		}
		model.addAttribute(Const.RUCHENOMS, noms);
		// récupération des coordonnées du dépôt
		Ruche ruche = new Ruche();
		ruche.setDateAcquisition(Utils.dateDecal(session));
		Rucher rucher = rucherRepository.findByDepotIsTrue();
		LatLon latLon = rucherService.dispersion(rucher.getLatitude(), rucher.getLongitude());
		ruche.setLatitude(latLon.lat());
		ruche.setLongitude(latLon.lon());
		model.addAttribute(Const.RUCHE, ruche);
		model.addAttribute(Const.RUCHETYPES, rucheTypeRepository.findAll());
		return RUCHE_RUCHEFORM;
	}

	/**
	 * Appel du formulaire pour le changement de rucher d'une ruche
	 */
	@GetMapping("/rucher/{rucheId}")
	public String rucher(HttpSession session, Model model, @PathVariable long rucheId) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			model.addAttribute(Const.RUCHE, ruche);
			model.addAttribute(Const.RUCHERS, rucherRepository.findProjectedIdNomByActifAndIdNotOrderByNom(true,
					(ruche.getRucher() == null) ? null : ruche.getRucher().getId()));
			model.addAttribute("depotId", rucherRepository.findByDepotIsTrue().getId());
			model.addAttribute(Const.DATE, Utils.dateTimeDecal(session));
			Evenement evenFirst = evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
					ooioo.ruches.evenement.TypeEvenement.RUCHEAJOUTRUCHER);
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
			model.addAttribute("dateTime", evenFirst.getDate());
			LocalDateTime dateTimeFirst = evenFirst.getDate().plusMinutes(1);
			model.addAttribute("dateFirst", dateTimeFirst.format(dateFormat));
			model.addAttribute("timeFirst", dateTimeFirst.format(timeFormat));
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "/ruche/rucheRucherForm";
	}

	/**
	 * Changement de rucher d'une ruche Création de l'événement RUCHEAJOUTRUCHER
	 */
	@PostMapping("/sauverucher/{rucheId}")
	public String sauveRucher(Model model, @RequestParam String date, @PathVariable long rucheId,
			@RequestParam String commentaire, @RequestParam Rucher rucher) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			// si la ruche n'a pas été changée de rucher on ne crée pas d'événement
			// ajouter éventuellement message "Pas de changement de rucher"
			if (!ruche.getRucher().getId().equals(rucher.getId())) {
				String[] ruchesNoms = new String[] { ruche.getNom() };
				rucherService.sauveAjouterRuches(rucher, ruchesNoms, date, commentaire);
			}
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "redirect:/ruche/" + rucheId;
	}

	/**
	 * Appel du formulaire pour modifier une ruche
	 */
	@GetMapping("/modifie/{rucheId}")
	public String modifie(Model model, @PathVariable long rucheId) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			model.addAttribute(Const.RUCHENOMS, rucheRepository.findAllProjectedBy().stream().map(Nom::nom)
					.filter(nom -> !nom.equals(ruche.getNom())).toList());
			model.addAttribute(Const.RUCHE, ruche);
			Iterable<RucheType> rucheTypes = rucheTypeRepository.findAll();
			model.addAttribute(Const.RUCHETYPES, rucheTypes);
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return RUCHE_RUCHEFORM;
	}

	/**
	 * Suppression d'une ruche
	 */
	@GetMapping("/supprime/{rucheId}")
	public String supprime(Model model, @PathVariable long rucheId) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Iterable<Evenement> evenements = evenementRepository.findByRucheId(rucheId);
			List<RecolteHausse> recolteHausses = recolteHausseRepository.findByRucheId(rucheId);
			Iterable<Hausse> hausses = hausseRepository.findCompletByRucheId(rucheId);
			if (recolteHausses.isEmpty()) {
				// on enlève les hausses de la ruche à supprimer
				for (Hausse hausse : hausses) {
					hausse.setRuche(null);
					hausse.setOrdreSurRuche(null);
				}
				// on supprime les événements associés à cette ruche
				for (Evenement evenement : evenements) {
					evenementRepository.delete(evenement);
				}
				Ruche ruche = rucheOpt.get();
				rucheRepository.delete(ruche);
				logger.info("Ruche {} supprimée, id {}", ruche.getNom(), ruche.getId());
			} else {
				model.addAttribute(Const.MESSAGE, "Cette ruche ne peut être supprimée");
				return Const.INDEX;
			}
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "redirect:/ruche/liste";
	}

	/**
	 * Enregistrement de la ruche
	 */
	@PostMapping("/sauve")
	public String sauve(@ModelAttribute Ruche ruche, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return RUCHE_RUCHEFORM;
		}
		// à sa création on met la ruche est au dépôt
		// par contre on garde les coordonnées qui ont pu être
		// être modifiées dans le formulaire
		if (ruche.getRucher() == null) {
			Rucher rucher = rucherRepository.findByDepotIsTrue();
			ruche.setRucher(rucher);
			rucheRepository.save(ruche);
			// date mis au dépôt = date acquisition de la ruche
			// la date d'acquisition est un LocalDate d'ou le asStartOfDay
			// pour renseigner le heure, minutes...
			Evenement eveAjout = new Evenement(ruche.getDateAcquisition().atStartOfDay(),
					TypeEvenement.RUCHEAJOUTRUCHER, ruche, null, rucher, null, null, "");
			evenementRepository.save(eveAjout);
		} else {
			rucheRepository.save(ruche);
		}
		logger.info("Ruche {} enregistrée, id {}", ruche.getNom(), ruche.getId());
		return "redirect:/ruche/" + ruche.getId();
	}

	/**
	 * Afficher une ruche et ses hausses
	 */
	@GetMapping("/{rucheId}")
	public String affiche(Model model, @PathVariable long rucheId) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			model.addAttribute(Const.RUCHE, ruche);
			List<String> noms = new ArrayList<>();
			for (Nom rucheNom : rucheRepository.findAllProjectedBy()) {
				noms.add(rucheNom.nom());
			}
			model.addAttribute("ruchenoms", noms);
			Iterable<Hausse> haussesRuche = hausseRepository.findByRucheIdOrderByOrdreSurRuche(rucheId);
			model.addAttribute("haussesRuche", haussesRuche);
			// trouver la date de la pose des hausses sur la ruche dans les événements
			List<LocalDateTime> datesPoseHausse = new ArrayList<>();
			for (Hausse hausse : haussesRuche) {
				// trouver la date d'ajout de la hausse sur la ruche
				Evenement evenPoseHausse = evenementRepository.findFirstByRucheAndHausseAndTypeOrderByDateDesc(ruche,
						hausse, TypeEvenement.HAUSSEPOSERUCHE);
				datesPoseHausse.add((evenPoseHausse == null) ? null : evenPoseHausse.getDate());
			}
			model.addAttribute("datesPoseHausse", datesPoseHausse);
			// Si des hausses de récolte référencent cette ruche, on ne pourra la supprimer
			List<RecolteHausse> recolteHausses = recolteHausseRepository.findByRucheId(rucheId);
			model.addAttribute("recolteHausses", recolteHausses.iterator().hasNext());
			// Si des événements référencent cette ruche, il faudra les supprimer si on la
			// supprime
			Iterable<Evenement> evenements = evenementRepository.findByRucheId(rucheId);
			model.addAttribute(Const.EVENEMENTS, evenements.iterator().hasNext());
			// Trouver l'événement association essaim ruche
			if (ruche.getEssaim() != null) {
				
				// rucheService.modelAddEvenement(model, ruche, TypeEvenement.AJOUTESSAIMRUCHE);
				model.addAttribute("eveEssaim", evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
						TypeEvenement.AJOUTESSAIMRUCHE));
			}
			
			// rucheService.modelAddEvenement(model, ruche, TypeEvenement.RUCHEAJOUTRUCHER);
			model.addAttribute("eveRucher", evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
					TypeEvenement.RUCHEAJOUTRUCHER));
			
			// rucheService.modelAddEvenement(model, ruche, TypeEvenement.COMMENTAIRERUCHE);
			model.addAttribute("eveComm", evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
					TypeEvenement.COMMENTAIRERUCHE));
			
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "ruche/rucheDetail";
	}

	/**
	 * Afficher une ruche pour le choix des hausses à ajouter/retirer
	 */
	@GetMapping("/hausses/{rucheId}")
	public String ajoutRetraitHausses(Model model, @PathVariable long rucheId) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			Iterable<Hausse> haussesRuche = hausseRepository.findByRucheIdOrderByOrdreSurRuche(rucheId);
			Iterable<Hausse> haussesRucheNull = hausseRepository.findByActiveAndRucheIsNull(true);
			Iterable<Hausse> haussesRucheNot = hausseRepository.findByActiveAndRucheIdNot(true, rucheId);
			model.addAttribute(Const.RUCHE, ruche);
			model.addAttribute("haussesRuche", haussesRuche);
			model.addAttribute("haussesRucheNull", haussesRucheNull);
			model.addAttribute("haussesRucheNot", haussesRucheNot);
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "ruche/rucheAjoutRetrait";
	}

	/**
	 * Enregistrer en base l'ordre des hausses modifié par drag and drop (appel
	 * XMLHttpRequest)
	 */
	@PostMapping("/ordreHausses/{rucheId}")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody String ordreHausses(@PathVariable long rucheId,
			@RequestParam(value = "hausses[]") Long[] hausses, @RequestParam(value = "ordre[]") Integer[] ordre) {
		for (int i = 0; i < hausses.length; i++) {
			Optional<Hausse> hausseOpt = hausseRepository.findById(hausses[i]);
			if (hausseOpt.isPresent()) {
				Hausse hausse = hausseOpt.get();
				if ((hausse.getRuche() != null) && (hausse.getRuche().getId() == rucheId)) {
					hausse.setOrdreSurRuche(ordre[i]);
					hausseRepository.save(hausse);
				} else {
					return "Id ruche incorrect";
				}
			} else {
				return "Hausse non trouvée";
			}
		}
		return "OK";
	}

	/**
	 * Ajouter une hausse sur une ruche
	 */
	@PostMapping("/hausse/ajout/{rucheId}/{hausseId}")
	public String ajoutHausse(Model model, @PathVariable long rucheId, @PathVariable long hausseId,
			@RequestParam String date, @RequestParam String commentaire) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
		if (rucheOpt.isPresent()) {
			if (hausseOpt.isPresent()) {
				Ruche ruche = rucheOpt.get();
				Hausse hausse = hausseOpt.get();
				Evenement evenementRetrait = null;
				Ruche rucheHausse = hausse.getRuche();
				LocalDateTime dateEve = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
				if (rucheHausse != null) {
					evenementRetrait = new Evenement(dateEve, TypeEvenement.HAUSSERETRAITRUCHE, rucheHausse,
							rucheHausse.getEssaim(), rucheHausse.getRucher(), hausse,
							hausse.getOrdreSurRuche().toString(), commentaire);
					evenementRepository.save(evenementRetrait);
				}
				hausse.setRuche(ruche);
				// mettre ordreSurRuche au max des ordreSurRuche
				Integer nbHausses = hausseRepository.countByRucheId(rucheId);
				hausse.setOrdreSurRuche(nbHausses + 1);
				hausseRepository.save(hausse);
				Evenement evenementPose = new Evenement(dateEve, TypeEvenement.HAUSSEPOSERUCHE, ruche,
						ruche.getEssaim(), ruche.getRucher(), hausse, hausse.getOrdreSurRuche().toString(),
						commentaire);
				if (evenementRetrait != null) {
					evenementPose.setDate(evenementRetrait.getDate().withSecond(0).withNano(0).plusMinutes(1L));
				}
				evenementRepository.save(evenementPose);
				logger.info("Hause {} posée sur le ruche {}", hausse.getNom(), ruche.getNom());
			} else {
				logger.error(Const.IDHAUSSEXXINCONNU, hausseId);
				model.addAttribute(Const.MESSAGE,
						messageSource.getMessage(Const.IDHAUSSEINCONNU, null, LocaleContextHolder.getLocale()));
				return Const.INDEX;
			}
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "redirect:/ruche/hausses/{rucheId}";
	}

	/**
	 * Retirer une hausse de sa ruche recalcule l'ordre des hausses de cette ruche
	 */
	@PostMapping("/hausse/retrait/{rucheId}/{hausseId}")
	public String retraitHausse(Model model, @PathVariable long rucheId, @PathVariable long hausseId,
			@RequestParam String date, @RequestParam String commentaire) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
		if (rucheOpt.isPresent()) {
			if (hausseOpt.isPresent()) {
				Hausse hausse = hausseOpt.get();
				// création événement avant mise à null de la ruche
				Ruche ruche = hausse.getRuche();
				LocalDateTime dateEve = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
				Essaim essaim = null;
				Rucher rucher = null;
				String rucheNom = "";
				if (ruche != null) {
					essaim = ruche.getEssaim();
					rucher = ruche.getRucher();
					rucheNom = ruche.getNom();
				}
				Evenement evenementRetrait = new Evenement(dateEve, TypeEvenement.HAUSSERETRAITRUCHE, ruche, essaim,
						rucher, hausse, hausse.getOrdreSurRuche().toString(), commentaire);
				evenementRepository.save(evenementRetrait);
				hausse.setRuche(null);
				hausse.setOrdreSurRuche(null);
				hausseRepository.save(hausse);
				rucheService.ordonneHaussesRuche(rucheId);
				logger.info("Hause {} retirée de le ruche {}", hausse.getNom(), rucheNom);
			} else {
				logger.error(Const.IDHAUSSEXXINCONNU, hausseId);
				model.addAttribute(Const.MESSAGE,
						messageSource.getMessage(Const.IDHAUSSEINCONNU, null, LocaleContextHolder.getLocale()));
				return Const.INDEX;
			}
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "redirect:/ruche/hausses/{rucheId}";
	}

	/**
	 * Déplacer une ruche (appel XMLHttpRequest)
	 */
	@PostMapping("/deplace/{rucheId}/{lat}/{lng}")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody String deplace(@PathVariable Long rucheId, @PathVariable Float lat, @PathVariable Float lng) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			ruche.setLatitude(lat);
			ruche.setLongitude(lng);
			rucheRepository.save(ruche);
			return "OK";
		}
		logger.error(Const.IDRUCHEXXINCONNU, rucheId);
		return "Id ruche inconnu";
	}

}