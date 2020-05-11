package bcsc.sample.client.entity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class RegisteredUser {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String subject;
	private String issuer;
	private String clientId;
	private String surname;
	private String firstName;
	private String middleName;
	private String displayName;
	private Date dateOfBirth;
	private Boolean age19OrOver;
	private String gender;
	private String streetAddress;
	private String locality;
	private String region;
	private String postalCode;
	private String country;
	private String email;
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastLoginAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public Boolean getAge19OrOver() {
		return age19OrOver;
	}

	public void setAge19OrOver(Boolean age19OrOver) {
		this.age19OrOver = age19OrOver;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Date getLastLoginAt() {
		return lastLoginAt;
	}

	public void setLastLoginAt(Date lastLoginAt) {
		this.lastLoginAt = lastLoginAt;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RegisteredUser [");
		if (id != null) {
			builder.append("id=");
			builder.append(id);
			builder.append(", ");
		}
		if (subject != null) {
			builder.append("subjectId=");
			builder.append(subject);
			builder.append(", ");
		}
		if (issuer != null) {
			builder.append("issuer=");
			builder.append(issuer);
		}
		builder.append("]");
		return builder.toString();
	}

	public void updateFromClaimsSet(Map<String, Object> attributes) {
		setSubject((String) attributes.get("sub"));
		setSurname((String) attributes.get("family_name"));
		setFirstName((String) attributes.get("given_name"));
		setMiddleName((String) attributes.get("given_names"));
		setDisplayName((String) attributes.get("display_name"));
		if (attributes.containsKey("birthday")) {
			setDateOfBirth(
					Date.from(LocalDate.parse((String) attributes.get("birthdate"), DateTimeFormatter.ISO_LOCAL_DATE)
							.atStartOfDay(ZoneId.systemDefault()).toInstant()));
		}
		setAge19OrOver((Boolean) attributes.get("age_19_or_over"));
		setGender((String) attributes.get("gender"));
		setEmail((String) attributes.get("email"));

		@SuppressWarnings("unchecked")
		Map<String, Object> addressAttributes = (Map<String, Object>) attributes.get("address");
		if (addressAttributes != null) {
			setStreetAddress((String) addressAttributes.get("street_address"));
			setLocality((String) addressAttributes.get("locality"));
			setRegion((String) addressAttributes.get("region"));
			setPostalCode((String) addressAttributes.get("postal_code"));
			setCountry((String) addressAttributes.get("country"));
		}else{
			setStreetAddress((String) attributes.get("street_address"));
			setLocality((String) attributes.get("locality"));
			setRegion((String) attributes.get("region"));
			setPostalCode((String) attributes.get("postal_code"));
			setCountry((String) attributes.get("country"));
		}
	}

	public static RegisteredUser createFromClaimsSet(Map<String, Object> attributes, String issuer, String clientId) {
		RegisteredUser newUser = new RegisteredUser();
		newUser.setIssuer(issuer);
		newUser.setClientId(clientId);
		newUser.updateFromClaimsSet(attributes);

		return newUser;
	}
}
