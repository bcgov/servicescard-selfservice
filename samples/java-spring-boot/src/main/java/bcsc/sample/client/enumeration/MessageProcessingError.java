package bcsc.sample.client.enumeration;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

@JsonFormat(shape = Shape.OBJECT)
public enum MessageProcessingError {

	// @formatter:off
	JSON_PARSE("json_parse", "Invalid JSON object."),
	JWT_PARSE("jwt_parse", "Invalid or un-parsable JWT or JSON structure."),
	JWT_HEADER("jwt_header", "Invalid JWT header was detected."),
	JWT_CRYPTO("jwt_crypto", "Unable to parse due to unsupported algorithm."),
	JWS("jws", "Signature was not validated."),
	JWE("jwe", "Unable to decrypt JWE encoded data."),
	JWT_AUD("jwt_aud", "Invalid audience value."),
	JWT_ISS("jwt_iss", "Issuer not recognized."),
	EVENT_TYPE("event_type", "An unexpected event type was received."),
	EVENT_PARSE("event_parse", "Invalid structure was encountered."),
	EVENT_DATA("event_data", "Event claims incomplete or invalid."),
	DUPLICATE("duplicate", "A duplicate message was received and has been ignored.");
	// @formatter: on

	private String error;
	private String description;

	private MessageProcessingError(String error, String description) {
		this.error = error;
		this.description = description;
	}

	public String getError() {
		return error;
	}

	public String getDescription() {
		return description;
	}
}
