package bcsc.sample.client.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;

import bcsc.sample.client.service.KeyManagementService;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class PublicApiController {

	private static Logger logger = LoggerFactory.getLogger(PublicApiController.class);

	private ClientRegistrationRepository clientRegistrationRepository;
	private KeyManagementService keyManagementService;
	private String oidcIssuer;

	public PublicApiController(
			ClientRegistrationRepository clientRegistrationRepository, KeyManagementService keyManagementService,
			@Value("${oauth2.provider.issuer}") final String oidcIssuer) {
		this.clientRegistrationRepository = clientRegistrationRepository;
		this.keyManagementService = keyManagementService;
		this.oidcIssuer = oidcIssuer;
	}

	@GetMapping(value = "/{registrationId}/jwks")
	public ResponseEntity<?> jwks(@PathVariable(name = "registrationId", required = true) String registrationId) {
		logger.debug("JWKS requested [registrationId={}].", registrationId);

		ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(registrationId);
		if (clientRegistration == null) {
			return ResponseEntity.notFound().build();
		}

		List<JWK> jwks = new ArrayList<>();
		List<RSAKey> rsaKeys = keyManagementService.getClientRSAKeys(registrationId);
		rsaKeys.forEach(k -> jwks.add(k.toPublicJWK()));

		return ResponseEntity.ok(new JWKSet(jwks).toString());
	}
}
