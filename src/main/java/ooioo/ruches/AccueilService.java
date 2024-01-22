package ooioo.ruches;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.essaim.EssaimRepository;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.hausse.HausseRepository;
import ooioo.ruches.recolte.Recolte;
import ooioo.ruches.recolte.RecolteRepository;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheRepository;
import ooioo.ruches.rucher.Rucher;
import ooioo.ruches.rucher.RucherRepository;

@Service
public class AccueilService {

	private final EssaimRepository essaimRepository;
	private final EvenementRepository evenementRepository;
	private final RucherRepository rucherRepository;
	private final RucheRepository rucheRepository;
	private final HausseRepository hausseRepository;
	private final RecolteRepository recolteRepository;

	@Value("${dist.ruches.loins}")
	private double distRuchesTropLoins;
	@Value("${dist.ruchers.loins}")
	private double distRuchersTropLoins;
	@Value("${retard.ruches.evenement}")
	private int retardRucheEvenement;
	@Value("${rucher.butinage.rayons}")
	private int[] rayonsButinage;

	public AccueilService(EssaimRepository essaimRepository, EvenementRepository evenementRepository,
			RucherRepository rucherRepository, RucheRepository rucheRepository, HausseRepository hausseRepository,
			RecolteRepository recolteRepository) {
		this.essaimRepository = essaimRepository;
		this.evenementRepository = evenementRepository;
		this.rucherRepository = rucherRepository;
		this.rucheRepository = rucheRepository;
		this.hausseRepository = hausseRepository;
		this.recolteRepository = recolteRepository;
	}

	/**
	 * Infos
	 */
	void infos(Model model) {
		// Nombre de ruchers actifs.
		model.addAttribute("nbRuchers", rucherRepository.countByActifTrue());
		// Nombre de ruches actives, nb production, nb élevage
		long nbRuches = rucheRepository.countByActiveTrue();
		long nbRuchesProd = rucheRepository.countByActiveTrueAndProductionTrue();
		model.addAttribute("nbRuches", nbRuches);
		model.addAttribute("nbRuchesProd", nbRuchesProd);
		model.addAttribute("nbRuchesElev", nbRuches - nbRuchesProd);
		// Hausses actives.
		model.addAttribute("nbHausses", hausseRepository.countByActiveTrue());
		// Ruches actives au dépôt, nb production, nb élevage
		long nbRuchesAuDepot = rucheRepository.countByActiveTrueAndRucherDepotTrue();
		model.addAttribute("nbRuchesAuDepot", nbRuchesAuDepot);
		long nbRuchesAuDepotProd = rucheRepository.countByActiveTrueAndRucherDepotTrueAndProductionTrue();
		model.addAttribute("nbRuchesAuDepotProd", nbRuchesAuDepotProd);
		model.addAttribute("nbRuchesAuDepotElev", nbRuchesAuDepot - nbRuchesAuDepotProd);
		// Hausses actives non posées.
		model.addAttribute("nbHaussesHorsRuche", hausseRepository.countByActiveTrueAndRucheNull());
		// Nombre de ruches actives avec essaim
		long nbRuchesAvecEssaim = rucheRepository.countByEssaimNotNullAndActiveTrue();
		model.addAttribute("nbRuchesAvecEssaim", nbRuchesAvecEssaim);
		long nbRuchesAvecEssaimProd = rucheRepository.countByEssaimNotNullAndActiveTrueAndProductionTrue();
		model.addAttribute("nbRuchesAvecEssaimProd", nbRuchesAvecEssaimProd);
		model.addAttribute("nbRuchesAvecEssaimElev", nbRuchesAvecEssaim - nbRuchesAvecEssaimProd);
		// Nombre de hausses actives posées sur des ruches avec essaim.
		model.addAttribute("nbHaussesSurRuchesAvecEssaim",
				hausseRepository.countByActiveTrueAndRucheNotNullAndRucheActiveTrueAndRucheEssaimNotNull());
		// Distances de butinage : les rayons des cercles de butinages affichables sur
		// les cartes des ruchers.
		model.addAttribute("rayonsButinage", rayonsButinage);
		valeurParAnnee(model);
		// Liste des ruches actives au dépôt avec essaim.
		model.addAttribute("ruchesDepotEssaim", rucheRepository.findByActiveTrueAndEssaimNotNullAndRucherDepotTrue());
		// Liste des ruches actives au dépôt avec des hausses.
		model.addAttribute("ruchesDepotHausses", rucheRepository.findByHaussesAndDepot());
		// Liste des ruches actives sans essaim qui ne sont pas au dépôt.
		model.addAttribute("ruchesPasDepotSansEssaim",
				rucheRepository.findByActiveTrueAndEssaimNullAndRucherDepotFalse());
		// Essaims actifs hors ruche.
		model.addAttribute("essaimsActifSansRuche", essaimRepository.findEssaimByActifSansRuche());
		List<Rucher> ruchers = rucherRepository.findByActifOrderByNom(true);
		ruchersMalCale(model, ruchers);
		rucherTropLoin(model, ruchers);
		// Liste des ruches actives et pas au dépôt sans événements depuis 4 semaines.
		model.addAttribute("retardRucheEvenement", retardRucheEvenement);
		model.addAttribute("ruchesPasDEvenement", rucheRepository
				.findPasDEvenementAvant(LocalDateTime.now().minus(retardRucheEvenement, ChronoUnit.WEEKS)));
		// Liste des essaims actifs dont la date naissance de la reine est supérieure à
		// la date d'acquisition.
		model.addAttribute("essaimDateNaissSupAcquis", essaimRepository.findEssaimDateNaissSupAcquis());
		model.addAttribute("rucheDepotFalseInact", rucheRepository.findByRucherDepotFalseAndActiveFalse());
	}

	private void valeurParAnnee(Model model) {
		// Valeurs par année : poids de miel mis en pots, nb d'essaims, nb d'essaims
		// créés, nb essaims dispersés, sucre, nb traitements.
		Essaim premierEssaim = essaimRepository.findFirstByOrderByDateAcquisition();
		// Calcul de l'année initiale du tableau à afficher.
		int dateDebut;
		if (premierEssaim == null) {
			dateDebut = 1;
		} else {
			dateDebut = premierEssaim.getDateAcquisition().getYear();
		}
		// Calcul de l'année finale du tableau.
		Essaim dernierEssaim = essaimRepository.findFirstByOrderByDateAcquisitionDesc();
		Recolte derniereRecolte = recolteRepository.findFirstByOrderByDateDesc();
		int dateFin;
		if ((dernierEssaim == null) || (derniereRecolte == null)) {
			dateFin = 0;
		} else {
			dateFin = java.lang.Math.max(dernierEssaim.getDateAcquisition().getYear(),
					derniereRecolte.getDate().getYear());
		}
		// nban, nombre d'années, pour dimensionner les ArrayList, avec une colonne de
		// plus pour le total.
		int nban = dateFin - dateDebut + 2;
		if (nban < 1) {
			nban = 1;
		} // nécessaire dans certains cas, plantage si < 0
		List<Integer> annees = new ArrayList<>(nban);
		List<Double> pdsMiel = new ArrayList<>(nban);
		List<Integer> nbRecoltes = new ArrayList<>(nban);
		List<Integer> nbEvesRucher = new ArrayList<>(nban);
		List<Integer> nbEves = new ArrayList<>(nban);
		List<Integer> nbEssaims = new ArrayList<>(nban);
		List<Integer> nbCreationEssaims = new ArrayList<>(nban);
		List<Integer> nbDispersionEssaims = new ArrayList<>(nban);
		List<Double> sucreEssaims = new ArrayList<>(nban);
		List<Integer> nbTraitementsEssaims = new ArrayList<>(nban);
		Double pdsMielTotal = 0d;
		Integer nbRecTotal = 0;
		Integer nbEveRucherTotal = 0;
		Integer nbEveTotal = 0;
		Integer nbCreationEssaimsTotal = 0;
		Integer nbDispersionEssaimsTotal = 0;
		Double sucreEssaimsTotal = 0.0;
		Integer nbTraitementsEssaimsTotal = 0;
		for (int date = dateDebut; date <= dateFin; date++) {
			annees.add(date);
			Optional<Double> poidsOpt = recolteRepository.findPoidsMielByYear(date);
			Double poids = poidsOpt.isPresent() ? poidsOpt.get() : 0.0;
			pdsMiel.add(poids);
			pdsMielTotal += poids;
			Optional<Integer> nbRecOpt = recolteRepository.findRecoltesByYear(date);
			Integer nbRec = nbRecOpt.isPresent() ? nbRecOpt.get() : 0;
			nbRecoltes.add(nbRec);
			nbRecTotal += nbRec;
			
			Optional<Integer> nbEveRucherOpt = evenementRepository.countEveAnneeRucher(date,
					ooioo.ruches.evenement.TypeEvenement.COMMENTAIRERUCHE.ordinal(),
					ooioo.ruches.evenement.TypeEvenement.COMMENTAIREHAUSSE.ordinal(),
					ooioo.ruches.evenement.TypeEvenement.COMMENTAIREHAUSSE.ordinal(),
					ooioo.ruches.evenement.TypeEvenement.COMMENTAIRERUCHER.ordinal());
			Integer nbEvRucher = nbEveRucherOpt.isPresent() ? nbEveRucherOpt.get() : 0;
			nbEvesRucher.add(nbEvRucher);
			nbEveRucherTotal += nbEvRucher;
			
			
			Optional<Integer> nbEveOpt = evenementRepository.countEveAnnee(date,
					ooioo.ruches.evenement.TypeEvenement.COMMENTAIRERUCHE.ordinal(),
					ooioo.ruches.evenement.TypeEvenement.COMMENTAIREHAUSSE.ordinal(),
					ooioo.ruches.evenement.TypeEvenement.COMMENTAIREHAUSSE.ordinal(),
					ooioo.ruches.evenement.TypeEvenement.COMMENTAIRERUCHER.ordinal());
			Integer nbEv = nbEveOpt.isPresent() ? nbEveOpt.get() : 0;
			nbEves.add(nbEv);
			nbEveTotal += nbEv;
			
			
			
			// Nombre d'essaims créés dans l'année (année acquisition = date)
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
		nbRecoltes.add(nbRecTotal);
		nbEvesRucher.add(nbEveRucherTotal);
		nbEves.add(nbEveTotal);
		nbCreationEssaims.add(nbCreationEssaimsTotal);
		nbTraitementsEssaims.add(nbTraitementsEssaimsTotal);
		sucreEssaims.add(sucreEssaimsTotal);
		nbDispersionEssaims.add(nbDispersionEssaimsTotal);
		for (int j = 1; j < nbEssaims.size(); j++) {
			nbEssaims.set(j, nbEssaims.get(j) + nbEssaims.get(j - 1));
		}
		model.addAttribute("annees", annees);
		model.addAttribute("pdsMiel", pdsMiel);
		model.addAttribute("nbRecoltes", nbRecoltes);
		model.addAttribute("nbEvesRucher", nbEvesRucher);
		model.addAttribute("nbEves", nbEves);
		model.addAttribute("nbEssaims", nbEssaims);
		model.addAttribute("nbCreationEssaims", nbCreationEssaims);
		model.addAttribute("nbDispersionEssaims", nbDispersionEssaims);
		model.addAttribute("sucreEssaims", sucreEssaims);
		model.addAttribute("nbTraitementsEssaims", nbTraitementsEssaims);
	}

	private void ruchersMalCale(Model model, List<Rucher> ruchers) {
		// Liste des ruchers actifs (coordonées de leur entrée) à plus de 20m du
		// barycentre de leurs ruches.
		model.addAttribute("distRuchersTropLoins", distRuchersTropLoins);
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
	}

	private void rucherTropLoin(Model model, List<Rucher> ruchers) {
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
	}

}
