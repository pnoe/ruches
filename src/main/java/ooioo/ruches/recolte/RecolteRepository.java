package ooioo.ruches.recolte;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "recolteRepository")
public interface RecolteRepository extends CrudRepository<Recolte, Long> {
	Iterable<Recolte> findAllByOrderByDateAsc();

	Iterable<Recolte> findAllByOrderByDateDesc();
	
	@Query(value = 
		"select date_part('year', date) as date, sum(poids_miel) as poids from recolte group by date_part('year', date) order by date_part('year', date)", 
		nativeQuery = true)
	Iterable<Object[]> findPoidsMielByYear();
	
	Recolte findFirstByOrderByDateDesc();
	
	
	@Query(value = 
			"select sum(poids_miel) as poids from recolte where date_part('year', date) = ?1", 
			nativeQuery = true)
		Double findPoidsMielByYear(Double year);
}