/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
