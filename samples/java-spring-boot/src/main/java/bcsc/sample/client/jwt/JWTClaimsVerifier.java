package bcsc.sample.client.jwt;

import java.util.Date;

import org.springframework.lang.NonNull;

import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.util.DateUtils;

import bcsc.sample.client.enumeration.MessageProcessingError;
import bcsc.sample.client.exception.InvalidJWTException;
import bcsc.sample.client.util.StringUtils;

public class JWTClaimsVerifier extends DefaultJWTClaimsVerifier<SecurityContext> {

	private String acceptedAudience;
	private String acceptedIssuer;

	public JWTClaimsVerifier(@NonNull final String acceptedAudience, @NonNull final String acceptedIssuer) {
		this.acceptedAudience = acceptedAudience;
		this.acceptedIssuer = acceptedIssuer;
	}

	public void setMaxClockSkew(final int maxClockSkew) {
		super.setMaxClockSkew(maxClockSkew);
	}

	@Override
	public void verify(final JWTClaimsSet claimsSet, final SecurityContext context) throws InvalidJWTException {
		// call the superclass verify method to validate exp and nbf claims
		try {
			super.verify(claimsSet, context);
		} catch (BadJWTException e) {
			throw new InvalidJWTException(e.getMessage(), MessageProcessingError.EVENT_DATA);
		}

		// validate iss claim
		if (claimsSet.getIssuer() == null) {
			throw new InvalidJWTException("iss claim missing", MessageProcessingError.EVENT_DATA);
		} else if (!claimsSet.getIssuer().equals(acceptedIssuer)) {
			throw new InvalidJWTException("iss claim not accepted", MessageProcessingError.EVENT_DATA);
		}

		// validate iat claim
		if (claimsSet.getIssueTime() == null) {
			throw new InvalidJWTException("iat claim missing", MessageProcessingError.EVENT_DATA);
		} else if (!DateUtils.isBefore(claimsSet.getIssueTime(), new Date(), getMaxClockSkew())) {
			throw new InvalidJWTException(MessageProcessingError.EVENT_DATA);
		}

		// validate aud claim
		if (claimsSet.getAudience() == null || claimsSet.getAudience().size() == 0) {
			throw new InvalidJWTException("aud claim missing", MessageProcessingError.JWT_AUD);
		} else if (!claimsSet.getAudience().stream().anyMatch(aud -> aud.equals(acceptedAudience))) {
			throw new InvalidJWTException("aud claim not accepted", MessageProcessingError.JWT_AUD);
		}

		// validate sub claim
		if (StringUtils.isNullOrWhiteSpace(claimsSet.getSubject())) {
			throw new InvalidJWTException("sub claim missing", MessageProcessingError.EVENT_DATA);
		}

		// validate jti claim
		if (StringUtils.isNullOrWhiteSpace(claimsSet.getJWTID())) {
			throw new InvalidJWTException("jti claim missing", MessageProcessingError.EVENT_DATA);
		}
	}
}
