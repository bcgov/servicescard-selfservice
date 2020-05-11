package bcsc.sample.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import bcsc.sample.client.entity.RegisteredUser;

public interface RegisteredUserRepository extends JpaRepository<RegisteredUser, Long> {
	RegisteredUser findByIssuerAndSubject(String issuer, String subject);
}
