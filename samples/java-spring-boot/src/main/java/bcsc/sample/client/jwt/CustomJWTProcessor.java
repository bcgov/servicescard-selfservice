package bcsc.sample.client.jwt;

import java.security.Key;
import java.text.ParseException;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.crypto.factories.DefaultJWEDecrypterFactory;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.proc.*;
import com.nimbusds.jwt.*;
import com.nimbusds.jwt.proc.*;


/* This CustomJWTProcessor is an exact copy of the standard JOSE DefaultJWTDecoder with the exception of a single line (and some debug logging) 
 * - the check for the content-type parameter on successful decryption which should return a JWT is removed because the JOSE header does  not currently
 * return cpntent type ("cty" = "JWT") . When it does this file can be removed. And the CustomJwtDecoderFactory can use the DefaultJwtProcessor.
 * 
 * CHeck out line 370 
 */
public class CustomJWTProcessor<C extends SecurityContext>
	implements ConfigurableJWTProcessor<C> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomJWTProcessor.class);

	// Cache exceptions
	private static final BadJOSEException PLAIN_JWT_REJECTED_EXCEPTION =
		new BadJOSEException("Unsecured (plain) JWTs are rejected, extend class to handle");
	private static final BadJOSEException NO_JWS_KEY_SELECTOR_EXCEPTION =
		new BadJOSEException("Signed JWT rejected: No JWS key selector is configured");
	private static final BadJOSEException NO_JWE_KEY_SELECTOR_EXCEPTION =
		new BadJOSEException("Encrypted JWT rejected: No JWE key selector is configured");
	private static final JOSEException NO_JWS_VERIFIER_FACTORY_EXCEPTION =
		new JOSEException("No JWS verifier is configured");
	private static final JOSEException NO_JWE_DECRYPTER_FACTORY_EXCEPTION =
		new JOSEException("No JWE decrypter is configured");
	private static final BadJOSEException NO_JWS_KEY_CANDIDATES_EXCEPTION =
		new BadJOSEException("Signed JWT rejected: Another algorithm expected, or no matching key(s) found");
	private static final BadJOSEException NO_JWE_KEY_CANDIDATES_EXCEPTION =
		new BadJOSEException("Encrypted JWT rejected: Another algorithm expected, or no matching key(s) found");
	private static final BadJOSEException INVALID_SIGNATURE =
		new BadJWSException("Signed JWT rejected: Invalid signature");
	private static final BadJWTException INVALID_NESTED_JWT_EXCEPTION =
		new BadJWTException("The payload is not a nested signed JWT");
	private static final BadJOSEException NO_MATCHING_VERIFIERS_EXCEPTION =
		new BadJOSEException("JWS object rejected: No matching verifier(s) found");
	private static final BadJOSEException NO_MATCHING_DECRYPTERS_EXCEPTION =
		new BadJOSEException("Encrypted JWT rejected: No matching decrypter(s) found");

	/**
	 * The JWS key selector.
	 */
	private JWSKeySelector<C> jwsKeySelector;
	
	
	/**
	 * The JWT claims aware JWS key selector, alternative to
	 * {@link #jwsKeySelector}.
	 */
	private JWTClaimsSetAwareJWSKeySelector<C> claimsSetAwareJWSKeySelector;


	/**
	 * The JWE key selector.
	 */
	private JWEKeySelector<C> jweKeySelector;


	/**
	 * The JWS verifier factory.
	 */
	private JWSVerifierFactory jwsVerifierFactory = new DefaultJWSVerifierFactory();


	/**
	 * The JWE decrypter factory.
	 */
	private JWEDecrypterFactory jweDecrypterFactory = new DefaultJWEDecrypterFactory();


	/**
	 * The claims verifier.
	 */
	private JWTClaimsSetVerifier<C> claimsVerifier = new DefaultJWTClaimsVerifier<>();
	
	
	/**
	 * The deprecated claims verifier.
	 */
	private JWTClaimsVerifier deprecatedClaimsVerifier = null;


	@Override
	public JWSKeySelector<C> getJWSKeySelector() {

		return jwsKeySelector;
	}


	@Override
	public void setJWSKeySelector(final JWSKeySelector<C> jwsKeySelector) {

		this.jwsKeySelector = jwsKeySelector;
	}
	
	
	@Override
	public JWTClaimsSetAwareJWSKeySelector<C> getJWTClaimsSetAwareJWSKeySelector() {
		
		return claimsSetAwareJWSKeySelector;
	}
	
	
	@Override
	public void setJWTClaimsSetAwareJWSKeySelector(final JWTClaimsSetAwareJWSKeySelector<C> jwsKeySelector) {
	
		this.claimsSetAwareJWSKeySelector = jwsKeySelector;
	}
	
	
	@Override
	public JWEKeySelector<C> getJWEKeySelector() {

		return jweKeySelector;
	}


	@Override
	public void setJWEKeySelector(final JWEKeySelector<C> jweKeySelector) {

		this.jweKeySelector = jweKeySelector;
	}


	@Override
	public JWSVerifierFactory getJWSVerifierFactory() {

		return jwsVerifierFactory;
	}


	@Override
	public void setJWSVerifierFactory(final JWSVerifierFactory factory) {

		jwsVerifierFactory = factory;
	}


	@Override
	public JWEDecrypterFactory getJWEDecrypterFactory() {

		return jweDecrypterFactory;
	}


	@Override
	public void setJWEDecrypterFactory(final JWEDecrypterFactory factory) {

		jweDecrypterFactory = factory;
	}
	
	
	@Override
	public JWTClaimsSetVerifier<C> getJWTClaimsSetVerifier() {
		
		return claimsVerifier;
	}
	
	
	@Override
	public void setJWTClaimsSetVerifier(final JWTClaimsSetVerifier<C> claimsVerifier) {
		
		this.claimsVerifier = claimsVerifier;
		this.deprecatedClaimsVerifier = null; // clear other verifier
	}
	
	
	@Override
	@Deprecated
	public JWTClaimsVerifier getJWTClaimsVerifier() {

		return deprecatedClaimsVerifier;
	}


	@Override
	@Deprecated
	public void setJWTClaimsVerifier(final JWTClaimsVerifier claimsVerifier) {

		this.claimsVerifier = null; // clear official verifier
		this.deprecatedClaimsVerifier = claimsVerifier;
	}
	
	
	private JWTClaimsSet extractJWTClaimsSet(final JWT jwt)
		throws BadJWTException {
		
		try {
			return jwt.getJWTClaimsSet();
		} catch (ParseException e) {
			// Payload not a JSON object
			throw new BadJWTException(e.getMessage(), e);
		}
	}


	private JWTClaimsSet verifyClaims(final JWTClaimsSet claimsSet, final C context)
		throws BadJWTException {
		
		if (getJWTClaimsSetVerifier() != null) {
			getJWTClaimsSetVerifier().verify(claimsSet, context);
		} else if (getJWTClaimsVerifier() != null) {
			// Fall back to deprecated claims verifier
			getJWTClaimsVerifier().verify(claimsSet);
		}
		return claimsSet;
	}
	
	
	private List<? extends Key> selectKeys(final JWSHeader header, final JWTClaimsSet claimsSet, final C context)
		throws KeySourceException, BadJOSEException {
		
		if (getJWTClaimsSetAwareJWSKeySelector() != null) {
			return getJWTClaimsSetAwareJWSKeySelector().selectKeys(header, claimsSet, context);
		} else if (getJWSKeySelector() != null) {
			return getJWSKeySelector().selectJWSKeys(header, context);
		} else {
			throw NO_JWS_KEY_SELECTOR_EXCEPTION;
		}
	}


	@Override
	public JWTClaimsSet process(final String jwtString, final C context)
		throws ParseException, BadJOSEException, JOSEException {

		return process(JWTParser.parse(jwtString), context);
	}


	@Override
	public JWTClaimsSet process(final JWT jwt, final C context)
		throws BadJOSEException, JOSEException {

		if (jwt instanceof SignedJWT) {
			return process((SignedJWT)jwt, context);
		}

		if (jwt instanceof EncryptedJWT) {
			return process((EncryptedJWT)jwt, context);
		}

		if (jwt instanceof PlainJWT) {
			return process((PlainJWT)jwt, context);
		}

		// Should never happen
		throw new JOSEException("Unexpected JWT object type: " + jwt.getClass());
	}


	@Override
	public JWTClaimsSet process(final PlainJWT plainJWT, final C context)
		throws BadJOSEException, JOSEException {

		throw PLAIN_JWT_REJECTED_EXCEPTION;
	}


	@Override
	public JWTClaimsSet process(final SignedJWT signedJWT, final C context)
		throws BadJOSEException, JOSEException {

		if (getJWSKeySelector() == null && getJWTClaimsSetAwareJWSKeySelector() == null) {
			// JWS key selector may have been deliberately omitted
			throw NO_JWS_KEY_SELECTOR_EXCEPTION;
		}

		if (getJWSVerifierFactory() == null) {
			throw NO_JWS_VERIFIER_FACTORY_EXCEPTION;
		}
		
		JWTClaimsSet claimsSet = extractJWTClaimsSet(signedJWT);

		List<? extends Key> keyCandidates = selectKeys(signedJWT.getHeader(), claimsSet, context);

		if (keyCandidates == null || keyCandidates.isEmpty()) {
			throw NO_JWS_KEY_CANDIDATES_EXCEPTION;
		}

		ListIterator<? extends Key> it = keyCandidates.listIterator();

		while (it.hasNext()) {

			JWSVerifier verifier = getJWSVerifierFactory().createJWSVerifier(signedJWT.getHeader(), it.next());

			if (verifier == null) {
				continue;
			}

			final boolean validSignature = signedJWT.verify(verifier);

			if (validSignature) {
				return verifyClaims(claimsSet, context);
			}

			if (! it.hasNext()) {
				// No more keys to try out
				throw INVALID_SIGNATURE;
			}
		}

		throw NO_MATCHING_VERIFIERS_EXCEPTION;
	}


	@Override
	public JWTClaimsSet process(final EncryptedJWT encryptedJWT, final C context)
		throws BadJOSEException, JOSEException {

		if (getJWEKeySelector() == null) {
			// JWE key selector may have been deliberately omitted
			throw NO_JWE_KEY_SELECTOR_EXCEPTION;
		}

		if (getJWEDecrypterFactory() == null) {
			throw NO_JWE_DECRYPTER_FACTORY_EXCEPTION;
		}

		List<? extends Key> keyCandidates = getJWEKeySelector().selectJWEKeys(encryptedJWT.getHeader(), context);

		if (keyCandidates == null || keyCandidates.isEmpty()) {
			throw NO_JWE_KEY_CANDIDATES_EXCEPTION;
		}

		ListIterator<? extends Key> it = keyCandidates.listIterator();

		while (it.hasNext()) {

			JWEDecrypter decrypter = getJWEDecrypterFactory().createJWEDecrypter(encryptedJWT.getHeader(), it.next());
LOGGER.info("Decrypted header is  {}", encryptedJWT.getHeader().toString());
LOGGER.info("In decrypter {}", decrypter.toString());
			if (decrypter == null) {
				continue;
			}

			try {
				encryptedJWT.decrypt(decrypter);

			} catch (JOSEException e) {

				if (it.hasNext()) {
					// Try next key
					continue;
				}

				// No more keys to try
				throw new BadJWEException("Encrypted JWT rejected: " + e.getMessage(), e);
			}

LOGGER.info("Decrypted JWT is {}", encryptedJWT.getPayload().toSignedJWT().serialize().toString());
/******* REPLACED CHECK FOR JWT IN JOSE HEADER WITH JUST ASSUME IT IS  **************/
//			if ("JWT".equalsIgnoreCase(encryptedJWT.getHeader().getContentType())) {
			if (true ) {

				// Handle nested signed JWT, see http://tools.ietf.org/html/rfc7519#section-5.2
				SignedJWT signedJWTPayload = encryptedJWT.getPayload().toSignedJWT();

				if (signedJWTPayload == null) {
					// Cannot parse payload to signed JWT
					throw INVALID_NESTED_JWT_EXCEPTION;
				}

				return process(signedJWTPayload, context);
			}

			JWTClaimsSet claimsSet = extractJWTClaimsSet(encryptedJWT);
LOGGER.info("Claims set is  {}", claimsSet.toString());
			return verifyClaims(claimsSet, context);
		}

		throw NO_MATCHING_DECRYPTERS_EXCEPTION;
	}
}