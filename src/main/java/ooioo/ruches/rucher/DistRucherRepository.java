package ooioo.ruches.rucher;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "distRucherRepository")
public interface DistRucherRepository extends CrudRepository<DistRucher, Long> {

	DistRucher findByRucherStartAndRucherEnd(Rucher r1, Rucher r2);

	@Modifying
	@Query(value = """
			delete
				from DistRucher
				where rucherStart = :rucher
			  		or rucherEnd = :rucher
				""")
	void deleteDists(Rucher rucher);

}