package bcsc.sample.client.jwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.context.junit4.SpringRunner;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import bcsc.sample.client.jwt.DefaultJWTSignatureValidationService;
import bcsc.sample.client.jwt.JWKSetLoader;
import bcsc.sample.client.service.ClientRegistrationManager;
import bcsc.sample.client.util.StringUtils;

@RunWith(SpringRunner.class)
public class DefaultJWTSignatureValidationServiceTest {

	private static final String PROVIDER_1_JWKS_URI = "https://gov.bc.ca/jwk";
	private static final String PROVIDER_2_JWKS_URI = "https://localhost/jwk";

	@Mock
	private ClientRegistrationManager clientRegistrationManager;
	@Mock
	private JWKSetLoader jwkSetLoader;

	private DefaultJWTSignatureValidationService service;
	private Map<String, ClientRegistration> clientRegistrationMap = new HashMap<>();
	private Map<String, JWKSet> providersJWKSet = new HashMap<>();

	@Before
	public void setup() throws Exception {
		service = new DefaultJWTSignatureValidationService(clientRegistrationManager, jwkSetLoader, 1);
		service.init();
		createClientRegistrations();
		generateJwkSets();

		given(clientRegistrationManager.getClientRegistrations()).willAnswer(new Answer<List<ClientRegistration>>() {
			@Override
			public List<ClientRegistration> answer(InvocationOnMock invocation) throws Throwable {
				return clientRegistrationMap.values().stream().collect(Collectors.toList());
			}
		});

		given(clientRegistrationManager.getClientRegistration(anyString()))
				.willAnswer(new Answer<ClientRegistration>() {
					@Override
					public ClientRegistration answer(InvocationOnMock invocation) throws Throwable {
						return clientRegistrationMap.get(invocation.getArgument(0));
					}
				});

		given(jwkSetLoader.load(anyString())).willAnswer(new Answer<JWKSet>() {
			@Override
			public JWKSet answer(InvocationOnMock invocation) throws Throwable {
				String jwkUri = invocation.getArgument(0);
				JWKSet jwkSet = providersJWKSet.get(jwkUri);
				if (jwkSet != null) {
					return jwkSet.toPublicJWKSet();
				}
				return null;
			}
		});
	}

	@Test
	public void testGetProviderJwkSet() {
		JWKSet client1Jwks = service.getProviderJwkSet("client1");
		assertNotNull(client1Jwks);
		assertEquals(1, client1Jwks.getKeys().size());
		assertNotNull(client1Jwks.getKeyByKeyId("provider2-key1"));

		JWKSet client2Jwks = service.getProviderJwkSet("client2");
		assertNotNull(client2Jwks);
		assertEquals(2, client2Jwks.getKeys().size());
		assertNotNull(client2Jwks.getKeyByKeyId("provider1-key1"));
		assertNotNull(client2Jwks.getKeyByKeyId("provider1-key2"));

		JWKSet client3Jwks = service.getProviderJwkSet("client3");
		assertNull(client3Jwks);
	}

	@Test
	public void testValidateSignature_CannotFindProviderJWKSet() throws JOSEException {
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().audience("https://gov.bc.ca/sample-client")
				.issuer("https://gov.bc.ca").build();
		SignedJWT signedJWT = signJWT(claimsSet, PROVIDER_1_JWKS_URI, "provider1-key2", null);

		boolean result = service.validateSignature("client3", signedJWT);

		assertFalse(result);
	}

	@Test
	public void testValidateSignature_CannotFindKeyWithKeyId() throws JOSEException {
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().audience("https://gov.bc.ca/sample-client")
				.issuer("https://gov.bc.ca").build();
		SignedJWT signedJWT = signJWT(claimsSet, PROVIDER_1_JWKS_URI, "provider1-key2", "some-other-kid");

		boolean result = service.validateSignature("client2", signedJWT);

		assertFalse(result);
	}

	@Test
	public void testValidateSignature_NoKeyIdProvidedInJWSHeader() throws JOSEException {
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().audience("https://gov.bc.ca/sample-client")
				.issuer("https://gov.bc.ca").build();
		SignedJWT signedJWT = signJWT(claimsSet, PROVIDER_1_JWKS_URI, "provider1-key2", "");

		boolean result = service.validateSignature("client2", signedJWT);

		assertTrue(result);
	}

	@Test
	public void testValidateSignature_InvalidSignature() throws JOSEException {
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().audience("https://gov.bc.ca/sample-client")
				.issuer("https://gov.bc.ca").build();
		SignedJWT signedJWT = signJWT(claimsSet, PROVIDER_1_JWKS_URI, "provider1-key1", "provider1-key2");

		boolean result = service.validateSignature("client2", signedJWT);

		assertFalse(result);
	}

	@Test
	public void testValidateSignature_ValidSignature() throws JOSEException {
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().audience("https://gov.bc.ca/sample-client")
				.issuer("https://gov.bc.ca").build();
		SignedJWT signedJWT = signJWT(claimsSet, PROVIDER_1_JWKS_URI, "provider1-key2", null);

		boolean result = service.validateSignature("client2", signedJWT);

		assertTrue(result);
	}

	private void createClientRegistrations() {
		Map<String, Object> client1ProviderMetadata = new HashMap<>();
		client1ProviderMetadata.put("jwks_uri", PROVIDER_2_JWKS_URI);
		// client1 uses provider2
		ClientRegistration clientRegistration1 = ClientRegistration.withRegistrationId("client1").clientId("client1")
				.providerConfigurationMetadata(client1ProviderMetadata)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUriTemplate("https://localhost/oauth2/code").authorizationUri("https://localhost/oauth2/auth")
				.tokenUri("https://localhost/oauth2/token").build();
		// client2 uses provider1
		Map<String, Object> client2ProviderMetadata = new HashMap<>();
		client2ProviderMetadata.put("jwks_uri", PROVIDER_1_JWKS_URI);
		ClientRegistration clientRegistration2 = ClientRegistration.withRegistrationId("client2").clientId("client2")
				.providerConfigurationMetadata(client2ProviderMetadata)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUriTemplate("https://localhost/oauth2/code").authorizationUri("https://localhost/oauth2/auth")
				.tokenUri("https://localhost/oauth2/token").build();
		clientRegistrationMap.put("client1", clientRegistration1);
		clientRegistrationMap.put("client2", clientRegistration2);
	}

	private void generateJwkSets() throws JOSEException {
		JWKSet provider1JwkSet = new JWKSet(
				Arrays.asList(new RSAKeyGenerator(2048).keyUse(KeyUse.SIGNATURE).keyID("provider1-key1").generate(),
						new RSAKeyGenerator(2048).keyUse(KeyUse.SIGNATURE).keyID("provider1-key2").generate()));
		JWKSet provider2JwkSet = new JWKSet(
				new RSAKeyGenerator(2048).keyUse(KeyUse.SIGNATURE).keyID("provider2-key1").generate());

		providersJWKSet.put(PROVIDER_1_JWKS_URI, provider1JwkSet);
		providersJWKSet.put(PROVIDER_2_JWKS_URI, provider2JwkSet);
	}

	/**
	 * Create a signed JWT.
	 * 
	 * @param claimsSet
	 *            the JWT claims set.
	 * @param providerJwkUri
	 *            the provider JWK URI (used for key lookup)
	 * @param keyIdToSign
	 *            the key ID (kid) used for signing. If {@code null}, will use
	 *            first available key in the JWK set.
	 * @param keyIdInHeader
	 *            the key ID (kid) used in the JWS header (for testing
	 *            purposes). If {@code null}, the actual key ID that was used to
	 *            sign the JWT will be used in the JWS header. If empty string,
	 *            KID attribute will not be added to the JWS header.
	 * @return the {@link SignedJWT}.
	 */
	private SignedJWT signJWT(final JWTClaimsSet claimsSet, final String providerJwkUri, final String keyIdToSign,
			final String keyIdInHeader) throws JOSEException {
		JWKSet jwkSet = providersJWKSet.get(providerJwkUri);
		if (jwkSet == null || jwkSet.getKeys().size() == 0) {
			throw new IllegalArgumentException("No JWKSet for the provided provider JWK URI.");
		}

		// gets the JWK for signing
		JWK jwk;
		if (StringUtils.isNullOrWhiteSpace(keyIdToSign)) {
			jwk = jwkSet.getKeys().get(0);
		} else {
			jwk = jwkSet.getKeyByKeyId(keyIdToSign);
			if (jwk == null) {
				throw new IllegalArgumentException("No JWK associated with the kid specified.");
			}
		}

		JWSSigner jwsSigner = new RSASSASigner((RSAKey) jwk);

		JWSHeader.Builder jwsHeaderBuilder = new JWSHeader.Builder(JWSAlgorithm.RS256);
		if (keyIdInHeader == null) {
			jwsHeaderBuilder.keyID(jwk.getKeyID());
		} else if (!keyIdInHeader.isEmpty()) {
			jwsHeaderBuilder.keyID(keyIdInHeader);
		}

		SignedJWT jwt = new SignedJWT(jwsHeaderBuilder.build(), claimsSet);

		jwt.sign(jwsSigner);

		return jwt;
	}
}
