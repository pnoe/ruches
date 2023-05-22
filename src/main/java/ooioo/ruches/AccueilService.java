package ooioo.ruches;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.essaim.EssaimRepository;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.hausse.HausseRepository;
import ooioo.ruches.recolte.Recolte;
import ooioo.ruches.recolte.RecolteRepository;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheRepository;
import ooioo.ruches.rucher.DistRucher;
import ooioo.ruches.rucher.DistRucherRepository;
import ooioo.ruches.rucher.Rucher;
import ooioo.ruches.rucher.RucherRepository;

@Service
public class AccueilService {

	private final Logger logger = LoggerFactory.getLogger(AccueilService.class);

	@Autowired
	private DistRucherRepository drRepo;
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

	@Value("${dist.ruches.loins}")
	private double distRuchesTropLoins;
	@Value("${dist.ruchers.loins}")
	private double distRuchersTropLoins;
	@Value("${retard.ruches.evenement}")
	private int retardRucheEvenement;
	@Value("${rucher.butinage.rayons}")
	private int[] rayonsButinage;
	@Value("${ign.url.itineraire}")
	private String urlIgnItineraire;

	/**
	 * Calcul des distances entre les ruchers par appel de l'api ign de calcul
	 * d'itinéraire. Ne calcule qu'un sens et non une valeur pour l'aller et une
	 * autre pour le retour. Le sens calculé est : départ du rucher de plus petit
	 * Id. Ne stocke pas la distance d'un rucher à lui même. En cas d'erreur
	 * renvoyée par l'api ign met 0 comme distance et temps de parcours. Pour
	 * éventuel intégration dans un calcul de distances parcourues pour les
	 * transhumances ou affichage brut du tableau Si appel /dist?reset=true toutes
	 * les distances sont effacées puis recalculées, si /dist seules les distances
	 * non enregistées sont recalculées
	 */
	public void dist(boolean reset) {
		// https://geoservices.ign.fr/documentation/services/api-et-services-ogc/itineraires/documentation-du-service-du-calcul
		// https://wxs.ign.fr/geoportail/itineraire/rest/1.0.0/getCapabilities
		// avec resource = OSRM erreur
		// https://wxs.ign.fr/calcul/geoportail/itineraire/rest/1.0.0/route?resource=bdtopo-pgr&getSteps=false&start=
		Iterable<Rucher> ruchers = rucherRepository.findAll();
		RestTemplate restTemplate = new RestTemplate();
		if (reset) {
			// Effacer la table dist_rucher sinon les distances déjà calculées apparaîtront
			// deux fois
			drRepo.deleteAll();
		}
		for (Rucher r1 : ruchers) {
			for (Rucher r2 : ruchers) {
				if (r1.getId().equals(r2.getId())) {
					// même ruchers
					continue;
				}
				if (r1.getId().intValue() < r2.getId().intValue()) {
					if (!reset && (drRepo.findByRucherStartAndRucherEnd(r1, r2) != null)) {
						// pas de reset demandé et la distance a déjà été calculée
						continue;
					}
					DistRucher dr = new DistRucher(r1, r2, 0, 0);
					StringBuilder uri = new StringBuilder(urlIgnItineraire);
					uri.append(r1.getLongitude()).append(",").append(r1.getLatitude()).append("&end=")
							.append(r2.getLongitude()).append(",").append(r2.getLatitude());
					try {
						Itineraire result = restTemplate.getForObject(uri.toString(), Itineraire.class);
						dr.setDist(Math.round(result.distance()));
						dr.setTemps(Math.round(result.duration()));
					} catch (HttpClientErrorException | HttpServerErrorException e) {
						// erreur 4xx ou 5xx
						logger.error(e.getMessage());
					}
					drRepo.save(dr);
					logger.info("{} => {}, distance {}m et temps {}min, enregistrés", r1.getNom(), r2.getNom(),
							dr.getDist(), dr.getTemps());
				}
			}
		}
	}

	/**
	 * Infos
	 */
	public void infos(Model model) {
		// Nombre de ruchers actifs.
		long nbRuchers = rucherRepository.countByActifTrue();
		model.addAttribute("nbRuchers", nbRuchers);
		// Nombre de ruches actives, nb production, nb élevage
		long nbRuches = rucheRepository.countByActiveTrue();
		long nbRuchesProd = rucheRepository.countByActiveTrueAndProductionTrue();
		model.addAttribute("nbRuches", nbRuches);
		model.addAttribute("nbRuchesProd", nbRuchesProd);
		model.addAttribute("nbRuchesElev", nbRuches - nbRuchesProd);
		// Hausses actives.
		long nbHausses = hausseRepository.countByActiveTrue();
		model.addAttribute("nbHausses", nbHausses);
		// Ruches actives au dépôt, nb production, nb élevage
		long nbRuchesAuDepot = rucheRepository.countByActiveTrueAndRucherDepotTrue();
		model.addAttribute("nbRuchesAuDepot", nbRuchesAuDepot);
		long nbRuchesAuDepotProd = rucheRepository.countByActiveTrueAndRucherDepotTrueAndProductionTrue();
		model.addAttribute("nbRuchesAuDepotProd", nbRuchesAuDepotProd);
		model.addAttribute("nbRuchesAuDepotElev", nbRuchesAuDepot - nbRuchesAuDepotProd);
		// Hausses actives non posées.
		long nbHaussesHorsRuche = hausseRepository.countByActiveTrueAndRucheNull();
		model.addAttribute("nbHaussesHorsRuche", nbHaussesHorsRuche);
		// Nombre de ruches actives avec essaim
		long nbRuchesAvecEssaim = rucheRepository.countByEssaimNotNullAndActiveTrue();
		model.addAttribute("nbRuchesAvecEssaim", nbRuchesAvecEssaim);
		long nbRuchesAvecEssaimProd = rucheRepository.countByEssaimNotNullAndActiveTrueAndProductionTrue();
		model.addAttribute("nbRuchesAvecEssaimProd", nbRuchesAvecEssaimProd);
		model.addAttribute("nbRuchesAvecEssaimElev", nbRuchesAvecEssaim - nbRuchesAvecEssaimProd);
		// Nombre de hausses actives posées sur des ruches avec essaim.
		long nbHaussesSurRuchesAvecEssaim = hausseRepository
				.countByActiveTrueAndRucheNotNullAndRucheActiveTrueAndRucheEssaimNotNull();
		model.addAttribute("nbHaussesSurRuchesAvecEssaim", nbHaussesSurRuchesAvecEssaim);

		// Distances de butinage : les rayons des cercles de butinages affichables sur
		// les cartes des ruchers.
		model.addAttribute("rayonsButinage", rayonsButinage);
		// Valeurs par année
		
		// Liste des ruches actives au dépôt avec essaim.
		Iterable<Ruche> ruchesDepotEssaim = rucheRepository.findByActiveTrueAndEssaimNotNullAndRucherDepotTrue();
		model.addAttribute("ruchesDepotEssaim", ruchesDepotEssaim);
		// Liste des ruches actives au dépôt avec des hausses.
		Iterable<Ruche> ruchesDepotHausses = rucheRepository.findByHaussesAndDepot();
		model.addAttribute("ruchesDepotHausses", ruchesDepotHausses);

		// Liste des ruches actives sans essaim qui ne sont pas au dépôt.
		Iterable<Ruche> ruchesPasDepotSansEssaim = rucheRepository.findByActiveTrueAndEssaimNullAndRucherDepotFalse();
		model.addAttribute("ruchesPasDepotSansEssaim", ruchesPasDepotSansEssaim);

		// Essaims actifs hors ruche.
		Iterable<Essaim> essaimsActifSansRuche = essaimRepository.findEssaimByActifSansRuche();
		model.addAttribute("essaimsActifSansRuche", essaimsActifSansRuche);

		// Liste des ruchers actifs (coordonées de leur entrée) à plus de 20m du barycentre de
		// leurs ruches.
		model.addAttribute("distRuchersTropLoins", distRuchersTropLoins);
		Iterable<Rucher> ruchers = rucherRepository.findByActifOrderByNom(true);
		List<Rucher> ruchersMalCales = new ArrayList<>();
		List<Double> distances = new ArrayList<>();
		for (Rucher rucher : ruchers) {
			// Calcul de la latitude et longitude du barycentre des ruches.
			double diametreTerre = 2 * Utils.rTerreLat(rucher.getLatitude());
			Float longitude;
			Float latitude = 0f;
			Iterable<Ruche> ruches = rucheRepository.findByRucherIdAndActiveTrueOrderByNom(rucher.getId());
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
			if (nbRuches2 == 0) {
				continue;
			}
			longitude = (float) (Math.atan2(ylon, xlon) * 180d / Math.PI);
			latitude /= nbRuches2;
			double dist = Utils.distance(diametreTerre, latitude, rucher.getLatitude(), longitude,
					rucher.getLongitude());
			if (dist > distRuchersTropLoins) {
				ruchersMalCales.add(rucher);
				distances.add(dist);
			}
		}
		model.addAttribute("ruchersMalCales", ruchersMalCales);
		model.addAttribute("distances", distances);

		
		// Liste de ruches actives trop éloignées de leurs ruchers.
		List<Ruche> ruchesTropLoins = new ArrayList<>();
		for (Rucher rucher : ruchers) {
			Float longRucher = rucher.getLongitude();
			Float latRucher = rucher.getLatitude();
			Iterable<Ruche> ruches = rucheRepository.findByRucherIdAndActiveTrueOrderByNom(rucher.getId());
			double diametreTerre = ((rucher.getAltitude() == null) ? 0 : rucher.getAltitude())
					+ 2 * Utils.rTerreLat(rucher.getLatitude());
			for (Ruche ruche : ruches) {
				if (Utils.distance(diametreTerre, latRucher, ruche.getLatitude(), longRucher,
						ruche.getLongitude()) > distRuchesTropLoins) {
					ruchesTropLoins.add(ruche);
				}
			}
		}
		model.addAttribute("ruchesTropLoins", ruchesTropLoins);
		model.addAttribute("distRuchesTropLoins", distRuchesTropLoins);

		
		
		// Liste des ruches actives et pas au dépôt sans événements depuis 4 semaines.
		model.addAttribute("retardRucheEvenement", retardRucheEvenement);
		LocalDateTime date4sem = LocalDateTime.now().minus(retardRucheEvenement, ChronoUnit.WEEKS);
		Iterable<Ruche> ruchesPasDEvenement = rucheRepository.findPasDEvenementAvant(date4sem);
		model.addAttribute("ruchesPasDEvenement", ruchesPasDEvenement);

		// Liste des essaims actifs dont la date naissance de la reine est supérieure à date
		// acquisition.
		Iterable<Essaim> essaimDateNaissSupAcquis = essaimRepository.findEssaimDateNaissSupAcquis();
		model.addAttribute("essaimDateNaissSupAcquis", essaimDateNaissSupAcquis);


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
		for (int i = dateDebut; i <= dateFin; i++) {
			Double date = Double.valueOf(i);
			annees.add(date);
			Optional<Double> poidsOpt = recolteRepository.findPoidsMielByYear(date);
			Double poids = poidsOpt.isPresent() ? poidsOpt.get() : 0.0;
			pdsMiel.add(poids == null ? 0 : poids);
			pdsMielTotal += (poids == null ? 0 : poids);
			Integer nbCree = essaimRepository.countEssaimsCreesDate(date);
			nbCreationEssaimsTotal += nbCree;
			nbCreationEssaims.add(nbCree);
			Integer nbDisperse = evenementRepository.countDispersionEssaimParAnnee(date);
			nbDispersionEssaimsTotal += nbDisperse;
			nbDispersionEssaims.add(nbDisperse);
			nbEssaims.add(nbCree - nbDisperse);
			Double sucreAnneeEssaims = evenementRepository.sucreEssaimParAnnee(date);
			if (sucreAnneeEssaims == null) {
				sucreAnneeEssaims = 0.0;
			}
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
		for (int j = 1; j < nbEssaims.size(); j++) {
			nbEssaims.set(j, nbEssaims.get(j) + nbEssaims.get(j - 1));
		}
		model.addAttribute("nbTraitementsEssaims", nbTraitementsEssaims);
		model.addAttribute("sucreEssaims", sucreEssaims);
		model.addAttribute("nbEssaims", nbEssaims);
		model.addAttribute("nbDispersionEssaims", nbDispersionEssaims);
		model.addAttribute("nbCreationEssaims", nbCreationEssaims);
		model.addAttribute("pdsMiel", pdsMiel);
		model.addAttribute("annees", annees);




	}

}
