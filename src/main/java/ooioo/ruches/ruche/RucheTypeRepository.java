package ooioo.ruches.ruche;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("rucheTypeRepository")
public interface RucheTypeRepository extends CrudRepository<RucheType, Long> {
	// pas d'autre méthodes que celles de CrudRepository
}