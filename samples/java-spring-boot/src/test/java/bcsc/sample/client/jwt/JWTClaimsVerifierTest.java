package bcsc.sample.client.jwt;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;

import bcsc.sample.client.enumeration.MessageProcessingError;
import bcsc.sample.client.exception.InvalidJWTException;
import bcsc.sample.client.jwt.JWTClaimsVerifier;

public class JWTClaimsVerifierTest {

	private JWTClaimsVerifier jwtClaimsVerifier;

	private final String clientId = "client-1";
	private final String issuer = "https://id.gov.bc.ca";

	@Before
	public void setup() {
		jwtClaimsVerifier = new JWTClaimsVerifier(clientId, issuer);
	}

	@Test
	public void testVerify_NoIssuerClaim() {
		// @formatter:off
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.audience(clientId)
				.issueTime(new Date())
				.subject(UUID.randomUUID().toString())
				.jwtID(UUID.randomUUID().toString())
				.build();
		// @formatter:on

		try {
			jwtClaimsVerifier.verify(claimsSet);
			fail("Expected InvalidJWTException");
		} catch (BadJWTException e) {
			if (e instanceof InvalidJWTException) {
				assertEquals(MessageProcessingError.EVENT_DATA, ((InvalidJWTException) e).getMessageProcessingError());
			} else {
				fail("Expected InvalidJWTException");
			}
		}
	}

	@Test
	public void testVerify_IssuerNotAccepted() {
		// @formatter:off
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.issuer("invalid-issuer")
				.audience(clientId)
				.issueTime(new Date())
				.subject(UUID.randomUUID().toString())
				.jwtID(UUID.randomUUID().toString())
				.build();
		// @formatter:on

		try {
			jwtClaimsVerifier.verify(claimsSet);
			fail("Expected InvalidJWTException");
		} catch (BadJWTException e) {
			if (e instanceof InvalidJWTException) {
				assertEquals(MessageProcessingError.EVENT_DATA, ((InvalidJWTException) e).getMessageProcessingError());
			} else {
				fail("Expected InvalidJWTException");
			}
		}
	}

	@Test
	public void testVerify_NoIssueTimeClaim() {
		// @formatter:off
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.issuer(issuer)
				.audience(clientId)
				.subject(UUID.randomUUID().toString())
				.jwtID(UUID.randomUUID().toString())
				.build();
		// @formatter:on

		try {
			jwtClaimsVerifier.verify(claimsSet);
			fail("Expected InvalidJWTException");
		} catch (BadJWTException e) {
			if (e instanceof InvalidJWTException) {
				assertEquals(MessageProcessingError.EVENT_DATA, ((InvalidJWTException) e).getMessageProcessingError());
			} else {
				fail("Expected InvalidJWTException");
			}
		}
	}

	@Test
	public void testVerify_FutureIssueTime() {
		// @formatter:off
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.issuer(issuer)
				.issueTime(Date.from(LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant()))
				.audience(clientId)
				.subject(UUID.randomUUID().toString())
				.jwtID(UUID.randomUUID().toString())
				.build();
		// @formatter:on

		try {
			jwtClaimsVerifier.verify(claimsSet);
			fail("Expected InvalidJWTException");
		} catch (BadJWTException e) {
			if (e instanceof InvalidJWTException) {
				assertEquals(MessageProcessingError.EVENT_DATA, ((InvalidJWTException) e).getMessageProcessingError());
			} else {
				fail("Expected InvalidJWTException");
			}
		}
	}

	@Test
	public void testVerify_NoAudienceClaim() {
		// @formatter:off
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.issuer(issuer)
				.issueTime(new Date())
				.subject(UUID.randomUUID().toString())
				.jwtID(UUID.randomUUID().toString())
				.build();
		// @formatter:on

		try {
			jwtClaimsVerifier.verify(claimsSet);
			fail("Expected InvalidJWTException");
		} catch (BadJWTException e) {
			if (e instanceof InvalidJWTException) {
				assertEquals(MessageProcessingError.JWT_AUD, ((InvalidJWTException) e).getMessageProcessingError());
			} else {
				fail("Expected InvalidJWTException");
			}
		}
	}

	@Test
	public void testVerify_AudienceNotAccepted() {
		// @formatter:off
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.issuer(issuer)
				.issueTime(new Date())
				.audience("client-2")
				.subject(UUID.randomUUID().toString())
				.jwtID(UUID.randomUUID().toString())
				.build();
		// @formatter:on

		try {
			jwtClaimsVerifier.verify(claimsSet);
			fail("Expected InvalidJWTException");
		} catch (BadJWTException e) {
			if (e instanceof InvalidJWTException) {
				assertEquals(MessageProcessingError.JWT_AUD, ((InvalidJWTException) e).getMessageProcessingError());
			} else {
				fail("Expected InvalidJWTException");
			}
		}
	}

	@Test
	public void testVerify_NoSubjectClaim() {
		// @formatter:off
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.issuer(issuer)
				.issueTime(new Date())
				.audience(clientId)
				.jwtID(UUID.randomUUID().toString())
				.build();
		// @formatter:on

		try {
			jwtClaimsVerifier.verify(claimsSet);
			fail("Expected InvalidJWTException");
		} catch (BadJWTException e) {
			if (e instanceof InvalidJWTException) {
				assertEquals(MessageProcessingError.EVENT_DATA, ((InvalidJWTException) e).getMessageProcessingError());
			} else {
				fail("Expected InvalidJWTException");
			}
		}
	}

	@Test
	public void testVerify_NoJwtIdClaim() {
		// @formatter:off
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.issuer(issuer)
				.issueTime(new Date())
				.audience(clientId)
				.subject(UUID.randomUUID().toString())
				.build();
		// @formatter:on

		try {
			jwtClaimsVerifier.verify(claimsSet);
			fail("Expected InvalidJWTException");
		} catch (BadJWTException e) {
			if (e instanceof InvalidJWTException) {
				assertEquals(MessageProcessingError.EVENT_DATA, ((InvalidJWTException) e).getMessageProcessingError());
			} else {
				fail("Expected InvalidJWTException");
			}
		}
	}

	@Test
	public void testVerify_ValidJWT() {
		// @formatter:off
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.issuer(issuer)
				.issueTime(new Date())
				.audience(Arrays.asList(clientId, "client-2"))
				.subject(UUID.randomUUID().toString())
				.jwtID(UUID.randomUUID().toString())
				.build();
		// @formatter:on

		try {
			jwtClaimsVerifier.verify(claimsSet);
		} catch (BadJWTException e) {
			fail("Unexpected exception");
		}
	}
}
