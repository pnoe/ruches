package ooioo.ruches.personne;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import ooioo.ruches.rucher.Rucher;
import ooioo.ruches.rucher.RucherRepository;

@Controller
@RequestMapping("/personne")
public class PersonneController {

	private static final String PERSONNE_PERSONNEFORM = "personne/personneForm";

	private final Logger logger = LoggerFactory.getLogger(PersonneController.class);

	@Autowired
	private PersonneRepository personneRepository;
	@Autowired
	private RucherRepository rucherRepository;
	@Autowired
	private PersonneService pService;

	@GetMapping("/liste")
	public String liste(HttpSession session, Model model) {
		pService.modelPersonnes(model, session);
		return "personne/personnesListe";
	}

	/**
	 * Appel du formulaire pour l'ajout d'une personne. Il faut être admin.
	 */
	@GetMapping("/cree")
	public String cree(Model model, Authentication authentication) {
		// Il faut être admin.
		if (pService.pasAdmin(authentication, model)) {
			return Const.INDEX;
		}
		List<String> logins = new ArrayList<>();
		for (PersonneLogin personneLogin : personneRepository.findAllProjectedBy()) {
			if (!"".equals(personneLogin.login())) {
				logins.add(personneLogin.login());
			}
		}
		model.addAttribute("personneLogins", logins);
		List<String> emails = new ArrayList<>();
		for (PersonneEmail personneEmail : personneRepository.findAllProjectedEmailBy()) {
			if (!"".equals(personneEmail.email())) {
				emails.add(personneEmail.email());
			}
		}
		model.addAttribute("personneEmails", emails);
		model.addAttribute(Const.PERSONNE, new Personne());
		return PERSONNE_PERSONNEFORM;
	}

	/**
	 * Appel du formulaire pour modifier une personne. Il faut être admin pour
	 * modifier une autre Personne que soi même.
	 */
	@GetMapping("/modifie/{personneId}")
	public String modifie(Model model, @PathVariable long personneId, Authentication authentication) {
		Optional<Personne> personneOpt = personneRepository.findById(personneId);
		if (personneOpt.isPresent()) {
			Personne personne = personneOpt.get();
			// Il faut être admin pour modifier une autre Personne.
			if (pService.droitsInsuffisants(personne, authentication, model)) {
				return Const.INDEX;
			}
			List<String> logins = new ArrayList<>();
			for (PersonneLogin personneLogin : personneRepository.findAllProjectedBy()) {
				if (!"".equals(personneLogin.login()) && !personne.getLogin().equals(personneLogin.login())) {
					logins.add(personneLogin.login());
				}
			}
			model.addAttribute("personneLogins", logins);
			List<String> emails = new ArrayList<>();
			for (PersonneEmail personneEmail : personneRepository.findAllProjectedEmailBy()) {
				if (!"".equals(personneEmail.email()) && !personne.getEmail().equals(personneEmail.email())) {
					emails.add(personneEmail.email());
				}
			}
			model.addAttribute("personneEmails", emails);
			model.addAttribute(Const.PERSONNE, personne);
		} else {
			logger.error(Const.IDPERSONNEXXINCONNU, personneId);
			model.addAttribute(Const.MESSAGE, Const.IDPERSONNEINCONNU);
			return Const.INDEX;
		}
		return PERSONNE_PERSONNEFORM;
	}

	/**
	 * Suppression d'une personne. On peut se supprimer sauf si on est référencé
	 * dans des ruchers. Il faut être admin pour supprimer une Personne.
	 */
	@GetMapping("/supprime/{personneId}")
	public String supprime(Model model, @PathVariable long personneId, Authentication authentication) {
		Optional<Personne> personneOpt = personneRepository.findById(personneId);
		if (personneOpt.isPresent()) {
			Personne personne = personneOpt.get();
			// Il faut être admin.
			if (pService.pasAdmin(authentication, model)) {
				return Const.INDEX;
			}
			Collection<Rucher> ruchers = rucherRepository.findByContactId(personneId);
			if (ruchers.isEmpty()) {
				personneRepository.delete(personne);
				logger.info("{} supprimée", personne);
			} else {
				model.addAttribute(Const.MESSAGE, "Cette personne ne peut être supprimée");
				return Const.INDEX;
			}
		} else {
			logger.error(Const.IDPERSONNEXXINCONNU, personneId);
			model.addAttribute(Const.MESSAGE, Const.IDPERSONNEINCONNU);
			return Const.INDEX;
		}
		return "redirect:/personne/liste";
	}

	/**
	 * Enregistrement de la personne crée ou modifiée.
	 */
	@PostMapping("/sauve")
	public String sauve(@ModelAttribute Personne personne, BindingResult bindingResult, Model model,
			Authentication authentication) {
		// Il faut être admin pour créer une Personne ou pour modifier une autre
		// personne que soi-même. Un non admin ne peut changer son rôle.
		if (pService.pDroitsInsufSauve(personne, authentication, model)) {
			return Const.INDEX;
		}
		if (bindingResult.hasErrors()) {
			return PERSONNE_PERSONNEFORM;
		}

		// On enlève les blancs aux extémités du nom.
		personne.setNom(personne.getNom().trim());
		if ("".equals(personne.getNom())) {
			logger.error("{} nom incorrect.", personne);
			model.addAttribute(Const.MESSAGE, "Nom de personne incorrect.");
			return Const.INDEX;
		}

		// On enlève les blancs aux extémités du prénom.
		personne.setPrenom(personne.getPrenom().trim());
		if ("".equals(personne.getPrenom())) {
			logger.error("{} prénom incorrect.", personne);
			model.addAttribute(Const.MESSAGE, "Prénom de personne incorrect.");
			return Const.INDEX;
		}

		// Vérification de l'unicité du login si différent de ""
		// et si la personne retrouvée en base n'est pas la personne elle même
		if (!"".equals(personne.getLogin())) {
			Personne pL = personneRepository.findByLogin(personne.getLogin());
			if (pL != null && !pL.getId().equals(personne.getId())) {
				logger.error("Login {} existant.", pL.getLogin());
				model.addAttribute(Const.MESSAGE, "Login existant.");
				return Const.INDEX;
			}
		}
		if (!"".equals(personne.getEmail())) {
			// Vérification de l'unicité de l'email si différent de ""
			Personne pE = personneRepository.findByEmail(personne.getEmail());
			if (pE != null && !pE.getId().equals(personne.getId())) {
				logger.error("Email {} existant.", pE.getEmail());
				model.addAttribute(Const.MESSAGE, "Email existant.");
				return Const.INDEX;
			}
		}
		String password = personne.getPassword();
		// si pas de password saisi on récupère le password existant si la personne
		// existe en base
		if ("".equals(password)) {
			if (personne.getId() != null) {
				Optional<Personne> personneOptTemp = personneRepository.findById(personne.getId());
				if (personneOptTemp.isPresent()) {
					String passwordTemp = personneOptTemp.get().getPassword();
					personne.setPassword(passwordTemp);
				} else {
					// erreur personne.id existe mais rien en base
					logger.error("Récupération en base de {}", personne.getId());
					model.addAttribute(Const.MESSAGE, "Erreur base de données.");
					return Const.INDEX;
				}
			}
			// Sinon la personne n'existe pas en base et son password est "".
			// C'est possible pour une personne que n'a pas de login
			// comme un simple contact pour un rucher.
		} else {
			personne.setPassword(new BCryptPasswordEncoder().encode(password));
		}
		String action = (personne.getId() == null) ? "créée" : "modifiée";
		personneRepository.save(personne);
		logger.info("{} {}", personne, action);
		return "redirect:/personne/" + personne.getId();
	}

	/**
	 * Détail d'une personne
	 */
	@GetMapping("/{personneId}")
	public String personne(Model model, @PathVariable long personneId) {
		Optional<Personne> personneOpt = personneRepository.findById(personneId);
		if (personneOpt.isPresent()) {
			// Si des ruchers référencent cette personne, on ne pourra pas la supprimer
			Collection<Rucher> ruchers = rucherRepository.findByContactId(personneId);
			model.addAttribute("ruchers", ruchers);
			model.addAttribute(Const.PERSONNE, personneOpt.get());
		} else {
			logger.error(Const.IDPERSONNEXXINCONNU, personneId);
			model.addAttribute(Const.MESSAGE, Const.IDPERSONNEINCONNU);
			return Const.INDEX;
		}
		return "personne/personneDetail";
	}

}