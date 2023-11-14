package ooioo.ruches.evenement;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

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

	@Query(value = "select evenement from Evenement evenement where evenement.date > ?1")
	Iterable<Evenement> findPeriode(LocalDateTime date);

//	https://docs.spring.io/spring-data/rest/docs/current/reference/html/#customizing-sdr.configuring-the-rest-url-path
	@RestResource(path = "findPeriodeDate1Date2")
	@Query(value = """
			select e
			  from Evenement e
			  where e.date > ?1 and e.date < ?2
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

	@Query(value = """
			select e
			  from Evenement e
				  where e.ruche.id = ?1 and
			    (e.type = ooioo.ruches.evenement.TypeEvenement.HAUSSEPOSERUCHE
			    or e.type = ooioo.ruches.evenement.TypeEvenement.HAUSSERETRAITRUCHE)
			  order by e.date desc
			""")
	List<Evenement> findEveRucheHausseDesc(Long rucheId);

	List<Evenement> findByEssaimIdAndTypeOrderByDateAsc(Long essaimId, TypeEvenement typeEvenement);

	Iterable<Evenement> findByHausseId(Long hausseId);

	Iterable<Evenement> findByRucherId(Long rucherId);

	List<Evenement> findByTypeOrderByDateDesc(TypeEvenement typeEvenement);

	// Recherche événement pose hausse suivant un événement dont la ruche,
	// l'essaim et la date sont donnés en paramètre.
	// ruche et essaim identiques.
	@Query(value = """
			select new ooioo.ruches.evenement.IdDate(e.id, e.date) from Evenement e
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