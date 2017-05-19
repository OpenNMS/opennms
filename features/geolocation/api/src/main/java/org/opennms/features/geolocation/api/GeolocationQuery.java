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

package org.opennms.features.geolocation.api;

import java.util.ArrayList;
import java.util.List;

/**
 * The query object to allow retrieving nodes with geolocations.
 *
 * @author mvrueden
 */
public class GeolocationQuery {

    /**
     * The strategy to calculate the status of each node.
     * If null, {@link StatusCalculationStrategy#None} is assumed.
     */
    private StatusCalculationStrategy statusCalculationStrategy = StatusCalculationStrategy.None;

    /**
     * Defines if only nodes of a certain location are returned.
     * If null, all locations are used.
     */
    private String location;

    /**
     * When {@link #statusCalculationStrategy} is {@link StatusCalculationStrategy#Alarms} this property defines,
     * weather to include acknowledged alarms in the status calculation.
     */
    private boolean includeAcknowledgedAlarms = false;

    /**
     * If defined only nodes which have a severity >= the given severity are included in the result.
     * Note: If {@link #statusCalculationStrategy} is {@link StatusCalculationStrategy#None} this property is ignored.
     */
    private GeolocationSeverity severity;

    /**
     * Limit the selection to the nodes with the following ids
     */
    private List<Integer> nodeIds = new ArrayList<>();

    public StatusCalculationStrategy getStatusCalculationStrategy() {
        return statusCalculationStrategy;
    }

    public void setStatusCalculationStrategy(StatusCalculationStrategy statusCalculationStrategy) {
        this.statusCalculationStrategy = statusCalculationStrategy;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isIncludeAcknowledgedAlarms() {
        return includeAcknowledgedAlarms;
    }

    public void setIncludeAcknowledgedAlarms(boolean includeAcknowledgedAlarms) {
        this.includeAcknowledgedAlarms = includeAcknowledgedAlarms;
    }

    public GeolocationSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(GeolocationSeverity severity) {
        this.severity = severity;
    }

    public List<Integer> getNodeIds() {
        return nodeIds;
    }

    public void setNodeIds(List<Integer> nodeIds) {
        this.nodeIds = nodeIds;
    }
}
