package bcsc.sample.client.service;

import java.util.List;

import com.nimbusds.jose.jwk.RSAKey;

public interface KeyManagementService {
	/**
	 * Gets a list of {@link RSAKey}s for a client.
	 *
	 * @param registrationId
	 *            the client registration ID.
	 * @return a list of {@link RSAKey}.
	 */
	public List<RSAKey> getClientRSAKeys(final String registrationId);
}
