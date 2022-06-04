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
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		return threadPoolTaskScheduler;
	}

	@Scheduled(cron = "${notification.cron}")
	public void scheduleTaskCron() {
		// Trouve tous les événements commentaires (essaims, ruches, ruchers, hausses)
		// de dateEve >= now (donc ok pour les événements de la journée courante)
		// et valeur != '' (un délai en jours a été renseigné)
		LocalDateTime dateNow = LocalDateTime.now();
		List<Evenement> evenements = evenementRepository.findNotification(dateNow);
		StringBuilder message = new StringBuilder(1000);
		message.append(entete);
		int i = 0;
		for (Evenement evenement : evenements) {
			try {
				int jours = Integer.parseInt(evenement.getValeur());
				// si la date de notification est atteinte
				if (dateNow.isAfter(evenement.getDate().minusDays(jours))) {
					StringBuilder messageEve = new StringBuilder(200);
					messageEve.append(evenement.getDate().format(formatter));
					boolean ok = true;
					switch (evenement.getType()) {
					case COMMENTAIRERUCHE:
						messageEve.append(", Ruche : ")
								.append(evenement.getRuche() == null ? "?" : evenement.getRuche().getNom())
								.append(", Essaim : ")
								.append(evenement.getEssaim() == null ? "?" : evenement.getEssaim().getNom())
								.append(", Rucher : ")
								.append(evenement.getRucher() == null ? "?" : evenement.getRucher().getNom());
						break;
					case COMMENTAIRERUCHER:
						messageEve.append(" Rucher : ")
								.append(evenement.getRucher() == null ? "?" : evenement.getRucher().getNom());
						break;
					case COMMENTAIREESSAIM:
						messageEve.append(", Essaim : ")
								.append(evenement.getEssaim() == null ? "?" : evenement.getEssaim().getNom())
								.append(", Ruche : ")
								.append(evenement.getRuche() == null ? "?" : evenement.getRuche().getNom())
								.append(", Rucher : ")
								.append(evenement.getRucher() == null ? "?" : evenement.getRucher().getNom());
						break;
					case COMMENTAIREHAUSSE:
						messageEve.append(", Hausse : ")
								.append(evenement.getHausse() == null ? "?" : evenement.getHausse().getNom())
								.append(", Ruche : ")
								.append(evenement.getRuche() == null ? "?" : evenement.getRuche().getNom())
								.append(", Rucher : ")
								.append(evenement.getRucher() == null ? "?" : evenement.getRucher().getNom());
						break;
					default:
						// le select ne prend que des événements commentaire
						ok = false;
						logger.error("Erreur type événement");
					}
					if (ok) {
						i++;
						message.append(messageEve).append(", Commentaire : ").append(evenement.getCommentaire())
								.append("\n");
					}
				}
			} catch (NumberFormatException nfe) {
				// valeur n'est pas un entier
			}
		}
		if (i > 0) {
			// Envoyer le mail
			message.append(pied);
			for (String mail : destinataires) {
				emailService.sendSimpleMessage(mail, objet,
						message.toString());
				System.out.println(mail);
				System.out.println(message);
			}
			logger.info("Message envoyé, {} événements", i);
		} else {
			logger.info("Pas de message envoyé");
		}
	}

}
