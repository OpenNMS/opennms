package org.opennms.features.poller.remote.gwt.server.geocoding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;

public class TestNominatimGeocoder extends AbstractGeocoderTest {

	@Test
	public void testLookupSuccess() throws Exception {
		if (shouldRun()) {
			Geocoder geocoder = new NominatimGeocoder("opennms@opennms.org");
			final GWTLatLng remote = geocoder.geocode("220 Chatham Business Dr, Pittsboro, NC 27312");
			final GWTLatLng local = new GWTLatLng(35.7182403203186, -79.1621859463074);
			assertEquals(local.hashCode(), remote.hashCode());
			assertEquals(local, remote);
		}
	}
	
	@Test
	public void testLookupFailure() throws Exception {
		if (shouldRun()) {
			Geocoder geocoder = new NominatimGeocoder("opennms@opennms.org");
			try {
				geocoder.geocode("asdasdasdasdasdasdasdasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdf");
				assertTrue("this should throw an exception", false);
			} catch (GeocoderException e) {
				assertEquals("Nominatim returned an OK status code, but no places", e.getMessage());
			}
		}
	}
}
