package ooioo.ruches.essaim;

/*
 *  Enum Origine des essaims
* 0  AEC Achat Essaim sur cadre. L'essaim et sa reine sont acquis à l'extérieur de l'exploitation. Il s'agit d'essaim sur cadre.
* 1  AEN Achat Essaim nu. L'essaim et sa reine sont acquis à l'extérieur de l’exploitation. Il s'agit d'essaim nu (sans cadre donc).
* 2  ARF Achat reine. On achète un reine fécondée livrée en cagette.
* 3  ARV Achat reine vierge. On achète un reine vierge livrée en cagette.
* 4  ENN Essaim naturel. Capture d'un essaim naturel.
* 5  ENB Essaim naturel bâti. Capture d'un essaim naturel installé avec rayons.
* 6  AUR Auto enruchement. Un essaim naturel s'installe spontanément dans une ruche ou ruchette piège.
* 7  EAN Essaim artificiel avec élevage naturel.
* 8  CRD Cellule royale dix jours.
* 9  CR3 Cellule royale  3 jours.
* 10 CR0 Cellule royale  0 jours. Cupule implantée dans un essaim.
* 11 RVN Reine vierge. Une reine vierge née en couveuse est implantée dans l'essaim.
* 12 CRE Cellule de réémergence. Une reine vierge née en couveuse est implantée dans l'essaim dans une cellule de réémergence.
* 13 ESS Essaimage. la reine en place succède à l'essaimage de le ruche. Il s'agit d'un remérage naturel.
*        Cela donne des reines de qualité mais avec avec un caractère essaimeur potentiellement marqué.
* 14 SUP Supercédure. la reine en place succède à la précédente sans le départ d'un essaim.
*        Les deux reines peuvent coexister une courte période. La vieille reine sera expulsée sans le départ d'essaim.
*        Ce caractère génétique (supercédure) est recherché par les apiculteurs
* 15 SAU Sauveté. La reine en place succède à la précédente suite à la mort accidentelle de cette dernière.
*        Reine de qualité souvent médiocre.
*/
public enum ReineOrigine {
	AEC, AEN, ARF, ARV, ENN, ENB, AUR, EAN, CRD, CR3, CR0, RVN, CRE, ESS, SUP, SAU
}
