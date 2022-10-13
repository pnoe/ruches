package ooioo.ruches.essaim;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ooioo.ruches.IdNom;
import ooioo.ruches.Nom;

@RepositoryRestResource(collectionResourceRel = "essaimRepository")
public interface EssaimRepository extends CrudRepository<Essaim, Long> {
	Essaim findByNom(String nom);

	Iterable<Essaim> findBySouche(Essaim essaim);

	Iterable<Essaim> findAllByOrderByNom();

	Iterable<Essaim> findByActifOrderByNom(boolean actif);
	Iterable<Essaim> findByActif(boolean actif);

	Collection<Nom> findAllProjectedBy();

	// liste ordonnée par nom d'essaims, des id et nom de la ruche associée
	// en paramètre essaim actif uniquement
	@Query(value = """
			select ruche.id, ruche.nom, rucher.id, rucher.nom
			  from Essaim essaim
			    left join Ruche ruche on ruche.essaim.id = essaim.id
			    left join Rucher rucher on ruche.rucher.id = rucher.id
			  where essaim.actif = ?1 order by essaim.nom
			""")
	Iterable<Object[]> findRucheIdNomByActifOrderByNom(boolean actif);

	// liste ordonnée par nom d'essaims, des id et nom de la ruche associée
	@Query(value = """
			select ruche.id, ruche.nom, rucher.id, rucher.nom
			  from Essaim essaim
			    left join Ruche ruche on ruche.essaim.id = essaim.id
			    left join Rucher rucher on ruche.rucher.id = rucher.id
			  order by essaim.nom
			""")
	Iterable<Object[]> findRucheIdNomOrderByNom();

	Iterable<IdNom> findAllProjectedIdNomByOrderByNom();

	
	// TODO erreur spring boot 3.0.0-M5
	// Validation failed for query for method public abstract java.lang.Iterable 
	//   ooioo.ruches.essaim.EssaimRepository.findProjectedIdNomByRucheIsNullOrderByDateAcquisitionDesc()
	// Pour remérage dans formulaire dispersion
	// liste des essaims actifs qui ne sont pas dans des ruches, ordonnés par date décroissante

	/*
	 TODO A rétablir !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	@Query(value = """
			select essaim.id, essaim.nom
			  from Essaim essaim
			    left join Ruche ruche on ruche.essaim.id = essaim.id
			  where ruche.essaim.id is null and essaim.actif is true
			  order by essaim.dateAcquisition desc
			""")
	Iterable<Object[]> findProjectedIdNomByRucheIsNullOrderByDateAcquisitionDesc();
	*/
	
	@Query(value = """
			select essaim
			  from Essaim essaim
			  where essaim.actif = 'true' and essaim not in
			    (select essaim
			       from Essaim essaim, Ruche ruche
			       where ruche.essaim = essaim)
			""")
	Iterable<Essaim> findEssaimByActifSansRuche();

	@Query(value = """
			select count(*) as nbessaims
			  from Essaim
			  where date_part('year', dateAcquisition)=?1
			""")
	Integer countEssaimsCreesDate(Double date);

	Essaim findFirstByOrderByDateAcquisition();
	Essaim findFirstByOrderByDateAcquisitionDesc();

	@Query(value = """
			select essaim
			  from Essaim essaim
			  where essaim.reineDateNaissance > essaim.dateAcquisition
			""")
	Iterable<Essaim> findEssaimDateNaissSupAcquis();



}