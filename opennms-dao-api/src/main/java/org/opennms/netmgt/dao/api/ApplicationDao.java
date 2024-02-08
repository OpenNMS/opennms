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

import java.net.InetAddress;
import java.util.List;

import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

/**
 * <p>ApplicationDao interface.</p>
 */
public interface ApplicationDao extends OnmsDao<OnmsApplication, Integer> {

    /**
     * <p>findByName</p>
     *
     * @param label a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsApplication} object.
     */
    OnmsApplication findByName(String label);

    /**
     * Determine the application's status.
     * As applications do not have a direct status attached, the status is calculated based on the nodeDown,
     * interfaceDown or serviceLost events/alarms from the application's monitored services.
     *
     * @return the application's status.
     */
    List<ApplicationStatus> getApplicationStatus();

    /**
     * same as {@link #getApplicationStatus()} but only calculates the status for the given applications.
     *
     * @param applications The applications to calculate the status for.
     * @return The application's status.
     */
    List<ApplicationStatus> getApplicationStatus(List<OnmsApplication> applications);

    /**
     * Load all alarms from the alarm table which have a node id, ip address and service type set.
     *
     * @return all alarms from the alarm table which have a node id, ip address and service type set.
     */
    List<MonitoredServiceStatusEntity> getAlarmStatus();

    List<MonitoredServiceStatusEntity> getAlarmStatus(List<OnmsApplication> applications);

    List<OnmsMonitoringLocation> getPerspectiveLocationsForService(final int nodeId, final InetAddress ipAddress, final String serviceName);

    List<ServicePerspective> getServicePerspectives();

}
