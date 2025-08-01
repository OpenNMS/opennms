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
package org.opennms.netmgt.dao.api;

import org.opennms.netmgt.model.OnmsNode;

public class MonitoringLocationUtils {

    /**
     * Returns <b>true</b> if the given location name is <b>null</b> or the system default.
     *
     * @param locationName
     * @return
     */
    public static boolean isDefaultLocationName(String locationName) {
        return locationName == null || MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID.equals(locationName);
    }

    /**
     * Returns the name to which the given node is associated, or null if the if node
     * is associated to either the default location, or no location.
     *
     * @param node
     * @return
     */
    public static String getLocationNameOrNullIfDefault(OnmsNode node) {
        final String locationName = node.getLocation() != null ? node.getLocation().getLocationName() : null;
        return isDefaultLocationName(locationName) ? null : locationName;
    }

}
