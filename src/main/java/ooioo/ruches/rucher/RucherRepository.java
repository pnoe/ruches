package ooioo.ruches.rucher;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ooioo.ruches.IdNom;
import ooioo.ruches.Nom;

@RepositoryRestResource(collectionResourceRel = "rucherRepository")
public interface RucherRepository extends CrudRepository<Rucher, Long> {

	Rucher findByDepotTrue();

	Rucher findByNom(String nom);

	Optional<RucherMap> findRucherMapById(Long rucherId);

	List<Rucher> findAllByOrderByNom();

	Collection<Rucher> findByContactId(Long id);

	List<Rucher> findByActifOrderByNom(boolean actif);

	List<RucherMap> findRucherMapBy();

	List<RucherMap> findRucherMapByActifTrue();

	List<Rucher> findByActifTrue();

	List<Nom> findAllProjectedBy();

	Collection<IdNom> findAllProjectedIdNomByOrderByNom();

	// Liste des ruchers actifs différents de ruherId, triés par nom.
	Collection<IdNom> findIdNomByActifTrueAndIdNotOrderByNom(Long rucherId);

	long countByActifTrue();
}