/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
