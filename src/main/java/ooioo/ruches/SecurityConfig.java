package ooioo.ruches;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final Logger loggerSecConfig = LoggerFactory.getLogger(SecurityConfig.class);

	private final MessageSource messageSource;

	@Resource(name = "userDetailService")
	private UserDetailsService userDetailsService;

	public SecurityConfig(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	// attention dans tomcat/conf/web.xml mettre la servlet jsp et son mapping en
	// commentaire
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		// https://docs.spring.io/spring-security/reference/migration-7/configuration.html
		// https://github.com/spring-projects/spring-security/issues/13568
		http.authorizeHttpRequests(
				autz -> autz.requestMatchers("/forgotPassword", "/resetPassword", "/resetPasswordFin", "/", "/css/**",
						"/js/**", "/images/**", "/doc/**", "/font/**").permitAll().anyRequest().authenticated())
				.formLogin(formLogin -> formLogin.loginPage("/login").permitAll()
						.successHandler(new SavedRequestAwareAuthenticationSuccessHandler() {
							@Override
							public void onAuthenticationSuccess(HttpServletRequest request,
									HttpServletResponse response, Authentication authentication)
									throws IOException, ServletException {
								UserDetails userDetails = (UserDetails) authentication.getPrincipal();
								loggerSecConfig.info(
										messageSource.getMessage("CONNECTE", null, LocaleContextHolder.getLocale()),
										userDetails.getUsername());
								super.onAuthenticationSuccess(request, response, authentication);
							}
						}))
				.logout(logout -> logout.permitAll()).csrf(csrf -> csrf.ignoringRequestMatchers("/rest/**"));
		// sans logout on n'a pas immédiatement le message "Vous avez été déconnecté" et
		// pour se reconnecter il faut le faire deux fois !
		return http.build();
	}

	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService);
	}

	/*
	 * Password encoder nécessaire sinon erreur : There is no PasswordEncoder mapped
	 * for the id "null"
	 */
	@Bean
	static BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}