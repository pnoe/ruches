package ooioo.ruches.evenement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import ooioo.ruches.IdDate;
import ooioo.ruches.TypeDate;
import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.hausse.Hausse;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.rucher.Rucher;

@RepositoryRestResource(collectionResourceRel = "evenementRepository")
public interface EvenementRepository extends CrudRepository<Evenement, Long> {

	boolean existsByRucher(Rucher rucher);

	Long countByRuche(Ruche ruche);

	boolean existsByEssaim(Essaim essaim);

	boolean existsByHausse(Hausse hausse);

	Evenement findFirstByOrderByDateDesc();

	/**
	 * Les événements notifications : événements Commentaire avec le champ valeur
	 * renseigné.
	 */
	@Query(value = """
			select e
			  from Evenement e
			  where
			    e.valeur <> ''
			    and (e.type = ooioo.ruches.evenement.TypeEvenement.COMMENTAIREESSAIM
			    or e.type = ooioo.ruches.evenement.TypeEvenement.COMMENTAIREHAUSSE
			    or e.type = ooioo.ruches.evenement.TypeEvenement.COMMENTAIRERUCHE
			    or e.type = ooioo.ruches.evenement.TypeEvenement.COMMENTAIRERUCHER)
			  order by e.date desc
			""")
	List<Evenement> findNotification();

	
	
	
	
	// select h.id, h.nom, h.date_acquisition, max(e.date) 
	//   from hausse h 
	//   left join evenement e 
	//     on h.id = e.hausse_id 
	//   group by h.id; 
	// Trouve le dernier événement de chaque hausse incactive.
	// Utilisé pour calculer le nombre total de hausse pour la courbe des hausses posées.
	// Problème : il n'y a qu'une hausse inactive, c'est louche !
	
	 // No property 'date' found for type 'Hausse'
	// new ooioo.ruches.IdDate(
	 
	@Query(value = """
			select new ooioo.ruches.IdDate(null as id, max(e.date) as date)
			  from Hausse h, Evenement e
			  where
			    h.active = false and 
			    h.id = e.hausse.id
			  group by h.id
			  order by max(e.date) asc 
			""")
	List<IdDate> findHausseInacLastEve();

	
	
	
	
	
	@Query(value = """
			select e
			  from Evenement e
			  where e.date > :date
			  """)
	Iterable<Evenement> findPeriode(LocalDateTime date);

//	https://docs.spring.io/spring-data/rest/docs/current/reference/html/#customizing-sdr.configuring-the-rest-url-path
	@RestResource(path = "findPeriodeDate1Date2")
	@Query(value = """
			select e
			  from Evenement e
			  where e.date > :date1 and e.date < :date2
			""")
	Iterable<Evenement> findPeriode(LocalDateTime date1, LocalDateTime date2);

	@RestResource(path = "findPeriodeTypeDate")
	@Query(value = """
			select e
			  from Evenement e
			  where e.type = ?1 and e.date > ?2
			""")
	List<Evenement> findTypePeriode(TypeEvenement typeEvenement, LocalDateTime date);

	@RestResource(path = "findPeriodeTypeDate1Date2")
	@Query(value = """
			select e
			  from Evenement e
			  where e.type = ?1 and e.date > ?2 and e.date < ?3
			""")
	List<Evenement> findTypePeriode(TypeEvenement typeEvenement, LocalDateTime date1, LocalDateTime date2);

	@RestResource(path = "findPeriodeType1Type2Date1")
	@Query(value = """
			select e
			  from Evenement e
			  where (e.type = ooioo.ruches.evenement.TypeEvenement.ESSAIMTRAITEMENT
			    or e.type = ooioo.ruches.evenement.TypeEvenement.ESSAIMTRAITEMENTFIN)
			    and e.date > ?1
			""")
	Iterable<Evenement> findTypePeriode(LocalDateTime date);

	@RestResource(path = "findPeriodeType1Type2Date1Date2")
	@Query(value = """
			select e
			  from Evenement e
			  where (e.type = ooioo.ruches.evenement.TypeEvenement.ESSAIMTRAITEMENT
			      or e.type = ooioo.ruches.evenement.TypeEvenement.ESSAIMTRAITEMENTFIN)
			    and e.date > ?1 and e.date < ?2
			""")
	Iterable<Evenement> findTypePeriode(LocalDateTime date1, LocalDateTime date2);

	List<Evenement> findByRucheId(Long rucheId);

	Iterable<Evenement> findByEssaimId(Long essaimId);

	// Les événéments ajout et retrait de hausses pour une ruche.
	@Query(value = """
			select e
			  from Evenement e
			  where ruche.id = :rucheId and
			    (type = ooioo.ruches.evenement.TypeEvenement.HAUSSEPOSERUCHE
			      or type = ooioo.ruches.evenement.TypeEvenement.HAUSSERETRAITRUCHE)
			  order by date desc
			""")
	List<Evenement> findEveRucheHausseDesc(Long rucheId);

	// Les événéments ajout et retrait de hausses pour un essaim.
	@Query(value = """
			select e
			  from Evenement e
			  where essaim.id = :essaimId and
			    (type = ooioo.ruches.evenement.TypeEvenement.HAUSSEPOSERUCHE
			      or type = ooioo.ruches.evenement.TypeEvenement.HAUSSERETRAITRUCHE)
			  order by date asc
			""")
	List<Evenement> findEveEssaimHausseAsc(Long essaimId);

	List<Evenement> findByEssaimIdAndTypeOrderByDateAsc(Long essaimId, TypeEvenement typeEvenement);

	Iterable<Evenement> findByHausseId(Long hausseId);

	Iterable<Evenement> findByRucherId(Long rucherId);

	List<Evenement> findByTypeOrderByDateDesc(TypeEvenement typeEvenement);

	// Recherche événement pose hausse suivant un événement dont la ruche,
	// l'essaim et la date sont donnés en paramètre.
	// ruche et essaim identiques.
	@Query(value = """
			select new ooioo.ruches.IdDate(e.id, e.date) from Evenement e
			  where e.type = ooioo.ruches.evenement.TypeEvenement.HAUSSEPOSERUCHE
			    and e.ruche = :ruche
			    and e.essaim = :essaim
			    and e.date > :date
			  order by date desc
			  limit 1
			""")
	IdDate findSucreEveAjoutHausse(Ruche ruche, Essaim essaim, LocalDateTime date);

	@Query(value = """
			select e
			  from Evenement e
			  where e.type = ooioo.ruches.evenement.TypeEvenement.ESSAIMTRAITEMENT
			    or e.type = ooioo.ruches.evenement.TypeEvenement.ESSAIMTRAITEMENTFIN
			""")
	List<Evenement> findTraitementDateDesc();

	@Query(value = """
			select e from Evenement e
			  where e.type = ooioo.ruches.evenement.TypeEvenement.RUCHEAJOUTRUCHER
			    and e.ruche is not null
			    and e.rucher is not null
			  order by date desc
			""")
	List<Evenement> findAjoutRucheOK();

	Iterable<Evenement> findByTypeOrderByDateAsc(TypeEvenement typeEvenement);

	Evenement findFirstByRucheAndTypeOrderByDateDesc(Ruche ruche, TypeEvenement typeEvenement);

	Evenement findFirstByHausseAndTypeOrderByDateDesc(Hausse hausse, TypeEvenement typeEvenement);

	Evenement findFirstByEssaimAndTypeOrderByDateDesc(Essaim essaim, TypeEvenement typeEvenement);

	@Query(value = """
			select e
			  from Evenement e
			  where e.ruche = :ruche and
			    e.type <> ooioo.ruches.evenement.TypeEvenement.HAUSSEPOSERUCHE and
			    e.type <> ooioo.ruches.evenement.TypeEvenement.RUCHEPESEE and
			    e.type <> ooioo.ruches.evenement.TypeEvenement.RUCHECADRE
			  order by date desc limit 3
			""")
	List<Evenement> find3EveListePlus(Ruche ruche);

	/*
	 * Renvoie le nombre d'interventions dans les ruchers par année. Une même
	 * journée d'intervention sur deux ruchers comptera pour 2 interventions. Les
	 * événements type1 à 4 sont exclus de la recherche. Appelé avec les types
	 * d'événements commentaires ruche, hausse, rucher et essaim.
	 */
	@Query(value = """
			select count(*) from (
			  select distinct rucher_id, date_trunc('day', date)
			    from evenement e
			    where extract(year from date) = :annee and
			      e.type <> :type1 and
			      e.type <> :type2 and
			      e.type <> :type3 and
			      e.type <> :type4)
			    as x
			""", nativeQuery = true)
	Optional<Integer> countEveAnneeRucher(Integer annee, int type1, int type2, int type3, int type4);

	/*
	 * Renvoie le nombre de jours d'interventions par année. Les événements type1 à
	 * 4 sont exclus de la recherche. Appelé avec les types d'événements
	 * commentaires ruche, hausse, rucher et essaim.
	 */
	@Query(value = """
			select count(*) from (
			  select distinct date_trunc('day', date)
			    from evenement e
			    where extract(year from date) = :annee and
			      e.type <> :type1 and
			      e.type <> :type2 and
			      e.type <> :type3 and
			      e.type <> :type4)
			    as x
			""", nativeQuery = true)
	Optional<Integer> countEveAnnee(Integer annee, int type1, int type2, int type3, int type4);

	@Query(value = """
			select e
			  from Evenement e
			  where e.essaim = ?1 and
			    (e.type = ooioo.ruches.evenement.TypeEvenement.ESSAIMTRAITEMENT or
			      e.type = ooioo.ruches.evenement.TypeEvenement.ESSAIMTRAITEMENTFIN)
			  order by date desc limit 1
			""")
	Evenement findFirstTraitemenetByEssaim(Essaim essaim);

	Evenement findFirstByRucheAndHausseAndTypeOrderByDateDesc(Ruche ruche, Hausse hausse, TypeEvenement typeEvenement);

	Evenement findFirstByRucheAndRucherAndTypeOrderByDateDesc(Ruche ruche, Rucher rucher, TypeEvenement typeEvenement);

	List<TypeDate> findByTypeOrTypeOrderByDateAsc(TypeEvenement type1, TypeEvenement type2);

	// https://spring.io/blog/2014/07/15/spel-support-in-spring-data-jpa-query-definitions
	// pour éviter de passer ruche en param.
	@Query(value = """
			select e
			 from Evenement e
			 where e.hausse = :hausse and
			   e.ruche = :#{#hausse.ruche} and
			   e.type = ooioo.ruches.evenement.TypeEvenement.HAUSSEPOSERUCHE
			 order by e.date desc
			 limit 1
			""")
	Evenement findEvePoseHausse(Hausse hausse);

	// Nombre d'essaims dispersés dans l'année passée en paramètre.
//	@Query(value = """
//			select count(*) as nbeven
//			from Evenement
//			where type=ooioo.ruches.evenement.TypeEvenement.ESSAIMDISPERSION
//				and date_part('year', date)=?1
//			""")

	@Query(value = """
			select count(*) as nbeven
			from Essaim
			where actif = false
				and date_part('year', dateDispersion)=:date
			""")
	Integer countDispersionEssaimParAnnee(int date);

	// Quantité de sucre distribuée dans l'année passée en paramètre.
	@Query(value = """
			select sum(cast(valeur as double)) as sucre
			  from Evenement
			  where type=ooioo.ruches.evenement.TypeEvenement.ESSAIMSUCRE
				and date_part('year', date)=?1
			""")
	Double sucreEssaimParAnnee(int date);

	// Nombre d'événements traitements faits dans l'année passée en paramètre.
	@Query(value = """
			select count(*) as nbtraitements
			  from Evenement
			  where type=ooioo.ruches.evenement.TypeEvenement.ESSAIMTRAITEMENT
				and date_part('year', date)=?1
			""")
	Integer countTraitementsEssaimParAnnee(int date);

}