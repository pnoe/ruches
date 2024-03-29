package ooioo.ruches;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * Internationalisation, paramètre lang sur l'url (par ex: ?lang=en)
 * https://www.baeldung.com/spring-boot-internationalization pour utitiliser le
 * header accept language voir :
 * https://stackoverflow.com/questions/55736861/retrieve-locale-based-on-the-accept-language-in-spring-boot/55740234
 */
@Configuration
@ComponentScan(basePackages = "ooioo.ruches")
public class MvcConfig implements WebMvcConfigurer {

	@Bean
	LocaleResolver localeResolver() {
		// Voir aussi SessionLocaleResolver et CookieLocaleResolver
		AcceptHeaderLocaleResolver lr = new AcceptHeaderLocaleResolver();
		lr.setDefaultLocale(Locale.FRENCH);
		return lr;
	}

	@Bean
	LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
		lci.setParamName("lang");
		return lci;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
	}
}
