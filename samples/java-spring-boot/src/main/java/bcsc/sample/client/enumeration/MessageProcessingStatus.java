package bcsc.sample.client.enumeration;

public enum MessageProcessingStatus {
	RECEIVED("Received"), PROCESSED("Processed"), ERROR("Error");

	private String status;

	private MessageProcessingStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
}
