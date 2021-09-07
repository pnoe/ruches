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

	Iterable<Evenement> findByHausseId(Long hausseId);

	Iterable<Evenement> findByRucherId(Long rucherId);

	Iterable<Evenement> findByTypeOrderByDateDesc(TypeEvenement typeEvenement);

	Iterable<Evenement> findByTypeOrderByDateAsc(TypeEvenement typeEvenement);


	// Iterable<Evenement> findByEssaimIdAndTypeOrderByDateDesc(Long essaimId, TypeEvenement typeEvenement);

	Evenement findFirstByEssaimAndType(Essaim essaim, TypeEvenement typeEvenement);



	Evenement findFirstByRucheAndTypeOrderByDateDesc(Ruche ruche, TypeEvenement typeEvenement);

	// Evenement findFirstByRucheOrderByDateDesc(Ruche ruche);

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

	// correction spring boot 2.2.0 iterable -> list
	Evenement findFirstByRucheAndHausseInAndTypeOrderByDateDesc(Ruche ruche, List<Hausse> hausse, TypeEvenement typeEvenement);

	@Query(value = "select count(*) as nbeven from Evenement where type=ooioo.ruches.evenement.TypeEvenement.ESSAIMDISPERSION and date_part('year', date)=?1")
	Integer countDispersionEssaimParAnnee(Double date);

	@Query(value = "select sum(cast(valeur as double)) as sucre from Evenement where type=ooioo.ruches.evenement.TypeEvenement.ESSAIMSUCRE and date_part('year', date)=?1")
	Double sucreEssaimParAnnee(Double date);

	@Query(value = "select count(*) as nbtraitements from Evenement where type=ooioo.ruches.evenement.TypeEvenement.ESSAIMTRAITEMENT and date_part('year', date)=?1")
	Integer countTraitementsEssaimParAnnee(Double date);

}