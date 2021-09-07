package ooioo.ruches.recolte;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "recolteRepository")
public interface RecolteRepository extends CrudRepository<Recolte, Long> {
	Iterable<Recolte> findAllByOrderByDateAsc();

	Iterable<Recolte> findAllByOrderByDateDesc();

	/*
	 * Renvoie un iterable d'objets : (année, poids de miel mis en pot en kg)
	 */
	/* Inutilisée d'après UCDetector
	@Query(value =
	"select date_part('year', date) as date, sum(poids_miel) as poids from recolte " +
	"group by date_part('year', date) order by date_part('year', date)", nativeQuery = true)
	Iterable<Object[]> findPoidsMielByYear();
	*/

	Recolte findFirstByOrderByDateDesc();

	Recolte findFirstByOrderByDateAsc();

	/*
	 * Renvoie le poids de miel mis en pot en kg pour l'année passée en paramètre
	 */
	@Query(value =
	"select sum(poids_miel) as poids from recolte where date_part('year', date) = ?1", nativeQuery = true)
	Double findPoidsMielByYear(Double year);

	/*
	 * Renvoie le poids de miel récolté dans les hausses pour l'année passée en paramètre
	 */
	@Query(value =
	"select sum(poids_avant) - sum(poids_apres) as p from recolte, recolte_hausse " +
	"where date_part('year', date) = ?1 and recolte_id = recolte.id", nativeQuery = true)
	int findPoidsHaussesByYear(Double year);
}