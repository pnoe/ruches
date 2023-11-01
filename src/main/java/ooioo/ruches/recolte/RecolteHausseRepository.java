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

	// List<RecolteHausse> findByRucheId(Long rucheId);

	List<RecolteHausse> findByHausseId(Long hausseId);

	// List<RecolteHausse> findByEssaimId(Long essaimId);

	List<RecolteHausse> findByRucherId(Long rucherId);

	@Query(value = """
			select sum(poidsAvant) - sum(poidsApres) as poids
			  from RecolteHausse
			  where essaim.id=?1 and recolte.id=?2
			""")
	Integer findPoidsMielByEssaimByRecolte(Long essaimId, Long recolteId);

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
	Iterable<Object[]> findPoidsMielNomEssaimByRecolte(Long recolteId);

	@Query(value = """
			select sum(poidsAvant) - sum(poidsApres) as poids
			  from RecolteHausse
			  where rucher.id=?1 and recolte.id=?2
			""")
	Integer findPoidsMielByRucherByRecolte(Long rucherId, Long recolteId);

}