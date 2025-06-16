/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
