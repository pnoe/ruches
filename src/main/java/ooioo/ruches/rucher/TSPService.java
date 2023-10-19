package ooioo.ruches.rucher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import ooioo.ruches.Utils;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheParcours;

@Service
public class TSPService {

	private final Random random = new Random();

	/*
	 * Calcul du chemin le plus court de visite des ruches du rucher
	 */
	public double cheminRuchesRucher(List<RucheParcours> chemin, Rucher rucher, List<Ruche> ruches, boolean redraw) {
		// TODO redraw true : augmenter nombre d'itérations
		RucheParcours entree = new RucheParcours(0l, 0, rucher.getLongitude(), rucher.getLatitude());
		chemin.add(entree);
		int ordre = 0;

		// TODO initialiser en prenant la ruche la plus proche de la précédente
		List<Ruche> ruchesTri = new ArrayList<>(ruches);

		double diametreTerre = ((rucher.getAltitude() == null) ? 0 : rucher.getAltitude())
				+ 2 * Utils.rTerreLat(rucher.getLatitude());

		for (int i = 1; i < ruches.size(); i++) {
			// rechercher dans ruchesTri la ruche la plus proche de la précédente
			double distMin = Double.MAX_VALUE;
			double d;
			int iMin = 0;
			int rIndex = 0;
			for (Ruche ruche : ruchesTri) {
				d = Utils.distance(diametreTerre, ruche.getLatitude(), chemin.get(i - 1).latitude(),
						ruche.getLongitude(), chemin.get(i - 1).longitude());
				if (d < distMin) {
					distMin = d;
					iMin = rIndex;
				}
				rIndex++;
			}
			chemin.add(new RucheParcours(ruchesTri.get(iMin).getId(), ordre, ruchesTri.get(iMin).getLongitude(),
					ruchesTri.get(iMin).getLatitude()));
			ruchesTri.remove(iMin);
		}
		// ajouter la dernière ruche qui reste dans ruchesTri
		chemin.add(new RucheParcours(ruchesTri.get(0).getId(), ordre, ruchesTri.get(0).getLongitude(),
				ruchesTri.get(0).getLatitude()));

		/*
		 * for (Ruche ruche : ruches) { ordre += 1; chemin.add(new
		 * RucheParcours(ruche.getId(), ordre, ruche.getLongitude(),
		 * ruche.getLatitude())); }
		 */

		int cheminSize = chemin.size();
		if (cheminSize > 1) {
			ordre += 1;
			RucheParcours fin = new RucheParcours(0l, ordre, rucher.getLongitude(), rucher.getLatitude());
			chemin.add(fin);
			cheminSize += 1;
			return this.cheminRuchesRucherRecuit(chemin, cheminSize, diametreTerre);
		} else {
			// 0 ruche il n'y a que l'entrée dans chemin
			return 0d;
		}
	}

	/**
	 * Calcul du chemin le plus court de visite des ruches du rucher algorithme
	 * recuit simulé http://www.tangentex.com/RecuitSimule.htm
	 * https://en.wikipedia.org/wiki/Simulated_annealing
	 */
	private double cheminRuchesRucherRecuit(List<RucheParcours> chemin, int cheminSize, double diametreTerre) {
		// Initialisation d'un tableau contenant les distances entre ruches
		double[][] distRuches = new double[cheminSize][cheminSize];
		double dist;

		for (Integer i = 1; i < cheminSize; i++) {
			for (Integer j = 0; j < i; j++) {
				RucheParcours ii = chemin.get(i);
				RucheParcours jj = chemin.get(j);
				dist = Utils.distance(diametreTerre, ii.latitude(), jj.latitude(), ii.longitude(), jj.longitude());
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
	 * chemin donné.
	 */
	private double cheminDistance(List<RucheParcours> chemin, double[][] distRuches, int cheminSize) {
		double dist = distRuches[chemin.get(0).ordre()][chemin.get(1).ordre()];
		for (int i = 2; i < cheminSize; i++) {
			dist += distRuches[chemin.get(i - 1).ordre()][chemin.get(i).ordre()];
		}
		return dist;
	}

}