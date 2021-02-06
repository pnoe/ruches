package ooioo.ruches.recolte;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
	
	@Value("${hausse.reste.miel}")
	private BigDecimal hausseResteMiel;
	

	@Autowired
	private RecolteHausseService recolteHausseService;

	/**
	 * Afficher une récolte avec ses hausses
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
			
			model.addAttribute("nbruches", recolteHausseService.nomsRuches(recolteHausses).size());
			
			model.addAttribute(Const.RUCHER, recolteHausseService.nomsRuchers(recolteHausses));
		} else {
			logger.error(Const.IDRECOLTEXXINCONNU, recolteId);
			model.addAttribute(Const.MESSAGE, Const.IDRECOLTEINCONNU);
			return Const.INDEX;
		}
		return "recolte/recolteDetail";
	}

	/**
	 * Afficher le formulaire du détail d'une hausse d'une récolte
	 */
	@GetMapping("/modifie/hausse/{recolteId}/{recolteHausseId}")
	public String modifie(Model model, @PathVariable long recolteHausseId, @PathVariable long recolteId) {
		Optional<RecolteHausse> recolteHausseOpt = recolteHausseRepository.findById(recolteHausseId);
		if (recolteHausseOpt.isPresent()) {
			Optional<Recolte> recolteOpt = recolteRepository.findById(recolteId);
			if (recolteOpt.isPresent()) {
				model.addAttribute(Const.RUCHES, rucheRepository.findAllProjectedIdNomByOrderByNom());
				model.addAttribute(Const.RUCHERS, rucherRepository.findAllProjectedIdNomByOrderByNom());
				model.addAttribute(Const.ESSAIMS, essaimRepository.findAllProjectedIdNomByOrderByNom());
				model.addAttribute("detailRecolte", recolteHausseOpt.get());
				model.addAttribute("recolte", recolteOpt.get());
			} else {
				logger.error("Id récolte {} inconnu.", recolteId);
				model.addAttribute(Const.MESSAGE, "Id récolte inconnu.");
				return Const.INDEX;
			}
		} else {
			logger.error("Id récolte hausse {} inconnu.", recolteHausseId);
			model.addAttribute(Const.MESSAGE, "Id récolte hausse inconnu.");
			return Const.INDEX;
		}
		return "recolte/recolteHausseForm";
	}

	/**
	 * Sauver le détail d'une hausse de la récolte
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
	 * Choix des hausses d'une récolte
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
	 * Ajout d'une série de hausses dans la récolte
	 */
	@GetMapping("/ajoutHausses/{recolteId}/{haussesNoms}")
	public String ajoutHausses(Model model, @PathVariable long recolteId, @PathVariable String[] haussesNoms) {
		for (String hausseNom : haussesNoms) {
			Optional<Hausse> hausseOpt = hausseRepository.findByNom(hausseNom);
			Optional<Recolte> recolteOpt = recolteRepository.findById(recolteId);
			if (recolteOpt.isPresent()) {
				if (hausseOpt.isPresent()) {
					Recolte recolte = recolteOpt.get();
					Hausse hausse = hausseOpt.get();
					BigDecimal poids = hausse.getPoidsVide().add(hausseResteMiel);
					RecolteHausse recolteHausse = new RecolteHausse(recolte, hausse, poids,	poids);
					Ruche ruche = hausse.getRuche();
					if (ruche == null) {
						// la hausse n'est pas sur une ruche. On ne saura ni la ruche, ni le rucher,
						// ni l'essaim correspondants
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
					logger.error("Nom hausse {} inconnu", hausseNom);
				}
			} else {
				logger.error(Const.IDRECOLTEXXINCONNU, recolteId);
				model.addAttribute(Const.MESSAGE, Const.IDRECOLTEINCONNU);
				return Const.INDEX;
			}
		}
		// si redirect pas besoin de addattribute !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		return "redirect:/recolte/choixHausses/" + recolteId;
	}

	/**
	 * Retrait d'une série de hausses dans la récolte
	 */
	@GetMapping("/retraitHausses/{recolteId}/{haussesNoms}")
	public String retraitHausses(Model model, @PathVariable long recolteId, @PathVariable String[] haussesNoms) {
		for (String hausseNom : haussesNoms) {
			Optional<Recolte> recolteOpt = recolteRepository.findById(recolteId);
			Optional<Hausse> hausseOpt = hausseRepository.findByNom(hausseNom);
			if (recolteOpt.isPresent()) {
				if (hausseOpt.isPresent()) {
					Recolte recolte = recolteOpt.get();
					Hausse hausse = hausseOpt.get();
					RecolteHausse recolteHausse = recolteHausseRepository.findByRecolteAndHausse(recolte, hausse);
					recolteHausseRepository.delete(recolteHausse);
				} else {
					logger.error("Nom hausse {} inconnu", hausseNom);
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
	 * Enlève les hausses de la récolte des ruches 
	 *  crée les événements retraits des hausses
	 *   et remplissage à 0
	 *   (appel XMLHttpRequest)
	 */
	@PostMapping("/haussesDepot/{recolteId}")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody String haussesDepot(HttpSession session, Model model, @PathVariable long recolteId) {
		Optional<Recolte> recolteOpt = recolteRepository.findById(recolteId);
		StringBuilder retHausses =  new StringBuilder();
		StringBuilder retRuches = new StringBuilder();
		if (recolteOpt.isPresent()) {
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
					//   de la hausseRécolte correspondante.
					//   Si différente ne rien faire, la hausse a été replacée sur une autre ruche
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
					// TODO date de l'événement modifiable dans un formulaire ?
					String commentaireEve = "Récolte " + recolte.getDate().toString(); 
					Evenement evenementRetrait = new Evenement(Utils.dateTimeDecal(session),
							TypeEvenement.HAUSSERETRAITRUCHE, ruche, essaim, rucher, hausse,
							hausse.getOrdreSurRuche().toString(), commentaireEve);
					evenementRepository.save(evenementRetrait);
					Evenement evenementRemplissage = new Evenement(Utils.dateTimeDecal(session),
							TypeEvenement.HAUSSEREMPLISSAGE, ruche, essaim, rucher, hausse,
							"0", commentaireEve);
					evenementRepository.save(evenementRemplissage);
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
			return "Pas de hausses à enlever de leurs ruches";
		} else {
			return "Les hausses " + retHausses + " ont été enlevées des ruches " + retRuches;
		}
		
	}

}