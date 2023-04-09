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
	private RucheTypeRepository rucheTypeRepository;
	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private MessageSource messageSource;

	/**
	 * Liste des types de ruche
	 */
	@GetMapping("/liste")
	public String liste(Model model) {
		model.addAttribute(Const.RUCHETYPES, rucheTypeRepository.findAll());
		return "ruchetype/rucheTypesListe";
	}

	/**
	 * Appel du formulaire pour la création d'un type ruche.
	 */
	@GetMapping("/cree")
	public String cree(Model model) {
		List<String> noms = new ArrayList<>();
		for (RucheType rucheType : rucheTypeRepository.findAll()) {
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
		Optional<RucheType> rucheTypeOpt = rucheTypeRepository.findById(rucheTypeId);
		if (rucheTypeOpt.isPresent()) {
			RucheType rucheType = rucheTypeOpt.get();
			model.addAttribute("rucheType", rucheType);
			// Liste des ruches de ce type
			Object voirInactif = session.getAttribute(Const.VOIRINACTIF);
			Iterable<Ruche> ruches;
			if (voirInactif != null && (boolean) voirInactif) {
				// Si affichage des Inactifs demandé dans les Préférences
				ruches = rucheRepository.findByTypeIdOrderByNom(rucheTypeId);
			} else {
				ruches = rucheRepository.findByActiveTrueAndTypeIdOrderByNom(rucheTypeId);
			}
			model.addAttribute(Const.RUCHES, ruches);
			
			// TODO Ajouter au model bool indiquant l'existence de ruches de ce type (actives ou inactives)
			
		} else {
			logger.error(Const.IDRUCHETYPEXXINCONNU, rucheTypeId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHETYPEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "ruchetype/rucheTypeDetail";
	}

	/**
	 * Modifie un type de ruche.
	 */
	@GetMapping("/modifie/{rucheTypeId}")
	public String modifie(Model model, @PathVariable long rucheTypeId) {
		Optional<RucheType> rucheTypeOpt = rucheTypeRepository.findById(rucheTypeId);
		if (rucheTypeOpt.isPresent()) {
			RucheType rucheType = rucheTypeOpt.get();
			model.addAttribute("rucheTypeNoms", StreamSupport.stream(rucheTypeRepository.findAll().spliterator(), false)
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
		rucheTypeRepository.save(rucheType);
		logger.info("{} {}", rucheType, action);
		return "redirect:/rucheType/" + rucheType.getId();
	}

}