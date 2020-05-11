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

	// @formatter:off
	/*
	 * The following code allows reading external configuration when the Spring Boot application
	 * is deployed as a WAR running in Tomcat.
	 * 
	 * To run this application in Tomcat with external configuration properties file:
	 * 1. Extend SpringBootServletInitializer
	 * 2. Uncomment the following code
	 * 3. Provide the CONFIG_LOCATION and CONFIG_NAME as JVM Argument
	 *    e.g. -Dsample-client.config.location={location of properties file} -Dsample-client.config.name={name of the properties file}
	 *

	private static final String CONFIG_LOCATION = "sample-client.config.location";
	private static final String CONFIG_NAME = "sample-client.config.name";
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		System.out.println("Starting up Spring Boot Servlet Initializer");

		String configLocation = System.getProperty(CONFIG_LOCATION);
		String configName = System.getProperty(CONFIG_NAME);

		LOGGER.debug("Reading system properties [{}={}, {}={}].", CONFIG_LOCATION, configLocation, CONFIG_NAME,
				configName);

		if (!StringUtils.isNullOrWhiteSpace(configLocation) && !StringUtils.isNullOrWhiteSpace(configName)) {
			Properties properties = new Properties();
			properties.setProperty("spring.config.location", configLocation);
			properties.setProperty("spring.config.name", configName);
			application.application().setDefaultProperties(properties);
		}

		return application.sources(MessagingClientApplication.class);
	}
	*/
	// @formatter:on

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
