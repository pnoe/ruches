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
			    valeur <> ''
			    and (type = ooioo.ruches.evenement.TypeEvenement.COMMENTAIREESSAIM
			    or type = ooioo.ruches.evenement.TypeEvenement.COMMENTAIREHAUSSE
			    or type = ooioo.ruches.evenement.TypeEvenement.COMMENTAIRERUCHE
			    or type = ooioo.ruches.evenement.TypeEvenement.COMMENTAIRERUCHER)
			  order by date desc
			""")
	List<Evenement> findNotification();

	/*
	 * Trouve pour chaque hausse inactive, le dernier événement la référençant.
	 */
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

	/*
	 * Trouve pour chaque ruche inactive, le dernier événement la référençant.
	 */
	@Query(value = """
			select new ooioo.ruches.IdDate(null, max(e.date))
			  from Ruche r, Evenement e
			  where
			    r.active = false and
			    r.id = e.ruche.id
			  group by r.id
			  order by max(e.date) asc
			""")
	List<IdDate> findRucheInacLastEve();

	/*
	 * Trouve pour chaque ruche de production et inactive, le dernier événement la
	 * référençant. En retour id de la ruche, date de cet événement.
	 * Cette date est un estimation de la date d'inactivation de la ruche.
	 */
	@Query(value = """
			select new ooioo.ruches.IdDate(r.id, max(e.date))
			  from Ruche r, Evenement e
			  where
			    r.production = true and
			    r.active = false and
			    r.id = e.ruche.id
			  group by r.id
			  order by max(e.date) asc
			""")
	List<IdDate> findRucheProdInacLastEve();

	@Query(value = """
			select e
			  from Evenement e
			  where date > :date
			  """)
	Iterable<Evenement> findPeriode(LocalDateTime date);

//	https://docs.spring.io/spring-data/rest/docs/current/reference/html/#customizing-sdr.configuring-the-rest-url-path
	@RestResource(path = "findPeriodeDate1Date2")
	@Query(value = """
			select e
			  from Evenement e
			  where date > :date1 and date < :date2
			""")
	Iterable<Evenement> findPeriode(LocalDateTime date1, LocalDateTime date2);

	@RestResource(path = "findPeriodeTypeDate")
	@Query(value = """
			select e
			  from Evenement e
			  where type = :typeEvenement and date > :date
			""")
	List<Evenement> findTypePeriode(TypeEvenement typeEvenement, LocalDateTime date);

	@RestResource(path = "findPeriodeTypeDate1Date2")
	@Query(value = """
			select e
			  from Evenement e
			  where type = :typeEvenement and date > :date1 and date < :date2
			""")
	List<Evenement> findTypePeriode(TypeEvenement typeEvenement, LocalDateTime date1, LocalDateTime date2);

	@RestResource(path = "findPeriodeType1Type2Date1")
	@Query(value = """
			select e
			  from Evenement e
			  where (type = :type1 or type = :type2)
			    and date > :date
			""")
	Iterable<Evenement> findTypePeriode(LocalDateTime date, TypeEvenement type1, TypeEvenement type2);

	@RestResource(path = "findPeriodeType1Type2Date1Date2")
	@Query(value = """
			select e
			  from Evenement e
			  where (type = :type1 or type = :type2)
			    and date > :date1 and date < :date2
			""")
	Iterable<Evenement> findTypePeriode(LocalDateTime date1, LocalDateTime date2, TypeEvenement type1,
			TypeEvenement type2);

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
			select new ooioo.ruches.IdDate(id, date) from Evenement
			  where type = ooioo.ruches.evenement.TypeEvenement.HAUSSEPOSERUCHE
			    and ruche = :ruche
			    and essaim = :essaim
			    and date > :date
			  order by date desc
			  limit 1
			""")
	IdDate findSucreEveAjoutHausse(Ruche ruche, Essaim essaim, LocalDateTime date);

	@Query(value = """
			select e
			  from Evenement e
			  where type = ooioo.ruches.evenement.TypeEvenement.ESSAIMTRAITEMENT
			    or type = ooioo.ruches.evenement.TypeEvenement.ESSAIMTRAITEMENTFIN
			""")
	List<Evenement> findTraitementDateDesc();

	@Query(value = """
			select e
			  from Evenement e
			  where type = ooioo.ruches.evenement.TypeEvenement.HAUSSEPOSERUCHE
			    or type = ooioo.ruches.evenement.TypeEvenement.HAUSSERETRAITRUCHE
			""")
	List<Evenement> findPoseHausseDateDesc();

	@Query(value = """
			select e from Evenement e
			  where type = ooioo.ruches.evenement.TypeEvenement.RUCHEAJOUTRUCHER
			    and ruche is not null
			    and rucher is not null
			  order by date desc
			""")
	List<Evenement> findAjoutRucheOK();

	List<Evenement> findByTypeOrderByDateAsc(TypeEvenement typeEvenement);

	Evenement findFirstByRucheAndTypeOrderByDateDesc(Ruche ruche, TypeEvenement typeEvenement);

	Evenement findFirstByHausseAndTypeOrderByDateDesc(Hausse hausse, TypeEvenement typeEvenement);

	Evenement findFirstByEssaimAndTypeOrderByDateDesc(Essaim essaim, TypeEvenement typeEvenement);

	@Query(value = """
			select e
			  from Evenement e
			  where ruche = :ruche and
			    type <> ooioo.ruches.evenement.TypeEvenement.HAUSSEPOSERUCHE and
			    type <> ooioo.ruches.evenement.TypeEvenement.RUCHEPESEE and
			    type <> ooioo.ruches.evenement.TypeEvenement.RUCHECADRE
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
			    from evenement
			    where extract(year from date) = :annee and
			      type <> :type1 and
			      type <> :type2 and
			      type <> :type3 and
			      type <> :type4)
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
			    from evenement
			    where extract(year from date) = :annee and
			      type <> :type1 and
			      type <> :type2 and
			      type <> :type3 and
			      type <> :type4)
			    as x
			""", nativeQuery = true)
	Optional<Integer> countEveAnnee(Integer annee, int type1, int type2, int type3, int type4);

	@Query(value = """
			select e
			  from Evenement e
			  where essaim = :essaim and
			    (type = ooioo.ruches.evenement.TypeEvenement.ESSAIMTRAITEMENT or
			      type = ooioo.ruches.evenement.TypeEvenement.ESSAIMTRAITEMENTFIN)
			  order by date desc limit 1
			""")
	Evenement findFirstTraitemenetByEssaim(Essaim essaim);

	Evenement findFirstByRucheAndHausseAndTypeOrderByDateDesc(Ruche ruche, Hausse hausse, TypeEvenement typeEvenement);

	Evenement findFirstByRucheAndRucherAndTypeOrderByDateDesc(Ruche ruche, Rucher rucher, TypeEvenement typeEvenement);

	List<TypeDate> findByTypeOrTypeOrderByDateAsc(TypeEvenement type1, TypeEvenement type2);

	// https://spring.io/blog/2014/07/15/spel-support-in-spring-data-jpa-query-definitions
	// pour éviter de passer ruche en param.
	// Liste du dernier événement pose hausse de la hausse passée en paramètre, et
	// de la ruche sur laquelle est posée cette hausse.
	@Query(value = """
			select e
			 from Evenement e
			 where hausse = :hausse and
			   ruche = :#{#hausse.ruche} and
			   type = ooioo.ruches.evenement.TypeEvenement.HAUSSEPOSERUCHE
			 order by date desc
			 limit 1
			""")
	Evenement findEvePoseHausse(Hausse hausse);

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