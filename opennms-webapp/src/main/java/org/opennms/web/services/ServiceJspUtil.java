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
package org.opennms.web.services;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

/** Utility class to limit Java code in service.jsp */
public class ServiceJspUtil {

    private OnmsMonitoredService service;
    private Collection<OnmsOutage> currentOutages;

    public ServiceJspUtil(OnmsMonitoredService service, Collection<OnmsOutage>currentOutages) {
        this.service = service;
        this.currentOutages = currentOutages;
    }

    public List<OnmsMonitoringLocation> getAllPerspectives () {
        return service.getApplications().stream()
                .flatMap(app -> app.getPerspectiveLocations().stream())
                .distinct()
                .sorted(Comparator.comparing(OnmsMonitoringLocation::getLocationName))
                .collect(Collectors.toList());
    }

    public Optional<OnmsOutage> getOutageForPerspective (final OnmsMonitoringLocation onmsMonitoringLocation) {
        return currentOutages.stream()
                .filter(out -> onmsMonitoringLocation.equals(out.getPerspective()))
                .findFirst();
    }

    public String getOutageUrl(OnmsOutage outage) {
        return String.format("<a href=\"outage/detail.htm?id=%s\">%s</a>", outage.getId(), outage.getId());
    }
}
