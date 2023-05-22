package ooioo.ruches.ruche;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ooioo.ruches.IdNom;
import ooioo.ruches.Nom;
import ooioo.ruches.ruche.type.RucheType;
import ooioo.ruches.rucher.Rucher;

@RepositoryRestResource(collectionResourceRel = "rucheRepository")
public interface RucheRepository extends CrudRepository<Ruche, Long> {
	Ruche findByNom(String nom);

	Iterable<Ruche> findAllByOrderByNom();

	Iterable<Ruche> findByActiveTrueOrderByNom();

	Iterable<Ruche> findByRucherIdOrderByNom(Long id);

	List<Ruche> findByRucherIdAndActiveTrueOrderByNom(Long id);
	
	Iterable<Ruche> findByRucherIdNotOrderByNom(Long id);

	Ruche findByEssaimId(Long id);

	Collection<Ruche> findCollByRucherId(Long rucherId);

	Collection<Nom> findAllProjectedBy();

	Long countByRucher(Rucher rucher);

	Collection<Nom> findByRucherId(Long id);

	Collection<Nom> findAllByEssaimNull();

	Collection<Ruche> findCompletByRucherId(Long id);

	Collection<Nom> findNomsByRucherId(Long id);

	Collection<IdNom> findAllProjectedIdNomByOrderByNom();

	Iterable<Ruche> findByRucherIdOrderById(Long id);
	Iterable<Ruche> findByActiveTrueAndRucherIdOrderById(Long id);

	List<Ruche> findByTypeIdOrderByNom(Long id);
	long countByTypeIdOrderByNom(Long id);
	List<Ruche> findByActiveTrueAndTypeIdOrderByNom(Long id);
	
	long countByActiveTrue();
	
	long countByActiveTrueAndProductionTrue();

	long countByEssaimNotNullAndActiveTrue();

	long countByActiveTrueAndRucherDepotTrue();

	long countByActiveTrueAndRucherDepotTrueAndProductionTrue();
	
	long countByEssaimNotNullAndActiveTrueAndProductionTrue();

	// Les ruches actives au dépôt qui ont un essaim
	Iterable<Ruche> findByActiveTrueAndEssaimNotNullAndRucherDepotTrue();

	// Iterable<Ruche> findByEssaimNotNullAndRucherDepotTrueAndProductionTrue();

	// Les ruches actives au dépôt qui ont au moins une hausse.
	@Query(value = """
			select r
			  from Ruche r, Hausse h
			  where r.active = true and r.rucher.depot = true and h.ruche = r
			""")
	Iterable<Ruche> findByHaussesAndDepot();

	// Les ruches actives qui ne sont pas au dépôt et qui n'ont pas d'essaim
	Iterable<Ruche> findByActiveTrueAndEssaimNullAndRucherDepotFalse();

	// Les ruches actives et pas au dépôt qui n'ont pas eu d'événement depuis "date"
	@Query(value = """
			select r
			  from Ruche r
			  where r.active = true and r.rucher.depot = false
			    and r not in
			      (select distinct r
			        from Ruche r, Evenement e
			        where e.ruche = r and e.date > ?1)
			""")
	Iterable<Ruche> findPasDEvenementAvant(LocalDateTime date);

	// Les ruches actives dont l'id est différent de id triées par nom
	@Query(value = """
			select r
			  from Ruche r
			  where r.active = true and r.id != ?1
			  order by r.nom
			""")
	Iterable<Ruche> findActiveIdDiffOrderByNom(Long id);
	
	Integer countByTypeAndActiveTrue(RucheType rt);
	
}