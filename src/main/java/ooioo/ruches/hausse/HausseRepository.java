package ooioo.ruches.hausse;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ooioo.ruches.IdDateNoTime;
import ooioo.ruches.IdNom;
import ooioo.ruches.Nom;

@RepositoryRestResource(collectionResourceRel = "hausseRepository")
public interface HausseRepository extends CrudRepository<Hausse, Long> {

	// Nombre de hausses créées dans l'année passée en paramètre.
	@Query(value = """
			select count(*)
			  from Hausse
			  where date_part('year', dateAcquisition)=:annee
			""")
	Integer countHaussesCreeesDate(int annee);

	Optional<Hausse> findByNom(String hausseNom);

	List<Hausse> findByRucheIdOrderByOrdreSurRuche(Long rucheId);

	@Query(value = """
			select new ooioo.ruches.IdDateNoTime(id, dateAcquisition)
				from Hausse
				order by dateAcquisition asc
			""")
	List<IdDateNoTime> findByOrderByDateAcquisition();

	@Query(value = """
			select h
				from Hausse h
				where active = true
				  and (ruche is null or ruche.id <> :rucheId)
			""")
	List<Hausse> findHaussesPourAjout(Long rucheId);

	Iterable<Hausse> findAllByOrderByNom();

	Iterable<Hausse> findByActiveOrderByNom(boolean actif);

	Integer countByActiveAndRucheIsNull(boolean active);

	// Liste des hausses qui ne sont pas dans la récolte recolteId :
	// hausse.ruche not null pour ne pas afficher des hausses qui n'aurait
	// pas de ruche/essaim/rucher associé
	// Ce select n'est pas remplaçable par un left join.
	@Query(value = """
			select h
				from Hausse h
				where ruche is not null
					and id not in
					(select hausse.id
						from RecolteHausse
						where recolte.id = :recolteId)
			""")
	Iterable<Hausse> findHaussesNotInRecolteId(Long recolteId);

	// Liste des hausses qui ne sont pas dans la récolte recolteId :
	@Query(value = """
			select h
				from Hausse h
				where id not in
					(select hausse.id
						from RecolteHausse
						where recolte.id = :recolteId)
			""")
	Iterable<Hausse> findTtHNotInRecolteId(Long recolteId);

	@Query(value = """
			select count(*)
			  from Hausse
			  where ruche.rucher.id = :rucherId
			""")
	Integer countHausseInRucher(Long rucherId);

	Integer countByRucheId(Long id);

	List<Nom> findAllProjectedBy();

	Collection<Nom> findByRucheId(Long rucherId);

	Collection<Hausse> findCompletByRucheId(Long rucherId);

	Collection<IdNom> findAllProjectedIdNomByOrderByNom();

	long countByActiveTrue();

	long countByActiveTrueAndRucheNotNullAndRucheActiveTrueAndRucheEssaimNotNull();

	long countByActiveTrueAndRucheNull();

}