package ooioo.ruches.essaim;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ooioo.ruches.IdDate;
import ooioo.ruches.IdDateNoTime;
import ooioo.ruches.IdNom;
import ooioo.ruches.Nom;

@RepositoryRestResource(collectionResourceRel = "essaimRepository")
public interface EssaimRepository extends ListCrudRepository<Essaim, Long> {
	Essaim findByNom(String nom);

	Iterable<Essaim> findBySouche(Essaim essaim);

	List<Essaim> findByActif(boolean actif);

	List<Nom> findAllProjectedBy();

	List<IdNom> findAllProjectedIdNomBy();

	/*
	 * Liste des id, dateAcqusition des essaims triés par dateAcquisition.
	 */
	@Query(value = """
			select new ooioo.ruches.IdDateNoTime(id, dateAcquisition)
				from Essaim
				order by dateAcquisition asc
			""")
	List<IdDateNoTime> findByOrderByDateAcquisition();

	/*
	 * Liste id, dateDispersion des essaims triés par dateDispersion.
	 */
	@Query(value = """
			select new ooioo.ruches.IdDate(id, dateDispersion)
				from Essaim
				where actif = false
				order by dateDispersion asc
			""")
	List<IdDate> findByOrderByDateDispersion();

	@Query(value = """
			select new ooioo.ruches.IdNom(e.id, e.nom)
			  from Essaim e, Ruche r
			  where r.essaim = e
			   and r.rucher.id = :rucherId
			  order by e.nom
			""")
	List<IdNom> findIdNomByRucherId(Long rucherId);

	@Query(value = """
			select new ooioo.ruches.essaim.EssaimIdNomReine(e.id, e.nom, e.reineDateNaissance, e.reineMarquee)
			  from Essaim e, Ruche r
			  where r.rucher.id = :rucherId
			    and r.essaim = e
			    and r.id = :rucheId
			""")
	EssaimIdNomReine findIdNomReine(Long rucherId, Long rucheId);

	// Liste ordonnée par nom d'essaims, des essaims, id et nom de la ruche associée
	// et id et nom de son rucher.
	// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#projections
	@Query(value = """
			select new ooioo.ruches.essaim.EssaimRucheRucher(e, r.id, r.nom, rr.id, rr.nom)
			  from Essaim e
			    left join Ruche r on r.essaim.id = e.id
			    left join Rucher rr on r.rucher.id = rr.id
			  where e.actif = true
			  order by e.nom
			""")
	List<EssaimRucheRucher> findEssaimActifRucheRucherOrderByNom();

	// Liste ordonnée par nom d'essaims, des id et nom de la ruche associée et
	// id et nom de son rucher.
	// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#projections
	@Query(value = """
			select new ooioo.ruches.essaim.EssaimRucheRucher(e, r.id, r.nom, rr.id, rr.nom)
			  from Essaim e
			    left join Ruche r on r.essaim.id = e.id
			    left join Rucher rr on r.rucher.id = rr.id
			  order by e.nom
			""")
	List<EssaimRucheRucher> findEssaimRucheRucherOrderByNom();

	Iterable<IdNom> findAllProjectedIdNomByOrderByNom();

	// Pour remérage dans formulaire dispersion
	// liste des essaims actifs qui ne sont pas dans des ruches, ordonnés par date
	// décroissante.
	@Query(value = """
			select new ooioo.ruches.IdNom(e.id, e.nom)
			  from Essaim e
			    left join Ruche r on r.essaim = e
			  where r is null and e.actif = true
			  order by e.dateAcquisition desc
			""")
	List<IdNom> findProjectedIdNomByRucheIsNullOrderByDateAcquisitionDesc();

	// Liste des essaims actif hors ruche.
	@Query(value = """
			select new ooioo.ruches.IdNom(e.id, e.nom)
			  from Essaim e
			  left join Ruche r on r.essaim = e
			  where r is null and e.actif = true
				""")
	List<IdNom> findEssaimByActifSansRuche();

	// Nombre d'essaims créés dans l'année passée en paramètre.
	@Query(value = """
			select count(*) as nbessaims
			  from Essaim
			  where date_part('year', dateAcquisition)=?1
			""")
	Integer countEssaimsCreesDate(int date);

	Essaim findFirstByOrderByDateAcquisition();

	Essaim findFirstByOrderByDateAcquisitionDesc();

	@Query(value = """
			select e
			  from Essaim e
			  where actif = true and reineDateNaissance > dateAcquisition
			""")
	Iterable<Essaim> findEssaimDateNaissSupAcquis();

}