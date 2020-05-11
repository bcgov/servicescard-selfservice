package bcsc.sample.client.jwt;

import org.springframework.lang.NonNull;

import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;

public interface JWTDecryptionService {

	/**
	 * Decrypts the JWE.
	 * 
	 * @param registrationId
	 *            the client's registration ID.
	 * @param encryptedJWT
	 *            the encrypted JWT.
	 * @return the decrypted JWT or {@code null} if JWT cannot be decrypted.
	 */
	public JWT decrypt(@NonNull final String registrationId, @NonNull final EncryptedJWT encryptedJWT);
}
