package ooioo.ruches.recolte;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.hausse.Hausse;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.rucher.Rucher;

@Repository("recolteHausseRepository")
public interface RecolteHausseRepository extends CrudRepository<RecolteHausse, Long> {

	RecolteHausse findByRecolteAndHausse(Recolte recolte, Hausse hausse);

	RecolteHausse findFirstByRecolteAndEssaim(Recolte recolte, Essaim essaim);

	List<RecolteHausse> findByRecolte(Recolte recolte);

	List<RecolteHausse> findByRecolteOrderByHausseNom(Recolte recolte);

	@Query(value = """
			select count(distinct ruche) as nbruches
			  from RecolteHausse
			  where recolte.id=?1 and rucher.id=?2
			""")
	Integer countRucheByRecolteByRucher(Long recolteId, Long rucherId);

	boolean existsByRucher(Rucher rucher);

	boolean existsByRuche(Ruche ruche);

	boolean existsByEssaim(Essaim essaim);

	boolean existsByHausse(Hausse hausse);

	// Poids de miel produit par un essaim pour une récolte
	@Query(value = """
			select sum(poidsAvant) - sum(poidsApres) as poids
			  from RecolteHausse
			  where essaim.id=:essaimId and recolte.id=:recolteId
			""")
	Integer findPoidsMielByEssaimByRecolte(Long essaimId, Long recolteId);

	// Poids de miel produit par un essaim pour toutes les récoltes
	@Query(value = """
			select sum(poidsAvant) - sum(poidsApres) as poids
			  from RecolteHausse as rh
			  where rh.essaim=:essaim
			""")
	Integer findPoidsMielByEssaim(Essaim essaim);

	// Trouve l'id du rucher de l'essaim présent dans la récolte.
	// Sans order by, limit n'est pas accepté.
	@Query(value = """
			select rh.rucher.id
			  from RecolteHausse rh
			  where recolte = :recolte
			    and essaim = :essaim
			    and rucher is not null
			  order by id
			  limit 1
			""")
	Long findRucherIdRecolteEssaim(Recolte recolte, Essaim essaim);
	

	// Trouve les id des rucher de la récolte.
	@Query(value = """
			select rh.rucher.id
			    from RecolteHausse rh
			    where recolte = :recolte
			      and rucher is not null
			    group by rucher.id
			""")
	List<Long> findRuchersRecolteEssaim(Recolte recolte);

	// Trouve les id des essaims de la récolte.
	@Query(value = """
			select rh.essaim.id
			    from RecolteHausse rh
			    where recolte = :recolte
			      and essaim is not null
			    group by essaim.id
			""")
	List<Long> findEssaimsRecolteEssaim(Recolte recolte);

	// Trouve l'id du rucher d'un essaim de la récolte.
	@Query(value = """
			select rh.rucher.id
			    from RecolteHausse rh
			    where recolte = :recolte
			      and essaim = :essaim
			    order by id
			    limit 1
			""")
	long findRuRRRecolte(Recolte recolte, Essaim essaim);
	
	
	// Trouve les id et noms des essaims de la récolte.
	// Attention, les IdNoms ne sont pas distints !!!!!!!!!!!!!!!!!!!!
	// Pas réussi à metttre distinct dans la requête, même avec sous
	// requête et/ou nativeQuery.
	/*
	@Query(value = """
			select new ooioo.ruches.IdNom(rh.essaim.id, rh.essaim.nom)
			    from RecolteHausse rh
			    where recolte = :recolte
			      and essaim is not null
			""")
	List<IdNom> findEssaimssRecolteEssaim(Recolte recolte);
	*/
	
	// La liste des id essaim, ruche, rucher de la récolte
	// Pas distincts !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	@Query(value = """
			select rh.essaim.id, rh.ruche.id, rh.rucher.id 
			    from RecolteHausse rh
			    where recolte = :recolte
			      and essaim is not null
			      and ruche is not null
			      and rucher is not null
			""")
	List<Long[]> findEsRuRRRecolteEssaim(Recolte recolte);
	
	
	
	
	/*
	@Query(value = """
			  select new ooioo.ruches.IdNom(r.id as id, r.nom as nom)
			    from recolte_hausse as rh, rucher as r
			    where rh.recolte_id = :recolte_id
			      and rh.rucher_id = r.id
			      and rh.rucher_id is not null
			""", nativeQuery = true)
	List<IdNom> findRuchersRecolteEssaim(Long recolte_id);
	*/
	
	
	
	/*
	@Query(value = """
			select sum(poidsAvant) - sum(poidsApres) as poids
			  from RecolteHausse
			  where recolte.id = :recolteId and rucher.id = :rucherId
			""")
	Integer findPTRecolte(Long recolteId, Long rucherId);
	*/
	
	// Compte le nombre d'essaims ayant participé à la récolte recolteId pour le
	// rucher rucherId
	/*
	@Query(value = """
			select count(distinct essaim)
			  from RecolteHausse
			  where recolte.id = :recolteId and rucher.id = :rucherId
			""")
	Integer findNbERecolte(Long recolteId, Long rucherId);
	*/

	// Calcule la moyenne et l'écart type des poids produits par les essaims de la
	// récolte recolteId, dans le rucher rucherId.
	@Query(value = """
			select avg(p), stddev_pop(p) from
			  (select (sum(poidsAvant) - sum(poidsApres)) as p
			    from RecolteHausse
			    where recolte.id = :recolteId and rucher.id = :rucherId
			    group by essaim) as xx
			""")
	List<Float[]> findAvgStdRecolte(Long recolteId, Long rucherId);
	
	// Calcule la moyenne et l'écart type des poids produits par les essaims de la
	// récolte recolteId, dans le rucher rucherId. Et aussi le nombre d'essaims et
	// le poids total de mieL.
	// select avg(p), stddev_pop(p), sum(p), count(*) from (select (sum(poids_avant) - sum(poids_apres)) as p from
    //  recolte_hausse where recolte_id = 17777 and rucher_id = 2621 group by essaim_id;) as xx;
	@Query(value = """
			select avg(p), stddev_pop(p), sum(p), count(p) from
			  (select (sum(poidsAvant) - sum(poidsApres)) as p
			    from RecolteHausse
			    where recolte.id = :recolteId and rucher.id = :rucherId
			    group by essaim) as xx
			""")
	List<Object[]> findAvgStdSumNbRecolte(Long recolteId, Long rucherId);
		
	// Poids de miel produit pour une récolte, dans le rucher passé en paramètre.
	/*
	@Query(value = """
			select sum(poidsAvant) - sum(poidsApres) as poids
			  from RecolteHausse
			  where recolte.id = :recolteId and rucher.id = :rucherId
			""")
	Integer findPdsRecolteRucher(Long recolteId, Long rucherId);
	*/
	
	// Poids de miel produit par un essaim pour une récolte pour un rucher
	@Query(value = """
			select sum(poidsAvant) - sum(poidsApres) as poids
			  from RecolteHausse
			  where essaim.id = :essaimId and recolte.id = :recolteId and rucher.id = :rucherId
			""")
	Integer findPoidsMielEssaimRecolteRucher(Long essaimId, Long recolteId, Long rucherId);

	@Query(value = """
			select sum(poids_avant) - sum(poids_apres) as p, e.nom
			  from recolte_hausse as r, essaim as e
			  where e.id=r.essaim_id and r.recolte_id=?1
			  group by r.essaim_id, e.nom
			  order by p desc
			""", nativeQuery = true)
	List<Object[]> findPoidsMielNomEssaimByRecolte(Long recolteId);

	@Query(value = """
			select sum(poidsAvant) - sum(poidsApres) as poids
			  from RecolteHausse
			  where rucher.id=?1 and recolte.id=?2
			""")
	Integer findPoidsMielByRucherByRecolte(Long rucherId, Long recolteId);

}