package ooioo.ruches.hausse;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import ooioo.ruches.Const;
import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;

@Service
public class HausseService {

	private final EvenementRepository evenementRepository;

	public HausseService(EvenementRepository evenementRepository) {
		this.evenementRepository = evenementRepository;
	}

	/*
	 * Ajoute au model Spring les chaînes date, valeur et commentaire du dernier
	 * événement de type typeEvenement
	 */
	public void modelAddEvenement(Model model, Hausse hausse, TypeEvenement typeEvenement) {
		Evenement evenement = evenementRepository.findFirstByHausseAndTypeOrderByDateDesc(hausse, typeEvenement);
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

}