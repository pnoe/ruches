package ooioo.ruches;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import org.springframework.boot.info.BuildProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class RuchesControllerAdvice {

	private final MessageSource messageSource;
	private final BuildProperties buildProperties;

	public RuchesControllerAdvice(MessageSource messageSource, BuildProperties buildProperties) {
		this.messageSource = messageSource;
		this.buildProperties = buildProperties;
	}

	/**
	 * Pour affichage de la date décalée en-tête de chaque page
	 */
	@ModelAttribute("currentDate")
	public String getCurrentDate(HttpSession session) {
		if ((session == null) || (session.getAttribute("decalagetemps") == null)) {
			return "";
		}
		return messageSource.getMessage("datedecalee", null, LocaleContextHolder.getLocale()) + " : "
				+ Utils.dateTimeDecal(session).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT));
	}

	/*
	 * Pour affichage de la date de compilation en pied de chaque page
	 */
	@ModelAttribute("buildTime")
	public String getBuildTime() {
		DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.FRENCH)
				.withZone(ZoneId.systemDefault());
		return formatter.format(buildProperties.getTime());
	}

}
