package bcsc.sample.client.jwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;

import bcsc.sample.client.jwt.DefaultJWTDecryptionService;
import bcsc.sample.client.service.InMemoryKeyManagementService;

public class DefaultJWTDecryptionServiceTest {

	private static final String SIGNING_KEY_ID = "signing-key";
	private static final String ENCRYPTION_KEY_ID = "encryption-key";
	private static RSAKey SIGNING_KEY;
	private static RSAKey ENCRYPTION_KEY;
	private static List<RSAKey> rsaKeys = new ArrayList<>();
	private static JWTClaimsSet jwtClaimsSet;

	private DefaultJWTDecryptionService service;

	@Mock
	private InMemoryKeyManagementService keyManagementService;

	@BeforeClass
	public static void init() throws JOSEException {
		ENCRYPTION_KEY = new RSAKeyGenerator(2048).keyUse(KeyUse.ENCRYPTION).keyID(ENCRYPTION_KEY_ID).generate();
		SIGNING_KEY = new RSAKeyGenerator(2048).keyUse(KeyUse.SIGNATURE).keyID(SIGNING_KEY_ID).generate();
		rsaKeys.addAll(Arrays.asList(SIGNING_KEY, ENCRYPTION_KEY));
		jwtClaimsSet = new JWTClaimsSet.Builder().audience("audience").issuer("issuer").subject("subject")
				.jwtID(UUID.randomUUID().toString()).build();
	}

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		service = new DefaultJWTDecryptionService(keyManagementService);
		given(keyManagementService.getClientRSAKeys(anyString())).willReturn(rsaKeys);
	}

	@Test
	public void testDecrypt_SignedJWT() throws JOSEException, ParseException {
		EncryptedJWT jwe = generateEncryptedJWT(true, null);

		JWT jwt = service.decrypt(UUID.randomUUID().toString(), jwe);

		assertNotNull(jwt);
		assertTrue(jwt instanceof SignedJWT);
		assertClaims(jwt);
	}

	@Test
	public void testDecrypt_PlainJWT() throws JOSEException, ParseException {
		EncryptedJWT jwe = generateEncryptedJWT(false, null);

		JWT jwt = service.decrypt(UUID.randomUUID().toString(), jwe);

		assertNotNull(jwt);
		assertTrue(jwt instanceof PlainJWT);
		assertClaims(jwt);
	}

	@Test
	public void testDecrypt_NoDecryptionKeyFound() throws JOSEException, ParseException {
		RSAKey encryptionKey = new RSAKeyGenerator(2048).keyUse(KeyUse.ENCRYPTION).keyID("key3").generate();
		EncryptedJWT jwe = generateEncryptedJWT(false, encryptionKey);

		JWT jwt = service.decrypt(UUID.randomUUID().toString(), jwe);

		assertNull(jwt);
	}

	@Test
	public void testFindDecryptionKey() throws JOSEException {
		RSAKey key1 = new RSAKeyGenerator(2048).keyID("key1").generate();
		RSAKey key2 = new RSAKeyGenerator(2048).keyUse(KeyUse.ENCRYPTION).generate();
		RSAKey key3 = new RSAKeyGenerator(2048).keyUse(KeyUse.ENCRYPTION).keyID("key3").generate().toPublicJWK();
		RSAKey key4 = new RSAKeyGenerator(2048).keyUse(KeyUse.SIGNATURE).keyID("key4").generate();

		RSAKey candidateKey = DefaultJWTDecryptionService.findDecryptionKey(Arrays.asList(key1, key2, key3, key4),
				null);

		assertNotNull(candidateKey);
		assertEquals(key2.getKeyID(), candidateKey.getKeyID());
		assertEquals(key2.getModulus(), candidateKey.getModulus());

		candidateKey = DefaultJWTDecryptionService.findDecryptionKey(Arrays.asList(key2, key3, key1, key4), "key1");

		assertNotNull(candidateKey);
		assertEquals(key1.getKeyID(), candidateKey.getKeyID());
		assertEquals(key1.getModulus(), candidateKey.getModulus());

		candidateKey = DefaultJWTDecryptionService.findDecryptionKey(Arrays.asList(key3, key1, key2, key4), "key2");

		assertNull(candidateKey);

		candidateKey = DefaultJWTDecryptionService.findDecryptionKey(Arrays.asList(key3, key1, key2, key4), "key3");

		assertNull(candidateKey);

		candidateKey = DefaultJWTDecryptionService.findDecryptionKey(Arrays.asList(key3, key1, key2, key4), "key4");

		assertNull(candidateKey);
	}

	private EncryptedJWT generateEncryptedJWT(final boolean signed, final RSAKey encryptionKey)
			throws JOSEException, ParseException {
		JWT jwt = new PlainJWT(jwtClaimsSet);

		if (signed) {
			JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(SIGNING_KEY_ID).build();
			jwt = new SignedJWT(jwsHeader, jwtClaimsSet);
			RSASSASigner signer = new RSASSASigner(SIGNING_KEY);
			((SignedJWT) jwt).sign(signer);
		}

		RSAKey rsaKey = (encryptionKey == null) ? ENCRYPTION_KEY : encryptionKey;
		JWEEncrypter encrypter = new RSAEncrypter(rsaKey);
		JWEHeader jweHeader = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM).contentType("JWT")
				.keyID(rsaKey.getKeyID()).build();
		if (jwt instanceof SignedJWT) {
			JWEObject jweObject = new JWEObject(jweHeader, new Payload((SignedJWT) jwt));
			jweObject.encrypt(encrypter);
			jwt = JWTParser.parse(jweObject.serialize());
		} else {
			jwt = new EncryptedJWT(jweHeader, jwt.getJWTClaimsSet());
			((EncryptedJWT) jwt).encrypt(encrypter);
		}

		return (EncryptedJWT) jwt;
	}

	private void assertClaims(JWT jwt) throws ParseException {
		JWTClaimsSet thisClaimsSet = jwt.getJWTClaimsSet();
		assertEquals(jwtClaimsSet.getAudience(), thisClaimsSet.getAudience());
		assertEquals(jwtClaimsSet.getIssuer(), thisClaimsSet.getIssuer());
		assertEquals(jwtClaimsSet.getSubject(), thisClaimsSet.getSubject());
	}
}
