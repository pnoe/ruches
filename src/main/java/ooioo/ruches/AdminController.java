package ooioo.ruches;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

	@Autowired
	private AdminService adminService;

	/**
	 * Tests2 recherche les erreurs de l'ajout et du retrait des ruches dans les
	 * ruchers. En partant du rucher vide et en ajoutant et retirant les ruches en
	 * parcourant les événements RUCHEAJOUTRUCHER dans l'orde ascendant des dates
	 * (plus ancien au plus récent) résultat dans la page tests.html. Test2
	 * (contrairement à Tests ci-après) n'est pas appelé par un menu et doit être
	 * appelé en tapant son url.
	 */
	@GetMapping("/tests2")
	public String tests2(Model model) {
		adminService.tests2(model);
		return "tests";
	}

	/**
	 * Tests recherche les erreurs de l'ajout et du retrait des ruches dans les
	 * ruchers. En partant du rucher dans son état actuel et en ajoutant et retirant
	 * les ruches en parcourant les événements RUCHEAJOUTRUCHER dans l'orde
	 * descendant des dates (plus récent au plus ancien) résultat dans la page
	 * tests.html. Tests de la validité des événements. Tests erreurs ajout/retrait
	 * des hausses sur les ruches.
	 */
	@GetMapping("/tests")
	public String tests(Model model) {
		adminService.tests(model);
		return "tests";
	}

}