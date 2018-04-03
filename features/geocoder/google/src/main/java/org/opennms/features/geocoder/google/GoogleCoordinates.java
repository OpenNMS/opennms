/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.geocoder.google;

import org.opennms.features.geocoder.Coordinates;
import org.opennms.features.geocoder.GeocoderException;

import com.google.code.geocoder.model.GeocoderGeometry;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;

public class GoogleCoordinates extends Coordinates {
    private static final long serialVersionUID = 5665827436870286281L;

    public GoogleCoordinates() {}
    public GoogleCoordinates(final GeocoderResult result) throws GeocoderException {
        super();

        if (result == null) {
            throw new GeocoderException("No valid geocoder result found!");
        }

        final GeocoderGeometry geometry = result.getGeometry();
        if (geometry == null) {
            throw new GeocoderException("No geometry found in Google geocoding response!");
        }

        final LatLng latLng = geometry.getLocation();
        if (latLng == null) {
            throw new GeocoderException("No latitude/longitude found in Google geocoding response!");
        }

        setCoordinates(latLng.getLng().doubleValue(), latLng.getLat().doubleValue());
    }
}
