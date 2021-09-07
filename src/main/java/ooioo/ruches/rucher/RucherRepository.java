package ooioo.ruches.rucher;

import java.util.Collection;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ooioo.ruches.IdNom;
import ooioo.ruches.Nom;

@RepositoryRestResource(collectionResourceRel = "rucherRepository")
public interface RucherRepository extends CrudRepository<Rucher, Long> {

	Rucher findByDepotIsTrue();

	Iterable<Rucher> findAllByOrderByNom();

	Collection<Rucher> findByContactId(Long id);

	Iterable<Rucher> findByActifOrderByNom(boolean actif);

	Collection<Nom> findAllProjectedBy();

	Collection<IdNom> findAllProjectedIdNomByOrderByNom();

	Collection<IdNom> findProjectedIdNomByActifOrderByNom(boolean actif);

	long countByActifTrue();
}