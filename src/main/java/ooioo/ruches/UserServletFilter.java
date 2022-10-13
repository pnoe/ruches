package ooioo.ruches;

import java.io.IOException;
import java.security.Principal;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Pour afficher le nom de l'utilisateur dans les logs voir doc logback
 * https://logback.qos.ch/manual/mdc.html
 */
@Component
@Order(1)
public class UserServletFilter implements Filter {

	private static final String USERKEY = "username";

	@Override
	public void destroy() {
		// destruction du filtre
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		boolean successfulRegistration = false;
		HttpServletRequest req = (HttpServletRequest) request;
		Principal principal = req.getUserPrincipal();
		if (principal != null) {
			String username = principal.getName();
			successfulRegistration = registerUsername(username);
		}
		try {
			chain.doFilter(request, response);
		} finally {
			if (successfulRegistration) {
				MDC.remove(USERKEY);
			}
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// initialisation du filtre
	}

	/**
	 * Register the user in the MDC under USER_KEY.
	 *
	 * @return true id the user can be successfully registered
	 */
	private boolean registerUsername(String username) {
		if (username != null && username.trim().length() > 0) {
			MDC.put(USERKEY, username);
			return true;
		}
		return false;
	}
}
