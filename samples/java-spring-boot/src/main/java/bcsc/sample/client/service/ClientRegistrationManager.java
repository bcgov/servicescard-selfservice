package bcsc.sample.client.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.ResolvableType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

@Component
public class ClientRegistrationManager {

	private ClientRegistrationRepository clientRegistrationRepository;

	public ClientRegistrationManager(final ClientRegistrationRepository clientRegistrationRepository) {
		this.clientRegistrationRepository = clientRegistrationRepository;
	}

	@SuppressWarnings("unchecked")
	public List<ClientRegistration> getClientRegistrations() {
		Iterable<ClientRegistration> clientRegistrations = null;
		ResolvableType resolvableType = ResolvableType.forInstance(clientRegistrationRepository).as(Iterable.class);

		if (resolvableType != ResolvableType.NONE
				&& ClientRegistration.class.isAssignableFrom(resolvableType.resolveGenerics()[0])) {
			clientRegistrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
		}

		List<ClientRegistration> registrations = new ArrayList<>();

		if (clientRegistrations != null) {
			clientRegistrations.forEach(registrations::add);
		}

		return registrations;
	}

	public ClientRegistration getClientRegistration(final String registrationId) {
		return clientRegistrationRepository.findByRegistrationId(registrationId);
	}
}
