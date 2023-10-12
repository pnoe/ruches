package ooioo.ruches.essaim;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import ooioo.ruches.Const;
import ooioo.ruches.IdNom;
import ooioo.ruches.Nom;
import ooioo.ruches.Utils;
import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;
import ooioo.ruches.recolte.Recolte;
import ooioo.ruches.recolte.RecolteHausseRepository;
import ooioo.ruches.recolte.RecolteRepository;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheRepository;
import ooioo.ruches.rucher.Rucher;
import ooioo.ruches.rucher.RucherRepository;

@Service
public class EssaimService {

	private final Logger logger = LoggerFactory.getLogger(EssaimService.class);

	@Autowired
	private EssaimRepository essaimRepository;
	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private RecolteRepository recolteRepository;
	@Autowired
	private RecolteHausseRepository recolteHausseRepository;
	@Autowired
	private RucherRepository rucherRepository;

	@Autowired
	private MessageSource messageSource;

	private static final String cree = "{} créé";
	private static final String modif = "{} modifié";

	/**
	 * Change un essaim de ruche. Si la ruche contient un essaim, le disperser.
	 * 
	 * @param essaim,        l'essaim à changer du ruche
	 * @param rucheDest,     la ruche dans laquelle on met l'essaim
	 * @param date,          la date saisie dans le formulaire
	 * @param commentaire,   le commentaire saisi dans le formulaire
	 * @param swapPositions, true si échange de position des ruches demandé
	 */
	@Transactional
	public void associeRucheSauve(Essaim essaim, Ruche rucheDest, String date, String commentaire,
			boolean swapPositions) {
		LocalDateTime dateEve = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
		// Si la rucheDest contient un essaim, le disperser
		Essaim essaimDisperse = rucheDest.getEssaim();
		if (essaimDisperse != null) {
			// On disperse cet essaim : inactif et date, commentaire de dispersion
			essaimDisperse.setActif(false);
			essaimDisperse.setDateDispersion(dateEve);
			essaimDisperse.setCommDisp(commentaire);
			essaimRepository.save(essaimDisperse);
			logger.info(modif, essaimDisperse);
		}
		// La ruche dans laquelle est l'essaim
		Ruche rucheActuelle = rucheRepository.findByEssaimId(essaim.getId());
		if (rucheActuelle != null) {
			if (swapPositions) {
				Float lat = rucheDest.getLatitude();
				Float lon = rucheDest.getLongitude();
				Rucher rucher = rucheDest.getRucher();
				rucheDest.setRucher(rucheActuelle.getRucher());
				rucheDest.setLatitude(rucheActuelle.getLatitude());
				rucheDest.setLongitude(rucheActuelle.getLongitude());
				rucheActuelle.setRucher(rucher);
				rucheActuelle.setLatitude(lat);
				rucheActuelle.setLongitude(lon);
				// création de deux événements rucherajouterucher si les ruchers sont
				// différents.
				// On peut avoir demandé d'échanger les positions des ruches alors qu'elles sont
				// dans les mêmes ruchers !
				if (!rucheActuelle.getRucher().getId().equals(rucheDest.getRucher().getId())) {
					Evenement eveRucheDest = new Evenement(dateEve.minusSeconds(1), TypeEvenement.RUCHEAJOUTRUCHER,
							rucheDest, rucheDest.getEssaim(), rucheDest.getRucher(), null, null, commentaire);
					evenementRepository.save(eveRucheDest);
					logger.info(cree, eveRucheDest);
					Evenement eveRucheActuelle = new Evenement(dateEve.minusSeconds(1), TypeEvenement.RUCHEAJOUTRUCHER,
							rucheActuelle, rucheActuelle.getEssaim(), rucheActuelle.getRucher(), null, null,
							commentaire);
					evenementRepository.save(eveRucheActuelle);
					logger.info(cree, eveRucheActuelle);
				}
			}
			rucheActuelle.setEssaim(null);
			rucheRepository.save(rucheActuelle);
			logger.info(modif, rucheActuelle);
		}
		rucheDest.setEssaim(essaim);
		rucheRepository.save(rucheDest);
		logger.info(modif, rucheDest);
		// on met dans l'événement le rucher rucheDest.getRucher car la position des
		// ruches
		// a pu être échangée
		Evenement evenementAjout = new Evenement(dateEve, TypeEvenement.AJOUTESSAIMRUCHE, rucheDest, essaim,
				rucheDest.getRucher(), null, null, commentaire); // valeur commentaire
		evenementRepository.save(evenementAjout);
		logger.info(cree, evenementAjout);
	}

	/*
	 * Clone d'un essaim.
	 */
	String clone(HttpSession session, Optional<Essaim> essaimOpt, String nomclones, String nomruches) {
		Essaim essaim = essaimOpt.get();
		List<String> noms = new ArrayList<>();
		for (Nom essaimNom : essaimRepository.findAllProjectedBy()) {
			noms.add(essaimNom.nom());
		}
		// Les noms des essaims à créer
		String[] nomarray = nomclones.split(",");
		// Les noms des ruches à créer
		String[] nomruchesarray = nomruches.split(",");
		List<String> nomsCrees = new ArrayList<>();
		LocalDateTime dateEve = Utils.dateTimeDecal(session);
		for (int i = 0; i < nomarray.length; i++) {
			if ("".equals(nomarray[i])) {
				// si le nom d'essaim est vide on l'ignore et on passe au suivant
				continue;
			}
			if (noms.contains(nomarray[i])) {
				logger.error("Clone d'un essaim : {} nom existant", nomarray[i]);
			} else {
				Essaim clone = new Essaim(essaim, nomarray[i]);
				essaimRepository.save(clone);
				nomsCrees.add(nomarray[i]);
				// pour éviter clone "a,a" : 2 fois le même nom dans la liste
				noms.add(nomarray[i]);
				if (i < nomruchesarray.length && !"".contentEquals(nomruchesarray[i])) {
					// S'il n'y a pas de dépassement de la taille du tableau
					// (liste des ruches incorrecte en paramètre) et si le nom de la
					// ruche est différent de ""
					Ruche ruche = rucheRepository.findByNom(nomruchesarray[i]);
					if (ruche.getEssaim() == null) {
						ruche.setEssaim(clone);
						rucheRepository.save(ruche);
						Evenement evenementAjout = new Evenement(dateEve, TypeEvenement.AJOUTESSAIMRUCHE, ruche, clone,
								ruche.getRucher(), null, null, "Clone essaim " + essaim.getNom());
						evenementRepository.save(evenementAjout);
						logger.info(cree, evenementAjout);
					} else {
						logger.error("Clone d'un essaim : {} la ruche {} n'est pas vide", nomarray[i],
								nomruchesarray[i]);
					}
				}
			}
		}
		String nomsJoin = String.join(",", nomsCrees);
		logger.info("Essaims {} créé(s)", nomsJoin);
		return messageSource.getMessage("cloneessaimcrees", new Object[] { nomsJoin }, LocaleContextHolder.getLocale());
	}

	/*
	 * Historique de la mise en ruchers d'un essaim. Les événements affichés dans
	 * l'historique : - les mise en rucher de ruches ou l'essaim apparait - la
	 * dispersion de l'essaim qui termine l'historique n'est plus affichée depuis le
	 * transfert vers l'entité essaim de la dispersion - la ou les mises en ruches
	 * de l'essaim qui peuvent impliquer des déplacements
	 */
	boolean historique(Model model, Long essaimId) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			Essaim essaim = essaimOpt.get();
			model.addAttribute(Const.ESSAIM, essaim);
			// la liste de tous les événements RUCHEAJOUTRUCHER concernant cet essaim
			// triés par ordre de date ascendante
			List<Evenement> evensEssaimAjout = evenementRepository.findByEssaimIdAndTypeOrderByDateAsc(essaimId,
					TypeEvenement.RUCHEAJOUTRUCHER);
			// Si l'essaim est dispersé cela termine le séjour dans le dernier rucher
			// plus d'événement dispersion, indiquer la dispersion autrement.
			// Ajouter les mises en ruche
			List<Evenement> miseEnRuche = evenementRepository.findByEssaimIdAndTypeOrderByDateAsc(essaim.getId(),
					TypeEvenement.AJOUTESSAIMRUCHE);
			evensEssaimAjout.addAll(miseEnRuche);
			// Trier par date
			evensEssaimAjout.sort((e1, e2) -> e1.getDate().compareTo(e2.getDate()));
			model.addAttribute("evensEssaimAjout", evensEssaimAjout);
			List<Long> durees = new ArrayList<>();
			if (!evensEssaimAjout.isEmpty()) {
				int i = 0;
				while (i < evensEssaimAjout.size() - 1) {
					// calcul de la durée de séjour dans le rucher
					durees.add(
							Duration.between(evensEssaimAjout.get(i).getDate(), evensEssaimAjout.get(i + 1).getDate())
									.toDays());
					i++;
				}
				durees.add(Duration.between(evensEssaimAjout.get(i).getDate(), LocalDateTime.now()).toDays());
				model.addAttribute("durees", durees);
			}
			return true;
		}
		return false;
	}

	/*
	 * Calcul des statistiques sur l'âge des reines.
	 */
	void statistiquesage(Model model) {
		Iterable<Essaim> essaims = essaimRepository.findByActif(true);
		int pas = 6;
		int maxAgeMois = 95; // reine ignorée si plus ancienne (96 mois, 8 ans)
		// ages : classes d'âge de largeur "pas" en mois
		// voir
		// https://stackoverflow.com/questions/7139382/java-rounding-up-to-an-int-using-math-ceil/21830188
		int[] ages = new int[(maxAgeMois + pas - 1) / pas];
		int indexMaxAges = 0;
		long ageMaxJours = 0;
		long ageMinJours = 0;
		boolean premier = true;
		long ageTotalJours = 0;
		int ageMoyenJours;
		int nb = 1;
		LocalDate dateNow = LocalDate.now();
		double m = 0;
		double s = 0;
		for (Essaim essaim : essaims) {
			if (essaim.getReineDateNaissance() != null) {
				if (essaim.getReineDateNaissance().isAfter(dateNow)) {
					// Si la reine n'est pas encore née on ne la prends pas en compte !
					continue;
				}
				long ageMois = ChronoUnit.MONTHS.between(essaim.getReineDateNaissance(), dateNow);
				if (ageMois > maxAgeMois) {
					// Si la reine à plus de maxAgeMois on ne la prends pas en compte
					// afficher un message en haut de la page de stat
					logger.info("Essaim {}, âge supérieur à {} mois", essaim.getNom(), maxAgeMois);
					continue;
				}
				if (rucheRepository.findByEssaimId(essaim.getId()) == null) {
					// Si la reine n'est pas dans une ruche on ne la prends pas en compte
					continue;
				}
				int indexAge = (int) ageMois / pas;
				ages[indexAge]++;
				indexMaxAges = Math.max(indexMaxAges, indexAge);
				long ageJours = ChronoUnit.DAYS.between(essaim.getReineDateNaissance(), dateNow);
				ageMaxJours = Math.max(ageMaxJours, ageJours);
				if (premier) {
					ageMinJours = ageJours;
					premier = false;
				} else {
					ageMinJours = Math.min(ageMinJours, ageJours);
				}
				ageTotalJours += ageJours;
				// Variance Welford's algorithm
				double tmpM = m;
				double ageJ = ageJours;
				m += (ageJ - tmpM) / nb;
				s += (ageJ - tmpM) * (ageJ - m);
				nb++;
			}
		}
		List<Integer> agesHisto = new ArrayList<>();
		for (int i = 0; i <= indexMaxAges; i++) {
			agesHisto.add(ages[i]);
		}
		ageMoyenJours = (int) Math.round((double) ageTotalJours / (nb - 1));
		// Variance sur population entière (nb est incrémenté avant la sortie de la
		// boucle)
		long ageVarianceJours = Math.round(Math.sqrt(s / (nb - 1)));
		model.addAttribute("ageMoyenJours", ageMoyenJours);
		model.addAttribute("agesHisto", agesHisto);
		model.addAttribute("ageVarianceJours", ageVarianceJours);
		model.addAttribute("ageMaxJours", ageMaxJours);
		model.addAttribute("ageMinJours", ageMinJours);
		model.addAttribute("pas", pas);
	}

	/**
	 * Statistiques tableau poids de miel par essaim Appel à partir de la liste des
	 * essaims.
	 *
	 * @param rucherId       optionnel pour ne prendre en compte que les hausses de
	 *                       récolte dans ce rucher.
	 * @param masquerInactif pour masquer les essaims inactifs.
	 */
	void statistiques(Model model, Long rucherId, boolean masquerInactif) {
		// pour équivalence appel Get ou Post avec rucherId = 0
		if ((rucherId != null) && rucherId.equals(0L)) {
			rucherId = null;
		}
		Iterable<Recolte> recoltes = recolteRepository.findAllByOrderByDateAsc();
		Iterable<Essaim> essaims = masquerInactif ? essaimRepository.findByActif(true) : essaimRepository.findAll();
		List<Map<String, String>> essaimsPoids = new ArrayList<>();
		DecimalFormat decimalFormat = new DecimalFormat("0.00",
				new DecimalFormatSymbols(LocaleContextHolder.getLocale()));
		Integer pTotal; // poids de miel total produit par l'essaim
		Integer pMax; // poids de miel max lors d'une récolte
		Integer pMin; // poids de miel min lors d'une récolte
		boolean essaimOK;
		for (Essaim essaim : essaims) {
			pTotal = 0;
			pMax = 0;
			pMin = 1000000;
			essaimOK = false;
			for (Recolte recolte : recoltes) {
				Integer poids = (rucherId == null)
						? recolteHausseRepository.findPoidsMielByEssaimByRecolte(essaim.getId(), recolte.getId())
						: recolteHausseRepository.findPoidsMielEssaimRecolteRucher(essaim.getId(), recolte.getId(),
								rucherId);
				if (poids != null) {
					// L'essaim à participé à au moins une récolte même si le poids est nul.
					essaimOK = true;
					pTotal += poids;
					pMax = Math.max(pMax, poids);
					pMin = Math.min(pMin, poids);
				}
			}
			if (pMin == 1000000) {
				pMin = 0;
			}
			// Si l'essaim n'a participé à aucune récolte, on ne l'affiche pas dans le
			// tableau.
			if (essaimOK) {
				Map<String, String> essaimPoids = new HashMap<>();
				essaimPoids.put("nom", essaim.getNom());
				essaimPoids.put("id", essaim.getId().toString());
				essaimPoids.put("dateAcquisition", essaim.getDateAcquisition().toString());
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				essaimPoids.put("dateDispersion",
						(essaim.getActif()) ? "" : essaim.getDateDispersion().format(formatter));
				// calcul moyenne production miel par jour d'existence de l'essaim
				if (rucherId == null) {
					LocalDateTime dateFin = (essaim.getActif()) ? LocalDateTime.now() : essaim.getDateDispersion();
					long duree = ChronoUnit.DAYS.between(essaim.getDateAcquisition().atStartOfDay(), dateFin);
					if (duree <= 0) {
						essaimPoids.put("pMoyen", "");
					} else {
						float pMoyen = pTotal * 0.365242f / duree;
						essaimPoids.put("pMoyen", decimalFormat.format(pMoyen));
					}
					essaimPoids.put("duree", Long.toString(duree));
				}
				essaimPoids.put("pTotal", decimalFormat.format(pTotal / 1000.0));
				essaimPoids.put("pMax", decimalFormat.format(pMax / 1000.0));
				essaimPoids.put("pMin", decimalFormat.format(pMin / 1000.0));
				essaimsPoids.add(essaimPoids);
			}
		}
		model.addAttribute("essaimsPoids", essaimsPoids);
		Collection<IdNom> rucherIdNom = rucherRepository.findAllProjectedIdNomByOrderByNom();
		model.addAttribute("rucherIdNom", rucherIdNom);
		model.addAttribute("rucherId", rucherId);
		model.addAttribute("masquerInactif", masquerInactif);
	}

	/**
	 * Enregistrement de l'essaimage.
	 *
	 * @param essaimId    l'id de l'essaim qui essaime.
	 * @param date        la date saisie dans le formulaire d'essaimage.
	 * @param nom         le nom du nouvel essaim restant dans la ruche saisi dans
	 *                    le formulaire d'essaimage.
	 * @param commentaire le commentaire saisi dans le formulaire d'essaimage.
	 * @param essaimOpt   l'essaim essaimId.
	 */
	Essaim essaimSauve(long essaimId, String date, String nom, String commentaire, Optional<Essaim> essaimOpt) {
		// L'essaim à disperser
		Essaim essaim = essaimOpt.get();
		// La ruche dans laquelle on va mettre le nouvel essaim à créer
		Ruche ruche = rucheRepository.findByEssaimId(essaimId);
		LocalDateTime dateEveAjout = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
		// On crée l'essaim : nom saisi dans le formulaire, date acquisition et
		// naissance reine
		// = date formulaire, souche = essaim dispersé
		Essaim nouvelEssaim = new Essaim(nom, true, // actif
				dateEveAjout.toLocalDate(), // acquisition
				commentaire, // Le champ commentaire du formulaire ? essaim ou événement dispersion ?
				dateEveAjout.toLocalDate(), // reineDateNaissance
				false, // reineMarquee
				essaim, // souche,
				essaim.getAgressivite(), // agressivite
				essaim.getProprete()); // proprete
		essaimRepository.save(nouvelEssaim);
		// On met cet essaim dans la ruche
		ruche.setEssaim(nouvelEssaim);
		Evenement evenementAjout = new Evenement(dateEveAjout, TypeEvenement.AJOUTESSAIMRUCHE, ruche, nouvelEssaim,
				ruche.getRucher(), null, null, commentaire);
		evenementRepository.save(evenementAjout);
		logger.info(cree, evenementAjout);
		rucheRepository.save(ruche);
		// On inactive l'essaim dispersé
		essaim.setActif(false);
		// eve dispersion supprimé, on met les infos dans l'entité essaim
		LocalDateTime dateEve = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
		essaim.setDateDispersion(dateEve);
		essaim.setCommDisp(commentaire);
		essaimRepository.save(essaim);
		// dispersion ajouté log essaim modifié
		logger.info(modif, essaim);
		return nouvelEssaim;
	}

	/*
	 * Ajoute au model Spring le dernier événement de type typeEvenement avec le nom
	 * d'attribut : Eve + typeEvenement.
	 */
	public void modelAddEve(Model model, Essaim essaim, TypeEvenement typeEvenement) {
		Evenement evenement = evenementRepository.findFirstByEssaimAndTypeOrderByDateDesc(essaim, typeEvenement);
		model.addAttribute("Eve" + typeEvenement, evenement);
	}

	/**
	 * Renvoie la liste des EssaimTree fils de l'essaim passé en paramètre
	 */
	List<EssaimTree> listeEssaimsFils(Essaim essaim) {
		List<EssaimTree> essaimTree = new ArrayList<>();
		calculeEssaimsFils(essaimTree, essaim);
		return essaimTree;
	}

	/**
	 * Calcul de la liste EssaimTree par appels récursifs
	 */
	private Integer calculeEssaimsFils(List<EssaimTree> resultat, Essaim essaim) {
		Integer poidsMielDescendance = 0;
		for (Essaim essaimSouche : essaimRepository.findBySouche(essaim)) {
			poidsMielDescendance += calculeEssaimsFils(resultat, essaimSouche);
		}
		Integer poidsMielEssaim = 0;
		for (Recolte recolte : recolteRepository.findAllByOrderByDateAsc()) {
			Integer poids = recolteHausseRepository.findPoidsMielByEssaimByRecolte(essaim.getId(), recolte.getId());
			if (poids != null) {
				poidsMielEssaim += poids;
			}
		}
		poidsMielDescendance += poidsMielEssaim;
		Ruche ruche = rucheRepository.findByEssaimId(essaim.getId());
		resultat.add(new EssaimTree(essaim.getNom(), essaim.getSouche() == null ? "null" : essaim.getSouche().getNom(),
				essaim.getId(), essaim.getActif(),
				// le parser json de essaim.EssaimController.graphedescendance
				// plante sur la date
				essaim.getDateAcquisition(), essaim.getReineCouleurMarquage().toString(),
				ruche == null ? "null" : ruche.getNom(), ruche == null ? "null" : ruche.getRucher().getNom(),
				poidsMielEssaim, poidsMielDescendance));
		return poidsMielDescendance;
	}

}