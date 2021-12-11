package ooioo.ruches.evenement;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.hausse.Hausse;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.rucher.Rucher;

@RepositoryRestResource(collectionResourceRel = "evenementRepository")
public interface EvenementRepository extends CrudRepository<Evenement, Long> {

	// Recherche de l'événement "ajout ruche" précédent un "ajout ruche"
	//   pour une ruche donnée en paramètre (pour historique rucher)
	// pas de limit 1 en jpql : utiliser Pageable et appel avec PageRequest.of(0,1)
	/*
	@Query(value = """
			select evenement from Evenement evenement
			where evenement.type=ooioo.ruches.evenement.TypeEvenement.RUCHEAJOUTRUCHER
				and evenement.ruche.id = ?1
				and evenement.date < ?2
			order by evenement.date desc
			"""
			)
	List<Evenement> findAjoutRuchePrec(Long rucheId, LocalDateTime date, Pageable pageable);
	*/
	
	// liste de la date, nom ruche, nom rucher des événements ajout ruche
	//    dans le rucher rucherId
	//    suivi du nom du rucher d'où provient la ruche
	/*
	@Query(value =
			"""
				select e.date, r.nom, rin.nom as rucherin,
			(select rout.nom as rucherout
			  from evenement as ee, rucher as rout
			  where rout.id = ?1
			  and ee.type = 0 and ee.ruche_id = e.ruche_id and ee.date < e.date
			  order by date desc limit 1)
			from evenement as e, rucher as rin, ruche as r
			  where e.rucher_id = ?1 and e.type = 0 
			    and rin.id = ?1 and r.id = e.ruche_id
			  order by e.date desc; 
			  """
			, nativeQuery = true)
	Iterable<Object[]> findAjoutRucher(Long rucherId);
	*/
	
	// liste des paires d'événements 
	//     "Ajout Ruche"
	//       et l'événement précédent "Ajout Ruche" de la même ruche  
	/*
	@Query(value = """
			select e.*,
				(select ee.id
				  from evenement as ee
				    where ee.type = 0 
				      and ee.ruche_id = e.ruche_id
				      and ee.date < e.date
				    order by date desc limit 1) as idprec
				from evenement as e
				  where e.type = 0
				  order by e.date desc; 
			  """
			, nativeQuery = true)
	Iterable<Object[]> findAjRucheEtPrec();
	*/
	
	// liste des paires d'événements 
	//     "Ajout Ruche"
	//       et l'événement précédent "Ajout Ruche" de la même ruche
	//   si l'un des deux est un ajout dans le rucher rucherId
	//   Il lister les champs xx.* pour ne pas avoir xx.idprec
	/*
	@Query(value = """
			select 
				
				xx.id as id1, xx.date as date1, xx.rucher_id as rucher_id1,
				eve2.id as id2, eve2.date as date2, eve2.rucher_id as rucher_id2,
				r.id as rucheid, r.nom as ruchenom
			    
			  from evenement as eve2, ruche as r,
			    (select e.*,
						(select ee.id
						  from evenement as ee
						    where ee.type = 0 
						      and ee.ruche_id = e.ruche_id
						      and ee.date < e.date
						    order by date desc limit 1) as idprec
						from evenement as e
						  where e.type = 0
						  order by e.date desc ) as xx
			  where 
				    eve2.id = xx.idprec
 					 and ( xx.rucher_id = ?1
				  	   or eve2.rucher_id = ?1)
				  	 and r.id = xx.ruche_id  
			  order by xx.date desc;
			"""
			, nativeQuery = true)
	Iterable<Object[]> findAjRucheEtPrec(Long rucherId);
	*/
	
	@Query(value = "select evenement from Evenement evenement where evenement.date > ?1")
	Iterable<Evenement> findPeriode(LocalDateTime date);

	@Query(value = "select evenement from Evenement evenement where evenement.date > ?1 and evenement.date < ?2")
	Iterable<Evenement> findPeriode(LocalDateTime date1, LocalDateTime date2);

	@Query(value = "select evenement from Evenement evenement where evenement.type = ?1 and evenement.date > ?2")
	Iterable<Evenement> findTypePeriode(TypeEvenement typeEvenement, LocalDateTime date);

	@Query(value = "select evenement from Evenement evenement where evenement.type = ?1 and evenement.date > ?2 and evenement.date < ?3")
	Iterable<Evenement> findTypePeriode(TypeEvenement typeEvenement, LocalDateTime date1, LocalDateTime date2);



	Iterable<Evenement> findByRucheId(Long rucheId);

	Iterable<Evenement> findByEssaimId(Long essaimId);
	
	
	Iterable<Evenement> findByEssaimIdAndTypeOrderByDateAsc(Long essaimId, TypeEvenement typeEvenement);
	

	Iterable<Evenement> findByHausseId(Long hausseId);

	Iterable<Evenement> findByRucherId(Long rucherId);

	List<Evenement> findByTypeOrderByDateDesc(TypeEvenement typeEvenement);

	Iterable<Evenement> findByTypeOrderByDateAsc(TypeEvenement typeEvenement);

	Evenement findFirstByEssaimAndType(Essaim essaim, TypeEvenement typeEvenement);

	Evenement findFirstByRucheAndTypeOrderByDateDesc(Ruche ruche, TypeEvenement typeEvenement);

	Evenement findFirstByHausseAndTypeOrderByDateDesc(Hausse hausse, TypeEvenement typeEvenement);

	Evenement findFirstByEssaimAndTypeOrderByDateDesc(Essaim essaim, TypeEvenement typeEvenement);

	Iterable<Evenement> findFirst3ByEssaimAndTypeOrderByDateDesc(Essaim essaim, TypeEvenement typeEvenement);

	//	@Query(value = "select evenement from Evenement evenement where evenement.essaim.id = ?1 and "
	//	+ "((evenement.typeEvenement = ooioo.ruches.evenement.TypeEvenement.ESSAIMTRAITEMENT) " +
	//	"or (evenement.typeEvenement = ooioo.ruches.evenement.TypeEvenement.ESSAIMTRAITEMENTFIN)) order by evenement.date desc")
	//    pas de limit en jpql, utiliser Pageable
	@Query(value = "select * from evenement where essaim_id = ?1 and ((type = ?2) " +
			"or (type = ?3)) order by date desc limit 1", nativeQuery = true)
	Evenement findFirstTraitemenetByEssaim(Long essaimId, int t1, int t2);

	Evenement findFirstByRucheAndHausseAndTypeOrderByDateDesc(Ruche ruche, Hausse hausse,
			TypeEvenement typeEvenement);

	Evenement findFirstByRucheAndRucherAndTypeOrderByDateDesc(Ruche ruche, Rucher rucher,
			TypeEvenement typeEvenement);

	Evenement findFirstByRucheAndHausseInAndTypeOrderByDateDesc(Ruche ruche, List<Hausse> hausse, TypeEvenement typeEvenement);

	@Query(value = "select count(*) as nbeven from Evenement where type=ooioo.ruches.evenement.TypeEvenement.ESSAIMDISPERSION and date_part('year', date)=?1")
	Integer countDispersionEssaimParAnnee(Double date);

	@Query(value = "select sum(cast(valeur as double)) as sucre from Evenement where type=ooioo.ruches.evenement.TypeEvenement.ESSAIMSUCRE and date_part('year', date)=?1")
	Double sucreEssaimParAnnee(Double date);

	@Query(value = "select count(*) as nbtraitements from Evenement where type=ooioo.ruches.evenement.TypeEvenement.ESSAIMTRAITEMENT and date_part('year', date)=?1")
	Integer countTraitementsEssaimParAnnee(Double date);

}