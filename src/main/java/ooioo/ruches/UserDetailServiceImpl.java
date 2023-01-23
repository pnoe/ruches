// https://www.boraji.com/spring-security-5-custom-userdetailsservice-example
package ooioo.ruches;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import ooioo.ruches.personne.Personne;
import ooioo.ruches.personne.PersonneRepository;

@Component(value = "userDetailService")
public class UserDetailServiceImpl implements UserDetailsService {

	@Autowired
	private PersonneRepository personneRepository;

	@Override
	public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
		if ("".equals(login)) {
			throw new UsernameNotFoundException("Login ou mot de passe incorrect.");
		}
		Personne personne = personneRepository.findByLogin(login);
		if (personne == null) {
			throw new UsernameNotFoundException("Login ou mot de passe incorrect.");
		}
		UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(login);
		builder.password(personne.getPassword());
		builder.roles(personne.getRoles());
		return builder.build();
	}

}