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

/**
 * <p>BoundsBuilder class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class BoundsBuilder {
    
    Double neLat;
    Double neLng;
    Double swLat;
    Double swLng;
    
    /**
     * <p>getBounds</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTBounds} object.
     */
    public GWTBounds getBounds() {
        return isEmpty() ? new GWTBounds(-90, -180, 90, 180) : new GWTBounds(swLat, swLng, neLat, neLng);
    }
    
    private boolean isEmpty() {
        return neLat == null;
    }
    
    /**
     * <p>extend</p>
     *
     * @param lat a double.
     * @param lng a double.
     */
    public void extend(double lat, double lng) {
        if (isEmpty()) {
            swLat = neLat = lat;
            swLng = neLng = lng;
       } else {
           swLat = Math.min(swLat, lat);
           neLat = Math.max(neLat, lat);
           
           if (!containsLongitude(lng)) {
               if (distanceEast(lng) < distanceWest(lng)) {
                   neLng = lng;
               } else {
                   swLng = lng;
               }
           }
           
       }
    }
    
    /**
     * <p>extend</p>
     *
     * @param coords a {@link org.opennms.features.poller.remote.gwt.client.GWTLatLng} object.
     */
    public void extend(GWTLatLng coords) {
        extend(coords.getLatitude(), coords.getLongitude());
    }
    
    private boolean containsLongitude(double lng) {
        if (swLng <= neLng) {
            return swLng <= lng && lng <= neLng;
        } else {
            return !(neLng < lng && lng < swLng);
        }
    }


    
    /**
     * <p>distanceEast</p>
     *
     * @param lng a double.
     * @return a double.
     */
    public double distanceEast(double lng) {
        return lng > neLng ? lng - neLng : lng+360.0 - neLng;
    }
    
    /**
     * <p>distanceWest</p>
     *
     * @param lng a double.
     * @return a double.
     */
    public double distanceWest(double lng) {
        return swLng > lng ? swLng - lng : swLng + 360.0 - lng;
    }

}
