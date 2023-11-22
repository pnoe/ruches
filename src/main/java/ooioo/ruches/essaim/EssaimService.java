package ooioo.ruches.essaim;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
import ooioo.ruches.Const;
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

	/**
	 * Change un essaim de ruche. Si la ruche contient un essaim, le disperser.
	 * 
	 * @param essaim,        l'essaim à changer du ruche
	 * @param rucheDest,     la ruche dans laquelle on met l'essaim
	 * @param date,          la date saisie dans le formulaire
	 * @param commentaire,   le commentaire saisi dans le formulaire
	 * @param swapPositions, true si échange de position des ruches demandé
	 */
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
			logger.info(Const.MODIFIE, essaimDisperse);
		}
		// La ruche dans laquelle est l'essaim.
		Ruche rucheActuelle = rucheRepository.findByEssaimId(essaim.getId());
		if (rucheActuelle != null) {
			if (swapPositions) {
				// Echange des positions rucheDest et rucheActuelle.
				Float lat = rucheDest.getLatitude();
				Float lon = rucheDest.getLongitude();
				Rucher rucher = rucheDest.getRucher();
				rucheDest.setRucher(rucheActuelle.getRucher());
				rucheDest.setLatitude(rucheActuelle.getLatitude());
				rucheDest.setLongitude(rucheActuelle.getLongitude());
				rucheActuelle.setRucher(rucher);
				rucheActuelle.setLatitude(lat);
				rucheActuelle.setLongitude(lon);
				// Création de deux événements rucherajouterucher si les ruchers sont
				// différents.
				// On peut avoir demandé d'échanger les positions des ruches alors qu'elles sont
				// dans les mêmes ruchers !
				if (!rucheActuelle.getRucher().getId().equals(rucheDest.getRucher().getId())) {
					Evenement eveRucheDest = new Evenement(dateEve.minusSeconds(1), TypeEvenement.RUCHEAJOUTRUCHER,
							rucheDest, rucheDest.getEssaim(), rucheDest.getRucher(), null, null, commentaire);
					evenementRepository.save(eveRucheDest);
					logger.info(Const.CREE, eveRucheDest);
					Evenement eveRucheActuelle = new Evenement(dateEve.minusSeconds(1), TypeEvenement.RUCHEAJOUTRUCHER,
							rucheActuelle, rucheActuelle.getEssaim(), rucheActuelle.getRucher(), null, null,
							commentaire);
					evenementRepository.save(eveRucheActuelle);
					logger.info(Const.CREE, eveRucheActuelle);
				}
			}
			rucheActuelle.setEssaim(null);
			rucheRepository.save(rucheActuelle);
			logger.info(Const.MODIFIE, rucheActuelle);
		}
		rucheDest.setEssaim(essaim);
		rucheRepository.save(rucheDest);
		logger.info(Const.MODIFIE, rucheDest);
		// On met dans l'événement le rucher rucheDest.getRucher car la position des
		// ruches a pu être échangée.
		Evenement evenementAjout = new Evenement(dateEve, TypeEvenement.AJOUTESSAIMRUCHE, rucheDest, essaim,
				rucheDest.getRucher(), null, null, commentaire); // valeur commentaire
		evenementRepository.save(evenementAjout);
		logger.info(Const.CREE, evenementAjout);
	}

	/**
	 * Clone d'un essaim.
	 * 
	 * @param essaimOpt l'essaim à cloner.
	 * @param nomclones les noms des essaims à créer séparés par des ",".
	 * @param nomruches les noms des ruches dans lesquelles mettre les essaims
	 *                  séparés par des ",".
	 */
	String clone(HttpSession session, Optional<Essaim> essaimOpt, String nomclones, String nomruches) {
		Essaim essaim = essaimOpt.get();
		// nomsRecords liste des noms d'essaims pour contrôle d'unicité.
		List<Nom> nomsRecords = essaimRepository.findAllProjectedBy();
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
			if (nomsRecords.contains(new Nom(nomarray[i]))) {
				logger.error("Clone d'un essaim : {} nom existant", nomarray[i]);
			} else {
				Essaim clone = new Essaim(essaim, nomarray[i]);
				essaimRepository.save(clone);
				nomsCrees.add(nomarray[i]);
				// pour éviter clone "a,a" : 2 fois le même nom dans la liste
				nomsRecords.add(new Nom(nomarray[i]));
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
						logger.info(Const.CREE, evenementAjout);
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
	 * Statistiques tableau poids de miel par essaim. Appel à partir de la liste des
	 * essaims.
	 *
	 * @param rucherId       optionnel pour ne prendre en compte que les hausses de
	 *                       récolte dans ce rucher.
	 * @param masquerInactif true pour masquer les essaims inactifs.
	 */
	void statistiques(Model model, Long rucherId, boolean masquerInactif) {
		// pour équivalence appel Get ou Post avec rucherId = 0
		if ((rucherId != null) && rucherId.equals(0L)) {
			rucherId = null;
		}
		List<Recolte> recoltes = recolteRepository.findAll();
		List<Essaim> essaims = masquerInactif ? essaimRepository.findByActif(true) : essaimRepository.findAll();
		// Liste essaimsPoids des essaims ayant participé à des récoltes :
		// nom, id, dateAcquisition, duree, poids pMoyen, pTotal, pMax, pMin et note.
		List<Map<String, String>> essaimsPoids = new ArrayList<>();
		DecimalFormat decimalFormat = new DecimalFormat("0.00",
				new DecimalFormatSymbols(LocaleContextHolder.getLocale()));
		// Une note par essaim égale à la moyenne des notes obtenue dans chaque récolte.
		
		// TODO : commentaire à modifier
		// La note obtenue dans une récolte est la proportion de miel produite par
		// l'essaim relativement au poids total de miel produit dans la même récolte,
		// dans le même rucher.
		
		for (Essaim essaim : essaims) {
			// Long essaimId = essaim.getId();
			Integer pTotal = 0; // poids de miel total produit par l'essaim
			Integer pMax = 0; // poids de miel max lors d'une récolte
			Integer pMin = Integer.MAX_VALUE; // poids de miel min lors d'une récolte
			boolean essaimOK = false;
			Float noteEssaim = 0f;
			int nbRec = 0;
			for (Recolte recolte : recoltes) {
				// Trouver pour cette récolte le rucher correspondant à l'essaim.
				// Une récolte peut comporter plusieurs ruchers.
				// Les hausses de récolte d'un essaim proviennent toutes du même rucher.
				// rrId l'id du rucher de la première hausse de la récolte pour cet essaim.
				// int pTRec = 0;
				// int nbERec = 0;
				Float avgRec = 0f;
				Float stdRec = 0f;
				Long rrId = recolteHausseRepository.findRucherIdRecolteEssaim(recolte, essaim);
				if (rrId != null) {
					// Si l'essaim a bien une hausse de récolte pour cette récolte et dont le
					// rucher rrId est bien renseigné.
					// Les poids de miel récolté sont limités aux hausses de récolte qui sont dans
					// le rucher rrId.

					// pTRec = recolteHausseRepository.findPTRecolte(recolte.getId(), rrId);
					// nbERec nombre d'essaims ayant participé à cette
					// récolte limitée au rucher rrId, pour pouvoir faire la moyenne.
					// nbERec = recolteHausseRepository.findNbERecolte(recolte.getId(), rrId);

					// Calcul de la moyenne et de l'écart type des poids récoltés par essaim pour la
					// récolte et pour le rucher rrId.
					List<Float[]> avgStd = recolteHausseRepository.findAvgStdRecolte(recolte.getId(), rrId);
					avgRec = (Float) avgStd.get(0)[0];
					stdRec = (Float) avgStd.get(0)[1];
				}
				Integer poids = (rucherId == null)
						? recolteHausseRepository.findPoidsMielByEssaimByRecolte(essaim.getId(), recolte.getId())
						: recolteHausseRepository.findPoidsMielEssaimRecolteRucher(essaim.getId(), recolte.getId(),
								rucherId);
				if (poids != null) {
					// essaimOK = true, l'essaim a participé à au moins une récolte même si le poids
					// est égal à 0.
					essaimOK = true;
					pTotal += poids;
					pMax = Math.max(pMax, poids);
					pMin = Math.min(pMin, poids);
					// La note de l'essaim pour cette récolte.

					// Premier calcul, la part du poids produit par l'essaim divisé par le poids
					// total pour cette récolte et pour le même rucher que l'essaim.
					// float note = 1000 * poids / pTRec;

					// Ecart simple standardisé : écart par rapport à la moyenne divisé par l'écart
					// type. note de l'essaim pour la récolte.
					Float note = (poids - avgRec) / stdRec;
					// On fait la somme des notes de l'essaim et on divisera par le nombe de notes.
					noteEssaim += note;
					nbRec++;
				}
			}
			if (pMin == Integer.MAX_VALUE) {
				pMin = 0;
			}
			// Si l'essaim n'a participé à aucune récolte, on ne l'affiche pas dans le
			// tableau.
			if (essaimOK) {
				Map<String, String> essaimPoids = new HashMap<>();
				essaimPoids.put("note", Integer.toString(Math.round(1000 * noteEssaim / nbRec)));
				essaimPoids.put("nbrec", Integer.toString(nbRec));
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
		model.addAttribute("rucherIdNom", rucherRepository.findAllProjectedIdNomByOrderByNom());
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
		logger.info(Const.CREE, evenementAjout);
		rucheRepository.save(ruche);
		// On inactive l'essaim dispersé
		essaim.setActif(false);
		// eve dispersion supprimé, on met les infos dans l'entité essaim
		LocalDateTime dateEve = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
		essaim.setDateDispersion(dateEve);
		essaim.setCommDisp(commentaire);
		essaimRepository.save(essaim);
		// dispersion ajouté log essaim modifié
		logger.info(Const.MODIFIE, essaim);
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
	 * Renvoie la liste des EssaimTree fils de l'essaim passé en paramètre. Appelé
	 * par EssaimController.graphedescendance avec l'essaim racine d'une
	 * arborescence en paramètre.
	 */
	List<EssaimTree> listeEssaimsFils(Essaim essaim) {
		// La liste des noeuds du graphe de descendance des essaims.
		List<EssaimTree> essaimTree = new ArrayList<>();
		calculeEssaimsFils(essaimTree, essaim);
		return essaimTree;
	}

	/**
	 * Calcul de la liste EssaimTree par appels récursifs.
	 */
	private Integer calculeEssaimsFils(List<EssaimTree> resultat, Essaim essaim) {
		Integer poidsMielDescendance = 0;
		for (Essaim essaimSouche : essaimRepository.findBySouche(essaim)) {
			// Appel récursif pour ajouter les noeuds de la descendance de cet essaim.
			// En retour somme des poids de miel de toute la descendance.
			poidsMielDescendance += calculeEssaimsFils(resultat, essaimSouche);
		}
		// Calcul du poids de miel produit par l'essaim "essaim".
		Integer poidsMielEssaim = recolteHausseRepository.findPoidsMielByEssaim(essaim);
		if (poidsMielEssaim == null) {
			poidsMielEssaim = 0;
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