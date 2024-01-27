package ooioo.ruches.hausse;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

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

	public void grapheHaussePose(Model model) {
		// Liste des événements pose et retrait hausse triés par date croissante
		List<Evenement> eves = evenementRepository.findByTypeOrTypeOrderByDateAsc(TypeEvenement.HAUSSEPOSERUCHE,
				TypeEvenement.HAUSSERETRAITRUCHE);
		List<Long> dates = new ArrayList<>();
		List<Integer> nbPosees = new ArrayList<>();
		if (!eves.isEmpty()) {
			int jour = eves.get(0).getDate().getDayOfYear();
			int nbPose = 0;
			for (Evenement e : eves) {
				nbPose += e.getType().equals(TypeEvenement.HAUSSEPOSERUCHE) ? 1 : -1;
				if (jour != e.getDate().getDayOfYear()) {
					nbPosees.add(nbPose);
					dates.add(e.getDate().toEpochSecond(ZoneOffset.UTC));
					jour = e.getDate().getDayOfYear();
				}
			}
		}
		model.addAttribute("dates", dates);
		model.addAttribute("nbPosees", nbPosees);
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