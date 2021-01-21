package ooioo.ruches.ruche;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ooioo.ruches.Const;

@Controller
@RequestMapping("/rucheType")
public class RucheTypeController {

	private static final String RUCHE_RUCHETYPEFORM = "ruche/rucheTypeForm";

	final Logger logger = LoggerFactory.getLogger(RucheTypeController.class);

	@Autowired
	private RucheTypeRepository rucheTypeRepository;

	/**
	 * Liste des types de ruche
	 */
	@GetMapping("/liste")
	public String liste(Model model) {
		model.addAttribute(Const.RUCHETYPES, rucheTypeRepository.findAll());
		return "ruche/rucheTypesListe";
	}

	/**
	 * Appel du formulaire pour la création d'un type ruche
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
	 * Modifie un type de ruche
	 */
	@GetMapping("/modifie/{rucheTypeId}")
	public String modifie(Model model, @PathVariable long rucheTypeId) {
		Optional<RucheType> rucheTypeOpt = rucheTypeRepository.findById(rucheTypeId);
		if (rucheTypeOpt.isPresent()) {
			RucheType rucheType = rucheTypeOpt.get();
			model.addAttribute("rucheTypeNoms",
					StreamSupport.stream(rucheTypeRepository.findAll().spliterator(), false).map(RucheType::getNom)
							.filter(nom -> !nom.equals(rucheType.getNom())).collect(Collectors.toList()));
			model.addAttribute("rucheType", rucheType);
		} else {
			logger.error("IdRucheType {} inconnu.", rucheTypeId);
			model.addAttribute(Const.MESSAGE, Const.IDRUCHETYPEINCONNU);
			return Const.INDEX;
		}
		return RUCHE_RUCHETYPEFORM;
	}

	/**
	 * Enregistrement du type de ruche
	 */
	@PostMapping("/sauve")
	public String sauve(@ModelAttribute RucheType rucheType, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return RUCHE_RUCHETYPEFORM;
		}
		rucheTypeRepository.save(rucheType);
		logger.info("RucheType {} enregistré, id {}", rucheType.getNom(), rucheType.getId());
		return "redirect:/rucheType/liste";
	}

}