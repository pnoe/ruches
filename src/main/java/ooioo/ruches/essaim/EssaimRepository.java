package ooioo.ruches.essaim;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ooioo.ruches.IdNom;
import ooioo.ruches.Nom;

@RepositoryRestResource(collectionResourceRel = "essaimRepository")
public interface EssaimRepository extends ListCrudRepository<Essaim, Long> {
	Essaim findByNom(String nom);

	Iterable<Essaim> findBySouche(Essaim essaim);

	List<Essaim> findByActif(boolean actif);

	// Collection -> List
	List<Nom> findAllProjectedBy();

	@Query(value = """
			select essaim
			  from Essaim es, Ruche r
			  where r.essaim = es
			   and r.rucher.id = :rucherId
			""")
	List<Essaim> findByRucherId(Long rucherId);

	// Liste ordonnée par nom d'essaims, des essaims, id et nom de la ruche associée
	// et
	// id et nom de son rucher.
	// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#projections
	@Query(value = """
			select new ooioo.ruches.essaim.EssaimRucheRucher(essaim, ruche.id, ruche.nom, rucher.id, rucher.nom)
			  from Essaim essaim
			    left join Ruche ruche on ruche.essaim.id = essaim.id
			    left join Rucher rucher on ruche.rucher.id = rucher.id
			  where essaim.actif = true order by essaim.nom
			""")
	Iterable<EssaimRucheRucher> findEssaimActifRucheRucherOrderByNom();

	// Liste ordonnée par nom d'essaims, des id et nom de la ruche associée et
	// id et nom de son rucher.
	// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#projections
	@Query(value = """
			select new ooioo.ruches.essaim.EssaimRucheRucher(essaim, ruche.id, ruche.nom, rucher.id, rucher.nom)
			  from Essaim essaim
			    left join Ruche ruche on ruche.essaim.id = essaim.id
			    left join Rucher rucher on ruche.rucher.id = rucher.id
			  order by essaim.nom
			""")
	Iterable<EssaimRucheRucher> findEssaimRucheRucherOrderByNom();

	Iterable<IdNom> findAllProjectedIdNomByOrderByNom();

	// Pour remérage dans formulaire dispersion
	// liste des essaims actifs qui ne sont pas dans des ruches ordonnés par date
	// décroissante
	@Query(value = """
			select new ooioo.ruches.IdNom(essaim.id, essaim.nom)
			  from Essaim essaim
			    left join Ruche ruche on ruche.essaim.id = essaim.id
			  where ruche.essaim.id is null and essaim.actif = true
			  order by essaim.dateAcquisition desc
			""")
	List<IdNom> findProjectedIdNomByRucheIsNullOrderByDateAcquisitionDesc();

	// Liste des essaims actif hors ruche
	@Query(value = """
			select new ooioo.ruches.IdNom(essaim.id, essaim.nom)
			  from Essaim essaim
			  left join Ruche ruche on ruche.essaim = essaim
			  where essaim.actif = true and ruche is null
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
			select essaim
			  from Essaim essaim
			  where essaim.actif = true and essaim.reineDateNaissance > essaim.dateAcquisition
			""")
	Iterable<Essaim> findEssaimDateNaissSupAcquis();

}