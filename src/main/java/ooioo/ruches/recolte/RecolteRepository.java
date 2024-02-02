package ooioo.ruches.recolte;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ooioo.ruches.IdDate;

@RepositoryRestResource(collectionResourceRel = "recolteRepository")
public interface RecolteRepository extends ListCrudRepository<Recolte, Long> {
	List<Recolte> findAllByOrderByDateAsc();

	List<IdDate> findAllIdDateByOrderByDateAsc();

	List<Recolte> findAllByOrderByDateDesc();

	Recolte findFirstByOrderByDateDesc();

	Recolte findFirstByOrderByDateAsc();
	
	/*
	 * Renvoie pour la liste des récoltes, le nombre de hausses de la récolte et la
	 * date de la récolte dans le Record IdDate.
	 */
	@Query(value = """
			select new ooioo.ruches.IdDate(count(rh), date)
			  from Recolte r
			  left join RecolteHausse rh on r.id = rh.recolte.id
			  group by r.id
			  order by date asc
			""")
	List<IdDate> recoltesNbHaussesDate();

	/*
	 * Renvoie le poids de miel mis en pot en kg pour l'année passée en paramètre Si
	 * aucune ligne trouvée (année sans récolte) Optional permet de traiter le
	 * retour null
	 */
	@Query(value = """
			select sum(poids_miel)
				from recolte
				where date_part('year', date) = :year
			""", nativeQuery = true)
	Optional<Double> findPoidsMielByYear(int year);
	
	/*
	 * Renvoie le nombre de récoltes pour l'année passée en paramètre Si
	 * aucune ligne trouvée (année sans récolte) Optional permet de traiter le
	 * retour null
	 */
	@Query(value = """
			select count(*)
				from recolte
				where date_part('year', date) = :year
			""", nativeQuery = true)
	Optional<Integer> findRecoltesByYear(int year);

	/*
	 * Renvoie le poids de miel récolté dans les hausses pour l'année passée en
	 * paramètre Si aucune ligne trouvée (année sans récolte) Optional permet de
	 * traiter le retour null
	 */
	@Query(value = """
			select sum(rh.poids_avant) - sum(rh.poids_apres)
				from recolte r , recolte_hausse rh
				where date_part('year', r.date) = :year and rh.recolte_id = r.id
			""", nativeQuery = true)
	Optional<Integer> findPoidsHaussesByYear(Double year);
}