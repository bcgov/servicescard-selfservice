package bcsc.sample.client.enumeration;

import java.util.HashMap;
import java.util.Map;

public enum EventType {
	// @formatter:off
	IDENTITY_UPDATE		("identity_update",		"Identity Updated"),
	IDENTITY_MERGE		("identity_merge",		"Identity Merged"),
	IDENTITY_INACTIVE	("identity_inactive",	"Identity Deactivated");
	// @formatter:on

	private static final Map<String, EventType> mapCode;

	static {
		mapCode = new HashMap<>();
		for (EventType eventType : EventType.values()) {
			mapCode.put(eventType.getCode(), eventType);
		}
	}

	public static EventType getEnumFromCode(final String code) {
		return mapCode.get(code);
	}

	private String 	code;
	private String	description;

	private EventType(String code, String description) {
		this.code = code;
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}
}
