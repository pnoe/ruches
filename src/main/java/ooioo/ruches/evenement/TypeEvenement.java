package ooioo.ruches.evenement;

/*
 * Le type d'événement
 * Attention ne pas changer l'ordre. Il est utilisé en base de donnée.
 *   0 RUCHEAJOUTRUCHER, 1 AJOUTESSAIMRUCHE
 *   2 HAUSSEPOSERUCHE, 3 HAUSSERETRAITRUCHE, 4 HAUSSEREMPLISSAGE
 *   5 ESSAIMTRAITEMENT, 6 ESSAIMTRAITEMENTFIN, 7 ESSAIMSUCRE
 *   8 COMMENTAIRERUCHE, 9 COMMENTAIREHAUSSE, 10 COMMENTAIREESSAIM, 11 COMMENTAIRERUCHER
 *   13 RUCHEPESEE, 14 RUCHECADRE
 *    l'événement 12 anciennnement dispersion a été intégré à l'entité essaim
 *    attention evenementForm.html saute LIBRE dans la liste des options du select
 */
public enum TypeEvenement {
	RUCHEAJOUTRUCHER, AJOUTESSAIMRUCHE,
	HAUSSEPOSERUCHE, HAUSSERETRAITRUCHE, HAUSSEREMPLISSAGE,
	ESSAIMTRAITEMENT, ESSAIMTRAITEMENTFIN, ESSAIMSUCRE,
	COMMENTAIRERUCHE, COMMENTAIREHAUSSE, COMMENTAIREESSAIM, COMMENTAIRERUCHER,
	RUCHEPESEE, RUCHECADRE
}
