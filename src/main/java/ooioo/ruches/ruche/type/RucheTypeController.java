package ooioo.ruches.ruche.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ooioo.ruches.Const;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheRepository;

@Controller
@RequestMapping("/rucheType")
public class RucheTypeController {

	private static final String RUCHE_RUCHETYPEFORM = "ruchetype/rucheTypeForm";

	final Logger logger = LoggerFactory.getLogger(RucheTypeController.class);

	@Autowired
	private RucheTypeRepository rucheTypeRepo;
	@Autowired
	private RucheRepository rucheRepo;
	@Autowired
	private MessageSource messageSource;

	/**
	 * Liste des types de ruche.
	 */
	@GetMapping("/liste")
	public String liste(Model model) {
		List<RucheType> lRT = rucheTypeRepo.findAllByOrderByNom();
		model.addAttribute(Const.RUCHETYPES, lRT);
		List<Integer> nbRuches = new ArrayList<>();
		for(RucheType rt : lRT) {
			nbRuches.add(rucheRepo.countByTypeAndActiveTrue(rt));
		}
		model.addAttribute("NbRuches", nbRuches);
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
		model.addAttribute("rucheType", new RucheType());
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
			model.addAttribute("rucheType", rucheType);
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
			model.addAttribute("rucheType", rucheType);
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
	public String sauve(@ModelAttribute RucheType rucheType, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return RUCHE_RUCHETYPEFORM;
		}
		String action = (rucheType.getId() == null) ? "créé" : "modifié";
		rucheTypeRepo.save(rucheType);
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