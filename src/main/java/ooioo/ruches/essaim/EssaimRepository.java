package ooioo.ruches.essaim;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ooioo.ruches.IdNom;
import ooioo.ruches.Nom;

@RepositoryRestResource(collectionResourceRel = "essaimRepository")
public interface EssaimRepository extends CrudRepository<Essaim, Long> {
	Essaim findByNom(String nom);

	Iterable<Essaim> findBySouche(Essaim essaim);

	Iterable<Essaim> findByActif(boolean actif);

	Collection<Nom> findAllProjectedBy();

	// Essaims inactifs sans événement dispersion
	@Query(value = """
			select essaim
			  from Essaim essaim
			  where essaim.actif = false
			   and essaim.id not in 
			     (select e.essaim.id 
			       from Evenement e 
			       where type = ooioo.ruches.evenement.TypeEvenement.ESSAIMDISPERSION)
			""")
	List<Essaim> findEssaimInactifPasDipserse();

	/*
	 * and essaim.id not in (select e.essaim.id from evenement e where type =
	 * ooioo.ruches.evenement.TypeEvenement.ESSAIMDISPERSION)
	 */

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
	// spring boot 3 : "is true" remplacé par "= true"
	@Query(value = """
			select essaim.id, essaim.nom
			  from Essaim essaim
			    left join Ruche ruche on ruche.essaim.id = essaim.id
			  where ruche.essaim.id is null and essaim.actif = true
			  order by essaim.dateAcquisition desc
			""")
	Iterable<Object[]> findProjectedIdNomByRucheIsNullOrderByDateAcquisitionDesc();

	@Query(value = """
			select essaim
			  from Essaim essaim
			  where essaim.actif = true and essaim not in
			    (select essaim
			       from Essaim essaim, Ruche ruche
			       where ruche.essaim = essaim)
			""")
	Iterable<Essaim> findEssaimByActifSansRuche();

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