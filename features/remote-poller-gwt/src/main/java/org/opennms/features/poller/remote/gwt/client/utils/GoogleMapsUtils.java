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

package org.opennms.features.poller.remote.gwt.client.utils;

import org.opennms.features.poller.remote.gwt.client.GWTBounds;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;

/**
 * <p>Abstract GoogleMapsUtils class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class GoogleMapsUtils {

	/**
	 * <p>toGWTBounds</p>
	 *
	 * @param bounds a {@link com.google.gwt.maps.client.geom.LatLngBounds} object.
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTBounds} object.
	 */
	public static GWTBounds toGWTBounds(LatLngBounds bounds) {
        return new GWTBounds(GoogleMapsUtils.toGWTLatLng(bounds.getSouthWest()), GoogleMapsUtils.toGWTLatLng(bounds.getNorthEast()));
    }
    
    /**
     * <p>toLatLngBounds</p>
     *
     * @param bounds a {@link org.opennms.features.poller.remote.gwt.client.GWTBounds} object.
     * @return a {@link com.google.gwt.maps.client.geom.LatLngBounds} object.
     */
    public static LatLngBounds toLatLngBounds(GWTBounds bounds) {
        return LatLngBounds.newInstance(toLatLng(bounds.getSouthWestCorner()), toLatLng(bounds.getNorthEastCorner()));
    }

    /**
     * <p>toLatLng</p>
     *
     * @param latLng a {@link org.opennms.features.poller.remote.gwt.client.GWTLatLng} object.
     * @return a {@link com.google.gwt.maps.client.geom.LatLng} object.
     */
    public static LatLng toLatLng(final GWTLatLng latLng) {
    	return LatLng.newInstance(latLng.getLatitude(), latLng.getLongitude());
    }

    /**
     * <p>toGWTLatLng</p>
     *
     * @param latLng a {@link com.google.gwt.maps.client.geom.LatLng} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTLatLng} object.
     */
    public static GWTLatLng toGWTLatLng(final LatLng latLng) {
        return new GWTLatLng(latLng.getLatitude(), latLng.getLongitude());
    }

}
