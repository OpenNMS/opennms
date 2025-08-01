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

import java.util.Objects;

import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

public class ServicePerspective {
    private OnmsMonitoredService service;
    private OnmsMonitoringLocation perspectiveLocation;

    public ServicePerspective(final OnmsMonitoredService service,
                              final OnmsMonitoringLocation perspectiveLocation) {
        this.service = Objects.requireNonNull(service);
        this.perspectiveLocation = Objects.requireNonNull(perspectiveLocation);
    }

    public OnmsMonitoredService getService() {
        return this.service;
    }

    public void setService(final OnmsMonitoredService service) {
        this.service = Objects.requireNonNull(service);
    }

    public OnmsMonitoringLocation getPerspectiveLocation() {
        return this.perspectiveLocation;
    }

    public void setPerspectiveLocation(final OnmsMonitoringLocation perspectiveLocation) {
        this.perspectiveLocation = Objects.requireNonNull(perspectiveLocation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServicePerspective)) {
            return false;
        }
        final ServicePerspective that = (ServicePerspective) o;
        return Objects.equals(this.service, that.service) &&
               Objects.equals(this.perspectiveLocation, that.perspectiveLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.service, this.perspectiveLocation);
    }
}
