package ooioo.ruches;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	// https://docs.oracle.com/en/java/javase/18/docs/api/java.base/java/util/regex/Pattern.html
	// un éventuel signe moins (groupe 1)
	// suivi d'un nombre d'un ou plusieurs chiffres (groupe 2)
	// suivi d'un éventuel .
	// suivi d'un éventuel groupe de chiffres (groupe 3)
	// suivi de n'importe quoi
	public static final Pattern MOINSNB1NB2 = Pattern.compile("(-?)(\\d+)\\.?(\\d?).*");

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
			try {
				Matcher m = MOINSNB1NB2.matcher(evenement.getValeur());
				if (!m.matches()) {
					continue;
				}
				int joursAvant = Integer.parseInt(m.group(2));
				int joursDuree = "".equals(m.group(3)) ? 0 : Integer.parseInt(m.group(3));
				// Si la date de notification est atteinte
				if (dateNow.isAfter(evenement.getDate().minusDays(joursAvant))
						&& (("-".equals(m.group(1))) || (dateNow.isBefore(evenement.getDate().plusDays(joursDuree))))) {
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
						if (joursDuree != 0) {
							message.append(", Fin : ")
									.append(evenement.getDate().plusDays(joursDuree).format(formatter));
						}
						message.append(", Commentaire : ").append(evenement.getCommentaire()).append("\n");
					}
				}
			} catch (NumberFormatException nfe) {
				// valeur n'est pas un entier
				logger.error("Erreur Integer.parseInt");
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
