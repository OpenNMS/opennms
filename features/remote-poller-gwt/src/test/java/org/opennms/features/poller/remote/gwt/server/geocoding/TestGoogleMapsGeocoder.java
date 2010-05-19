package org.opennms.features.poller.remote.gwt.server.geocoding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;

public class TestGoogleMapsGeocoder extends AbstractGeocoderTest {

	@Test
	public void testGoogleLookupSuccess() throws Exception {
		if (shouldRun()) {
			Geocoder geocoder = new GoogleMapsGeocoder();
			final GWTLatLng remote = geocoder.geocode("220 Chatham Business Dr, Pittsboro, NC 27312");
			final GWTLatLng local = new GWTLatLng(35.717372, -79.161857);
			assertEquals(local.hashCode(), remote.hashCode());
			assertEquals(local, remote);
		}
	}

	@Test
	public void testGoogleLookupFailure() {
		if (shouldRun()) {
			Geocoder geocoder = new GoogleMapsGeocoder();
			try {
				geocoder.geocode("asdasdasdasdasdasdasdasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdf");
				assertTrue("this should throw an exception", false);
			} catch (GeocoderException e) {
				assertEquals("geo.google.GeoException: Error Status Code: G_GEO_UNKNOWN_ADDRESS", e.getMessage());
			}
		}
	}

}
