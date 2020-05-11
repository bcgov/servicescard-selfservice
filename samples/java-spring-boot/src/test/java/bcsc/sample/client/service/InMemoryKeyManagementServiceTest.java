package bcsc.sample.client.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.test.context.junit4.SpringRunner;

import com.nimbusds.jose.jwk.RSAKey;

import bcsc.sample.client.service.ClientRegistrationManager;
import bcsc.sample.client.service.InMemoryKeyManagementService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InMemoryKeyManagementServiceTest {

	@Autowired
	private InMemoryKeyManagementService service;

	@Autowired
	private ClientRegistrationManager clientRegistrationManager;

	@Test
	public void testGetClientRSAKeys() {
		List<ClientRegistration> clientRegistrations = clientRegistrationManager.getClientRegistrations();

		for (ClientRegistration clientRegistration : clientRegistrations) {
			List<RSAKey> rsaKeys = service.getClientRSAKeys(clientRegistration.getRegistrationId());
			assertNotNull(rsaKeys);
			assertEquals(1, rsaKeys.size());
		}

		List<RSAKey> rsaKeys = service.getClientRSAKeys(UUID.randomUUID().toString());
		assertNotNull(rsaKeys);
		assertEquals(0, rsaKeys.size());
	}
}
