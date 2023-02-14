package ooioo.ruches.rucher;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import ooioo.ruches.Const;
import ooioo.ruches.LatLon;
import ooioo.ruches.Nom;
import ooioo.ruches.Utils;
import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;
import ooioo.ruches.hausse.HausseRepository;
import ooioo.ruches.personne.PersonneRepository;
import ooioo.ruches.recolte.Recolte;
import ooioo.ruches.recolte.RecolteHausse;
import ooioo.ruches.recolte.RecolteHausseRepository;
import ooioo.ruches.recolte.RecolteRepository;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheParcours;
import ooioo.ruches.ruche.RucheRepository;

@Controller
@RequestMapping("/rucher")
public class RucherController {

	private static final String RUCHER_RUCHERFORM = "rucher/rucherForm";
	private static final String RUCHER_RUCHERLISTE = "rucher/ruchersListe";
	private static final String HISTO = "histo";

	private final Logger logger = LoggerFactory.getLogger(RucherController.class);

	@Autowired
	private RucherRepository rucherRepository;
	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private HausseRepository hausseRepository;
	@Autowired
	private PersonneRepository personneRepository;
	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private RecolteHausseRepository recolteHausseRepository;
	@Autowired
	private RucherService rucherService;
	@Autowired
	private RecolteRepository recolteRepository;
	@Autowired
	private DistRucherRepository drRepo;

	@Autowired
	MessageSource messageSource;

	@Value("${rucher.butinage.rayons}")
	private int[] rayonsButinage;
	@Value("${ign.carteIGN.license}")
	private boolean ignCarteLiscense;
	@Value("${ruche.dist.max}")
	private float distMaxRuche;
	
	/**
	 * Transhumances d'un rucher.
	 *
	 * Les événements sont utilisés par date décroissante : du plus récent au plus
	 * ancien. Liste les événements correspondant à l'ajout ou au retrait de ruches
	 * dans ce rucher. Option "Grouper" pour grouper les événements de même date
	 * (année, mois et jour) et de même type (Ajout/Retrait).
	 *
	 * @param model : on ajoute au model "histo" ou "histoGroup" la liste des événements, "group"
	 *              true si regroupement fait, "rucher" l'objet rucher (titre, lien)
	 * @param group : si true les événements de même jour sont regroupés
	 */
	@GetMapping("/historique/{rucherId}/{group}")
	public String historique(Model model, @PathVariable long rucherId, @PathVariable boolean group) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			model.addAttribute(Const.RUCHER, rucher);
			// la liste de tous les événements RUCHEAJOUTRUCHER triés par ordre de date
			// descendante avec les champs ruche et rucher non null
			List<Evenement> evensRucheAjout = evenementRepository.findAjoutRucheOK();
			// Les nom des ruches présentes dans le rucher
			Collection<Nom> nomRuchesX = rucheRepository.findNomsByRucherId(rucherId);
			List<String> ruches = new ArrayList<>();
			for (Nom nomR : nomRuchesX) {
				ruches.add(nomR.nom());
			}
			List<Transhumance> histo = new ArrayList<>();
			for (int i = 0, levens = evensRucheAjout.size(); i < levens; i++) {
				Evenement eve = evensRucheAjout.get(i);
				if (eve.getRucher().getId().equals(rucherId)) {
					// si l'événement est un ajout dans le rucher
					// on retire après l'affichage la ruche de l'événement
					// de la liste des ruches du rucher
					// On cherche l'événement précédent ajout de cette ruche
					// pour indication de sa provenance
					Evenement evePrec = null;
					for (int j = i + 1; j < levens; j++) {
						if ((evensRucheAjout.get(j).getRuche().getId().equals(eve.getRuche().getId()))
								&& !(evensRucheAjout.get(j).getRuche().getId().equals(rucherId))) {
							// si evensRucheAjout.get(j).getRuche().getId().equals(rucherId)
							// c'est une erreur, deux ajouts successifs dans le même rucher
							evePrec = evensRucheAjout.get(j);
							break;
						}
					}
					histo.add(new Transhumance(null, true, // type = true Ajout
							eve.getDate(),
							Collections.singleton(evePrec == null ? "Inconnue" : evePrec.getRucher().getNom()),
							Arrays.asList(eve.getRuche().getNom()), new ArrayList<>(ruches), eve.getId()));
					if (!ruches.remove(eve.getRuche().getNom())) {
						logger.error("Événement {} le rucher {} ne contient pas la ruche {}", eve.getDate(),
								eve.getRucher().getNom(), eve.getRuche().getNom());
					}
				} else {
					// l'événenemt eve ajoute une ruche dans un autre rucher
					// On cherche l'événement précédent ajout de cette ruche
					for (int j = i + 1; j < levens; j++) {
						Evenement eveJ = evensRucheAjout.get(j);
						if (eveJ.getRuche().getId().equals(eve.getRuche().getId())) {
							if (eveJ.getRucher().getId().equals(rucherId)) {
								// si l'événement précédent evePrec était un ajout dans le
								// rucher, alors eve retire la ruche du rucher
								if (!ruches.contains(eve.getRuche().getNom())) {
									// si l'événement précédent evePrec était un ajout dans le
									// rucher, alors eve retire la ruche du rucher
									histo.add(new Transhumance(null, false, // type = false Retrait
											eve.getDate(), Collections.singleton(eve.getRucher().getNom()),
											Arrays.asList(eve.getRuche().getNom()), new ArrayList<>(ruches),
											eve.getId()));
									ruches.add(eve.getRuche().getNom());
								}
								break;
							} else {
								// c'est un événement ajout dans la ruche mais
								// dans un autre rucher. IL y a deux événements
								// successifs ajout de la ruche dans un autre rucher
								// on revient à la boucle principale qui traitera
								// ce deuxième événement
								break;
							}
						}
					}
				}
			}
			if (group) {
				// Si le groupement est demandé, on boucle sur histo
				// pour créer histoGroup
				List<Transhumance> histoGroup = new ArrayList<>();
				int lhisto = histo.size();
				// pour stockage des provenances/destinations et suppression de doublons
				Set<String> destP;
				int i = 0;
				int j;
				while (i < lhisto) {
					Transhumance itemHisto = histo.get(i);
					List<String> ruchesGroup = new ArrayList<>(itemHisto.ruche());
					destP = new HashSet<>();
					destP.addAll(itemHisto.destProv());
					// on recherche si les événements suivants peuvent être groupés
					// même date et même type (Ajout/Retrait)
					// par contre les destinations provenances peuvent être différentes
					j = i + 1;
					LocalDate itemHistoJour = itemHisto.date().toLocalDate();
					while (j < lhisto) {
						Transhumance itemHistoN = histo.get(j);
						if (itemHistoJour.equals(itemHistoN.date().toLocalDate())
								&& (itemHisto.type() == itemHistoN.type())) {
							// si regroupables : même type et même Date (année, mois et jour)
							// regrouper en concaténant les ruches et en stockant les dest/prov
							ruchesGroup.addAll(itemHistoN.ruche());
							if (!destP.contains(itemHistoN.destProv().iterator().next())) {
								destP.addAll(itemHistoN.destProv());
							}
							j += 1;
						} else {
							break;
						}
					}
					// le nombre de ruches ajoutées ou retirées est j - i
					if (i == j - 1) {
						histoGroup.add(itemHisto);
					} else {
						histoGroup.add(new Transhumance(null, itemHisto.type(), itemHisto.date(), destP,
								new ArrayList<>(ruchesGroup), itemHisto.etat(), itemHisto.eveid()));
					}
					i = j;
				}
				model.addAttribute(HISTO, histoGroup);
			} else {
				model.addAttribute(HISTO, histo);
			}
			if (!ruches.isEmpty()) {
				logger.error(
						"Historique : après traitement des événements en reculant dans le temps, le rucher n'est pas vide");
			}
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		model.addAttribute("group", group);
		return "rucher/rucherHisto";
	}

	/**
	 * Transhumances de tous les ruchers.
	 */
	@GetMapping("/historiques/{group}")
	public String historiques(Model model, @PathVariable boolean group) {
		List<Rucher> ruchers = rucherRepository.findByActif(true);
		List<Transhumance> histoAll = new ArrayList<>();
		List<Transhumance> histoGroup = new ArrayList<>();
		for (Rucher rucher : ruchers) {
			if (rucher.getDepot()) {
				continue;
			}
			// la liste de tous les événements RUCHEAJOUTRUCHER triés par ordre de date
			// descendante avec les champs ruche et rucher non null
			// On ne peut exclure le dépôt qui sert pour trouver les retraits d'un
			// rucher vers le dépôt
			List<Evenement> evensRucheAjout = evenementRepository.findAjoutRucheOK();
			// Les nom des ruches présentes dans le rucher
			Collection<Nom> nomRuchesX = rucheRepository.findNomsByRucherId(rucher.getId());
			List<String> ruches = new ArrayList<>();
			for (Nom nomR : nomRuchesX) {
				ruches.add(nomR.nom());
			}
			List<Transhumance> histo = new ArrayList<>();
			for (int i = 0, levens = evensRucheAjout.size(); i < levens; i++) {
				Evenement eve = evensRucheAjout.get(i);
				if (eve.getRucher().getId().equals(rucher.getId())) {
					// si l'événement est un ajout dans le rucher
					// on retire après l'affichage la ruche de l'événement
					// de la liste des ruches du rucher
					// On cherche l'événement précédent ajout de cette ruche
					// pour indication de sa provenance
					Evenement evePrec = null;
					for (int j = i + 1; j < levens; j++) {
						if ((evensRucheAjout.get(j).getRuche().getId().equals(eve.getRuche().getId()))
								&& !(evensRucheAjout.get(j).getRuche().getId().equals(rucher.getId()))) {
							// si (evensRucheAjout.get(j).getRuche().getId().equals(rucherId))
							// c'est une erreur, deux ajouts successifs dans le même rucher
							evePrec = evensRucheAjout.get(j);
							break;
						}
					}
					histo.add(new Transhumance(rucher, true, // type = true Ajout
							eve.getDate(),
							Collections.singleton(evePrec == null ? "Inconnue" : evePrec.getRucher().getNom()),
							Arrays.asList(eve.getRuche().getNom()), new ArrayList<>(ruches), eve.getId()));
					if (!ruches.remove(eve.getRuche().getNom())) {
						logger.error("Événement {} le rucher {} ne contient pas la ruche {}", eve.getDate(),
								eve.getRucher().getNom(), eve.getRuche().getNom());
					}
				} else {
					// l'événenemt eve ajoute une ruche dans un autre rucher
					// On cherche l'événement précédent ajout de cette ruche
					for (int j = i + 1; j < levens; j++) {
						Evenement eveJ = evensRucheAjout.get(j);
						if (eveJ.getRuche().getId().equals(eve.getRuche().getId())) {
							if (eveJ.getRucher().getId().equals(rucher.getId())) {
								// si l'événement précédent evePrec était un ajout dans le
								// rucher, alors eve retire la ruche du rucher
								if (!ruches.contains(eve.getRuche().getNom())) {
									// si l'événement précédent evePrec était un ajout dans le
									// rucher, alors eve retire la ruche du rucher
									histo.add(new Transhumance(rucher, false, // type = false Retrait
											eve.getDate(), Collections.singleton(eve.getRucher().getNom()),
											Arrays.asList(eve.getRuche().getNom()), new ArrayList<>(ruches),
											eve.getId()));
									ruches.add(eve.getRuche().getNom());
								}
								break;
							} else {
								// c'est un événement ajout dans la ruche mais
								// dans un autre rucher. IL y a deux événements
								// successifs ajout de la ruche dans un autre rucher
								// on revient à la boucle principale qui traitera
								// ce deuxième événement
								break;
							}
						}
					}
				}
			}
			if (group) {
				// Si le groupement est demandé, on boucle sur histo
				// pour créer histoGroup
				int lhisto = histo.size();
				// pour stockage des provenances/destinations et suppression de doublons
				Set<String> destP;
				int i = 0;
				int j;
				while (i < lhisto) {
					Transhumance itemHisto = histo.get(i);
					List<String> ruchesGroup = new ArrayList<>(itemHisto.ruche());
					destP = new HashSet<>();
					destP.addAll(itemHisto.destProv());
					// on recherche si les événements suivants peuvent être groupés
					// même date et même type (Ajout/Retrait)
					// par contre les destinations provenances peuvent être différentes
					j = i + 1;
					LocalDate itemHistoJour = itemHisto.date().toLocalDate();
					while (j < lhisto) {
						Transhumance itemHistoN = histo.get(j);
						if (itemHistoJour.equals(itemHistoN.date().toLocalDate())
								&& (itemHisto.type() == itemHistoN.type())) {
							// si regroupables
							// regrouper en concaténant les ruches et en stockant les dest/prov
							ruchesGroup.addAll(itemHistoN.ruche());
							if (!destP.contains(itemHistoN.destProv().iterator().next())) {
								destP.addAll(itemHistoN.destProv());
							}
							j += 1;
						} else {
							break;
						}
					}
					// le nombre de ruches ajoutées ou retirées est j - i
					if (i == j - 1) {
						histoGroup.add(itemHisto);
					} else {
						// enregistrer groupe dans histoGroup
						// la date est la date du premier événement
						// les autres peuvent avoir des heures et minutes
						// différentes
						// l'id eve est l'id du premier événements, on perd les
						// des autres événements
						histoGroup.add(new Transhumance(rucher, itemHisto.type(), itemHisto.date(), destP,
								new ArrayList<>(ruchesGroup), itemHisto.etat(), itemHisto.eveid()));
					}
					i = j;
				}
			} else {
				// ajouter dans histoxx les items d'histo
				histoAll.addAll(histo);
			}
			if (!ruches.isEmpty()) {
				logger.error(
						"Historique : après traitement des événements en reculant dans le temps, le rucher n'est pas vide");
			}
		}
		// On trie la liste à afficher par date et on l'ajoute au model
		if (group) {
			Collections.sort(histoGroup, (b, a) -> a.date().compareTo(b.date()));
			model.addAttribute(HISTO, histoGroup);
		} else {
			Collections.sort(histoAll, (b, a) -> a.date().compareTo(b.date()));
			model.addAttribute(HISTO, histoAll);
		}
		model.addAttribute("group", group);
		return "rucher/ruchersHisto";
	}

	/**
	 * Statistiques tableau poids de miel par rucher
	 */
	@RequestMapping("/statistiques")
	public String statistiques(Model model) {
		Iterable<Recolte> recoltes = recolteRepository.findAllByOrderByDateAsc();
		Iterable<Rucher> ruchers = rucherRepository.findAll();
		List<Map<String, String>> ruchersPoids = new ArrayList<>();
		DecimalFormat decimalFormat = new DecimalFormat("0.00",
				new DecimalFormatSymbols(LocaleContextHolder.getLocale()));
		Integer pTotal; // poids de miel total produit par le rucher
		Integer pMax; // poids de miel max lors d'une récolte
		Integer pMin; // poids de miel min lors d'une récolte
		int nbRecoltes;
		for (Rucher rucher : ruchers) {
			pTotal = 0;
			pMax = 0;
			pMin = 1000000;
			nbRecoltes = 0;
			for (Recolte recolte : recoltes) {
				Integer poids = recolteHausseRepository.findPoidsMielByRucherByRecolte(rucher.getId(), recolte.getId());
				if (poids != null) {
					nbRecoltes++;
					pTotal += poids;
					pMax = Math.max(pMax, poids);
					pMin = Math.min(pMin, poids);
				}
			}
			if (pMin == 1000000) {
				pMin = 0;
			}
			Map<String, String> rucherPoids = new HashMap<>();
			rucherPoids.put("nom", rucher.getNom());
			rucherPoids.put("id", rucher.getId().toString());
			rucherPoids.put("pTotal", decimalFormat.format(pTotal / 1000.0));
			rucherPoids.put("pMax", decimalFormat.format(pMax / 1000.0));
			rucherPoids.put("pMin", decimalFormat.format(pMin / 1000.0));
			if (nbRecoltes <= 0) {
				rucherPoids.put("pMoyen", "");
			} else {
				float pMoyen = pTotal / (float) nbRecoltes;
				rucherPoids.put("pMoyen", decimalFormat.format(pMoyen / 1000));
			}
			rucherPoids.put("nbRecoltes", Integer.toString(nbRecoltes));
			ruchersPoids.add(rucherPoids);
		}
		model.addAttribute("ruchersPoids", ruchersPoids);
		return "rucher/rucherStatistiques";
	}

	/**
	 * Liste des ruchers
	 */
	@GetMapping("/liste")
	public String liste(HttpSession session, Model model) {
		Object voirInactif = session.getAttribute(Const.VOIRINACTIF);
		Iterable<Rucher> ruchers;
		if (voirInactif != null && (boolean) voirInactif) {
			ruchers = rucherRepository.findAllByOrderByNom();
		} else {
			ruchers = rucherRepository.findByActifOrderByNom(true);
		}
		Collection<Long> nbRuches = new ArrayList<>();
		Collection<Integer> nbHausses = new ArrayList<>();
		for (Rucher rucher : ruchers) {
			nbRuches.add(rucheRepository.countByRucher(rucher));
			// si rucher.depot == true alors ajouter les hausses ou hausse.ruche == null
			Integer nb = hausseRepository.countHausseInRucher(rucher.getId());
			if (rucher.getDepot()) {
				// on ajoute les hausses qui ne sont pas sur des ruches
				// à la somme pour le dépôt
				nb += hausseRepository.countByActiveAndRucheIsNull(true);
			}
			nbHausses.add(nb);
		}
		model.addAttribute(Const.RUCHERS, ruchers);
		model.addAttribute("nbRuches", nbRuches);
		model.addAttribute(Const.NBHAUSSES, nbHausses);
		return RUCHER_RUCHERLISTE;
	}

	/**
	 * Création d'un rucher
	 */
	@GetMapping("/cree")
	public String cree(Model model) {
		List<String> noms = new ArrayList<>();
		for (Nom rucherNom : rucherRepository.findAllProjectedBy()) {
			noms.add(rucherNom.nom());
		}
		model.addAttribute(Const.RUCHERNOMS, noms);
		Rucher rucher = new Rucher();
		// Récupération des coordonnées du dépôt
		Rucher rucherDepot = rucherRepository.findByDepotIsTrue();
		LatLon latLon = rucherService.dispersion(rucherDepot.getLatitude(), rucherDepot.getLongitude());
		rucher.setLatitude(latLon.lat());
		rucher.setLongitude(latLon.lon());
		model.addAttribute(Const.RUCHER, rucher);
		model.addAttribute(Const.PERSONNES, personneRepository.findAll());
		return RUCHER_RUCHERFORM;
	}

	/**
	 * Modifier un rucher
	 */
	@GetMapping("/modifie/{rucherId}")
	public String modifie(Model model, @PathVariable long rucherId) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			model.addAttribute(Const.RUCHERNOMS, rucherRepository.findAllProjectedBy().stream().map(Nom::nom)
					.filter(nom -> !nom.equals(rucher.getNom())).toList());
			model.addAttribute(Const.RUCHER, rucher);
			model.addAttribute(Const.PERSONNES, personneRepository.findAll());
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return RUCHER_RUCHERFORM;
	}

	/**
	 * Enregistrement du rucher créé ou modifié
	 */
	@PostMapping("/sauve")
	public String sauve(Model model, @ModelAttribute Rucher rucher, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return RUCHER_RUCHERFORM;
		}
		// Vérification de l'unicité du nom
		Rucher rNom = rucherRepository.findByNom(rucher.getNom());
		if (rNom != null && !rNom.getId().equals(rucher.getId())) {
			logger.error("Nom de rucher {} existant.", rNom.getNom());
			model.addAttribute(Const.MESSAGE, "Nom de rucher existant.");
			return Const.INDEX;
		}
		String action = ((rucher.getId() == null)?"créé":"modifié");
		rucherRepository.save(rucher);
		logger.info("{} " + action, rucher);
		return "redirect:/rucher/" + rucher.getId();
	}

	/**
	 * Afficher un rucher et ses ruches
	 */
	@GetMapping("/{rucherId}")
	public String affiche(Model model, @PathVariable long rucherId) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			model.addAttribute(Const.RUCHER, rucher);
			// liste des ruches de ce rucher
			Iterable<Ruche> ruches = rucheRepository.findByRucherIdOrderByNom(rucherId);
			model.addAttribute(Const.RUCHES, ruches);
			// si rucher.depot == true alors ajouter les hausses ou hausse.ruche == null
			Integer nbHausses = hausseRepository.countHausseInRucher(rucherId);
			if (rucher.getDepot()) {
				// on ajoute les hausses qui ne sont pas sur des ruches
				// à la somme pour le dépôt
				nbHausses += hausseRepository.countByActiveAndRucheIsNull(true);
			} else {
				// lecture en base de la distance et du temps pour aller du dépôt
				// à ce rucher
				Rucher depot = rucherRepository.findByDepotIsTrue();
				DistRucher dr = (depot.getId().intValue() > rucher.getId().intValue())
						? drRepo.findByRucherStartAndRucherEnd(rucher, depot)
						: drRepo.findByRucherStartAndRucherEnd(depot, rucher);
				if (dr != null) {
					// distance en km
					model.addAttribute("dist", dr.getDist() / 1000.);
					// temps en h et min
					int t = dr.getTemps() / 60;
					model.addAttribute("temps", ((t == 0) ? "" : t + "h ") + dr.getTemps() % 60 + "min");
				}
			}
			model.addAttribute("nbHausses", nbHausses);
			List<Integer> listNbHausses = new ArrayList<>();
			// Nonmbre de cadres dans le dernier événement cadre
			Collection<String> nbCadres = new ArrayList<>();
			for (Ruche ruche : ruches) {
				// Compte du nombre de hausses de chaque ruche
				listNbHausses.add(hausseRepository.countByRucheId(ruche.getId()));
				Evenement evenCadres = evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
						TypeEvenement.RUCHECADRE);
				if (evenCadres == null) {
					nbCadres.add("");
				} else {
					nbCadres.add(evenCadres.getValeur());
				}
			}
			model.addAttribute("nbCadres", nbCadres);
			model.addAttribute("listNbHausses", listNbHausses);
			// Si des hausses de récolte référencent ce rucher, on ne pourra la supprimer
			List<RecolteHausse> recolteHausses = recolteHausseRepository.findByRucherId(rucherId);
			model.addAttribute("recolteHausses", recolteHausses.iterator().hasNext());
			// Si des événements référencent cet rucher, il faudra les supprimer si on la
			// supprime
			Iterable<Evenement> evenements = evenementRepository.findByRucherId(rucherId);
			model.addAttribute(Const.EVENEMENTS, evenements.iterator().hasNext());
			// Calcul du poids de miel par récoltes pour ce rucher
			Iterable<Recolte> recoltes = recolteRepository.findAllByOrderByDateAsc();
			List<Integer> poidsListe = new ArrayList<>();
			List<Recolte> recoltesListe = new ArrayList<>();
			List<Integer> recoltesNbRuches = new ArrayList<>();
			Integer poidsTotal = 0;
			for (Recolte recolte : recoltes) {
				Integer poids = recolteHausseRepository.findPoidsMielByRucherByRecolte(rucher.getId(), recolte.getId());
				if (poids != null) {
					poidsListe.add(poids);
					recoltesListe.add(recolte);
					poidsTotal += poids;
					// nombre de ruche du rucher rucherId dans cette récolte
					recoltesNbRuches
							.add(recolteHausseRepository.countRucheByRecolteByRucher(recolte.getId(), rucherId));
				}
			}
			model.addAttribute("recoltesListe", recoltesListe);
			model.addAttribute("recoltesNbRuches", recoltesNbRuches);
			model.addAttribute("poidsListe", poidsListe);
			model.addAttribute("poidsTotal", poidsTotal);
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "rucher/rucherDetail";
	}

	/**
	 * Rapprocher les ruches trop éloignées de leur rucher
	 */
	@GetMapping({ "/rapproche/Ign/{rucherId}", "/rapproche/Osm/{rucherId}" })
	public String rapproche(Model model, @PathVariable long rucherId, HttpServletRequest request) {
		String servletPath = request.getServletPath().split("/")[3];
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			Float longRucher = rucher.getLongitude();
			Float latRucher = rucher.getLatitude();
			// liste des ruches de ce rucher
			Iterable<Ruche> ruches = rucheRepository.findByRucherIdOrderByNom(rucherId);
			for (Ruche ruche : ruches) {
				if (rucherService.distance(latRucher, ruche.getLatitude(), longRucher, ruche.getLongitude()) > distMaxRuche) {
					// On calcule un point près de l'entrée du rucher
					LatLon latLon = rucherService.dispersion(rucher.getLatitude(), rucher.getLongitude());
					// On met la ruche à ce point
					ruche.setLatitude(latLon.lat());
					ruche.setLongitude(latLon.lon());
					rucheRepository.save(ruche);
					logger.info("{} repositionnée", ruche);
				}
			}
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "redirect:/rucher/" + servletPath + "/{rucherId}";
	}

	/**
	 * Appel du template pour l'ajout de ruches dans un rucher
	 */
	@GetMapping("/ruches/{rucherId}")
	public String afficheRuches(Model model, @PathVariable long rucherId) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			model.addAttribute(Const.RUCHER, rucher);
			// liste des ruches de ce rucher
			Iterable<Ruche> ruches = rucheRepository.findByRucherIdOrderByNom(rucherId);
			// liste des ruches sur un autre rucher
			Iterable<Ruche> ruchesNot = rucheRepository.findByRucherIdNotOrderByNom(rucherId);
			model.addAttribute(Const.RUCHES, ruches);
			model.addAttribute("ruchesNot", ruchesNot);
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "rucher/rucherAjout";
	}

	/**
	 * Suppression d'un rucher On ne peut pas supprimer le dépôt
	 */
	@GetMapping("/supprime/{rucherId}")
	public String supprime(Model model, @PathVariable long rucherId) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucherDepot = rucherRepository.findByDepotIsTrue();
			Rucher rucher = rucherOpt.get();
			if (rucher.getDepot()) {
				model.addAttribute(Const.MESSAGE, "On ne peut pas supprimer le rucher dépôt");
				return Const.INDEX;
			}
			Iterable<Evenement> evenements = evenementRepository.findByRucherId(rucherId);
			List<RecolteHausse> recolteHausses = recolteHausseRepository.findByRucherId(rucherId);
			Iterable<Ruche> ruches = rucheRepository.findCompletByRucherId(rucherId);
			if (recolteHausses.isEmpty()) {
				// on enlève les ruches du rucher à supprimer
				for (Ruche ruche : ruches) {
					// mettre la ruche au dépôt
					ruche.setRucher(rucherDepot);
				}
				// on supprime les événements associées à cette ruche
				for (Evenement evenement : evenements) {
					evenementRepository.delete(evenement);
				}
				rucherRepository.delete(rucher);
				logger.info("{} supprimé", rucher);
			} else {
				model.addAttribute(Const.MESSAGE, "Ce rucher ne peut être supprimé");
				return Const.INDEX;
			}
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "redirect:/rucher/liste";
	}

	/**
	 * Affiche la carte avec les ruches du rucher rucherId.
	 *  Gg google maps, Ign ou Osm OpenStreetMap
	 */
	@GetMapping({ "/Gg/{rucherId}", "/Ign/{rucherId}", "/Osm/{rucherId}" })
	public String rucheMap(Model model, @PathVariable long rucherId, HttpServletRequest request) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			Iterable<Ruche> ruches = rucheRepository.findByRucherIdOrderByNom(rucherId);
			List<RucheParcours> cheminRet = new ArrayList<>();
			double retParcours = rucherService.cheminRuchesRucher(cheminRet, rucher, ruches, false);
			model.addAttribute("distParcours", retParcours);
			model.addAttribute("rucheParcours", cheminRet);
			List<String> nomHausses = new ArrayList<>();
			// Pour calcul du barycentre des ruches, centre des cercles de butinage
			Float longitude;
			Float latitude = 0f;
			int nbRuches = 0;
			double xlon = 0d;
			double ylon = 0d;
			for (Ruche ruche : ruches) {
				nomHausses.add(hausseRepository.findByRucheId(ruche.getId()).stream().map(Nom::nom)
						.reduce("", (a, b) -> a + " " + b).trim());
				nbRuches++;
				Float longrad = (float) (ruche.getLongitude() * Math.PI / 180.0d);
				xlon += Math.cos(longrad);
				ylon += Math.sin(longrad);
				latitude += ruche.getLatitude();
			}
			if (nbRuches != 0) {
				longitude = (float) (Math.atan2(ylon, xlon) * 180d / Math.PI);
				latitude /= nbRuches;
			} else {
				longitude = rucher.getLongitude();
				latitude = rucher.getLatitude();
			}
			model.addAttribute("longitudeCentre", longitude);
			model.addAttribute("latitudeCentre", latitude);
			model.addAttribute("rayonsButinage", rayonsButinage);
			model.addAttribute(Const.HAUSSENOMS, nomHausses);
			model.addAttribute(Const.RUCHER, rucher);
			model.addAttribute(Const.RUCHES, ruches);
			model.addAttribute("ignCarteLiscense", ignCarteLiscense);
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "rucher/rucherDetail" + request.getServletPath().split("/")[2];
	}

	/**
	 * Affiche la carte de tous les ruchers.
	 *  Gg google maps, Ign ou Osm OpenStreetMap
	 */
	@GetMapping({ "/Gg", "/Ign", "/Osm" })
	public String rucherMap(HttpSession session, Model model, HttpServletRequest request) {
		Object voirInactif = session.getAttribute(Const.VOIRINACTIF);
		Iterable<Rucher> ruchers;
		if (voirInactif != null && (boolean) voirInactif) {
			ruchers = rucherRepository.findAllByOrderByNom();
		} else {
			ruchers = rucherRepository.findByActifOrderByNom(true);
		}
		List<String> nomRuches = new ArrayList<>();
		for (Rucher rucher : ruchers) {
			nomRuches.add(rucheRepository.findByRucherId(rucher.getId()).stream().map(Nom::nom)
					.reduce("", (a, b) -> a + " " + b).trim());
		}
		model.addAttribute(Const.RUCHENOMS, nomRuches);
		model.addAttribute(Const.RUCHERS, ruchers);
		model.addAttribute("ignCarteLiscense", ignCarteLiscense);
		return RUCHER_RUCHERLISTE + request.getServletPath().split("/")[2];
	}

	/**
	 * Appel du formulaire pour ajouter une liste de ruches dans un rucher
	 */
	@GetMapping("/ruches/ajouter/{rucherId}/{ruchesNoms}")
	public String ajouterRuches(HttpSession session, Model model, @PathVariable long rucherId,
			@PathVariable String ruchesNoms) {
		// le formulaire affiche le nom du rucher et la liste des noms de ruche
		// il permet le choix de la date et du commentaire
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			model.addAttribute(Const.DATE, Utils.dateTimeDecal(session));
			model.addAttribute(Const.RUCHER, rucherOpt.get());
			model.addAttribute("ruchesNoms", ruchesNoms);
			// On cherche la date du dernier événement RUCHEAJOUTRUCHER
			// pour imposer cette date comme min dans le formulaire
			LocalDateTime dateTimeMin = LocalDateTime.MIN;
			String[] ruchesNomsSplit = ruchesNoms.split(",");
			for (String rucheNom : ruchesNomsSplit) {
				Ruche ruche = rucheRepository.findByNom(rucheNom);
				if (ruche != null) {
					Evenement evenFirst = evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
							ooioo.ruches.evenement.TypeEvenement.RUCHEAJOUTRUCHER);
					if (dateTimeMin.isBefore(evenFirst.getDate())) {
						dateTimeMin = evenFirst.getDate();
					}
				}
			}
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
			model.addAttribute("dateTime", dateTimeMin);
			LocalDateTime dateTimeFirst = dateTimeMin.plusMinutes(1);
			model.addAttribute("dateFirst", dateTimeFirst.format(dateFormat));
			model.addAttribute("timeFirst", dateTimeFirst.format(timeFormat));

		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "rucher/rucherRuchesAjoutForm";
	}

	/**
	 * Ajoute une liste de ruches dans un rucher. Création de l'événement
	 * RUCHEAJOUTRUCHER par ruche
	 */
	@PostMapping("/ruches/ajouter/sauve/{rucherId}/{ruchesNoms}")
	public String sauveAjouterRuches(Model model, @PathVariable long rucherId, @PathVariable String[] ruchesNoms,
			@RequestParam String date, @RequestParam String commentaire) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			rucherService.sauveAjouterRuches(rucher, ruchesNoms, date, commentaire);
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "redirect:/rucher/ruches/" + rucherId;
	}

	/**
	 * Déplace un rucher (appel xmlhttp)
	 */
	@PostMapping("/deplace/{rucherId}/{lat}/{lng}")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody String deplace(@PathVariable Long rucherId, @PathVariable Float lat, @PathVariable Float lng) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			rucher.setLatitude(lat);
			rucher.setLongitude(lng);
			rucherRepository.save(rucher);
			logger.info("Rucher {} déplacé, id {}", rucher.getNom(), rucher.getId());
			return "OK";
		}
		logger.error(Const.IDRUCHERXXINCONNU, rucherId);
		return "Id rucher inconnu";

	}

	/**
	 * Météo d'un rucher
	 *
	 * https://github.com/Prominence/openweathermap-java-api/blob/master/docs/SNAPSHOT.md
	 * https://openweathermap.org/api/one-call-api https://openweathermap.org/api
	 * http://api.openweathermap.org/data/2.5/onecall?lat=43.4900093&lon=5.49108076&APPID=xxxx
	 *
	 */
	@GetMapping("/meteo/{rucherId}")
	public String meteo(Model model, @PathVariable long rucherId) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			model.addAttribute(Const.RUCHER, rucher);
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "rucher/rucherMeteo";
	}

	/**
	 * Repositionne tous les ruchers au barycentre de leurs ruches
	 */
	@GetMapping("/recentre")
	public String recentre(HttpSession session) {
		Object voirInactif = session.getAttribute(Const.VOIRINACTIF);
		Iterable<Rucher> ruchers;
		if (voirInactif != null && (boolean) voirInactif) {
			ruchers = rucherRepository.findAllByOrderByNom();
		} else {
			ruchers = rucherRepository.findByActifOrderByNom(true);
		}
		for (Rucher rucher : ruchers) {
			Float longitude;
			Float latitude = 0f;
			Iterable<Ruche> ruches = rucheRepository.findByRucherIdOrderByNom(rucher.getId());
			Float longitudeAvant = rucher.getLongitude();
			Float latitudeAvant = rucher.getLatitude();
			int nbRuches = 0;
			double xlon = 0d;
			double ylon = 0d;
			for (Ruche ruche : ruches) {
				nbRuches++;
				Float longrad = (float) (ruche.getLongitude() * Math.PI / 180.0d);
				xlon += Math.cos(longrad);
				ylon += Math.sin(longrad);
				latitude += ruche.getLatitude();
			}
			if (nbRuches == 0)
				continue;
			longitude = (float) (Math.atan2(ylon, xlon) * 180d / Math.PI);
			latitude /= nbRuches;
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(6);
			rucher.setLongitude(longitude);
			rucher.setLatitude(latitude);
			rucherRepository.save(rucher);
			logger.info(
					"Repositionnement du rucher {} au centre de ses ruches. Avant long {} lat {}. Après long {} lat {}.",
					rucher.getNom(), nf.format(longitudeAvant), nf.format(latitudeAvant), nf.format(longitude),
					nf.format(latitude));
		}
		return "redirect:/rucher/Ign";
	}

	/**
	 * Calcul du parcours des ruches d'un rucher (appel XMLHttpRequest).
	 *  redraw = 0 recalcul si l'utilisateur déplace une ruche sur la carte 
	 *  redraw = 1 recalcul si l'utilisateur le demande pour améliore de parcours
	 */
	@GetMapping("/parcours/{rucherId}/{redraw}")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody Map<String, Object> parcours(@PathVariable Long rucherId, @PathVariable boolean redraw) {
		Map<String, Object> map = new HashMap<>();
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			Iterable<Ruche> ruches = rucheRepository.findByRucherIdOrderByNom(rucherId);
			List<RucheParcours> cheminRet = new ArrayList<>();
			double retParcours = rucherService.cheminRuchesRucher(cheminRet, rucher, ruches, redraw);
			map.put("distParcours", retParcours);
			map.put("rucheParcours", cheminRet);
			return map;
		}
		logger.error(Const.IDRUCHERXXINCONNU, rucherId);
		map.put("erreur", "Id rucher inconnu");
		return map;
	}

	/**
	 * Sauvegarde d'un dessin sur carte ign ou osm (appel XMLHttpRequest)
	 */
	@PostMapping("/sauveDessin/{rucherId}")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody String sauveDessin(@PathVariable Long rucherId, @RequestBody String dessin) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			rucher.setDessin(dessin);
			rucherRepository.save(rucher);
			return "OK";
		}
		logger.error(Const.IDRUCHERXXINCONNU, rucherId);
		return "error";
	}

}
