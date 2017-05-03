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

package org.opennms.web.rest.v2.status;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.model.OnmsSeverity;

public class StatusSummary {
    private final List<OnmsSeverity> severityList;
    private final long totalCount;

    public StatusSummary(List<OnmsSeverity> severityList, long totalCount) {
        this.severityList = Objects.requireNonNull(severityList);
        this.totalCount = totalCount;
    }

    public Map<OnmsSeverity, Long> getSeverityMap() {
        final Map<OnmsSeverity, Long> severityMap = severityList.stream().collect(
                Collectors.groupingBy(severity -> severity, Collectors.counting()));
        final long applicationWithSeverityCount = severityMap.values().stream().mapToLong(count -> count.longValue()).sum();

        // update normal severity
        final long normalCount = totalCount - applicationWithSeverityCount + severityMap.getOrDefault(OnmsSeverity.NORMAL, 0L);
        severityMap.put(OnmsSeverity.NORMAL, normalCount);

        // Enforce each severity to be present
        enrich(severityMap);

        return severityMap;
    }

    // Ensures that each severity is present
    public static void enrich(Map<OnmsSeverity, Long> severityMap) {
        severityMap.putIfAbsent(OnmsSeverity.NORMAL, 0L);
        severityMap.putIfAbsent(OnmsSeverity.WARNING, 0L);
        severityMap.putIfAbsent(OnmsSeverity.MINOR, 0L);
        severityMap.putIfAbsent(OnmsSeverity.MAJOR, 0L);
        severityMap.putIfAbsent(OnmsSeverity.CRITICAL, 0L);
    }
}
