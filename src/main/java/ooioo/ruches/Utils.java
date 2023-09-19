package ooioo.ruches;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.servlet.http.HttpSession;

public final class Utils {

	/*
	 * Formatte le nbre de jours d'une notification si int enlève les 0 en tête
	 * (0023 -> 23) si 0 renvoie "" si pas un integer renvoie ""
	 */
	public static final String notifIntFmt(String txtInt) {
		try {
			Integer i = Integer.parseInt(txtInt);
			return i.equals(0) ? "" : i.toString();
		} catch (NumberFormatException e) {
			return "";
		}
	}

	/*
	 * Renvoie la date et heure LocalDateTime.now() décalée éventuellement du
	 * décalage demandé par le menu préférences mémorisé en session
	 */
	public static final LocalDateTime dateTimeDecal(HttpSession session) {
		return (session.getAttribute(Const.DECALAGETEMPS) != null)
				? LocalDateTime.now().plus((Duration) session.getAttribute(Const.DECALAGETEMPS))
				: LocalDateTime.now();
	}

	/*
	 * Renvoie la date LocalDate.now() décalée éventuellement du décalage demandé
	 * par le menu préférences mémorisé en session
	 */
	public static final LocalDate dateDecal(HttpSession session) {
		return (session.getAttribute(Const.DECALAGETEMPS) != null)
				? LocalDate.now().plusDays(((Duration) session.getAttribute(Const.DECALAGETEMPS)).toDays())
				: LocalDate.now();
	}

	/*
	 * Pourcentage ? Renvoie true si str est un int compris entre 0 et 100
	 */
	static boolean isPourCent(String str) {
		if (str == null) {
			return false;
		}
		try {
			int d = Integer.parseInt(str);
			if (d < 0 || d > 100) {
				return false;
			}
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	/*
	 * Pour test nb hausses correct Renvoie true si str est un int compris entre 0
	 * et 10
	 */
	static boolean isIntInf10(String str) {
		if (str == null) {
			return false;
		}
		try {
			int d = Integer.parseInt(str);
			if (d < 0 || d > 10) {
				return false;
			}
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	/*
	 * Renvoie true si str est numérique
	 */
	public static boolean isNum(String str) {
		if (str == null) {
			return false;
		}
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	/**
	 * Calcul de la distance en mètres entre deux points donnés en latitude,
	 * longitude. Méthode de Haversine, distance orhodromique avec diamètre de la
	 * terre en paramètre. Le diamètre de la terre sur l'ellipsoide WGS-84 peut être
	 * calculé en fonction de la latitude.
	 * https://fr.wikipedia.org/wiki/Formule_de_haversine Voir algorithme de
	 * Vincenty pour plus de précision, calcul selon un ellipsoide.
	 */
	public static double distance(double diamTerre, double lat1, double lat2, double lon1, double lon2) {
		double sinDiffLat = Math.sin(Math.toRadians(lat2 - lat1) / 2d);
		double sinDiffLon = Math.sin(Math.toRadians(lon2 - lon1) / 2d);
		double a = sinDiffLat * sinDiffLat
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * sinDiffLon * sinDiffLon;
		return diamTerre * Math.asin(Math.sqrt(a));
		// ou encore : return 12742000d * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		// 12742000d diamètre de la terre en mètres
		// https://github.com/openlayers/openlayers/blob/main/src/ol/sphere.js D =
		// 12742017.6
		// https://en.wikipedia.org/wiki/Earth_radius#Mean_radius
	}

	/**
	 * Calcul le rayon de courbure de la terre en mètres à une latitude en degrés
	 * donnée. Rayon en mètres, latitude en degrés. Ellipsoide WGS-84.
	 * https://en.m.wikipedia.org/wiki/Earth_radius#Global_radii
	 * https://en.m.wikipedia.org/wiki/Earth_radius#Geocentric_radius
	 */
	public static double rTerreLat(double latitude) {
		double a = 6378137.0;
		double b = 6356752.3142;
		// https://en.m.wikipedia.org/wiki/Earth_radius#Geocentric_radius
		double fi = Math.toRadians(latitude);
		double d1 = a * Math.cos(fi);
		double d2 = b * Math.sin(fi);
		double n1 = a * d1;
		double n2 = b * d2;
		return Math.sqrt((n1 * n1 + n2 * n2) / (d1 * d1 + d2 * d2));
	}

	private Utils() {
		throw new IllegalStateException("Constant class");
	}

}
