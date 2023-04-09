package ooioo.ruches.ruche.type;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ooioo.ruches.ruche.Ruche;

@Repository("rucheTypeRepository")
public interface RucheTypeRepository extends CrudRepository<RucheType, Long> {
}