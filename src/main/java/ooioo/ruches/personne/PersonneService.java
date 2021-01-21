package ooioo.ruches.personne;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import ooioo.ruches.Const;

@Service
public class PersonneService {

	private final Logger logger = LoggerFactory.getLogger(PersonneService.class);

	/**
	 * Vérification des droits utiliteurs pour modifier une "personne"
	 */
	public boolean personneDroitsInsuffisants(Personne personne, Authentication authentication, Model model) {
		// si l'utilisateur connecté n'est pas administrateur
		GrantedAuthority auth = authentication.getAuthorities().iterator().next();
		String role = auth.getAuthority();
		if (role == null) {
			return false;
		}
		if (!"ROLE_admin".equals(role) && !authentication.getName().equals(personne.getLogin())) {
			// si l'utilisateur connecté tente de modifier un autre utilisateur
			logger.error("Erreur /personne/... Droits insuffisants.");
			model.addAttribute(Const.MESSAGE, "Droits insuffisants.");
			return true;
		}
		return false;
	}

	/**
	 * Vérifie que l'utilisateur est admin
	 */
	public boolean personneAdmin(Authentication authentication, Model model) {
		// si l'utilisateur connecté n'est pas administrateur
		GrantedAuthority auth = authentication.getAuthorities().iterator().next();
		String role = auth.getAuthority();
		if ("ROLE_admin".equals(role)) {
			return true;
		}
		logger.error("Rôle admin obligatoire.");
		model.addAttribute(Const.MESSAGE, "rôle admin obligatoire.");
		return false;
	}

}