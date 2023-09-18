package ooioo.ruches.ruche.type;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("rucheTypeRepository")
public interface RucheTypeRepository extends CrudRepository<RucheType, Long> {
	List<RucheType> findAllByOrderByNom();
	
	RucheType findByNom(String nom);
	
}