package ooioo.ruches.hausse;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ooioo.ruches.IdNom;
import ooioo.ruches.Nom;

@RepositoryRestResource(collectionResourceRel = "hausseRepository")
public interface HausseRepository extends CrudRepository<Hausse, Long> {

	Optional<Hausse> findByNom(String hausseNom);

	List<Hausse> findByRucheIdOrderByOrdreSurRuche(Long rucheId);

	@Query(value = """
			select h
				from Hausse h
				where h.active = true
				  and (h.ruche is null or h.ruche.id <> ?1)
			""")
	List<Hausse> findHaussesPourAjout(Long rucheId);

	Iterable<Hausse> findAllByOrderByNom();

	Iterable<Hausse> findByActiveOrderByNom(boolean actif);

	Integer countByActiveAndRucheIsNull(boolean active);

	// Liste des hausses qui ne sont pas dans la récolte recolteId :
	// hausses non référencées dans les détails de la récolte recolteId
	// et hausse.ruche not null pour ne pas afficher des hausses qui n'aurait
	// pas de ruche/essaim/rucher associé
	@Query(value = """
			select h
				from Hausse h
				where h.ruche is not null and h.id not in
					(select rh.hausse.id
						from RecolteHausse rh
						where rh.recolte.id = ?1)
			""")
	Iterable<Hausse> findHaussesNotInRecolteId(Long recolteId);

	@Query(value = """
			select h
			  from Hausse h
			  where h.id in
			    (select rh.hausse.id
			      from RecolteHausse rh
			      where rh.recolte.id = ?1)
			""")
	Iterable<Hausse> findHaussesInRecolteId(Long recolteId);

	@Query(value = """
			select rh, h
			  from RecolteHausse rh, Hausse h
			  where rh.recolte.id = :recolteId
			    and rh.hausse.id = h.id
			  order by h.nom
			""")
	List<Object[]> findHaussesRecHausses(Long recolteId);

	@Query(value = """
			select count(h)
			  from Hausse h, Ruche r, Rucher rr
			  where h.ruche.id = r.id and r.rucher.id = rr.id and rr.id = ?1
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