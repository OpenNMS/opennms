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
package org.opennms.core.utils;

public class LocationUtils {

    public static final String DEFAULT_LOCATION_NAME = "Default";

    /**
     * Returns the effective location name, using the default value if the given
     * location name is <b>null</b> or an empty string.
     *
     * @param locationName
     * @return the effective location name
     */
    public static String getEffectiveLocationName(final String locationName) {
        if (locationName == null || locationName.isEmpty()) {
            return DEFAULT_LOCATION_NAME;
        }
        return locationName;
    }

    /**
     * Returns <b>true</b> if the given location name is <b>null</b>, an empty string,
     * or the system default.
     *
     * @param locationName
     * @return
     */
    public static boolean isDefaultLocationName(String locationName) {
        return DEFAULT_LOCATION_NAME.equals(getEffectiveLocationName(locationName));
    }

    public static boolean doesLocationsMatch(String location1, String location2) {
        return getEffectiveLocationName(location1).equals(getEffectiveLocationName(location2));
    }


}
