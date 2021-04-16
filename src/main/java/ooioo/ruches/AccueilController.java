package ooioo.ruches;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.essaim.EssaimRepository;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.hausse.HausseRepository;
import ooioo.ruches.mail.EmailService;
import ooioo.ruches.personne.Personne;
import ooioo.ruches.personne.PersonneRepository;
import ooioo.ruches.recolte.Recolte;
import ooioo.ruches.recolte.RecolteRepository;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheRepository;
import ooioo.ruches.rucher.Rucher;
import ooioo.ruches.rucher.RucherRepository;
import ooioo.ruches.rucher.RucherService;

@Controller
public class AccueilController {
	
	private final Logger logger = LoggerFactory.getLogger(AccueilController.class);

	@Autowired
	ServletContext servletContext;
	@Autowired
	public EmailService emailService;
	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private EssaimRepository essaimRepository;
	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private RucherRepository rucherRepository;
	@Autowired
	private HausseRepository hausseRepository;
	@Autowired
	private RecolteRepository recolteRepository;
	@Autowired
	private RucherService rucherService;
	@Autowired
	private PersonneRepository personneRepository;
	
	@Value("${accueil.titre}")
	private String accueilTitre;
	@Value("${dist.ruches.loins}")
	private double distRuchesTropLoins;
	@Value("${dist.ruchers.loins}")
	private double distRuchersTropLoins;
	@Value("${retard.ruches.evenement}")
	private int retardRucheEvenement;
	@Value("${rucher.butinage.rayons}")
	private int[] rayonsButinage;
	
	/**
	 * Page d'accueil
	 */
	@GetMapping(path = "/")
	public String index(Model model) {
		model.addAttribute(Const.ACCUEILTITRE, accueilTitre);
		return Const.INDEX;
	}
	
	@GetMapping(path = "/infos")
	public String infos(Model model) {
		
		model.addAttribute("rayonsButinage", rayonsButinage);
		
		long nbRuches = rucheRepository.countByActiveTrue();
		long nbRuchesAvecEssaim = rucheRepository.countByEssaimNotNullAndActiveTrue();
		long nbHaussesSurRuchesAvecEssaim = hausseRepository.countByActiveTrueAndRucheNotNullAndRucheActiveTrueAndRucheEssaimNotNull();
		long nbHausses = hausseRepository.countByActiveTrue();
		long nbHaussesHorsRuche = hausseRepository.countByActiveTrueAndRucheNull();
		long nbRuchesAuDepot = rucheRepository.countByActiveTrueAndRucherDepotTrue();
		long nbRuchers = rucherRepository.countByActifTrue();
		Iterable<Ruche> ruchesDepotEssaim = rucheRepository.findByEssaimNotNullAndRucherDepotTrue();
		Iterable<Ruche> ruchesDepotHausses = rucheRepository.findByHaussesAndDepot();
		Iterable<Ruche> ruchesPasDepotSansEssaim = rucheRepository.findByEssaimNullAndRucherDepotFalse();
		Iterable<Essaim> essaimsActifSansRuche = essaimRepository.findEssaimByActifSansRuche();
		Essaim premierEssaim = essaimRepository.findFirstByOrderByDateAcquisition();
		int dateDebut;
		if (premierEssaim == null) {
			dateDebut = 1;
		} else {
			dateDebut = premierEssaim.getDateAcquisition().getYear();
		}
		Essaim dernierEssaim = essaimRepository.findFirstByOrderByDateAcquisitionDesc();
		Recolte derniereRecolte = recolteRepository.findFirstByOrderByDateDesc();
		int dateFin;
		if ((dernierEssaim == null) || (derniereRecolte == null)) {
			dateFin = 0;
		} else {
			dateFin = java.lang.Math.max(dernierEssaim.getDateAcquisition().getYear(),
				derniereRecolte.getDate().getYear());
		}
		List<Integer> nbEssaims = new ArrayList<>();
		List<Integer> nbCreationEssaims = new ArrayList<>();
		List<Double> pdsMiel = new ArrayList<>();
		List<Double> annees = new ArrayList<>();
		Integer nbCreationEssaimsTotal = 0;
		Integer nbDispersionEssaimsTotal = 0;
		List<Integer> nbDispersionEssaims = new ArrayList<>();
		Double sucreEssaimsTotal = 0.0;
		List<Double> sucreEssaims = new ArrayList<>();
		Integer nbTraitementsEssaimsTotal = 0;
		List<Integer> nbTraitementsEssaims = new ArrayList<>();

		Double pdsMielTotal = 0d;
		for(int i = dateDebut; i <= dateFin; i++) {
			Double date = Double.valueOf(i);
			annees.add(date);
			Double poids = recolteRepository.findPoidsMielByYear(date);
			pdsMiel.add(poids == null?0:poids);
			pdsMielTotal += (poids == null?0:poids);
			Integer nbCree = essaimRepository.countEssaimsCreesDate(date);
			nbCreationEssaimsTotal += nbCree;
			nbCreationEssaims.add(nbCree);
			Integer nbDisperse = evenementRepository.countDispersionEssaimParAnnee(date);
			nbDispersionEssaimsTotal += nbDisperse;
			nbDispersionEssaims.add(nbDisperse);
			nbEssaims.add(nbCree - nbDisperse);
			Double sucreAnneeEssaims = evenementRepository.sucreEssaimParAnnee(date);
			if (sucreAnneeEssaims == null) { sucreAnneeEssaims = 0.0; }
			sucreEssaimsTotal += sucreAnneeEssaims;
			sucreEssaims.add(sucreAnneeEssaims);
			
			Integer nbTraitements = evenementRepository.countTraitementsEssaimParAnnee(date);
			nbTraitementsEssaimsTotal += nbTraitements;
			nbTraitementsEssaims.add(nbTraitements);
			
		}
		pdsMiel.add(pdsMielTotal);
		nbCreationEssaims.add(nbCreationEssaimsTotal);
		nbTraitementsEssaims.add(nbTraitementsEssaimsTotal);
		sucreEssaims.add(sucreEssaimsTotal);
		nbDispersionEssaims.add(nbDispersionEssaimsTotal);
		for(int j = 1; j < nbEssaims.size(); j++) {
			nbEssaims.set(j, nbEssaims.get(j) + nbEssaims.get(j - 1));
		}
		model.addAttribute("nbTraitementsEssaims", nbTraitementsEssaims);
		model.addAttribute("sucreEssaims", sucreEssaims);
		model.addAttribute("nbEssaims", nbEssaims);
		model.addAttribute("nbDispersionEssaims", nbDispersionEssaims);
		model.addAttribute("nbCreationEssaims", nbCreationEssaims);
		model.addAttribute("pdsMiel", pdsMiel);
		model.addAttribute("annees", annees);
		model.addAttribute(Const.ACCUEILTITRE, accueilTitre);
		model.addAttribute("nbRuchesAuDepot", nbRuchesAuDepot);
		model.addAttribute("nbRuches", nbRuches);
		model.addAttribute("nbRuchers", nbRuchers);
		model.addAttribute("nbRuchesAvecEssaim", nbRuchesAvecEssaim);
		model.addAttribute("nbHausses", nbHausses);
		model.addAttribute("nbHaussesHorsRuche", nbHaussesHorsRuche);
		model.addAttribute("nbHaussesSurRuchesAvecEssaim", nbHaussesSurRuchesAvecEssaim);
		model.addAttribute("ruchesDepotEssaim", ruchesDepotEssaim);
		model.addAttribute("ruchesDepotHausses", ruchesDepotHausses);
		model.addAttribute("ruchesPasDepotSansEssaim", ruchesPasDepotSansEssaim);
		model.addAttribute("essaimsActifSansRuche", essaimsActifSansRuche);
		// ruches trop éloignées de leurs ruchers
		Iterable<Rucher> ruchers = rucherRepository.findByActifOrderByNom(true);
		List<Ruche> ruchesTropLoins = new ArrayList<>();
		for (Rucher rucher : ruchers) {
			Float longRucher = rucher.getLongitude();
			Float latRucher = rucher.getLatitude();
			Iterable<Ruche> ruches = rucheRepository.findByRucherIdOrderByNom(rucher.getId());
			for (Ruche ruche : ruches) {
				if (rucherService.distance(latRucher, ruche.getLatitude(), longRucher, ruche.getLongitude()) > distRuchesTropLoins) {
					ruchesTropLoins.add(ruche);
				}
			}
		}
		model.addAttribute("ruchesTropLoins", ruchesTropLoins);
		model.addAttribute("distRuchesTropLoins", distRuchesTropLoins);
		// rucher trop éloignés du barycentre de ses ruches
		List<Rucher> ruchersMalCales = new ArrayList<>();
		List<Double> distances =  new ArrayList<>();
		for (Rucher rucher : ruchers) {
			Float longitude;
			Float latitude = 0f;		
			Iterable<Ruche> ruches = rucheRepository.findByRucherIdOrderByNom(rucher.getId());
			int nbRuches2 = 0;
			double xlon = 0d;
			double ylon = 0d;
			for (Ruche ruche : ruches) {
				nbRuches2++;
				Float longrad = (float) (ruche.getLongitude() * Math.PI / 180.0d);
				xlon += Math.cos(longrad);
				ylon += Math.sin(longrad);
				latitude += ruche.getLatitude();
			}
			if (nbRuches2 == 0) continue;
			longitude = (float) (Math.atan2(ylon, xlon) * 180d / Math.PI);
			latitude /= nbRuches2;
			double dist = rucherService.distance(latitude, rucher.getLatitude(), longitude, rucher.getLongitude());
			if (dist > distRuchersTropLoins) {
				ruchersMalCales.add(rucher);
				distances.add(dist);
			}
		}
		model.addAttribute("ruchersMalCales", ruchersMalCales);
		model.addAttribute("distances", distances);
		model.addAttribute("distRuchersTropLoins", distRuchersTropLoins);
		
		LocalDateTime date = LocalDateTime.now().minus(retardRucheEvenement, ChronoUnit.WEEKS);
		Iterable<Ruche> ruchesPasDEvenement = rucheRepository.findPasDEvenementAvant(date);
		model.addAttribute("ruchesPasDEvenement", ruchesPasDEvenement);
		model.addAttribute("retardRucheEvenement", retardRucheEvenement);
		
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
	 * Appel du formulaire de saisie des préférences
	 *  Affichages inactifs et décalage de date 
	 */
	@GetMapping("/parametres")
	public String parametres() {
		return "parametresForm";
	}
	
	/**
	 * Sauvegarde en session des préférences 
	 */
	@PostMapping("/sauveParametres")
	public String sauveParametres(HttpSession session, @RequestParam(defaultValue = "false") boolean voirInactif,
			@RequestParam(defaultValue = "false") boolean voirLatLon,
			@RequestParam String date) {
		session.setAttribute(Const.VOIRINACTIF,voirInactif);
		session.setAttribute(Const.VOIRLATLON,voirLatLon);
		if (date.equals("")) {
			session.removeAttribute(Const.DECALAGETEMPS);
		} else { 
			LocalDateTime dateSaisieDecalage = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
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
			//  email à valider coté client
			//  evite erreur si "" findByEmail retourne plusieurs personnes
			logger.error("Réinitialisation du mot de passe, email incorrect {}", email);
			model.addAttribute(Const.MESSAGE, "Email incorrect");
			model.addAttribute(Const.ACCUEILTITRE, accueilTitre);
			return Const.INDEX;
		}
		// si plusieurs personnes avec le même email erreur findByEmail
		Personne personne = personneRepository.findByEmail(email);
		if ((personne == null) || ("".contentEquals(personne.getLogin()))) {
			// Pas d'email trouvé ou persone sans login
			logger.error("Réinitialisation du mot de passe, email {} incorrect", email);
			model.addAttribute(Const.MESSAGE, "Email incorrect");
			model.addAttribute(Const.ACCUEILTITRE, accueilTitre);
			return Const.INDEX;
		} else {
			String token = UUID.randomUUID().toString();
			personne.setToken(token);			
	        personne.setTokenExpiration(LocalDateTime.now().plusMinutes(tokenValidite));
			personneRepository.save(personne);
			StringBuffer appUrl = request.getRequestURL();
			emailService.sendSimpleMessage(email, "Réinitialisation du mot de passe", 
					"Pour réinitialiser votre mot de passe, cliquez sur le lien ci-dessous:\n" +
			         appUrl + "?token=" + token);
		}
		model.addAttribute(Const.MESSAGE, "Un email a été envoyé à cette adresse");
		model.addAttribute(Const.ACCUEILTITRE, accueilTitre);
		return Const.INDEX;
	}
	
	@GetMapping("/resetPassword")
	public String resetPasswordGet(@RequestParam String token, HttpServletRequest request, Model model) {
		Personne personne = personneRepository.findByToken(token);
		if ((personne == null) || ("".contentEquals(personne.getLogin())) || 
				personne.getTokenexpiration().isBefore(LocalDateTime.now())) {
			logger.error("Réinitialisation du mot de passe, token {} invalide", token);
			model.addAttribute(Const.MESSAGE, "Token invalide");
			model.addAttribute(Const.ACCUEILTITRE, accueilTitre);
			return Const.INDEX;
		}
		model.addAttribute("token", token);
		return "resetPasswordForm";
	}
	
	@PostMapping("/resetPasswordFin")
	public String resetPasswordFin(@RequestParam String token, @RequestParam String password, HttpServletRequest request, Model model) {
		Personne personne = personneRepository.findByToken(token);
		if ((personne == null) || ("".contentEquals(personne.getLogin())) || 
				personne.getTokenexpiration().isBefore(LocalDateTime.now())) {
			logger.error("Réinitialisation du mot de passe, token {} invalide", token);
			model.addAttribute(Const.MESSAGE, "Token invalide");
			model.addAttribute(Const.ACCUEILTITRE, accueilTitre);
			return Const.INDEX;
		}
		personne.setPassword(new BCryptPasswordEncoder().encode(password));
		personne.setToken(null);
		personne.setTokenExpiration(null);
		personneRepository.save(personne);
		return "redirect:login";
	}

}