package ooioo.ruches;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

@Configuration
@EnableWebSecurity
public class SecurityConfig { // extends WebSecurityConfigurerAdapter {

	private final Logger loggerSecConfig = LoggerFactory.getLogger(SecurityConfig.class);

	@Autowired
	MessageSource messageSource;

	@Resource(name = "userDetailService")
	private UserDetailsService userDetailsService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		/*
		 * Pour Spring Boot 3 
		 * http.authorizeHttpRequests()
		 * .requestMatchers("/forgotPassword", "/resetPassword", "/resetPasswordFin",
		 * "/", "/css/**", "/js/**", "/images/**", "/doc/**", "/font/**")
		 */
		http.authorizeRequests()
				.antMatchers("/forgotPassword", "/resetPassword", "/resetPasswordFin", "/", "/css/**", "/js/**",
						"/images/**", "/doc/**", "/font/**")
				.permitAll().anyRequest().authenticated().and().formLogin().loginPage("/login").permitAll()
				.successHandler(new SavedRequestAwareAuthenticationSuccessHandler() {
					@Override
					public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
							Authentication authentication) throws IOException, ServletException {
						UserDetails userDetails = (UserDetails) authentication.getPrincipal();
						loggerSecConfig.info(
								messageSource.getMessage("CONNECTE", null, LocaleContextHolder.getLocale()),
								userDetails.getUsername());
						super.onAuthenticationSuccess(request, response, authentication);
					}
				}).and().logout().permitAll()
				// désactivation du csrf pour l'api rest
				.and().csrf().ignoringAntMatchers("/rest/**");
		/*
		 * Pour Spring Boot 3 
		 * .and().csrf().ignoringRequestMatchers("/rest/**");
		 */
		return http.build();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService);
	}

	/*
	 * Password encoder nécessaire sinon erreur : There is no PasswordEncoder mapped
	 * for the id "null"
	 */
	@Bean
	public static BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}