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

import ooioo.ruches.Const;
import ooioo.ruches.LatLon;
import ooioo.ruches.Nom;
import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;
import ooioo.ruches.ruche.Ruche;
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
	 * @param rucher          Le rucher dont on liste les transhumances
	 * @param evensRucheAjout Tous les événements Ruche Ajout dans Rucher triés par
	 *                        ordre de date
	 * @param group           Si true les transhumances sont regroupées par date
	 * @param histo           En retour les transhumances si pas de regroupement
	 * @param histoGroup      En retour les transhumances si regroupement
	 */
	void transhum(Rucher rucher, List<Evenement> evensRucheAjout, boolean group, List<Transhumance> histo,
			List<Transhumance> histoGroup) {
		// Les nom des ruches présentes dans le rucher
		Collection<Nom> nomRuchesX = rucheRepository.findNomsByRucherId(rucher.getId());
		List<String> ruches = new ArrayList<>();
		for (Nom nomR : nomRuchesX) {
			ruches.add(nomR.nom());
		}
		for (int i = 0, levens = evensRucheAjout.size(); i < levens; i++) {
			// Pour chaque événement eve ruche ajout dans rucher, du plus récent
			// au plus ancien
			Evenement eve = evensRucheAjout.get(i);
			if (eve.getRucher().getId().equals(rucher.getId())) {
				// si l'événement est un ajout dans le rucher,
				// On cherche l'événement précédent ajout de cette ruche
				// pour indication de sa provenance.
				Evenement evePrec = null;
				for (int j = i + 1; j < levens; j++) {
					if ((evensRucheAjout.get(j).getRuche().getId().equals(eve.getRuche().getId()))
							// même ruche
							&& !(evensRucheAjout.get(j).getRucher().getId().equals(rucher.getId()))
					// et rucher différent
					) {
						// si (evensRucheAjout.get(j).getRucher().getId().equals(rucherId))
						// c'est une erreur, deux ajouts successifs dans le même rucher
						evePrec = evensRucheAjout.get(j);
						break;
					}
				}
				// on stocke l'événement ajout dans histo
				histo.add(new Transhumance(rucher, true, // type = true Ajout
						eve.getDate(),
						Collections.singleton(evePrec == null ? "Inconnue" : evePrec.getRucher().getNom()),
						Arrays.asList(eve.getRuche().getNom()), new ArrayList<>(ruches), eve.getId()));
				// on retire le nom de la ruche de la liste des noms de ruches du rucher
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
				Evenement eveAjout = new Evenement(dateEveAjout, TypeEvenement.RUCHEAJOUTRUCHER, ruche,
						ruche.getEssaim(), rucher, null, provenance, commentaire); // valeur commentaire
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
	 * Calcule un point dans un cercle centré sur lat,lon de rayon dispersionRuche
	 * (voir application.properties).
	 */
	public LatLon dispersion(Float lat, Float lon) {
		// Math.random : double value in a range from 0.0 (inclusive) to 1.0
		// (exclusive).
		// w random distance, /111300d transformation en degrés au centre de la terre
		// sqrt() pour une distribution plus régulère dans le cercle
		double w = dispersionRuche * Math.sqrt(Math.random()) / 111300d;
		// angle en radians random par rapport au centre du cercle
		double t = 2d * Math.PI * Math.random();
		return new LatLon(lat + (float) (w * Math.sin(t)),
				lon + (float) (w * Math.cos(t) / Math.cos(Math.toRadians(lat))));
	}

}