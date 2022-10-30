package ooioo.ruches;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ooioo.ruches.mail.EmailService;
import ooioo.ruches.personne.Personne;
import ooioo.ruches.personne.PersonneRepository;

@Controller
public class AccueilController {

	private final Logger logger = LoggerFactory.getLogger(AccueilController.class);

	@Autowired
	ServletContext servletContext;
	@Autowired
	public EmailService emailService;
	@Autowired
	private PersonneRepository personneRepository;
	@Autowired
	private AccueilService accueilService;

	/**
	 * Page d'accueil
	 */
	@GetMapping(path = "/")
	public String index(Model model) {
		return Const.INDEX;
	}

	/**
	 * Calcul des distances entre les ruchers par appel de l'api ign de calcul
	 * d'itinéraire.
	 */
	@GetMapping(path = "/dist")
	public String dist(Model model, @RequestParam(required = false) boolean reset) {
		accueilService.dist(reset);
		model.addAttribute(Const.MESSAGE, "Calcul des distances terminé.");
		return Const.INDEX;
	}
	
	/**
	 * Affiche la page des informations détaillées sur l'état général.
	 * Appel par menu Admin/Infos.
	 */
	@GetMapping(path = "/infos")
	public String infos(Model model) {
		accueilService.infos(model);
		return "infos";
	}

	@GetMapping(path = "/login")
	public String login() {
		return "login";
	}

	@GetMapping("/login-error")
	public String loginError(Model model) {
		model.addAttribute("loginError", true);
		return "login";
	}

	/**
	 * Appel du formulaire de saisie des préférences. Affichages inactifs et décalage
	 * de date.
	 */
	@GetMapping("/parametres")
	public String parametres() {
		return "parametresForm";
	}

	/**
	 * Sauvegarde en session des préférences.
	 */
	@PostMapping("/sauveParametres")
	public String sauveParametres(HttpSession session, @RequestParam(defaultValue = "false") boolean voirInactif,
			@RequestParam(defaultValue = "false") boolean voirLatLon, @RequestParam String date) {
		session.setAttribute(Const.VOIRINACTIF, voirInactif);
		session.setAttribute(Const.VOIRLATLON, voirLatLon);
		if (date.equals("")) {
			session.removeAttribute(Const.DECALAGETEMPS);
		} else {
			LocalDateTime dateSaisieDecalage = LocalDateTime.parse(date,
					DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
			Duration decalage = Duration.between(LocalDateTime.now(), dateSaisieDecalage);
			session.setAttribute(Const.DECALAGETEMPS, decalage);
		}
		return "redirect:/";
	}

	@GetMapping("/forgotPassword")
	public String forgotPassword() {
		return "passwordResetForm";
	}

	@PostMapping("/resetPassword")
	public String resetPassword(@RequestParam String email, HttpServletRequest request, Model model) {
		final long tokenValidite = 60;
		if ("".contentEquals(email)) {
			// email à valider coté client
			// evite erreur si "" findByEmail retourne plusieurs personnes
			logger.error("Réinitialisation du mot de passe, email incorrect {}", email);
			model.addAttribute(Const.MESSAGE, "Email incorrect");
			return Const.INDEX;
		}
		// si plusieurs personnes avec le même email erreur findByEmail
		Personne personne = personneRepository.findByEmail(email);
		if ((personne == null) || ("".contentEquals(personne.getLogin()))) {
			// Pas d'email trouvé ou persone sans login
			logger.error("Réinitialisation du mot de passe, email {} incorrect", email);
			model.addAttribute(Const.MESSAGE, "Email incorrect");
			return Const.INDEX;
		} else {
			String token = UUID.randomUUID().toString();
			personne.setToken(token);
			personne.setTokenExpiration(LocalDateTime.now().plusMinutes(tokenValidite));
			personneRepository.save(personne);
			StringBuffer appUrl = request.getRequestURL();
			emailService.sendSimpleMessage(email, "Réinitialisation du mot de passe",
					"Pour réinitialiser votre mot de passe, cliquez sur le lien ci-dessous:\n" + appUrl + "?token="
							+ token);
		}
		model.addAttribute(Const.MESSAGE, "Un email a été envoyé à cette adresse");
		return Const.INDEX;
	}

	@GetMapping("/resetPassword")
	public String resetPasswordGet(@RequestParam String token, HttpServletRequest request, Model model) {
		Personne personne = personneRepository.findByToken(token);
		if ((personne == null) || ("".contentEquals(personne.getLogin()))
				|| personne.getTokenexpiration().isBefore(LocalDateTime.now())) {
			logger.error("Réinitialisation du mot de passe, token {} invalide", token);
			model.addAttribute(Const.MESSAGE, "Token invalide");
			return Const.INDEX;
		}
		model.addAttribute("token", token);
		return "resetPasswordForm";
	}

	@PostMapping("/resetPasswordFin")
	public String resetPasswordFin(@RequestParam String token, @RequestParam String password,
			HttpServletRequest request, Model model) {
		Personne personne = personneRepository.findByToken(token);
		if ((personne == null) || ("".contentEquals(personne.getLogin()))
				|| personne.getTokenexpiration().isBefore(LocalDateTime.now())) {
			logger.error("Réinitialisation du mot de passe, token {} invalide", token);
			model.addAttribute(Const.MESSAGE, "Token invalide");
			return Const.INDEX;
		}
		personne.setPassword(new BCryptPasswordEncoder().encode(password));
		personne.setToken(null);
		personne.setTokenExpiration(null);
		personneRepository.save(personne);
		return "redirect:login";
	}

}