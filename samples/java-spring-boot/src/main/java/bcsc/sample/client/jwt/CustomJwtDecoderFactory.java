package bcsc.sample.client.jwt;

import java.io.File;
import java.text.ParseException;
import java.util.List;
import java.util.HashSet;
import java.util.Arrays;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.google.common.base.Strings;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.*;
import com.nimbusds.jose.jwk.source.*;
import com.nimbusds.jwt.*;
import com.nimbusds.jwt.proc.*;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jose.proc.JWEKeySelector;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWEDecryptionKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import com.nimbusds.jose.proc.SecurityContext;

import bcsc.sample.client.service.KeyManagementService;
import bcsc.sample.client.service.ClientRegistrationManager;

@Component
@ConfigurationProperties(prefix = "bcsc.sample.client.jwt")
public class CustomJwtDecoderFactory  {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomJwtDecoderFactory.class);

	private KeyManagementService keyManagementService;
	
	private String  privateJWKSFile;
	private String  publicJWKSURL;
	private JWEAlgorithm expectedJWEAlg;
	private JWSAlgorithm expectedJWSAlg;
	private ClientRegistrationManager clientRegistrationManager;
	public String getPrivateJWKSFile() {
		return privateJWKSFile;
	}

	public void setPrivateJWKSFile(String privateJWKSFile) {
		this.privateJWKSFile = privateJWKSFile;
		LOGGER.debug ("Private JWKS file is" + privateJWKSFile);
	}

	public String getPublicJWKSURL() {
		return publicJWKSURL;
	}

	public void setPublicJWKSURL(String publicJWKSURL) {
		this.publicJWKSURL = publicJWKSURL;
		LOGGER.debug ("Public JWKS URL is" + publicJWKSURL);
	}

	private EncryptionMethod expectedJWEEnc;
	

	public JWSAlgorithm getExpectedJWSAlg() {
		return expectedJWSAlg;
	}

	public void setExpectedJWSAlg(JWSAlgorithm expectedJWSAlg) {
		this.expectedJWSAlg = expectedJWSAlg;
	}

	public JWEAlgorithm getExpectedJWEAlg() {
		return expectedJWEAlg;
	}

	public void setExpectedJWEAlg(JWEAlgorithm expectedJWEAlg) {
		this.expectedJWEAlg = expectedJWEAlg;
	}

	public EncryptionMethod getExpectedJWEEnc() {
		return expectedJWEEnc;
	}

	public void setExpectedJWEEnc(EncryptionMethod expectedJWEEnc) {
		this.expectedJWEEnc = expectedJWEEnc;
	}
	

	private  JWKSource getJWKSource (String keyfile, String keyId)  {
		// Load JWK set from JSON file, etc.
		try {
			JWKSet jwkSet = JWKSet.load(new File(keyfile));
			// Create JWK source backed by a JWK set
			JWKSource keySource = new ImmutableJWKSet(jwkSet);
			return keySource;
	}
	catch (Exception e ) {LOGGER.debug("Exception in custom JWT processor " , e);return null; }

	}

//	public DefaultJWTSignatureAndDecryptionService(@NonNull final KeyManagementService keyManagementService) throws Throwable  {
//		this.keyManagementService = keyManagementService;
//	}
	
	@Bean
    public JwtDecoderFactory<ClientRegistration> jwtDecoderFactory() {
        final JwtDecoder decoder = this.jwtDecoder();
        return new JwtDecoderFactory<ClientRegistration>() {
            @Override
            public JwtDecoder createDecoder(ClientRegistration context) {
                return decoder;
            }
        };

    }
	
	private JwtDecoder jwtDecoder() {
		return new NimbusJwtDecoder (this.getExtJwtProcessor());
	}

	@SuppressWarnings("unchecked")
	private JWTProcessor<SecurityContext> getExtJwtProcessor() {

		LOGGER.debug("In custom JWT processor ");
		ConfigurableJWTProcessor<SecurityContext> jwtProcessor =    new DefaultJWTProcessor<>();
		JWEKeySelector<SecurityContext> jweKeySelector =   
				new JWEDecryptionKeySelector<>(expectedJWEAlg, expectedJWEEnc, getJWKSource (this.privateJWKSFile, "") );
		jwtProcessor.setJWEKeySelector(jweKeySelector);
		
//		JWSKeySelector jwsKeySelector =  new JWSVerificationKeySelector<>(expectedJWSAlg, getJWKSource (this.publicJWKSURL, "") );
// 		jwtProcessor.setJWSKeySelector(jwsKeySelector);
 		
 	// Set the required JWT claims for access tokens issued by the Connect2id
 	// server, may differ with other servers
 	//	jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier());
 			
 			
// 	    new JWTClaimsSet.Builder().issuer("https://idtest.gov.bc.ca/oauth2/jwk").build(),
// 	    new HashSet<>(Arrays.asList("sub", "iat", "exp", "scp", "cid", "jti"))));

 	// Process the token
//	 	SecurityContext ctx = null; // optional context parameter, not required here
//	 	JWTClaimsSet claimsSet = jwtProcessor.process(encryptedJWT, null);
//		LOGGER.debug("Returned claim set [registrationId={}, claimsSet={}].", registrationId, claimsSet.toJSONObject());
	 	return   jwtProcessor;
	 	
		

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
	
/*
	private JWTProcessor<SecurityContext> jwtProcessor() {
		JWKSource<SecurityContext> jwsJwkSource = new RemoteJWKSet<>(this.jwkSetUri);
		JWSKeySelector<SecurityContext> jwsKeySelector =
				new JWSVerificationKeySelector<>(this.jwsAlgorithm, jwsJwkSource);

		JWKSource<SecurityContext> jweJwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey()));
		JWEKeySelector<SecurityContext> jweKeySelector =
				new JWEDecryptionKeySelector<>(this.jweAlgorithm, this.encryptionMethod, jweJwkSource);

		ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
		jwtProcessor.setJWSKeySelector(jwsKeySelector);
		jwtProcessor.setJWEKeySelector(jweKeySelector);

		return jwtProcessor;
	}
*/
}
