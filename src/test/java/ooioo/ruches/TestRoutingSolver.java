package ooioo.ruches;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.Assignment;
import com.google.ortools.constraintsolver.FirstSolutionStrategy;
import com.google.ortools.constraintsolver.LocalSearchMetaheuristic;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import com.google.ortools.constraintsolver.RoutingSearchParameters;
import com.google.ortools.constraintsolver.main;
import com.google.protobuf.Duration;

/*
 * Test du routing solver Traveling Salesman Google.
 * Voir /ooioo/ruches/rucher/RucherService.java
 */
public class TestRoutingSolver {

	private final Logger logger = LoggerFactory.getLogger(TestRoutingSolver.class);

	/*
	 * Inner class static pour les distances entre ruches
	 */
	private static class DataModel {
		// distanceMatrix initialisation pour test
		public static long[][] distanceMatrix = { { 0l, 100l, 2104l, 444l, 2209l }, { 3331l, 0l, 2534l, 999l, 666l },
				{ 1245l, 210l, 0l, 765l, 971l }, { 8525l, 270l, 8350l, 0l, 184l }, { 3525l, 1370l, 5250l, 5011l, 0l } };
		// le nombre de personnes à parcourir les ruches
		public static final int vehicleNumber = 1;
		// l'indice du point de départ
		public static final int depot = 0;
	}

	/*
	 * Calcul du chemin le plus court de visite des ruches du rucher.
	 * https://developers.google.com/optimization/routing/tsp Le chemin est calculé
	 */
	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void cheminRuchesRucher(boolean redraw) {
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
			long previousIndex = index;
			index = solution.value(routing.nextVar(index));
			routeDistance += routing.getArcCostForVehicle(previousIndex, index, 0l);
		}
		logger.info("redraw " + redraw + ". Distance " + routeDistance);
		assertEquals(5777l,routeDistance);
	}

}