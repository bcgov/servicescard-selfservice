package bcsc.sample.client.jwt;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.common.cache.CacheLoader;
import com.nimbusds.jose.jwk.JWKSet;

@Component
public class JWKSetLoader extends CacheLoader<String, JWKSet> {
	private RestTemplate restTemplate = new RestTemplate();

	@Override
	public JWKSet load(String key) throws Exception {
		String jwks = restTemplate.getForObject(key, String.class);
		return JWKSet.parse(jwks);
	}
}
