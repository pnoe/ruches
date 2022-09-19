package ooioo.ruches.rucher;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "distRucherRepository")
public interface DistRucherRepository extends CrudRepository<DistRucher, Long> {

	 DistRucher findByRucherStartAndRucherEnd(Rucher r1, Rucher r2);

}