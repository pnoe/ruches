package ooioo.ruches.essaim;

/*
 *  Enum Sortie des essaims
 * 0  DES Désertion. La ruche est retrouvée vide.
 * 1  ESS Essaimage. La reine est partie avec une partie des ouvrières.
 *        Motif appliqué automatiquement lors de l'utilisation du bouton "essaimer" dans le formulaire de modification d'un essaim.
 * 2  FAM Famine. Abeilles et reine mortes enfoncées dans les rayons.
 * 3  DIS Dispersion. La ruche trop faible est secouée par l'apiculteur.
 *        Motif appliqué automatiquement lors de l'utilisation du bouton "disperser" dans le formulaire de modification d'un essaim.
 * 4  DBR Dispersion bourdonneuse. La ruche bourdonneuse est secouée par l'apiculteur.
 * 5  ECH Echec de l'élevage. La ruche n'a pas réussi à élever une nouvelle reine.
 *        Elle n'est pas vide, pas encore bourdonneuse, elle n'est pas secouée.
 * 6  REM Remérage. La reine est euthanasiée par l'apiculteur.
 * 7  BRD Bourdonneuse. Suite à un échec de l'élevage non détecté à temps par l'apiculteur, des abeilles pondeuses apparaissent.
 *        Au lieu de secouer le contenu de la ruche comme pour le cas DBR, on va essayer de la relancer avec EAN, CR0, CRD
 *        ou une nouvelle reine.
 * 8  MLD Maladie.
 * 9  PST Empoisonnement. Pesticide ? Cause à faire valider par un labo
 * 10 DIV Vol ou autre perte diverse (tempête, incendie, vandalisme, sanglier, accident)
*/
public enum ReineSortie {
	DES, ESS, FAM, DIS, DBR, ECH, REM, BRD, MLD, PST, DIV
}
