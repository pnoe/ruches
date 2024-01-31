package ooioo.ruches.recolte;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ooioo.ruches.IdNom;
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

	// Trouve le nombre de hausses de récolte incomplètes dans une recolte.
	@Query(value = """
			select count(*)
			    from RecolteHausse r
			    where recolte = :recolte
			      and (rucher is null
			        or ruche is null
			        or essaim is null)
			""")
	Integer countHRecIncompletes(Recolte recolte);

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
			  from RecolteHausse
			  where essaim=:essaim
			""")
	Integer findPoidsMielByEssaim(Essaim essaim);

	// Trouve l'id du rucher de l'essaim présent dans la récolte.
	// Sans order by, limit n'est pas accepté.
	@Query(value = """
			select rucher.id
			  from RecolteHausse
			  where recolte = :recolte
			    and essaim = :essaim
			    and rucher is not null
			  order by id
			  limit 1
			""")
	Long findRucherIdRecolteEssaim(Recolte recolte, Essaim essaim);

	// Trouve les id des ruchers de la récolte.
	@Query(value = """
			select rucher.id
			    from RecolteHausse
			    where recolte = :recolte
			      and rucher is not null
			    group by rucher.id
			""")
	List<Long> findRuchersRecolteEssaim(Recolte recolte);

	// Trouve les idNoms des ruchers de la récolte.
	@Query(value = """
			select new ooioo.ruches.IdNom(rucher.id, rucher.nom)
			    from RecolteHausse
			    where recolte = :recolte
			      and rucher is not null
			    group by rucher.id, rucher.nom
			""")
	List<IdNom> findIdNomsEssaimsRecolte(Recolte recolte);

	// Trouve les id des essaims de la récolte.
	@Query(value = """
			select essaim.id
			    from RecolteHausse
			    where recolte = :recolte
			      and essaim is not null
			    group by essaim.id
			""")
	List<Long> findEssaimsRecolteEssaim(Recolte recolte);

	// Trouve l'id du rucher d'un essaim de la récolte.
	@Query(value = """
			select rucher.id
			    from RecolteHausse
			    where recolte = :recolte
			      and essaim = :essaim
			    order by id
			    limit 1
			""")
	long findRuRRRecolte(Recolte recolte, Essaim essaim);

	// Calcule la moyenne et l'écart type des poids produits par les essaims de la
	// récolte recolteId, dans le rucher rucherId.
	@Query(value = """
			select avg(p), stddev_pop(p) from
			  (select (sum(poidsAvant) - sum(poidsApres)) as p
			    from RecolteHausse
			    where recolte.id = :recolteId and rucher.id = :rucherId
			    group by essaim) as xx
			""")
	List<Double[]> findAvgStdRecolte(Long recolteId, Long rucherId);

	// Calcule la moyenne et l'écart type des poids produits par les essaims de la
	// récolte recolteId, dans le rucher rucherId. Et aussi le nombre d'essaims et
	// le poids total de mieL.
	@Query(value = """
			select avg(p), stddev_pop(p), sum(p), count(p), min(p), max(p) from
			  (select (sum(poidsAvant) - sum(poidsApres)) as p
			    from RecolteHausse
			    where recolte.id = :recolteId and rucher.id = :rucherId
			    group by essaim) as xx
			""")
	List<Object[]> findAvgStdSumNbRecolte(Long recolteId, Long rucherId);

	// Calcule le poids de miel produit par un essaim pour une récolte, ainsi que le
	// nombre de hausses de récoltes.
	@Query(value = """
			  select (sum(poidsAvant) - sum(poidsApres)) as p, count(*) as c
			    from RecolteHausse
			    where recolte.id = :recolteId
			      and essaim.id = :essaimId
			""")
	List<Object[]> findSumNbRecolte(Long recolteId, Long essaimId);

	// Poids de miel produit par un essaim pour une récolte pour un rucher
	@Query(value = """
			select sum(poidsAvant) - sum(poidsApres) as poids
			  from RecolteHausse
			  where essaim.id = :essaimId and recolte.id = :recolteId and rucher.id = :rucherId
			""")
	Integer findPoidsMielEssaimRecolteRucher(Long essaimId, Long recolteId, Long rucherId);

	// Pour chaque essaim d'une récolte recolteId : poids de miel, nom de l'essaim
	@Query(value = """
			select sum(poidsAvant) - sum(poidsApres) as p, essaim.nom
			  from RecolteHausse
			  where recolte.id = :recolteId
			  group by essaim.nom
			  order by p desc
			""")
	List<Object[]> findPoidsMielNomEssaimByRecolte(Long recolteId);

	// Pour chaque essaim d'une récolte recolteId et d'un rucher rucherId : poids de
	// miel, nom de l'essaim
	@Query(value = """
			select sum(poidsAvant) - sum(poidsApres) as p, essaim.nom
			  from RecolteHausse
			  where recolte.id = :recolteId
			    and rucher.id = :rucherId
			  group by essaim.nom
			  order by p desc
			""")
	List<Object[]> findPoidsMielNomEssaimByRecRucher(Long recolteId, Long rucherId);

	@Query(value = """
			select sum(poidsAvant) - sum(poidsApres) as poids
			  from RecolteHausse
			  where rucher.id=?1 and recolte.id=?2
			""")
	Integer findPoidsMielByRucherByRecolte(Long rucherId, Long recolteId);

}