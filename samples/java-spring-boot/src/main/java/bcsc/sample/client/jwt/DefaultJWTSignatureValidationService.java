package bcsc.sample.client.jwt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;

import bcsc.sample.client.service.ClientRegistrationManager;
import bcsc.sample.client.util.StringUtils;

@Service
public class DefaultJWTSignatureValidationService implements JWTSignatureValidationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJWTSignatureValidationService.class);

	private ClientRegistrationManager clientRegistrationManager;
	private JWKSetLoader jwkSetLoader;
	private int jwksCacheDuration;

	private LoadingCache<String, JWKSet> providerJwkSets;

	/**
	 * Create an instance of {@link DefaultJWTSignatureValidationService}.
	 *
	 * @param clientRegistrationManager
	 *            the {@link ClientRegistrationManager}.
	 * @param jwkSetLoader
	 *            the {@link JWKSetLoader}.
	 * @param jwksCacheDuration
	 *            number of hours the JWK set is cached.
	 */
	public DefaultJWTSignatureValidationService(@NonNull final ClientRegistrationManager clientRegistrationManager,
			@NonNull final JWKSetLoader jwkSetLoader,
			@NonNull @Value("${oauth2.provider.jwks-cache.duration}") int jwksCacheDuration) {
		this.clientRegistrationManager = clientRegistrationManager;
		this.jwkSetLoader = jwkSetLoader;
		this.jwksCacheDuration = jwksCacheDuration;
	}

	@PostConstruct
	public void init() {
		this.providerJwkSets = CacheBuilder.newBuilder().expireAfterWrite(jwksCacheDuration, TimeUnit.HOURS)
				.maximumSize(10).build(jwkSetLoader);
	}

	@Override
	public JWKSet getProviderJwkSet(final String registrationId) {
		String jwksUri = resolveJwksUri(registrationId);
		LOGGER.debug("Fetching provider JWK set for client [registrationId={}, jwksUri={}].", registrationId, jwksUri);

		if (jwksUri == null) {
			return null;
		}

		try {
			return providerJwkSets.get(jwksUri);
		} catch (ExecutionException e) {
			LOGGER.error(String.format("Failed to retrieve JWK set [jwkUri=%s].", jwksUri), e);
			return null;
		}
	}

	@Override
	public boolean validateSignature(final String registrationId, final SignedJWT signedJWT) {
		JWKSet jwks = getProviderJwkSet(registrationId);
		if (jwks == null) {
			LOGGER.debug("No JWKS for client registration [registrationId={}].", registrationId);
			return false;
		}

		List<JWK> keys = new ArrayList<>();

		String kid = signedJWT.getHeader().getKeyID();
		if (StringUtils.isNullOrWhiteSpace(kid)) {
			LOGGER.debug("JWS header does not contain KID.");
			// no kid specified; try all available keys
			keys = jwks.getKeys();
		} else {
			JWK jwk = jwks.getKeyByKeyId(kid);
			if (jwk != null) {
				keys.add(jwk);
			}
		}

		if (keys.size() == 0) {
			LOGGER.debug("No JWK available for client registration [registrationId={}].", registrationId);
			return false;
		}

		boolean isValid = false;

		for (JWK jwk : keys) {
			try {
				// assume keys are always RSA Keys
				JWSVerifier jwsVerifier = new RSASSAVerifier((RSAKey) jwk);
				isValid = signedJWT.verify(jwsVerifier);
				if (isValid) {
					break;
				}
			} catch (JOSEException e) {
				LOGGER.error("Error occurred while validating JWT signature.", e);
			}
		}

		return isValid;
	}

	private String resolveJwksUri(final String registrationId) {
		ClientRegistration clientRegistration = clientRegistrationManager.getClientRegistration(registrationId);
		String jwksUri = null;

		if (clientRegistration != null) {
			jwksUri = (String) clientRegistration.getProviderDetails().getJwkSetUri();
			if (jwksUri == null
					&& clientRegistration.getProviderDetails().getConfigurationMetadata().containsKey("jwks_uri")) {
				jwksUri = (String) clientRegistration.getProviderDetails().getConfigurationMetadata().get("jwks_uri");
			}
		}
		return jwksUri;
	}
}
