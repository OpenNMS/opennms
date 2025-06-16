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
     * Maybe null.
     */
    private StatusCalculationStrategy statusCalculationStrategy;

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
     * Note: If {@link #statusCalculationStrategy} is null this property is ignored.
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
