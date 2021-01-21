package ooioo.ruches;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Pour affichage de la date décalée en-tête de chaque page 
 */
@ControllerAdvice
public class RuchesControllerAdvice {
	@Autowired
	MessageSource messageSource;
	
	@ModelAttribute("currentDate")
	public String getCurrentDate(HttpSession session) {
		if ((session == null) || (session.getAttribute("decalagetemps") == null)) {
			return "";
		}
		return messageSource.getMessage("datedecalee", null, LocaleContextHolder.getLocale()) +
				" : " +
				Utils.dateTimeDecal(session).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT));
	}
}
