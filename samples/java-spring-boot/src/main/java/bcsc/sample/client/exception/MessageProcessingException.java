package bcsc.sample.client.exception;

import bcsc.sample.client.enumeration.MessageProcessingError;

public class MessageProcessingException extends Exception {
	private static final long serialVersionUID = 1L;
	MessageProcessingError error;
	
	public MessageProcessingException(MessageProcessingError err){
		super();
		this.error = err;
	}

	public MessageProcessingError getMessageProcessingError(){
		return error;
	}
}