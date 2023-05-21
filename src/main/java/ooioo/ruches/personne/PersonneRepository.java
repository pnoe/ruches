package ooioo.ruches.personne;

import java.util.Collection;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "personneRepository")
public interface PersonneRepository extends CrudRepository<Personne, Long> {
	Iterable<Personne> findAllByOrderByNom();

	Iterable<Personne> findByActiveTrueOrderByNom();

	Personne findByLogin(String login);

	Personne findByEmail(String email);

	Personne findByToken(String token);

	Collection<PersonneLogin> findAllProjectedBy();

	Collection<PersonneEmail> findAllProjectedEmailBy();

}