package bcsc.sample.client.jwt;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.SignedJWT;

public interface JWTSignatureValidationService {
	/**
	 * Gets the provider's JWK set.
	 *
	 * @param registrationId
	 *            the client registration ID.
	 * @return the {@link JWKSet} for the provider or {@code null} if the
	 *         {@code registrationId} is not a registered client or if the JWK
	 *         set cannot be fetched.
	 */
	public JWKSet getProviderJwkSet(final String registrationId);

	/**
	 * Checks the signature of the given JWT using the JWK published by the
	 * issuer.
	 *
	 * @param registrationId
	 *            the client registration ID.
	 * @param signedJWT
	 *            the JWT.
	 * @return {@code true} if the signature is valid; {@code false} otherwise.
	 */
	public boolean validateSignature(final String registrationId, final SignedJWT signedJWT);
}
