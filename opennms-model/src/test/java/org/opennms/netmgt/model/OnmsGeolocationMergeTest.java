/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;

public class OnmsGeolocationMergeTest {

    // Verify that merging of geolocation's is working properly
    // See NMS-9316
    @Test
    public void verifyMerge() {
        // The current geolocation simulates an updated geolocation by the GeolocationProvisioningAdapter
        OnmsGeolocation current = new OnmsGeolocation();
        current.setCity("Fulda");
        current.setLatitude(1d);
        current.setLongitude(2d);

        // update simulates an updated geolocation by the user (via requisition)
        OnmsGeolocation update = new OnmsGeolocation();
        update.setCity("Fulda");

        // we don't upate, which should not reset long/lat
        current.mergeGeolocation(update);
        Assert.assertFalse("Current and update geolocation should not be equal", isEqual(current, update));
        Assert.assertNotEquals(current.getLatitude(), update.getLatitude());
        Assert.assertNotEquals(current.getLongitude(), update.getLongitude());

        // Update and verify again
        update.setCity("Stuttgart");
        current.mergeGeolocation(update);
        Assert.assertTrue("Current and update Geolocation should be equal", isEqual(current, update));

        // update lat/long and verify it is also equal
        update.setLatitude(21d);
        update.setLongitude(22d);
        current.mergeGeolocation(update);
        Assert.assertTrue("Current and update Geolocation should be equal", isEqual(current, update));
    }

    private boolean isEqual(OnmsGeolocation current, OnmsGeolocation update) {
        if (current != null && update != null) {
            boolean equals = Objects.equals(current.getAddress1(), update.getAddress1())
                    && Objects.equals(current.getAddress2(), update.getAddress2())
                    && Objects.equals(current.getCity(), update.getCity())
                    && Objects.equals(current.getZip(), update.getZip())
                    && Objects.equals(current.getState(), update.getState())
                    && Objects.equals(current.getCountry(), update.getCountry())
                    && Objects.equals(current.getLatitude(), update.getLatitude())
                    && Objects.equals(current.getLongitude(), update.getLongitude());
            return equals;
        }
        return false;
    }
}
