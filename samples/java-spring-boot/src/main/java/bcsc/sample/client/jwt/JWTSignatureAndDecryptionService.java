package bcsc.sample.client.jwt;

import org.springframework.lang.NonNull;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.JWTClaimsSet;


public interface JWTSignatureAndDecryptionService {
	public JWTClaimsSet decryptAndValidate(@NonNull final String registrationId, @NonNull final EncryptedJWT encryptedJWT) throws Throwable;
}
