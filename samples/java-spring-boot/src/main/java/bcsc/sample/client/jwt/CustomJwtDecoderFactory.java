package bcsc.sample.client.jwt;

import java.io.File;
import java.util.Arrays;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.proc.JWEKeySelector;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWEDecryptionKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;

import bcsc.sample.client.service.ClientRegistrationManager;

/*
 * This CustomJwtDecoderFactory is the simplest way currently to add decryption to the Spring Boot implementation of JWT,
 * which currently does not support JWE out the box (if it did you could just add the values in the application.properties)
 * By declaring a JwtDecoderFactory bean it magically gets used in place of the existing default JwtDecoderFactory.
 * 
 * In it we need to declare the keys which get used for encryption of the JWT as well as the signing thereof. 
 * These keys are contained in public JWKS Url (which defines the signing public key)  and private JWKS files (which define the 
 * encryption private key ) . 
 * 
 *  These file names, expected url, signing and encryption algorithms etc. are all pulled in through Spring Boot magic through the 
 *  @ConfigurationProperties annotation from the application.properties file.
 *  
 *  Then they are plugged into the default Jose DefautJWTProcessor which is what Spring Boot uses by default (albeit with no decryption) 
 *   which automagically manages the whole JWT/OIDC rigmarole for us. 
 */

@Component
@ConfigurationProperties(prefix = "bcsc.sample.client.jwt")
public class CustomJwtDecoderFactory  {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomJwtDecoderFactory.class);
	
	private String  privateJWKSFile;
	private String  publicJWKSURL;
	private JWEAlgorithm expectedJWEAlg;
	private JWSAlgorithm expectedJWSAlg;
	private EncryptionMethod expectedJWEEnc;
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
	catch (Exception e ) {LOGGER.debug("Exception getting keysource from file " , e);return null; }

	}
	private  JWKSource getJWKSURL (String url, String keyId)  {
		// Load JWK set from URL.
		try {
			JWKSource keySource = new RemoteJWKSet<>(new URL(url));
			// Create JWK source backed by a JWK set
			return keySource;
		}
		catch (Exception e ) {LOGGER.debug("Exception getting keysource from remoteURL " , e);return null; }
	}
	
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
		
		JWSKeySelector jwsKeySelector =  new JWSVerificationKeySelector<>(expectedJWSAlg, getJWKSURL (this.publicJWKSURL, "") );
 		jwtProcessor.setJWSKeySelector(jwsKeySelector);
	
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
}
