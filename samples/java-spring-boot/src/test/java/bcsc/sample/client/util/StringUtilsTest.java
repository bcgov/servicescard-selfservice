package bcsc.sample.client.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import bcsc.sample.client.util.StringUtils;

public class StringUtilsTest {
	@Test
	public void testIsNullOrWhiteSpace() {
		assertTrue(StringUtils.isNullOrWhiteSpace(null));
		assertTrue(StringUtils.isNullOrWhiteSpace(""));
		assertTrue(StringUtils.isNullOrWhiteSpace(" "));
		assertTrue(StringUtils.isNullOrWhiteSpace("  "));
		assertFalse(StringUtils.isNullOrWhiteSpace(" a "));
	}
}
