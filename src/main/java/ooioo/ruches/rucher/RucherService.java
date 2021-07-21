package ooioo.ruches.rucher;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.Assignment;
import com.google.ortools.constraintsolver.FirstSolutionStrategy;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import com.google.ortools.constraintsolver.RoutingSearchParameters;

import ooioo.ruches.Const;
import ooioo.ruches.LatLon;
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
	private String dispersion;
	
	private static int chSize;
	
	private static class DataModel {
		  public final long[][] distanceMatrix = new long[chSize][chSize];
		  public final int vehicleNumber = 1;
		  public final int depot = 0;
	}

	/*
	 * Calcul du chemin le plus court de visite des ruches du rucher
	 */
	public double cheminRuchesRucher(List<RucheParcours> chemin, Rucher rucher, Iterable<Ruche> ruches) {
		RucheParcours entree = new RucheParcours(0l, 0, rucher.getLongitude(), rucher.getLatitude());
		chemin.add(entree);
		int ordre = 0;
		for (Ruche ruche : ruches) {
			ordre += 1;
			chemin.add(new RucheParcours(ruche.getId(), ordre, ruche.getLongitude(), ruche.getLatitude()));
		}
		int cheminSize = chemin.size();
		// return cheminRuchesRucherOrTools(chemin, cheminSize);
		// Initialisation d'un tableau contenant les distances entre ruches
		chSize = cheminSize;
		final DataModel data = new DataModel();
		double dist;
		for (int i = 1; i < cheminSize; i++) {
			for (int j = 0; j < i; j++) {
				RucheParcours ii = chemin.get(i);
				RucheParcours jj = chemin.get(j);
				dist = distance(ii.getLatitude(), jj.getLatitude(), ii.getLongitude(), jj.getLongitude());
				data.distanceMatrix[i][j] = (int) (dist * 1000.0);
				data.distanceMatrix[j][i] = data.distanceMatrix[i][j];
			}
		}
		for (int i = 0; i < cheminSize; i++) {
			data.distanceMatrix[i][i] = 0;
		}
		Loader.loadNativeLibraries();
		RoutingIndexManager manager =
		    new RoutingIndexManager(data.distanceMatrix.length, data.vehicleNumber, data.depot);
		RoutingModel routing = new RoutingModel(manager);
		final int transitCallbackIndex =
			    routing.registerTransitCallback((long fromIndex, long toIndex) -> {
			      // Convert from routing variable Index to user NodeIndex.
			      int fromNode = manager.indexToNode(fromIndex);
			      int toNode = manager.indexToNode(toIndex);
			      return data.distanceMatrix[fromNode][toNode];
			    });
		routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);
		RoutingSearchParameters searchParameters =
				com.google.ortools.constraintsolver.main.defaultRoutingSearchParameters()
			        .toBuilder()
			        .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
			        .build();
		Assignment solution = routing.solveWithParameters(searchParameters);
		List<RucheParcours> cheminRet = new ArrayList<>();
		long routeDistance = 0;
		long index = routing.start(0);
		while (!routing.isEnd(index)) {
			cheminRet.add(chemin.get(manager.indexToNode(index)));
			long previousIndex = index;
		    index = solution.value(routing.nextVar(index));
		    routeDistance += routing.getArcCostForVehicle(previousIndex, index, 0);
		    }
		cheminRet.add(chemin.get(manager.indexToNode(routing.end(0))));
		chemin.clear();
		for (int i = 0; i <= cheminSize; i++) {
			chemin.add(cheminRet.get(i));
		}
		return routeDistance / 1000.0;
	}

	/**
	 * Ajoute une liste de ruches dans un rucher. 
	 * Création de l'événement RUCHEAJOUTRUCHER
	 */
	public void sauveAjouterRuches(Rucher rucher, String[] ruchesNoms, String date, String commentaire) {
		LocalDateTime dateEveAjout = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
		for (String rucheNom : ruchesNoms) {
			Ruche ruche = rucheRepository.findByNom(rucheNom);
			ruche.setRucher(rucher);
			// Mettre les coord. de la ruche à celles du rucher
			// dans un rayon égal à dispersion
			LatLon latLon = dispersion(rucher.getLatitude(), rucher.getLongitude());
			ruche.setLatitude(latLon.getLat());
			ruche.setLongitude(latLon.getLon());
			rucheRepository.save(ruche);
			// Créer un événement ajout dans le rucher rucherId
			Evenement eveAjout = new Evenement(dateEveAjout, TypeEvenement.RUCHEAJOUTRUCHER, ruche, ruche.getEssaim(),
					rucher, null, null, commentaire); // valeur commentaire
			evenementRepository.save(eveAjout);
			logger.info("Ruches {} déplacées dans le rucher {}", ruchesNoms, rucher.getNom());
		}
	}

	/**
	 * Calcule un point dans un cercle centré sur lat,lon de rayon "dispersion"
	 * (voir application.properties)
	 */
	public LatLon dispersion(Float lat, Float lon) {
		double w = Double.parseDouble(dispersion) * Math.sqrt(Math.random()) / 111300f;
		double t = 2.0 * Math.PI * Math.random();
		return new LatLon(lat + (float) (w * Math.sin(t)),
				lon + (float) (w * Math.cos(t) / Math.cos(Math.toRadians(lat))));
	}

	/**
	 * Calcul de la distance entre deux points donnés en latitude, longitude
	 * Méthode de Haversine, distance orhodromique avec rayon de la terre moyen 6371km
	 */
	public double distance(double lat1, double lat2, double lon1, double lon2) {
		double sinLatDistance2 = Math.sin(Math.toRadians(lat2 - lat1) / 2d);
		double sinLonDistance2 = Math.sin(Math.toRadians(lon2 - lon1) / 2d);
		double a = sinLatDistance2 * sinLatDistance2 + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * sinLonDistance2 * sinLonDistance2;
		return 12742000d * Math.asin(Math.sqrt(a));
		// ou encore : return 12742000d * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)); 
		// 12742000d diamètre de la terre en mètres
	}

}