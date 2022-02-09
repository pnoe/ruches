package ooioo.ruches.recolte;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "recolteRepository")
public interface RecolteRepository extends CrudRepository<Recolte, Long> {
	Iterable<Recolte> findAllByOrderByDateAsc();

	Iterable<Recolte> findAllByOrderByDateDesc();

	Recolte findFirstByOrderByDateDesc();

	Recolte findFirstByOrderByDateAsc();
	
	/*
	 * Renvoie le poids de miel mis en pot en kg pour l'année passée en paramètre
	 * Si aucune ligne trouvée (année sans récolte) Optional permet de traiter le 
	 *   retour null
	 */
	@Query(value = """
	  select sum(poids_miel) as poids 
	  from recolte 
	  where date_part('year', date) = ?1
			""", nativeQuery = true)
	Optional<Double> findPoidsMielByYear(Double year);

	/*
	 * Renvoie le poids de miel récolté dans les hausses pour l'année passée en paramètre
	 * Si aucune ligne trouvée (année sans récolte) Optional permet de traiter le 
	 *   retour null
	 */
	@Query(value = """
	  select sum(rh.poids_avant) - sum(rh.poids_apres) as p 
	  from recolte r , recolte_hausse rh
	  where date_part('year', r.date) = ?1 and rh.recolte_id = r.id
			""", nativeQuery = true)
	Optional<Integer> findPoidsHaussesByYear(Double year);
}