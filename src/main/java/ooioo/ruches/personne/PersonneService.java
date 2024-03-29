package ooioo.ruches.personne;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpSession;
import ooioo.ruches.Const;

@Service
public class PersonneService {

	private final Logger logger = LoggerFactory.getLogger(PersonneService.class);

	private final PersonneRepository persRepository;

	public PersonneService(PersonneRepository persRepository) {
		this.persRepository = persRepository;
	}

	private static final String droitsInsuff = "Droits insuffisants.";
	private static final String roleIncorrect = "Erreur rôle incorrect.";
	private static final String roleAdmin = "ROLE_admin";

	/**
	 * Ajoute la liste des Personnes au Model
	 *
	 * @param model
	 * @param session Pour masquer éventuellement les personnnes inactivées
	 */
	void modelPersonnes(Model model, HttpSession session) {
		Object voirInactif = session.getAttribute(Const.VOIRINACTIF);
		if (voirInactif != null && (boolean) voirInactif) {
			model.addAttribute(Const.PERSONNES, persRepository.findAllByOrderByNom());
		} else {
			model.addAttribute(Const.PERSONNES, persRepository.findByActiveTrueOrderByNom());
		}
	}

	/**
	 * Vérification du droit admin pour la personne connectée. Si pas admin : log,
	 * message dans model et return true.
	 */
	boolean pasAdmin(Authentication authentication, Model model) {
		GrantedAuthority auth = authentication.getAuthorities().iterator().next();
		String role = auth.getAuthority();
		if (role == null) {
			// authentication.getName() est déjà affiché par le logger
			logger.error(roleIncorrect);
			model.addAttribute(Const.MESSAGE, roleIncorrect);
			return true;
		}
		if (!roleAdmin.equals(role)) {
			logger.error(roleIncorrect);
			model.addAttribute(Const.MESSAGE, droitsInsuff);
			return true;
		}
		return false;
	}

	/**
	 * Vérification des droits de l'utiliteur connecté pour afficher le formulaire
	 * de modification d'une Personne. Il faut être admin pour modifier la personne
	 * passée en paramètre si son login avant modification est différent de la
	 * personne connectée.
	 */
	boolean droitsInsuffisants(Personne personne, Authentication authentication, Model model) {
		GrantedAuthority auth = authentication.getAuthorities().iterator().next();
		// Rôle de l'utilisateur connecté
		String role = auth.getAuthority();
		if (role == null) {
			logger.error(roleIncorrect);
			model.addAttribute(Const.MESSAGE, roleIncorrect);
			return true;
		}
		if (!roleAdmin.equals(role) && !authentication.getName().equals(personne.getLogin())) {
			// si l'utilisateur connecté n'est pas admin et tente de modifier ou supprimer
			// un autre utilisateur
			logger.error(droitsInsuff);
			model.addAttribute(Const.MESSAGE, droitsInsuff);
			return true;
		}
		return false;
	}

	/**
	 * Vérification des droits de l'utiliteur connecté pour modifier une "personne".
	 * Il faut être admin pour modifier la personne passée en paramètre si son login
	 * avant modification est différent de la personne connectée.
	 */
	boolean pDroitsInsufSauve(Personne personne, Authentication authentication, Model model) {
		GrantedAuthority auth = authentication.getAuthorities().iterator().next();
		// Rôle de l'utilisateur connecté : ROLE_admin ou ROLE_
		String role = auth.getAuthority();
		if (role == null) {
			logger.error(roleIncorrect);
			model.addAttribute(Const.MESSAGE, roleIncorrect);
			return true;
		}
		// Si utilisateur non admin
		if (!roleAdmin.equals(role)) {
			// le login de la personne qu'il modifie doit être le sien
			// et son role ne doit pas être changé
			if (!authentication.getName().equals(personne.getLogin()) || !role.equals("ROLE_" + personne.getRoles())) {
				logger.error(droitsInsuff);
				model.addAttribute(Const.MESSAGE, droitsInsuff);
				return true;
			}
		}
		return false;
	}

}