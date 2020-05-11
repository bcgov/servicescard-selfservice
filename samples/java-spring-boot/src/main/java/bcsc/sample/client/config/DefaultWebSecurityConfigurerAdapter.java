package bcsc.sample.client.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@Configuration
public class DefaultWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.authorizeRequests()
				.antMatchers("**").permitAll()
				.antMatchers("/secure/**").authenticated()
			.and().oauth2Login()
				.defaultSuccessUrl("/secure/")
			.and().logout()
				.logoutSuccessUrl("/")
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID")
			.and().csrf()
				.ignoringAntMatchers("/api/**");
		// @formatter:on
	}
}