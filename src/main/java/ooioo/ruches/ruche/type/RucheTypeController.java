package ooioo.ruches.ruche.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import ooioo.ruches.Const;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheRepository;

@Controller
@RequestMapping("/rucheType")
public class RucheTypeController {

	private static final String RUCHE_RUCHETYPEFORM = "ruchetype/rucheTypeForm";
	private static final String RUCHE_RUCHETYPE = "rucheType";

	final Logger logger = LoggerFactory.getLogger(RucheTypeController.class);

	// https://docs.spring.io/spring-framework/reference/core/beans/annotation-config/autowired.html
	private final RucheTypeRepository rucheTypeRepo;
	private final RucheRepository rucheRepo;
	private final MessageSource messageSource;

	public RucheTypeController(RucheTypeRepository rucheTypeRepo, RucheRepository rucheRepo,
			MessageSource messageSource) {
		this.rucheTypeRepo = rucheTypeRepo;
		this.rucheRepo = rucheRepo;
		this.messageSource = messageSource;
	}

	/**
	 * Liste des types de ruche.
	 */
	@GetMapping("/liste")
	public String liste(Model model) {
		List<RucheType> lRT = rucheTypeRepo.findAllByOrderByNom();
		model.addAttribute(Const.RUCHETYPES, lRT);
		List<Integer> nbRuchesEssaim = new ArrayList<>();
		List<Integer> nbRuchesSansEssaim = new ArrayList<>();
		for (RucheType rt : lRT) {
			nbRuchesEssaim.add(rucheRepo.countByTypeAndActiveTrueAndEssaimNotNull(rt));
			nbRuchesSansEssaim.add(rucheRepo.countByTypeAndActiveTrueAndEssaimNull(rt));
		}
		model.addAttribute("nbRuchesEssaim", nbRuchesEssaim);
		model.addAttribute("nbRuchesSansEssaim", nbRuchesSansEssaim);
		return "ruchetype/rucheTypesListe";
	}

	/**
	 * Appel du formulaire pour la création d'un type ruche.
	 */
	@GetMapping("/cree")
	public String cree(Model model) {
		List<String> noms = new ArrayList<>();
		for (RucheType rucheType : rucheTypeRepo.findAll()) {
			noms.add(rucheType.getNom());
		}
		model.addAttribute("rucheTypeNoms", noms);
		model.addAttribute(RUCHE_RUCHETYPE, new RucheType());
		return RUCHE_RUCHETYPEFORM;
	}

	/**
	 * Afficher un type de ruche.
	 */
	@GetMapping("/{rucheTypeId}")
	public String affiche(HttpSession session, Model model, @PathVariable long rucheTypeId) {
		Optional<RucheType> rucheTypeOpt = rucheTypeRepo.findById(rucheTypeId);
		if (rucheTypeOpt.isPresent()) {
			RucheType rucheType = rucheTypeOpt.get();
			model.addAttribute(RUCHE_RUCHETYPE, rucheType);
			// Liste des ruches de ce type
			Object voirInactif = session.getAttribute(Const.VOIRINACTIF);
			List<Ruche> ruches;
			long nbRuchesTotal = 0;
			if (voirInactif != null && (boolean) voirInactif) {
				// Si affichage des Inactifs demandé dans les Préférences
				ruches = rucheRepo.findByTypeIdOrderByNom(rucheTypeId);
				nbRuchesTotal = ruches.size();
			} else {
				ruches = rucheRepo.findByActiveTrueAndTypeIdOrderByNom(rucheTypeId);
				nbRuchesTotal = rucheRepo.countByTypeIdOrderByNom(rucheTypeId);
			}
			model.addAttribute(Const.RUCHES, ruches);
			model.addAttribute("nbRuchesTotal", nbRuchesTotal);
		} else {
			logger.error(Const.IDRUCHETYPEXXINCONNU, rucheTypeId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHETYPEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "ruchetype/rucheTypeDetail";
	}

	/**
	 * Appel du formulaire pour modifier un type de ruche.
	 */
	@GetMapping("/modifie/{rucheTypeId}")
	public String modifie(Model model, @PathVariable long rucheTypeId) {
		Optional<RucheType> rucheTypeOpt = rucheTypeRepo.findById(rucheTypeId);
		if (rucheTypeOpt.isPresent()) {
			RucheType rucheType = rucheTypeOpt.get();
			model.addAttribute("rucheTypeNoms", StreamSupport.stream(rucheTypeRepo.findAll().spliterator(), false)
					.map(RucheType::getNom).filter(nom -> !nom.equals(rucheType.getNom())).toList());
			model.addAttribute(RUCHE_RUCHETYPE, rucheType);
		} else {
			logger.error("IdRucheType {} inconnu.", rucheTypeId);
			model.addAttribute(Const.MESSAGE, Const.IDRUCHETYPEINCONNU);
			return Const.INDEX;
		}
		return RUCHE_RUCHETYPEFORM;
	}

	/**
	 * Enregistrement du type de ruche.
	 */
	@PostMapping("/sauve")
	public String sauve(Model model, @ModelAttribute RucheType rucheType, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return RUCHE_RUCHETYPEFORM;
		}
		// On enlève les blancs aux extémités du commentaire.
		rucheType.setCommentaire(rucheType.getCommentaire().trim());
		// On enlève les blancs aux extémités du nom.
		rucheType.setNom(rucheType.getNom().trim());
		if ("".equals(rucheType.getNom())) {
			logger.error("{} nom incorrect.", rucheType);
			model.addAttribute(Const.MESSAGE, "Nom de type de ruche incorrect.");
			return Const.INDEX;
		}
		// Vérification de l'unicité du nom
		RucheType rNom = rucheTypeRepo.findByNom(rucheType.getNom());
		if (rNom != null && !rNom.getId().equals(rucheType.getId())) {
			logger.error("{} nom existant.", rucheType);
			model.addAttribute(Const.MESSAGE, "Nom de type de ruche existant.");
			return Const.INDEX;
		}
		String action = (rucheType.getId() == null) ? "créé" : "modifié";
		try {
			rucheTypeRepo.save(rucheType);
		} catch (ObjectOptimisticLockingFailureException e) {
			logger.error("{} modification annulée, accès concurrent.", rucheType);
			model.addAttribute(Const.MESSAGE, Const.CONCURACCESS);
			return Const.INDEX;
		}
		logger.info("{} {}", rucheType, action);
		return "redirect:/rucheType/" + rucheType.getId();
	}

	/**
	 * Suppression d'un type de ruche.
	 */
	@GetMapping("/supprime/{rucheTypeId}")
	public String supprime(Model model, @PathVariable long rucheTypeId) {
		Optional<RucheType> rucheTypeOpt = rucheTypeRepo.findById(rucheTypeId);
		if (rucheTypeOpt.isPresent()) {
			RucheType rucheType = rucheTypeOpt.get();
			long nbRuchesTotal = rucheRepo.countByTypeIdOrderByNom(rucheTypeId);
			if (nbRuchesTotal == 0) {
				rucheTypeRepo.delete(rucheType);
				logger.info("{} supprimé", rucheType);
			} else {
				model.addAttribute(Const.MESSAGE, "Ce type de ruche ne peut être supprimé");
				return Const.INDEX;
			}
		} else {
			logger.error("IdRucheType {} inconnu.", rucheTypeId);
			model.addAttribute(Const.MESSAGE, Const.IDRUCHETYPEINCONNU);
			return Const.INDEX;
		}
		return "redirect:/rucheType/liste";
	}

}