package ooioo.ruches.personne;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import ooioo.ruches.Const;

@Service
public class PersonneService {

	private final Logger logger = LoggerFactory.getLogger(PersonneService.class);

	@Autowired
	private PersonneRepository persRepository;

	/**
	 * Ajoute la liste des Personnes au Model
	 *
	 * @param model
	 * @param session Pour masquer éventuellement les personnnes inactivées
	 */
	public void modelPersonnes(Model model, HttpSession session) {
		Object voirInactif = session.getAttribute(Const.VOIRINACTIF);
		if (voirInactif != null && (boolean) voirInactif) {
			model.addAttribute(Const.PERSONNES, persRepository.findAllByOrderByNom());
		} else {
			model.addAttribute(Const.PERSONNES, persRepository.findByActiveOrderByNom(true));
		}
	}

	/**
	 * Vérification des droits utiliteurs pour créer ou modifier une "personne"
	 */
	public boolean personneDroitsInsuffisants(Personne personne, Authentication authentication, Model model) {
		// si l'utilisateur connecté n'est pas administrateur
		GrantedAuthority auth = authentication.getAuthorities().iterator().next();
		String role = auth.getAuthority();
		if (role == null) {
			return false;
		}
		if (!"ROLE_admin".equals(role) && !authentication.getName().equals(personne.getLogin())) {
			// si l'utilisateur connecté n'est pas admin et tente de modifier un autre utilisateur
			logger.error("Erreur /personne/... Droits insuffisants.");
			model.addAttribute(Const.MESSAGE, "Droits insuffisants.");
			return true;
		}
		return false;
	}

}