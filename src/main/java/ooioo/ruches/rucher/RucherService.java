package ooioo.ruches.rucher;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

	// Algorithme voyageur de commerce Branch and Bound
	// nombre de noeuds
	private int nBandB;
	// finalPathBandB[] stores the final solution ie, the
	// path of the salesman.
	private int[] finalPathBandB;
	// visitedBandB[] keeps track of the already visited nodes
	// in a particular path
	private boolean[] visitedBandB;
	// Stores the final minimum weight of shortest tour.
	private int finalResBandB;
	
	private final Random random = new Random();
	
	/*
	 * Calcul du chemin le plus court de visite des ruches du rucher
	 */
	public double cheminRuchesRucher(List<RucheParcours> chemin, Rucher rucher, Iterable<Ruche> ruches, int nRuchesLimite) {
		RucheParcours entree = new RucheParcours(0l, 0, rucher.getLongitude(), rucher.getLatitude());
		chemin.add(entree);
		int ordre = 0;
		for (Ruche ruche : ruches) {
			ordre += 1;
			chemin.add(new RucheParcours(ruche.getId(), ordre, ruche.getLongitude(), ruche.getLatitude()));
		}
		int cheminSize = chemin.size();
		if (cheminSize > nRuchesLimite) {
			// nRuchesLimite ruches ou plus : on fait une recherche approchée
			ordre += 1;
			RucheParcours fin = new RucheParcours(0l, ordre, rucher.getLongitude(), rucher.getLatitude());
			chemin.add(fin);
			cheminSize += 1;
			return this.cheminRuchesRucherRecuit(chemin, cheminSize);
		} else if (cheminSize == 1) {
			// 0 ruche il n'y a que l'entrée dans chemin
			return 0d;
		}
		// Sinon recherche du parcours le plus court
		return this.cheminRuchesRucherBandB(chemin, cheminSize);
	}

	/*
	 * Calcul du chemin le plus court de visite des ruches du rucher algorithme
	 * branch and bound
	 * https://www.geeksforgeeks.org/traveling-salesman-problem-using-branch-and-
	 * bound-2/
	 */
	private double cheminRuchesRucherBandB(List<RucheParcours> chemin, int cheminSize) {
		this.nBandB = cheminSize;
		// Initialisation d'un tableau contenant les distances entre ruches
		int[][] adj = new int[this.nBandB][this.nBandB];
		double dist;
		for (int i = 1; i < this.nBandB; i++) {
			for (int j = 0; j < i; j++) {
				RucheParcours ii = chemin.get(i);
				RucheParcours jj = chemin.get(j);
				dist = distance(ii.getLatitude(), jj.getLatitude(), ii.getLongitude(), jj.getLongitude());
				adj[i][j] = (int) (dist * 1000.0);
				adj[j][i] = adj[i][j];
			}
		}
		for (int i = 0; i < this.nBandB; i++) {
			adj[i][i] = 0;
		}
		int[] currPath = new int[this.nBandB + 1];
		// finalPathBandB[] stores the final solution ie, the
		// path of the salesman.
		this.finalPathBandB = new int[this.nBandB + 1];
		// visitedBandB[] keeps track of the already visited nodes
		// in a particular path
		this.visitedBandB = new boolean[this.nBandB];
		// Stores the final minimum weight of shortest tour.
		this.finalResBandB = Integer.MAX_VALUE;
		// Calculate initial lower bound for the root node
		// using the formula 1/2 * (sum of first min +
		// second min) for all edges.
		// Also initialize the currPath and visited array
		int currBound = 0;
		Arrays.fill(currPath, -1);
		Arrays.fill(this.visitedBandB, false);
		// Compute initial bound
		for (int i = 0; i < this.nBandB; i++)
			currBound += (firstMin(adj, i) + secondMin(adj, i));
		// Rounding off the lower bound to an integer
		currBound = (currBound == 1) ? currBound / 2 + 1 : currBound / 2;
		// We start at vertex 1 so the first vertex
		// in currPath[] is 0
		this.visitedBandB[0] = true;
		currPath[0] = 0;
		// Call to tSPRec for currWeight equal to
		// 0 and level 1
		tSPRec(adj, currBound, 0, 1, currPath);
		List<RucheParcours> cheminRet = new ArrayList<>();
		for (int i = 0; i <= this.nBandB; i++) {
			cheminRet.add(chemin.get(this.finalPathBandB[i]));
		}
		chemin.clear();
		for (int i = 0; i <= this.nBandB; i++) {
			chemin.add(cheminRet.get(i));
		}
		return this.finalResBandB / 1000.0;
	}

	/**
	 * Recherche du sommet le plus proche du sommet i
	 */
	private int firstMin(int[][] adj, int i) {
		int min = Integer.MAX_VALUE;
		for (int k = 0; k < i; k++) {
			if (adj[i][k] < min) {
				min = adj[i][k];
			}
		}
		for (int k = i + 1; k < this.nBandB; k++) {
			if (adj[i][k] < min) {
				min = adj[i][k];
			}
		}
		return min;
	}

	/**
	 * Recherche du deuxième sommet le plus proche du sommet i
	 */
	private int secondMin(int[][] adj, int i) {
		int first = Integer.MAX_VALUE;
		int second = Integer.MAX_VALUE;
		for (int j = 0; j < i; j++) {
			if (adj[i][j] <= first) {
				second = first;
				first = adj[i][j];
			} else if (adj[i][j] <= second && adj[i][j] != first) {
				second = adj[i][j];
			}
		}
		for (int j = i + 1; j < this.nBandB; j++) {
			if (adj[i][j] <= first) {
				second = first;
				first = adj[i][j];
			} else if (adj[i][j] <= second && adj[i][j] != first) {
				second = adj[i][j];
			}
		}
		return second;
	}

	/**
	 * pCurrBound -> lower bound of the root node pCurrWeight-> stores the weight of
	 * the path so far level-> current level while moving in the search space tree
	 * currPath[] -> where the solution is being stored which would later be copied
	 * to finalPathBandB[]
	 */
	private void tSPRec(int[][] adj, int pCurrBound, int pCurrWeight, int level, int[] currPath) {
		int currBound = pCurrBound;
		int currWeight = pCurrWeight;
		// base case is when we have reached level nBandB which
		// means we have covered all the nodes once
		if (level == this.nBandB) {
			// check if there is an edge from last vertex in
			// path back to the first vertex
			if (adj[currPath[level - 1]][currPath[0]] != 0) {
				// currRes has the total weight of the
				// solution we got
				int currRes = currWeight + adj[currPath[level - 1]][currPath[0]];
				// Update final result and final path if
				// current result is better.
				if (currRes < this.finalResBandB) {
					System.arraycopy(currPath, 0, finalPathBandB, 0, this.nBandB);
					this.finalPathBandB[this.nBandB] = currPath[0];
					this.finalResBandB = currRes;
				}
			}
			return;
		}
		// for any other level iterate for all vertices to
		// build the search space tree recursively
		for (int i = 0; i < this.nBandB; i++) {
			// Consider next vertex if it is not same (diagonal
			// entry in adjacency matrix and not visited
			// already)
			if (adj[currPath[level - 1]][i] != 0 && !this.visitedBandB[i]) {
				int temp = currBound;
				currWeight += adj[currPath[level - 1]][i];
				// different computation of currBound for
				// level 2 from the other levels
				if (level == 1)
					currBound -= ((firstMin(adj, currPath[level - 1]) + firstMin(adj, i)) / 2);
				else
					currBound -= ((secondMin(adj, currPath[level - 1]) + firstMin(adj, i)) / 2);
				// currBound + currWeight is the actual lower bound
				// for the node that we have arrived on
				// If current lower bound < finalResBandB, we need to explore
				// the node further
				if (currBound + currWeight < this.finalResBandB) {
					currPath[level] = i;
					this.visitedBandB[i] = true;
					// call tSPRec for the next level
					tSPRec(adj, currBound, currWeight, level + 1, currPath);
				}
				// Else we have to prune the node by resetting
				// all changes to currWeight and currBound
				currWeight -= adj[currPath[level - 1]][i];
				currBound = temp;
				// Also reset the visited array
				Arrays.fill(this.visitedBandB, false);
				for (int j = 0; j <= level - 1; j++)
					this.visitedBandB[currPath[j]] = true;
			}
		}
	}

	/**
	 * Calcul du chemin le plus court de visite des ruches du rucher algorithme
	 * recuit simulé http://www.tangentex.com/RecuitSimule.htm
	 * 	https://en.wikipedia.org/wiki/Simulated_annealing
	 */
	private double cheminRuchesRucherRecuit(List<RucheParcours> chemin, int cheminSize) {
		// Initialisation d'un tableau contenant les distances entre ruches
		double[][] distRuches = new double[cheminSize][cheminSize];
		double dist;
		for (Integer i = 1; i < cheminSize; i++) {
			for (Integer j = 0; j < i; j++) {
				RucheParcours ii = chemin.get(i);
				RucheParcours jj = chemin.get(j);
				dist = this.distance(ii.getLatitude(), jj.getLatitude(), ii.getLongitude(), jj.getLongitude());
				distRuches[i][j] = dist;
				distRuches[j][i] = dist;
			}
		}
		double ec = this.cheminDistance(chemin, distRuches, cheminSize);
		double t0 = 1000d;
		double tMin = 0.1d;
		double tau = 1000000d;
		double ef;
		double t = 0d;
		double tCurr = t0;
		int iRuche;
		int jRuche;
		int nbRuches = cheminSize - 2;
		// Random random = new Random();
		while (tCurr > tMin) {
			iRuche = random.nextInt(nbRuches) + 1;
			jRuche = random.nextInt(nbRuches) + 1;
			while (jRuche == iRuche) {
				jRuche = random.nextInt(nbRuches) + 1;
			}
			Collections.swap(chemin, iRuche, jRuche);
			ef = this.cheminDistance(chemin, distRuches, cheminSize);
			if (ef <= ec) {
				ec = ef;
			} else {
				if (Math.random() > Math.exp((ec - ef) / tCurr)) {
					Collections.swap(chemin, iRuche, jRuche);
				} else {
					ec = ef;
				}
			}
			t += 1d;
			tCurr = t0 / Math.exp(t / tau);
		}
		return ec;
	}

	/**
	 * Calcul de la distance totale pour parcourir les ruches d'un rucher selon un
	 * chemin donné
	 */
	private double cheminDistance(List<RucheParcours> chemin, double[][] distRuches, int cheminSize) {
		double dist = distRuches[chemin.get(0).getOrdre()][chemin.get(1).getOrdre()];
		for (int i = 2; i < cheminSize; i++) {
			dist += distRuches[chemin.get(i - 1).getOrdre()][chemin.get(i).getOrdre()];
		}
		return dist;
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