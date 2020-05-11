package bcsc.sample.client.jwt;

import java.text.ParseException;
import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;

import bcsc.sample.client.service.KeyManagementService;

@Service
public class DefaultJWTDecryptionService implements JWTDecryptionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJWTDecryptionService.class);

	private KeyManagementService keyManagementService;

	public DefaultJWTDecryptionService(@NonNull final KeyManagementService keyManagementService) {
		this.keyManagementService = keyManagementService;
	}

	@Override
	public JWT decrypt(@NonNull final String registrationId, @NonNull final EncryptedJWT encryptedJWT) {
		LOGGER.debug("Decrypting JWT [registrationId={}, encryptedJWT={}].", registrationId, encryptedJWT.serialize());

		List<RSAKey> rsaKeys = keyManagementService.getClientRSAKeys(registrationId);

		String keyId = encryptedJWT.getHeader().getKeyID();
		RSAKey candidateRsaKey = findDecryptionKey(rsaKeys, keyId);

		if (candidateRsaKey != null) {
			try {
				LOGGER.debug("Decrypting JWT using RSAKey [keyID={}].", candidateRsaKey.getKeyID());
				encryptedJWT.decrypt(new RSADecrypter(candidateRsaKey));
				SignedJWT jws = encryptedJWT.getPayload().toSignedJWT();
				if (jws == null) {
					// JWT is not signed
					try {
						return new PlainJWT(encryptedJWT.getJWTClaimsSet());
					} catch (ParseException e) {
						LOGGER.error("Failed to retrieve JWT claims set from decrypted JWT.", e);
					}
				} else {
					return jws;
				}
			} catch (JOSEException e) {
				LOGGER.error(String.format("JWT decryption failed [registrationId=%s, keyId=%s, encryptedJWT=%s].",
						registrationId, keyId, encryptedJWT.serialize()), e);
			}
		} else {
			LOGGER.info("No key found for JWT decryption [registrationId={}, keyId={}].", registrationId, keyId);
		}

		return null;
	}

	static RSAKey findDecryptionKey(final List<RSAKey> rsaKeys, final String keyId) {
		Predicate<RSAKey> decryptionKeyPredicate = key -> key.isPrivate()
				&& (key.getKeyUse() == null || key.getKeyUse() == KeyUse.ENCRYPTION);
		Predicate<RSAKey> emptyKeyIdPredicate = key -> Strings.isNullOrEmpty(keyId)
				&& Strings.isNullOrEmpty(key.getKeyID());
		Predicate<RSAKey> nonEmptyKeyIdPredicate = key -> key.getKeyID() != null && key.getKeyID().equals(keyId);

		return rsaKeys.stream().filter(decryptionKeyPredicate.and(emptyKeyIdPredicate.or(nonEmptyKeyIdPredicate)))
				.findAny().orElse(null);
	}

}
