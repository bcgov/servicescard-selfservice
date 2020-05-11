package bcsc.sample.client.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;

@Service
public class InMemoryKeyManagementService implements KeyManagementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryKeyManagementService.class);

	private ClientRegistrationManager clientRegistrationManager;
	private Map<String, RSAKey> keyStore;

	public InMemoryKeyManagementService(final ClientRegistrationManager clientRegistrationManager) {
		this.clientRegistrationManager = clientRegistrationManager;
	}

	@PostConstruct
	public void init() throws Exception {
		keyStore = new HashMap<>();

		for (ClientRegistration clientRegistration : clientRegistrationManager.getClientRegistrations()) {
			// generate one RSA key for each registered client to be used for
			// JWT encryption
			LOGGER.debug("Creating RSA key for client [registrationId={}].", clientRegistration.getRegistrationId());
			RSAKey jwk = new RSAKeyGenerator(2048).keyUse(KeyUse.ENCRYPTION).keyID(UUID.randomUUID().toString())
					.generate();
			keyStore.put(clientRegistration.getRegistrationId(), jwk);
		}
	}

	@Override
	public @NonNull List<RSAKey> getClientRSAKeys(final String registrationId) {
		List<RSAKey> rsaKeys = new ArrayList<>();

		RSAKey rsaKey = keyStore.get(registrationId);
		if (rsaKey != null) {
			rsaKeys.add(rsaKey);
		}

		return rsaKeys;
	}
}
