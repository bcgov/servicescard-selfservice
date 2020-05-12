package bcsc.sample.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.ForwardedHeaderFilter;

@SpringBootApplication
public class SampleClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(SampleClientApplication.class, args);
	}

	/**
	 * Registers a ForwardedHeaderFilter with high precedence
	 * ({@code OrderedFilter.HIGHEST_PRECEDENCE+1)}) that will use the
	 * X-Forwarded-* HTTP Headers to re-build requests and redirects when behind
	 * a proxy server.
	 * 
	 * @return
	 */
	@Bean
	public FilterRegistrationBean<ForwardedHeaderFilter> forwardedFilter() {
		FilterRegistrationBean<ForwardedHeaderFilter> registrationBean = new FilterRegistrationBean<>();

		registrationBean.setFilter(new ForwardedHeaderFilter());
		registrationBean.setOrder(OrderedFilter.HIGHEST_PRECEDENCE + 1);

		return registrationBean;
	}
}
