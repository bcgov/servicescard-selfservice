package bcsc.sample.client.util;

public final class StringUtils {
	public static boolean isNullOrWhiteSpace(final String value) {
		return value == null || value.trim().isEmpty();
	}
}
