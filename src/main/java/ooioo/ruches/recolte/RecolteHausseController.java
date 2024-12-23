package ooioo.ruches.recolte;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
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

import jakarta.servlet.http.HttpSession;
import ooioo.ruches.Const;
import ooioo.ruches.Utils;
import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.essaim.EssaimRepository;
import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;
import ooioo.ruches.hausse.Hausse;
import ooioo.ruches.hausse.HausseRepository;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheRepository;
import ooioo.ruches.ruche.RucheService;
import ooioo.ruches.rucher.Rucher;
import ooioo.ruches.rucher.RucherRepository;

@Controller
@RequestMapping("/recolte")
public class RecolteHausseController {

	private static final String HAUSSESRECOLTE = "haussesRecolte";
	private static final String HAUSSESNOTINRECOLTE = "haussesNotInRecolte";
	private static final String RECOLTECHOIXHAUSSES = "recolte/recolteChoixHausses";

	final Logger logger = LoggerFactory.getLogger(RecolteHausseController.class);

	private final RecolteRepository recolteRepository;
	private final RecolteHausseRepository recolteHausseRepository;
	private final HausseRepository hausseRepository;
	private final EvenementRepository evenementRepository;
	private final RucheRepository rucheRepository;
	private final EssaimRepository essaimRepository;
	private final RucherRepository rucherRepository;
	private final MessageSource messageSource;
	private final RecolteHausseService recolteHausseService;
	private final RucheService rucheService;

	public RecolteHausseController(RecolteRepository recolteRepository, RecolteHausseRepository recolteHausseRepository,
			HausseRepository hausseRepository, EvenementRepository evenementRepository, RucheRepository rucheRepository,
			EssaimRepository essaimRepository, RucherRepository rucherRepository, MessageSource messageSource,
			RecolteHausseService recolteHausseService, RucheService rucheService) {
		this.recolteRepository = recolteRepository;
		this.recolteHausseRepository = recolteHausseRepository;
		this.hausseRepository = hausseRepository;
		this.evenementRepository = evenementRepository;
		this.rucheRepository = rucheRepository;
		this.essaimRepository = essaimRepository;
		this.rucherRepository = rucherRepository;
		this.messageSource = messageSource;
		this.recolteHausseService = recolteHausseService;
		this.rucheService = rucheService;
	}

	@Value("${hausse.reste.miel}")
	private BigDecimal hausseResteMiel;

	private static final String modifiee = "{} modifiée";

	/**
	 * Appel du formulaire pour la saisie tabulaire des poids de miel des hausses de
	 * la récolte.
	 */
	@GetMapping("/haussesMiel/{recolteId}")
	public String haussesMiel(Model model, @PathVariable long recolteId) {
		Optional<Recolte> recolteOpt = recolteRepository.findById(recolteId);
		if (recolteOpt.isPresent()) {
			Recolte recolte = recolteOpt.get();
			model.addAttribute(Const.RECOLTE, recolte);
			model.addAttribute("dateRecolteEpoch", recolte.getDate().toEpochSecond(ZoneOffset.UTC));
			List<RecolteHausse> recolteHausses = recolteHausseRepository.findByRecolteOrderByHausseNom(recolte);
			List<Hausse> hausses = new ArrayList<>();
			// Initialiser RecolteMiel.java à partir des hausses.
			List<RecolteHausseMiel> recolteHaussesMiel = new ArrayList<>();
			for (RecolteHausse recolteHausse : recolteHausses) {
				RecolteHausseMiel rHM = new RecolteHausseMiel();
				rHM.setRecolteHausseId(recolteHausse.getId());
				hausses.add(recolteHausse.getHausse());
				rHM.setPoidsApres(recolteHausse.getPoidsApres());
				rHM.setPoidsAvant(recolteHausse.getPoidsAvant());
				recolteHaussesMiel.add(rHM);
			}
			RecolteMiel recolteMiel = new RecolteMiel();
			recolteMiel.setRecolteHaussesMiel(recolteHaussesMiel);
			model.addAttribute("recolteMiel", recolteMiel);
			model.addAttribute("hausses", hausses);
		} else {
			logger.error(Const.IDRECOLTEXXINCONNU, recolteId);
			model.addAttribute(Const.MESSAGE, Const.IDRECOLTEINCONNU);
			return Const.INDEX;
		}
		return "recolte/recolteMielForm";
	}

	/**
	 * Sauve la saisie tabulaire des poids de miel d'une récolte.
	 */
	@PostMapping("/haussesMiel/sauve/{recolteId}")
	public String recolteHaussesMielSauve(Model model, @ModelAttribute RecolteMiel recolteMiel,
			BindingResult bindingResult, @PathVariable long recolteId) {
		if (bindingResult.hasErrors()) {
			logger.error("Erreur formulaire miel, récolte {}", recolteId);
			model.addAttribute(Const.MESSAGE, "Erreur formulaire miel");
			return Const.INDEX;
		}
		Optional<Recolte> recolteOpt = recolteRepository.findById(recolteId);
		if (recolteOpt.isPresent()) {
			for (RecolteHausseMiel rHM : recolteMiel.getRecolteHaussesMiel()) {
				Optional<RecolteHausse> recolteHausseOpt = recolteHausseRepository.findById(rHM.getRecolteHausseId());
				if (recolteHausseOpt.isPresent()) {
					RecolteHausse recolteHausse = recolteHausseOpt.get();
					if (recolteHausse.getRecolte().getId().equals(recolteId)) {
						// si la hausse est bien une hausse de la récolte
						if (!(recolteHausse.getPoidsApres().equals(rHM.getPoidsApres())
								&& recolteHausse.getPoidsAvant().equals(rHM.getPoidsAvant()))) {
							recolteHausse.setPoidsApres(rHM.getPoidsApres());
							recolteHausse.setPoidsAvant(rHM.getPoidsAvant());
							recolteHausseRepository.save(recolteHausse);
							logger.info(modifiee, recolteHausse);
						}
					} else {
						logger.error("{} n'est pas de la récolte {}", recolteHausse, recolteId);
					}
				} else {
					logger.error("{} inconnu", recolteHausseOpt);
					model.addAttribute(Const.MESSAGE, "Recolte Hausse inconnue");
					return Const.INDEX;
				}
			}
			return "redirect:/recolte/" + recolteId;
		} else {
			logger.error(Const.IDRECOLTEXXINCONNU, recolteId);
			model.addAttribute(Const.MESSAGE, Const.IDRECOLTEINCONNU);
			return Const.INDEX;
		}
	}

	/**
	 * Afficher une récolte avec ses hausses.
	 */
	@GetMapping("/{recolteId}")
	public String recolte(Model model, @PathVariable long recolteId) {
		Optional<Recolte> recolteOpt = recolteRepository.findById(recolteId);
		if (recolteOpt.isPresent()) {
			Recolte recolte = recolteOpt.get();
			model.addAttribute(Const.RECOLTE, recolte);
			model.addAttribute("dateRecolteEpoch", recolte.getDate().toEpochSecond(ZoneOffset.UTC));
			List<RecolteHausse> recolteHausses = recolteHausseRepository.findByRecolte(recolte);
			model.addAttribute("detailsRecolte", recolteHausses);
			// Pour afficher le nombre de ruches ayant fourni des hausses.
			model.addAttribute(Const.NBRUCHES, recolteHausseService.recHausNbRuches(recolteHausses));
			// Pour afficher les noms des ruchers ayant contribués à la récolte.
			model.addAttribute(Const.RUCHER, recolteHausseService.idNomRuchers(recolteHausses));
			// Pour afficher le nombre de hausses de récolte incomplètes.
			model.addAttribute("hRecIncomplete", recolteHausseRepository.countHRecIncompletes(recolte));
		} else {
			logger.error(Const.IDRECOLTEXXINCONNU, recolteId);
			model.addAttribute(Const.MESSAGE, Const.IDRECOLTEINCONNU);
			return Const.INDEX;
		}
		return "recolte/recolteDetail";
	}

	/**
	 * Affiche le formulaire d'une hausse d'une récolte.
	 */
	@GetMapping("/modifie/hausse/{recolteId}/{recolteHausseId}")
	public String modifie(Model model, @PathVariable long recolteHausseId, @PathVariable long recolteId) {
		Optional<RecolteHausse> recolteHausseOpt = recolteHausseRepository.findById(recolteHausseId);
		if (recolteHausseOpt.isPresent()) {
			model.addAttribute("detailRecolte", recolteHausseOpt.get());
		} else {
			logger.error("Id récolte hausse {} inconnu.", recolteHausseId);
			model.addAttribute(Const.MESSAGE, "Id récolte hausse inconnu.");
			return Const.INDEX;
		}
		return "recolte/recolteHausseForm";
	}

	/**
	 * Pour bouton Plus du formulaire de modification de hausses de récolte.
	 * XMLHttpRequest appelé de recolteHausseForm.js. Charge les listes déroulantes
	 * essaim, ruche et rucher.
	 */
	@GetMapping("/listesPlus")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody Map<String, Object> listePlus() {
		Map<String, Object> map = new HashMap<>();
		map.put(Const.RUCHES, rucheRepository.findAllProjectedIdNomByOrderByNom());
		map.put(Const.RUCHERS, rucherRepository.findAllProjectedIdNomByOrderByNom());
		map.put(Const.ESSAIMS, essaimRepository.findAllProjectedIdNomByOrderByNom());
		return map;
	}

	/**
	 * Sauver le détail d'une hausse de la récolte.
	 */
	@PostMapping("/detail/sauve")
	public String recolteHausseSauve(@ModelAttribute RecolteHausse recolteHausse, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "recolte/recolteHausseForm";
		}
		recolteHausseRepository.save(recolteHausse);
		logger.info(modifiee, recolteHausse);
		return "redirect:/recolte/" + recolteHausse.getRecolte().getId();
	}

	/**
	 * Appel de la page de choix des hausses d'une récolte.
	 *
	 * @param toutesHausses boolean true si on veut afficher toutes les hausses dans
	 *                      le tableau de choix des hausses à ajouter.
	 */
	@GetMapping("/choixHausses/{recolteId}/{toutesHausses}")
	public String choixHausses(Model model, @PathVariable long recolteId, @PathVariable boolean toutesHausses) {
		Optional<Recolte> recolteOpt = recolteRepository.findById(recolteId);
		if (recolteOpt.isPresent()) {
			Recolte recolte = recolteOpt.get();
			model.addAttribute(Const.RECOLTE, recolte);
			// Les hausses récolte déjà ajoutées dans la récolte, pour le tableau "Retirer
			// des hausses". Attention "hausses récolte" et non hausses.
			model.addAttribute(HAUSSESRECOLTE, recolteHausseRepository.findByRecolte(recolte));
			// Les hausses posées sur des ruches, qui ne sont pas dans la récolte, pour le
			// tableau "Ajouter des hausses".
			model.addAttribute(HAUSSESNOTINRECOLTE, toutesHausses ? hausseRepository.findTtHNotInRecolteId(recolteId)
					: hausseRepository.findHaussesNotInRecolteId(recolteId));
		} else {
			logger.error(Const.IDRECOLTEXXINCONNU, recolteId);
			model.addAttribute(Const.MESSAGE, Const.IDRECOLTEINCONNU);
			return Const.INDEX;
		}
		return RECOLTECHOIXHAUSSES;
	}

	/**
	 * Ajout d'une série de hausses dans la récolte.
	 */
	@GetMapping("/ajoutHausses/{recolteId}/{haussesIds}")
	public String ajoutHausses(Model model, @PathVariable long recolteId, @PathVariable Long[] haussesIds) {
		for (Long hausseId : haussesIds) {
			Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
			Optional<Recolte> recolteOpt = recolteRepository.findById(recolteId);
			if (recolteOpt.isPresent()) {
				if (hausseOpt.isPresent()) {
					Recolte recolte = recolteOpt.get();
					Hausse hausse = hausseOpt.get();
					// Si la hausse est déjà dans la récolte, log erreur et on passe cette hausse.
					if (recolteHausseRepository.findByRecolteAndHausse(recolte, hausse) != null) {
						logger.error("La hausse {} est déjà dans la récolte {}", hausse.getNom(), recolte.getDate());
						continue;
					}
					BigDecimal poids = hausse.getPoidsVide().add(hausseResteMiel);
					RecolteHausse recolteHausse = new RecolteHausse(recolte, hausse, poids, poids);
					Ruche ruche = hausse.getRuche();
					if (ruche == null) {
						// la hausse n'est pas sur une ruche. On ne saura ni la ruche, ni le rucher,
						// ni l'essaim correspondants.
						logger.error("Récolte {} hausse {} Id ruche inconnu", recolteId, hausse.getId());
					} else {
						recolteHausse.setRuche(ruche);
						Rucher rucher = ruche.getRucher();
						recolteHausse.setRucher(rucher);
						Essaim essaim = ruche.getEssaim();
						recolteHausse.setEssaim(essaim);
						// on n'enleve pas la hausse de la ruche
						// on a alors accès au nom de la ruche dans la liste haussesRecolte
						// et on peut retirer une hausse sans avoir à la réaffecter à une ruche
					}
					recolteHausseRepository.save(recolteHausse);
				} else {
					logger.error("Hausse d'Id {} inconnue", hausseId);
					// On continue le traitement des autres hausses
				}
			} else {
				logger.error(Const.IDRECOLTEXXINCONNU, recolteId);
				model.addAttribute(Const.MESSAGE, Const.IDRECOLTEINCONNU);
				return Const.INDEX;
			}
		}
		return "redirect:/recolte/choixHausses/" + recolteId + "/false";
	}

	/**
	 * Retrait d'une série de hausses de la récolte.
	 */
	@GetMapping("/retraitHausses/{recolteId}/{haussesIds}")
	public String retraitHausses(Model model, @PathVariable long recolteId, @PathVariable Long[] haussesIds) {
		for (Long haussesId : haussesIds) {
			Optional<Recolte> recolteOpt = recolteRepository.findById(recolteId);
			Optional<Hausse> hausseOpt = hausseRepository.findById(haussesId);
			if (recolteOpt.isPresent()) {
				Recolte recolte = recolteOpt.get();
				if (hausseOpt.isPresent()) {
					Hausse hausse = hausseOpt.get();
					RecolteHausse recolteHausse = recolteHausseRepository.findByRecolteAndHausse(recolte, hausse);
					if (recolteHausse == null) {
						logger.error("La hausse {} n'est pas dans le récolte {}", hausse.getNom(), recolte.getDate());
						continue;
					}
					recolteHausseRepository.delete(recolteHausse);
				} else {
					logger.error("Hausse d'ID {} inconnue", haussesId);
				}
			} else {
				logger.error(Const.IDRECOLTEXXINCONNU, recolteId);
				model.addAttribute(Const.MESSAGE, Const.IDRECOLTEINCONNU);
				return Const.INDEX;
			}
		}
		return "redirect:/recolte/choixHausses/" + recolteId + "/false";
	}

	/**
	 * Enlève toutes les hausses de la récolte des ruches. Crée les événements
	 * retraits des hausses et remplissage à 0. Réordonne les hausses restantes sur
	 * les ruches. Appel XMLHttpRequest.
	 *
	 * @param date le date pour les événements à créer, pas de paramètre si test
	 *             uniquement (required = false)
	 * @return String si test la chaîne doit se terminer par un "?" si des ruches
	 *         peuvent être retirées. Le code javascript utilise ce "?" pour
	 *         distinguer les deux retours possibles.
	 */
	@PostMapping("/haussesDepot/{recolteId}")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody String haussesDepot(HttpSession session, Model model, @PathVariable long recolteId,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm") LocalDateTime date) {
		Optional<Recolte> recolteOpt = recolteRepository.findById(recolteId);
		StringBuilder retHausses = new StringBuilder();
		StringBuilder retRuches = new StringBuilder();
		if (recolteOpt.isPresent()) {
			DecimalFormat decimalFormat = new DecimalFormat("0.00",
					new DecimalFormatSymbols(LocaleContextHolder.getLocale()));
			Recolte recolte = recolteOpt.get();
			model.addAttribute(Const.RECOLTE, recolte);
			List<RecolteHausse> reH = recolteHausseRepository.findByRecolte(recolte);
			Set<Long> rucheIds = new HashSet<>();
			for (RecolteHausse recolteHausse : reH) {
				Hausse hausse = recolteHausse.getHausse();
				Ruche ruche = hausse.getRuche();
				if ((ruche != null) && (recolteHausse.getRuche() != null)
						&& (ruche.getId().equals(recolteHausse.getRuche().getId()))) {
					// La ruche de la hausse est non null (la hausse est sur une ruche)
					// La hausse de récolte référence bien une ruche
					// La ruche sur laquelle est la hausse est la même que la ruche
					// de la hausseRécolte correspondante.
					// Sinon on ignore la hausse.
					// Pour affichage des hausses enlevées des ruches
					retHausses.append(hausse.getNom()).append(" ");
					retRuches.append(ruche.getNom()).append(" ");
					if (date == null) {
						// date est null si test uniquement
						continue;
					}
					String commentaireEve = "Récolte "
							+ recolte.getDate().format(DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM)) + " "
							+ decimalFormat.format(
									recolteHausse.getPoidsAvant().subtract(recolteHausse.getPoidsApres()))
							+ "kg";
					Evenement evenementRetrait = new Evenement(date, TypeEvenement.HAUSSERETRAITRUCHE, ruche,
							ruche.getEssaim(), ruche.getRucher(), hausse, hausse.getOrdreSurRuche().toString(),
							commentaireEve);
					evenementRepository.save(evenementRetrait);
					logger.info("{} créé", evenementRetrait);
					Evenement evenementRemplissage = new Evenement(Utils.dateTimeDecal(session),
							TypeEvenement.HAUSSEREMPLISSAGE, ruche, ruche.getEssaim(), ruche.getRucher(), hausse, "0",
							commentaireEve);
					evenementRepository.save(evenementRemplissage);
					rucheIds.add(ruche.getId());
					logger.info("{} créé", evenementRemplissage);
					hausse.setRuche(null);
					hausse.setOrdreSurRuche(null);
					hausseRepository.save(hausse);
					logger.info(modifiee, hausse);
				}
			}
			for (Long rId : rucheIds) {
				rucheService.ordonneHaussesRuche(rId);
			}
		} else {
			logger.error(Const.IDRECOLTEXXINCONNU, recolteId);
			model.addAttribute(Const.MESSAGE, Const.IDRECOLTEINCONNU);
			return Const.IDRECOLTEINCONNU;
		}
		if (retHausses.length() == 0) {
			return messageSource.getMessage("pasDeHausseAenlever", null, LocaleContextHolder.getLocale());
		} else {
			return messageSource.getMessage((date == null) ? "enleverHausses" : "haussesEnlevees",
					new Object[] { retHausses, retRuches }, LocaleContextHolder.getLocale());
		}
	}

}