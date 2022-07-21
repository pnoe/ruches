package ooioo.ruches;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.mail.EmailService;

@Configuration
@EnableScheduling
public class Notification {

	private final Logger logger = LoggerFactory.getLogger(Notification.class);

	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	private static final String RUCHEPREF = ", Ruche : ";
	private static final String RUCHERPREF = ", Rucher : ";
	private static final String HAUSSEPREF = ", Hausse : ";
	private static final String ESSAIMPREF = ", Essaim : ";

	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	public EmailService emailService;

	@Value("${notification.destinataires}")
	private String[] destinataires;
	@Value("${notification.objet}")
	private String objet;
	@Value("${notification.entete}")
	private String entete;
	@Value("${notification.pied}")
	private String pied;

	@Bean
	public TaskScheduler taskScheduler() {
		return new ThreadPoolTaskScheduler();
	}

	@Scheduled(cron = "${notification.cron}")
	public void scheduleTaskCron() {
		LocalDateTime dateNow = LocalDateTime.now();
		// Trouve tous les événements commentaires (essaims, ruches, ruchers, hausses)
		// de valeur != '' (un délai en jours a été renseigné)
		List<Evenement> evenements = evenementRepository.findNotification();
		StringBuilder message = new StringBuilder(1000);
		message.append(entete);
		int i = 0;
		for (Evenement evenement : evenements) {
				int joursAvant = Integer.parseInt(evenement.getValeur());
				LocalDateTime min;
				LocalDateTime max;
				if (joursAvant >= 0) {
					min = evenement.getDate().minusDays(joursAvant);
					max = evenement.getDate();
				} else {
					// si joursAvant est négatif la plage de notification
					//  est la date de l'évenement - (la date eve plus le nb de jours)
					// fonctionnalité  : notif apès date de l'événement
					max = evenement.getDate().minusDays(joursAvant);
					min = evenement.getDate();		
				}
				// Si on est dans la plage de notification
				if (dateNow.isAfter(min) && (dateNow.isBefore(max))) {
					StringBuilder messageEve = new StringBuilder(200);
					messageEve.append(evenement.getDate().format(formatter));
					boolean ok = true;
					switch (evenement.getType()) {
					case COMMENTAIRERUCHE:
						messageEve.append(RUCHEPREF)
								.append(evenement.getRuche() == null ? "?" : evenement.getRuche().getNom())
								.append(ESSAIMPREF)
								.append(evenement.getEssaim() == null ? "?" : evenement.getEssaim().getNom())
								.append(RUCHERPREF)
								.append(evenement.getRucher() == null ? "?" : evenement.getRucher().getNom());
						break;
					case COMMENTAIRERUCHER:
						messageEve.append(RUCHERPREF)
								.append(evenement.getRucher() == null ? "?" : evenement.getRucher().getNom());
						break;
					case COMMENTAIREESSAIM:
						messageEve.append(ESSAIMPREF)
								.append(evenement.getEssaim() == null ? "?" : evenement.getEssaim().getNom())
								.append(RUCHEPREF)
								.append(evenement.getRuche() == null ? "?" : evenement.getRuche().getNom())
								.append(RUCHERPREF)
								.append(evenement.getRucher() == null ? "?" : evenement.getRucher().getNom());
						break;
					case COMMENTAIREHAUSSE:
						messageEve.append(HAUSSEPREF)
								.append(evenement.getHausse() == null ? "?" : evenement.getHausse().getNom())
								.append(RUCHEPREF)
								.append(evenement.getRuche() == null ? "?" : evenement.getRuche().getNom())
								.append(RUCHERPREF)
								.append(evenement.getRucher() == null ? "?" : evenement.getRucher().getNom());
						break;
					default:
						// le select ne prend que des événements commentaire
						ok = false;
						logger.error("Erreur type événement");
					}
					if (ok) {
						i++;
						message.append(messageEve);
						message.append(", Commentaire : ").append(evenement.getCommentaire()).append("\n");
					}
				}
		}
		if (i > 0) {
			// Envoyer le mail
			message.append(pied);
			for (String mail : destinataires) {
				emailService.sendSimpleMessage(mail, objet, message.toString());
			}
			logger.info("Message envoyé, {} événements", i);
		} else {
			logger.info("Pas de message envoyé");
		}
	}

}
