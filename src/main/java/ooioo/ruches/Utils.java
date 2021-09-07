package ooioo.ruches;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.servlet.http.HttpSession;

public final class Utils {

	/*
	 * Renvoie la date et heure LocalDateTime.now() décalée éventuellement du décalage
	 *  demandé par le menu préférences mémorisé en session
	 */
	public static final LocalDateTime dateTimeDecal(HttpSession session) {
		return (session.getAttribute(Const.DECALAGETEMPS) != null)
				? LocalDateTime.now().plus((Duration) session.getAttribute(Const.DECALAGETEMPS))
				: LocalDateTime.now();
	}

	/*
	 * Renvoie la date LocalDate.now() décalée éventuellement du décalage
	 *  demandé par le menu préférences mémorisé en session
	 */
	public static final LocalDate dateDecal(HttpSession session) {
		return (session.getAttribute(Const.DECALAGETEMPS) != null)
				? LocalDate.now().plusDays(((Duration) session.getAttribute(Const.DECALAGETEMPS)).toDays())
				: LocalDate.now();
	}

	private Utils() {
		throw new IllegalStateException("Constant class");
	}

}
