package bcsc.sample.client.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import bcsc.sample.client.entity.RegisteredUser;
import bcsc.sample.client.repository.RegisteredUserRepository;
import  bcsc.sample.client.jwt.DefaultJWTSignatureAndDecryptionService;
//import bcsc.sample.client.service.ClientRegistrationManager;

@Controller
public class DefaultController {
	class Templates {
		static final String HOME = "home";
		static final String LANDING = "landing";
		static final String INDEX = "index";
		static final String FIRST_LOGIN = "firstlogin";
		static final String VIEW_MESSAGES = "messages";
		static final String SUBSCRIPTIONS = "subscriptions";
	}

	class ModelVars {
		static final String CLIENT = "client";
		static final String CLIENTLIST = "clientList";
		static final String USER = "user";
		static final String SUBSCRIPTION = "subscription";
		static final String MESSAGES = "messages";
	}

	Logger log = LoggerFactory.getLogger(DefaultController.class);
	private RegisteredUserRepository registeredUserRepository;
	private ClientRegistrationRepository clientRegistrationRepository;

	private static final String REDIR = "redirect:";
	private static final String CLIENT_REG_PATHVAR = "client_reg";

	private String oidcIssuer;

	public DefaultController(RegisteredUserRepository registeredUserRepository,
			ClientRegistrationRepository clientRegRepo,
			@Value("${oauth2.provider.issuer}") final String oidcIssuer ) {
		this.registeredUserRepository = registeredUserRepository;
		this.clientRegistrationRepository = clientRegRepo;
		this.oidcIssuer = oidcIssuer;
//		this.clientRegistrationManager = clregman;
	}

	@GetMapping("/")
	public ModelAndView getLanding(OAuth2AuthenticationToken authentication, HttpServletRequest request) {
		ModelAndView model = new ModelAndView(Templates.LANDING);
		if (clientRegistrationRepository instanceof Iterable<?>) {
			ArrayList<ClientRegistration> clientList = new ArrayList<>();
			@SuppressWarnings("unchecked")
			Iterable<ClientRegistration> clientIter = (Iterable<ClientRegistration>) clientRegistrationRepository;
			for (ClientRegistration cr : clientIter) {
				clientList.add(cr);
			}
			clientList.sort(new Comparator<ClientRegistration>() {
				@Override
				public int compare(ClientRegistration o1, ClientRegistration o2) {
					return o1.getClientName().compareTo(o2.getClientName());
				}
			});
			model.addObject(ModelVars.CLIENTLIST, clientList);
		}
		return model;
	}

	@GetMapping("/home/{" + CLIENT_REG_PATHVAR + "}")
	public ModelAndView getHome(@PathVariable(CLIENT_REG_PATHVAR) String clientRegistration) {
		ModelAndView model = new ModelAndView(Templates.HOME);
		model.addObject(ModelVars.CLIENT, clientRegistrationRepository.findByRegistrationId(clientRegistration));
		return model;
	}

	@GetMapping("/secure/")
	public String getHomeBase(OAuth2AuthenticationToken authentication,
			@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient) {
		return REDIR + authorizedClient.getClientRegistration().getRegistrationId();
	}

	@GetMapping("/secure/{" + CLIENT_REG_PATHVAR + "}")
	public ModelAndView getHome2(OAuth2AuthenticationToken authentication,
			@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
			@PathVariable(CLIENT_REG_PATHVAR) String clientRegistration) {
		verifyAccess(authorizedClient, clientRegistration);

		String issuer = (String) authorizedClient.getClientRegistration().getProviderDetails()
				.getConfigurationMetadata().getOrDefault("issuer", oidcIssuer);
		RegisteredUser registeredUser = registeredUserRepository.findByIssuerAndSubject(issuer,
				authentication.getName());

		ModelAndView model = new ModelAndView();
		model.addObject(ModelVars.CLIENT, authorizedClient.getClientRegistration());
		if (registeredUser != null) {
			log.debug("existing user, update last login");
			model.setViewName(Templates.INDEX);
			registeredUser.setLastLoginAt(new Date());
			registeredUserRepository.save(registeredUser);
			model.addObject(ModelVars.USER, registeredUser);
			model.addObject(ModelVars.CLIENT, authorizedClient.getClientRegistration());
			
		} else {
			log.debug("first time user");
			model.setViewName(Templates.FIRST_LOGIN);
			model.addObject(ModelVars.USER,
					RegisteredUser.createFromClaimsSet(authentication.getPrincipal().getAttributes(), issuer,
							authorizedClient.getClientRegistration().getClientId()));
			return model;
		}
		return model;
	}

	@GetMapping("/secure/{" + CLIENT_REG_PATHVAR + "}/register")
	public ModelAndView register(OAuth2AuthenticationToken authentication,
			@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
			@PathVariable(CLIENT_REG_PATHVAR) String clientRegistrationId) {
		verifyAccess(authorizedClient, clientRegistrationId);

		String issuer = (String) authorizedClient.getClientRegistration().getProviderDetails()
				.getConfigurationMetadata().getOrDefault("issuer", oidcIssuer);
		RegisteredUser registeredUser = registeredUserRepository.findByIssuerAndSubject(issuer,
				authentication.getName());

		if (registeredUser == null) {
			log.debug("User not registered; saving registration");
			registeredUser = RegisteredUser.createFromClaimsSet(authentication.getPrincipal().getAttributes(), issuer,
					authorizedClient.getClientRegistration().getClientId());
			Date now = new Date();
			registeredUser.setCreatedAt(now);
			registeredUser.setLastLoginAt(now);
			registeredUser = registeredUserRepository.save(registeredUser);
		}

		ModelAndView model = new ModelAndView(Templates.INDEX);
		model.addObject(ModelVars.CLIENT, authorizedClient.getClientRegistration());
		model.addObject(ModelVars.USER, registeredUser);

		return model;
	}

	void verifyAccess(final OAuth2AuthorizedClient authorizedClient, final String clientRegistrationId) {
		if (!authorizedClient.getClientRegistration().getRegistrationId().equals(clientRegistrationId)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
					"Attempting to access client with token from another client.");
		}
	}

	RegisteredUser getRegisteredUser(final OAuth2AuthorizedClient authorizedClient,
			final OAuth2AuthenticationToken authenticationToken) {
		String issuer = (String) authorizedClient.getClientRegistration().getProviderDetails()
				.getConfigurationMetadata().getOrDefault("issuer", oidcIssuer);
		return registeredUserRepository.findByIssuerAndSubject(issuer, authenticationToken.getName());
	}
}
