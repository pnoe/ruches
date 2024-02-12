package ooioo.ruches.ruche;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ooioo.ruches.IdDateNoTime;
import ooioo.ruches.IdNom;
import ooioo.ruches.Nom;
import ooioo.ruches.ruche.type.RucheType;
import ooioo.ruches.rucher.Rucher;

@RepositoryRestResource(collectionResourceRel = "rucheRepository")
public interface RucheRepository extends CrudRepository<Ruche, Long> {

	// Nombre de ruches créées dans l'année passée en paramètre.
	@Query(value = """
			select count(*)
			  from Ruche
			  where date_part('year', dateAcquisition)=:annee
			""")
	Integer countRuchesCreeesDate(int annee);

	/*
	 * Liste des id, dateAcqusition des ruches triées par dateAcquisition.
	 */
	@Query(value = """
			select new ooioo.ruches.IdDateNoTime(id, dateAcquisition)
				from Ruche
				order by dateAcquisition asc
			""")
	List<IdDateNoTime> findByOrderByDateAcquisition();

	/*
	 * Liste des id, dateAcqusition des ruches en production triées par
	 * dateAcquisition.
	 */
	@Query(value = """
			select new ooioo.ruches.IdDateNoTime(id, dateAcquisition)
				from Ruche
				where production = true
				order by dateAcquisition asc
			""")
	List<IdDateNoTime> findByProdOrderByDateAcquisition();

	Ruche findByNom(String nom);

	List<Ruche> findAllByOrderByNom();

	List<Ruche> findByActiveTrueOrderByNom();

	List<Ruche> findByRucherIdOrderByNom(Long id);

	List<RucheIdNomLatLon> findIdLatLonByRucherIdOrderByNom(Long rucherId);

	List<Ruche> findByRucherIdAndActiveTrueOrderByNom(Long id);

	List<Ruche> findByRucherDepotFalseAndActiveFalse();

	Iterable<Ruche> findByRucherIdNotOrderByNom(Long id);

	Ruche findByEssaimId(Long id);

	// Attention à mettre un constructeur dans PoidsNom sinon erreur à l'exécution.
	PoidsNom findPoidsNomByEssaimId(Long id);

	Collection<Ruche> findCollByRucherId(Long rucherId);

	List<Nom> findAllProjectedBy();

	Long countByRucher(Rucher rucher);

	Collection<Nom> findByRucherId(Long id);

	Collection<Nom> findAllByEssaimNull();

	Collection<Nom> findNomsByRucherId(Long id);

	Collection<IdNom> findAllProjectedIdNomByOrderByNom();

	List<Ruche> findByRucherIdOrderById(Long id);

	List<Ruche> findByActiveTrueAndRucherIdOrderById(Long id);

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
			  left join Evenement e on e.ruche = r and e.date > :date
			  where r.active = true and r.rucher.depot = false and e is null
			""")
	Iterable<Ruche> findPasDEvenementAvant(LocalDateTime date);

	// Les ruches actives dont l'id est différent de id triées par nom
	@Query(value = """
			select r
			  from Ruche r
			  where active = true and id <> :id
			  order by nom
			""")
	Iterable<Ruche> findActiveIdDiffOrderByNom(Long id);

	Integer countByTypeAndActiveTrueAndEssaimNotNull(RucheType rt);

	Integer countByTypeAndActiveTrueAndEssaimNull(RucheType rt);

}