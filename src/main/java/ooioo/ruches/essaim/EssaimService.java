package ooioo.ruches.essaim;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import ooioo.ruches.Const;
import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;
import ooioo.ruches.recolte.Recolte;
import ooioo.ruches.recolte.RecolteHausseRepository;
import ooioo.ruches.recolte.RecolteRepository;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheRepository;

@Service
public class EssaimService {

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

	/*
	 * Ajoute au model Spring les chaînes date, valeur et commentaire du dernier
	 * événement de type typeEvenement
	 */
	public void modelAddEvenement(Model model, Essaim essaim, TypeEvenement typeEvenement) {
		Evenement evenement = evenementRepository.findFirstByEssaimAndTypeOrderByDateDesc(essaim, typeEvenement);
		String type = typeEvenement.toString();
		if (evenement == null) {
			model.addAttribute(Const.DATE + type, null);
			model.addAttribute(Const.VALEUR + type, null);
			model.addAttribute(Const.COMMENTAIRE + type, null);
		} else {
			model.addAttribute(Const.DATE + type, evenement.getDate());
			model.addAttribute(Const.VALEUR + type, evenement.getValeur());
			model.addAttribute(Const.COMMENTAIRE + type, evenement.getCommentaire());
		}
	}

	/*
	 * Ajoute au model Spring les chaînes date, valeur et commentaire du dernier
	 * événement de type ESSAIMTRAITEMENT ou ESSAIMTRAITEMENTFIN
	 */
	public void modelAddEvenTraitement(Model model, Essaim essaim) {
		Evenement evenement = evenementRepository.findFirstTraitemenetByEssaim(essaim.getId(),
				TypeEvenement.ESSAIMTRAITEMENT.ordinal(), TypeEvenement.ESSAIMTRAITEMENTFIN.ordinal());
		if (evenement == null) {
			model.addAttribute("dateTraitement", null);
			model.addAttribute("commentaireTraitement", null);
			model.addAttribute("typeTraitement", null);
		} else {
			// si la date de l'événement fin est avant le date de l'événement début
			// ou si pas d'événement fin trouvé, on affiche l'événement
			model.addAttribute("dateTraitement", evenement.getDate());
			model.addAttribute("commentaireTraitement", evenement.getCommentaire());
			model.addAttribute("typeTraitement",
					(evenement.getType() == TypeEvenement.ESSAIMTRAITEMENT) ? "Début" : "Fin");
		}
	}

	/**
	 * Renvoie la liste des EssaimTree fils de l'essaim passé en paramètre
	 */
	public List<EssaimTree> listeEssaimsFils(Essaim essaim) {
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
		resultat.add(new EssaimTree(essaim.getNom(),
				(essaim.getSouche() == null) ? "null" : essaim.getSouche().getNom(),
				essaim.getId(),
				essaim.getReineCouleurMarquage().toString(),
				(ruche == null) ? "null" : ruche.getNom(),
				(ruche == null) ? "null" : ruche.getRucher().getNom(),
				poidsMielEssaim,
				poidsMielDescendance
				));
		return poidsMielDescendance;
	}

}