package org.opennms.features.poller.remote.gwt.server.geocoding;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;

public class TestMapquestGeocoder extends AbstractGeocoderTest {

	@Before
	public void setUp() {
		System.setProperty("gwt.geocoder.referer", "http://localhost/");
	}

	@Test
	public void testSuccessfulLookup() throws Exception {
		if (shouldRun()) {
			Geocoder geocoder = new MapquestGeocoder();
			final GWTLatLng remote = geocoder.geocode("220 Chatham Business Dr, Pittsboro, NC 27312");
			final GWTLatLng local = new GWTLatLng(35.71735, -79.16181);
			assertEquals(local.hashCode(), remote.hashCode());
			assertEquals(local, remote);
		}
	}
	
	@Test
	public void testSuccessfulLookupFailsMinimumQuality() throws Exception {
		if (shouldRun()) {
			Geocoder geocoder = new MapquestGeocoder();
			try {
				geocoder.geocode("asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdf");
			} catch (GeocoderException e) {
				assertEquals("response did not meet minimum quality requirement (COUNTRY is less specific than ZIP)", e.getMessage());
			}
		}
	}
	
	@Test
	public void testBadApiKey() throws Exception {
		if (shouldRun()) {
			Geocoder geocoder = new MapquestGeocoder("unitTestBadApiKey");
			try {
				geocoder.geocode("asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdf");
			} catch (GeocoderException e) {
				assertEquals("an error occurred when querying MapQuest (statusCode=403, message=This is not a valid key. Please check that you have entered this correctly. If you do not have a key, you can obtain a free key by registering at http://developer.mapquest.com.)", e.getMessage());
			}
		}
	}
}
