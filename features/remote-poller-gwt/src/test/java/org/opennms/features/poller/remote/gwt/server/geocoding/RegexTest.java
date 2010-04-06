package org.opennms.features.poller.remote.gwt.server.geocoding;

import static org.junit.Assert.*;

import org.junit.Test;

public class RegexTest {

	@Test
	public void testRegexes() {
		final String regexString = "^\\s*[\\-\\d\\.]+\\s*,\\s*[\\-\\d\\.]+\\s*$";
		assertTrue("35.7174,-79.1619".matches(regexString));
		assertTrue(" 35.7174,-79.1619".matches(regexString));
		assertTrue("35.7174,-79.1619 ".matches(regexString));
		assertTrue(" 35.7174,-79.1619 ".matches(regexString));
		assertTrue("35.7174, -79.1619".matches(regexString));
		assertTrue("35.7174 ,-79.1619".matches(regexString));
		assertTrue(" 35.7174 , -79.1619 ".matches(regexString));
	}
}
