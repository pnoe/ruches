package ooioo.ruches.rucher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.Assignment;
import com.google.ortools.constraintsolver.FirstSolutionStrategy;
import com.google.ortools.constraintsolver.LocalSearchMetaheuristic;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import com.google.ortools.constraintsolver.RoutingSearchParameters;
import com.google.ortools.constraintsolver.main;
import com.google.protobuf.Duration;

import ooioo.ruches.Const;
import ooioo.ruches.LatLon;
import ooioo.ruches.Nom;
import ooioo.ruches.Utils;
import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheParcours;
import ooioo.ruches.ruche.RucheRepository;

@Service
public class RucherService {

	private final Logger logger = LoggerFactory.getLogger(RucherService.class);

	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private EvenementRepository evenementRepository;

	@Value("${rucher.ruche.dispersion}")
	private double dispersionRuche;

	/**
	 * Calcul des transhumances d'un rucher.
	 *
	 * @param rucher Le rucher dont on liste les transhumances
	 * @param evensRucheAjout Tous les événements Ruche Ajout dans Rucher
	 * @param group Si true les transhumances sont regroupées par date
	 * @param histo En retour les transhumances si pas de regroupement
	 * @param histoGroup En retour les transhumances si pas de regroupement
	 */
	public void transhum(Rucher rucher, List<Evenement> evensRucheAjout, boolean group, List<Transhumance> histo,
			List<Transhumance> histoGroup) {
		// Les nom des ruches présentes dans le rucher
		Collection<Nom> nomRuchesX = rucheRepository.findNomsByRucherId(rucher.getId());
		List<String> ruches = new ArrayList<>();
		for (Nom nomR : nomRuchesX) {
			ruches.add(nomR.nom());
		}
		for (int i = 0, levens = evensRucheAjout.size(); i < levens; i++) {
			Evenement eve = evensRucheAjout.get(i);
			if (eve.getRucher().getId().equals(rucher.getId())) {
				// si l'événement est un ajout dans le rucher,
				// on retire après l'affichage la ruche de l'événement
				// de la liste des ruches du rucher.
				// On cherche l'événement précédent ajout de cette ruche
				// pour indication de sa provenance.
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
										Arrays.asList(eve.getRuche().getNom()), new ArrayList<>(ruches), eve.getId()));
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
					// la date est la date du premier événement,
					// les autres peuvent avoir des heures et minutes
					// différentes.
					// l'id eve est l'id du premier événements, on perd les
					// id des autres événements.
					histoGroup.add(new Transhumance(rucher, itemHisto.type(), itemHisto.date(), destP,
							new ArrayList<>(ruchesGroup), itemHisto.etat(), itemHisto.eveid()));
				}
				i = j;
			}
		}
		if (!ruches.isEmpty()) {
			logger.error(
					"Transhumances : après traitement des événements en reculant dans le temps, le rucher {} n'est pas vide",
					rucher.getNom());
		}
	}

	/*
	 * Inner class static pour les distances entre ruches
	 */
	private static class DataModel {
		// distanceMatrix est initialisée dans cheminRuchesRucher
		public static long[][] distanceMatrix;
		// le nombre de personnes à parcourir les ruches
		public static final int vehicleNumber = 1;
		// l'indice du point de départ
		public static final int depot = 0;
	}

	/*
	 * Calcul du chemin le plus court de visite des ruches du rucher.
	 * https://developers.google.com/optimization/routing/tsp Le chemin est calculé
	 * dans List<RucheParcours> cheminRet, et la distance en retour de la fonction.
	 */
	public double cheminRuchesRucher(List<RucheParcours> cheminRet, Rucher rucher, Iterable<Ruche> ruches,
			boolean redraw) {
		RucheParcours entree = new RucheParcours(0l, 0, rucher.getLongitude(), rucher.getLatitude());
		List<RucheParcours> chemin = new ArrayList<>();
		chemin.add(entree);
		int ordre = 0;
		for (Ruche ruche : ruches) {
			ordre += 1;
			chemin.add(new RucheParcours(ruche.getId(), ordre, ruche.getLongitude(), ruche.getLatitude()));
		}
		int cheminSize = chemin.size();
		if (cheminSize == 1) {
			return 0d;
		}
		// Initialisation d'un tableau contenant les distances entre ruches
		DataModel.distanceMatrix = new long[cheminSize][cheminSize];
		// Initialisation de la matrice d'int des distances entre les ruches
		// les distances sont en mm
		double dist;
		double diametreTerre = ((rucher.getAltitude() == null) ? 0 : rucher.getAltitude()) +
				2 * Utils.rTerreLat(rucher.getLatitude());
		for (int i = 1; i < cheminSize; i++) {
			for (int j = 0; j < i; j++) {
				RucheParcours ii = chemin.get(i);
				RucheParcours jj = chemin.get(j);
				dist = Utils.distance(diametreTerre, ii.latitude(), jj.latitude(), ii.longitude(), jj.longitude());
				DataModel.distanceMatrix[i][j] = (long) (dist * 1000.0);
				DataModel.distanceMatrix[j][i] = DataModel.distanceMatrix[i][j];
			}
		}
		// Initialistion de la diagonale à 0
		for (int i = 0; i < cheminSize; i++) {
			DataModel.distanceMatrix[i][i] = 0l;
		}
		Loader.loadNativeLibraries();
		// Création du modèle de routage
		RoutingIndexManager manager = new RoutingIndexManager(DataModel.distanceMatrix.length, DataModel.vehicleNumber,
				DataModel.depot);
		RoutingModel routing = new RoutingModel(manager);
		// création de la callback de calcul de distance utilisant
		// la matrice des distances
		final int transitCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
			// Convert from routing variable Index to user NodeIndex.
			int fromNode = manager.indexToNode(fromIndex);
			int toNode = manager.indexToNode(toIndex);
			return DataModel.distanceMatrix[fromNode][toNode];
		});
		// Coût du déplacement. Ici le coût est la distance entre deux ruches.
		routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);
		// Méthode pour calculer le premier parcours
		// PATH_CHEAPEST_ARC recherche la ruche la plus proche
		// https://developers.google.com/optimization/routing/routing_options#first_sol_options
		RoutingSearchParameters searchParameters = redraw
				? main.defaultRoutingSearchParameters().toBuilder()
						.setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
						// advanced search strategy : guided local search
						// pour sortir d'un minimum local
						// https://developers.google.com/optimization/routing/routing_options#local_search_options
						.setLocalSearchMetaheuristic(LocalSearchMetaheuristic.Value.GUIDED_LOCAL_SEARCH)
						// timeLimit obligatoire sinon recherche infinie
						// la recherche durera 10s dans tous les cas
						.setTimeLimit(Duration.newBuilder().setSeconds(10).build()).build()
				: main.defaultRoutingSearchParameters().toBuilder()
						.setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC).build();
		// Appelle le solver
		Assignment solution = routing.solveWithParameters(searchParameters);
		long routeDistance = 0;
		long index = routing.start(0);
		while (!routing.isEnd(index)) {
			cheminRet.add(chemin.get(manager.indexToNode(index)));
			long previousIndex = index;
			index = solution.value(routing.nextVar(index));
			routeDistance += routing.getArcCostForVehicle(previousIndex, index, 0l);
		}
		cheminRet.add(chemin.get(manager.indexToNode(routing.end(0))));
		return routeDistance / 1000.0;
	}

	/**
	 * Ajoute une liste de ruches dans un rucher. Création de l'événement
	 * RUCHEAJOUTRUCHER. Le nom du rucher de provenance est mis dans le champ
	 * valeur. Le commentaire est le commentaire saisi dans le formulaire
	 */
	public void sauveAjouterRuches(Rucher rucher, Long[] ruchesIds, String date, String commentaire) {
		LocalDateTime dateEveAjout = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
		for (Long rucheId : ruchesIds) {
			Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
			if (rucheOpt.isPresent()) {
				Ruche ruche = rucheOpt.get();
			// Si la ruche est déjà dans le rucher, log de l'erreur puis on continue.
			// L'utilisateur ne voit donc pas l'erreur (sauf s'il consulte les logs).
			if ((ruche.getRucher() != null) && (ruche.getRucher().getId().equals(rucher.getId()))) {
				logger.info("Ruche {} déjà présente dans le rucher {}", ruche.getNom(), rucher.getNom());
				continue;
			}
			String provenance = (ruche.getRucher() == null) ? "" : ruche.getRucher().getNom();
			ruche.setRucher(rucher);
			// Mettre les coord. de la ruche à celles du rucher
			// dans un rayon égal à dispersion
			LatLon latLon = dispersion(rucher.getLatitude(), rucher.getLongitude());
			ruche.setLatitude(latLon.lat());
			ruche.setLongitude(latLon.lon());
			rucheRepository.save(ruche);
			// Créer un événement ajout dans le rucher rucherId
			Evenement eveAjout = new Evenement(dateEveAjout, TypeEvenement.RUCHEAJOUTRUCHER, ruche, ruche.getEssaim(),
					rucher, null, provenance, commentaire); // valeur commentaire
			evenementRepository.save(eveAjout);
			logger.info("{} créé", eveAjout);
			} else {
				// Si l'id de la ruche est inconnu, log de l'erreur puis on continue.
				// L'utilisateur ne voit donc pas l'erreur (sauf s'il consulte les logs).
				logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			}
		}
	}

	/**
	 * Calcule un point dans un cercle centré sur lat,lon de rayon "dispersion"
	 * (voir application.properties)
	 */
	public LatLon dispersion(Float lat, Float lon) {
		double w = dispersionRuche * Math.sqrt(Math.random()) / 111300d;
		double t = 2d * Math.PI * Math.random();
		return new LatLon(lat + (float) (w * Math.sin(t)),
				lon + (float) (w * Math.cos(t) / Math.cos(Math.toRadians(lat))));
	}

}