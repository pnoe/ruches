package ooioo.ruches;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Resource(name = "userDetailService")
	private UserDetailsService userDetailsService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// https://www.baeldung.com/spring-security-cache-control-headers
		// https://docs.spring.io/spring-security/reference/servlet/exploits/headers.html#servlet-headers-cache-control
		// https://docs.spring.io/spring-framework/docs/5.0.0.RELEASE/spring-framework-reference/web.html#mvc-config-static-resources
		// Activé jkmount sur ovh pour servir les ressources statiques
		// http.headers().disable();
		http.authorizeRequests()
				.antMatchers("/forgotPassword", "/resetPassword",
						"/resetPasswordFin", "/", "/css/**", "/js/**",
						"/images/**", "/doc/**", "/font/**")
				.permitAll().anyRequest().authenticated().and()
				.formLogin().loginPage("/login").permitAll().and()
				.logout().permitAll()
				// désactivation du csrf pour l'api rest
				.and().csrf().ignoringAntMatchers("/rest/**");
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService);
	}

	/*
	 * Password encoder nécessaire sinon erreur :
	 *  There is no PasswordEncoder mapped for the id "null"
	 */
	@Bean
	public static BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
}