package ooioo.ruches.recolte;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import ooioo.ruches.rucher.Rucher;
import ooioo.ruches.rucher.RucherRepository;

@Controller
@RequestMapping("/recolte")
public class RecolteHausseController {

	private static final String HAUSSESRECOLTE = "haussesRecolte";
	private static final String HAUSSESNOTINRECOLTE = "haussesNotInRecolte";
	private static final String RECOLTECHOIXHAUSSES = "recolte/recolteChoixHausses";

	final Logger logger = LoggerFactory.getLogger(RecolteHausseController.class);

	@Autowired
	private RecolteRepository recolteRepository;
	@Autowired
	private RecolteHausseRepository recolteHausseRepository;
	@Autowired
	private HausseRepository hausseRepository;
	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private EssaimRepository essaimRepository;
	@Autowired
	private RucherRepository rucherRepository;

	@Autowired
	private MessageSource messageSource;

	@Value("${hausse.reste.miel}")
	private BigDecimal hausseResteMiel;

	@Autowired
	private RecolteHausseService recolteHausseService;

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
			// si pas de hausse afficher et log erreur
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

		Optional<Recolte> recolteOpt = recolteRepository.findById(recolteId);
		if (recolteOpt.isPresent()) {

			if (bindingResult.hasErrors()) {
				Recolte recolte = recolteOpt.get();
				model.addAttribute(Const.RECOLTE, recolte);
				// TODO à vérifier en supprimant controle "number"
				return "recolte/recolteMielForm";
			}
			for (RecolteHausseMiel rHM : recolteMiel.getRecolteHaussesMiel()) {
				Optional<RecolteHausse> recolteHausseOpt = recolteHausseRepository.findById(rHM.getRecolteHausseId());
				if (recolteHausseOpt.isPresent()) {
					RecolteHausse recolteHausse = recolteHausseOpt.get();
					if (recolteHausse.getRecolte().getId().equals(recolteId)) {
						// si la hausse est bien une hausse de la récolte
						recolteHausse.setPoidsApres(rHM.getPoidsApres());
						recolteHausse.setPoidsAvant(rHM.getPoidsAvant());
						recolteHausseRepository.save(recolteHausse);
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
			model.addAttribute(Const.NBRUCHES, recolteHausseService.nomsRuches(recolteHausses).size());
			model.addAttribute(Const.RUCHER, recolteHausseService.idNomRuchers(recolteHausses));
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
			model.addAttribute(Const.RUCHES, rucheRepository.findAllProjectedIdNomByOrderByNom());
			model.addAttribute(Const.RUCHERS, rucherRepository.findAllProjectedIdNomByOrderByNom());
			model.addAttribute(Const.ESSAIMS, essaimRepository.findAllProjectedIdNomByOrderByNom());
			model.addAttribute("detailRecolte", recolteHausseOpt.get());
		} else {
			logger.error("Id récolte hausse {} inconnu.", recolteHausseId);
			model.addAttribute(Const.MESSAGE, "Id récolte hausse inconnu.");
			return Const.INDEX;
		}
		return "recolte/recolteHausseForm";
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
		return "redirect:/recolte/" + recolteHausse.getRecolte().getId();
	}

	/**
	 * Choix des hausses d'une récolte.
	 */
	@GetMapping("/choixHausses/{recolteId}")
	public String choixHausses(Model model, @PathVariable long recolteId) {
		Optional<Recolte> recolteOpt = recolteRepository.findById(recolteId);
		if (recolteOpt.isPresent()) {
			Recolte recolte = recolteOpt.get();
			model.addAttribute(Const.RECOLTE, recolte);
			model.addAttribute(HAUSSESRECOLTE, hausseRepository.findHaussesInRecolteId(recolteId));
			model.addAttribute(HAUSSESNOTINRECOLTE, hausseRepository.findHaussesNotInRecolteId(recolteId));
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
		return "redirect:/recolte/choixHausses/" + recolteId;
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
		return "redirect:/recolte/choixHausses/" + recolteId;
	}

	/**
	 * Enlève toutes les hausses de la récolte des ruches. Crée les événements
	 * retraits des hausses et remplissage à 0. (appel XMLHttpRequest)
	 */
	@PostMapping("/haussesDepot/{recolteId}")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody String haussesDepot(HttpSession session, Model model, @PathVariable long recolteId,
			@RequestParam @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm") LocalDateTime date) {
		Optional<Recolte> recolteOpt = recolteRepository.findById(recolteId);
		StringBuilder retHausses = new StringBuilder();
		StringBuilder retRuches = new StringBuilder();
		if (recolteOpt.isPresent()) {
			DecimalFormat decimalFormat = new DecimalFormat("0.00",
					new DecimalFormatSymbols(LocaleContextHolder.getLocale()));
			Recolte recolte = recolteOpt.get();
			model.addAttribute(Const.RECOLTE, recolte);
			Iterable<Hausse> hausses = hausseRepository.findHaussesInRecolteId(recolteId);
			for (Hausse hausse : hausses) {
				Ruche ruche = hausse.getRuche();
				RecolteHausse recolteHausse = recolteHausseRepository.findByRecolteAndHausse(recolte, hausse);
				if ((ruche != null) && (ruche.getId().equals(recolteHausse.getRuche().getId()))) {
					retHausses.append(hausse.getNom());
					retHausses.append(" ");
					retRuches.append(ruche.getNom());
					retRuches.append(" ");
					// Teste que la ruche sur laquelle est la hausse est la même que la ruche
					// de la hausseRécolte correspondante.
					// Si différente ne rien faire, la hausse a été replacée sur une autre ruche
					// Pour renumérotation de l'ordre des hausses
					Long rucheId = null;
					Integer hausseOrdre;
					rucheId = ruche.getId();
					hausseOrdre = hausse.getOrdreSurRuche();
					if (hausseOrdre == null) {
						logger.error("Récolte {} Ordre hausse {} null.", recolteId, hausse.getNom());
						hausseOrdre = 100;
					}
					// création événement avant mise à null de la ruche
					Essaim essaim = null;
					Rucher rucher = null;
					if (ruche != null) {
						essaim = ruche.getEssaim();
						rucher = ruche.getRucher();
					}
					String commentaireEve = "Récolte "
							+ recolte.getDate().format(DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM)) + " "
							+ decimalFormat.format(
									recolteHausse.getPoidsAvant().subtract(recolteHausse.getPoidsApres()))
							+ "kg";
					Evenement evenementRetrait = new Evenement(date, TypeEvenement.HAUSSERETRAITRUCHE, ruche, essaim,
							rucher, hausse, hausse.getOrdreSurRuche().toString(), commentaireEve);
					evenementRepository.save(evenementRetrait);
					logger.info("{} créé", evenementRetrait);
					Evenement evenementRemplissage = new Evenement(Utils.dateTimeDecal(session),
							TypeEvenement.HAUSSEREMPLISSAGE, ruche, essaim, rucher, hausse, "0", commentaireEve);
					evenementRepository.save(evenementRemplissage);
					logger.info("{} créé", evenementRemplissage);
					hausse.setRuche(null);
					hausse.setOrdreSurRuche(null);
					hausseRepository.save(hausse);
					// renuméroter l'ordre des hausses de la ruche
					Iterable<Hausse> haussesRuche = hausseRepository.findByRucheIdOrderByOrdreSurRuche(rucheId);
					for (Hausse hausseRuche : haussesRuche) {
						Integer ordre = hausseRuche.getOrdreSurRuche();
						// si ordre est null plantage, cela est arrivé quand le champ
						// ordreSurRuche n'était pas présent en hidden dans le formulaire de la hausse
						if (ordre == null) {
							logger.error("Récolte {} Ordre hausse {} null.", recolteId, hausseRuche.getNom());
							hausseRuche.setOrdreSurRuche(10);
							hausseRepository.save(hausseRuche);
						} else if (ordre > hausseOrdre) {
							hausseRuche.setOrdreSurRuche(ordre - 1);
							hausseRepository.save(hausseRuche);
						}
						// else si ordre <= hausseOrdre on ne fait rien
					}
				}
			}
		} else {
			logger.error(Const.IDRECOLTEXXINCONNU, recolteId);
			model.addAttribute(Const.MESSAGE, Const.IDRECOLTEINCONNU);
			return Const.IDRECOLTEINCONNU;
		}
		if (retHausses.length() == 0) {
			return messageSource.getMessage("pasDeHausseAenlever", null, LocaleContextHolder.getLocale());
		} else {
			return messageSource.getMessage("haussesEnlevees", new Object[] { retHausses, retRuches },
					LocaleContextHolder.getLocale());
		}
	}

}