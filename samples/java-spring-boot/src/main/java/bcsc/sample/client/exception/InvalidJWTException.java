package bcsc.sample.client.exception;

import com.nimbusds.jwt.proc.BadJWTException;

import bcsc.sample.client.enumeration.MessageProcessingError;

public class InvalidJWTException extends BadJWTException {

	private static final long serialVersionUID = -1524243706429874563L;
	private MessageProcessingError messageProcessingError;

	public InvalidJWTException(MessageProcessingError messageProcessingError) {
		super(messageProcessingError.getDescription());
		this.messageProcessingError = messageProcessingError;
	}

	public InvalidJWTException(String message, MessageProcessingError messageProcessingError) {
		super(message);
		this.messageProcessingError = messageProcessingError;
	}

	public InvalidJWTException(final String message, final Throwable throwable,
			MessageProcessingError messageProcessingError) {
		super(message, throwable);
		this.messageProcessingError = messageProcessingError;
	}

	public MessageProcessingError getMessageProcessingError() {
		return messageProcessingError;
	}
}
