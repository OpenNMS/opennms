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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.HeatMapElement;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.model.outage.CurrentOutageDetails;
import org.opennms.netmgt.model.outage.OutageSummary;


/**
 * <p>OutageDao interface.</p>
 */
public interface OutageDao extends LegacyOnmsDao<OnmsOutage, Integer> {

    /**
     * <p>currentOutageCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    Integer currentOutageCount();

    /**
     * <p>currentOutages</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsOutage> currentOutages();

    /**
     * Open outages grouped by {@link OnmsMonitoredService} id.
     */
    Map<Integer, Set<OnmsOutage>> currentOutagesByServiceId();

    /**
     * Return the current open outage for the service or if the service
     * is up and has no open outage, return null.
     */
    OnmsOutage currentOutageForService(OnmsMonitoredService service);

    OnmsOutage currentOutageForServiceFromPerspective(final OnmsMonitoredService service, final OnmsMonitoringLocation perspective);

    /**
     * Return all current open outages for the given service be it detected from Perspective Poller.
     */
    Collection<OnmsOutage> currentOutagesForServiceFromPerspectivePoller(OnmsMonitoredService service);

    /**
     * Finds the latest (unresolved) outages that match the given services.
     *
     * @param services a list of services
     * @return a {@link java.util.Collection} of outages
     */
    Collection<CurrentOutageDetails> newestCurrentOutages(List<String> services);

    /**
     * Finds all current (unresolved) outages that match the given service selector.
     *
     * @param selector a service selector (filter + service list)
     * @return a {@link java.util.Collection} of outages
     */
    Collection<OnmsOutage> matchingCurrentOutages(ServiceSelector selector);

    /**
     * <p>findAll</p>
     *
     * @param offset a {@link java.lang.Integer} object.
     * @param limit a {@link java.lang.Integer} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsOutage> findAll(Integer offset, Integer limit);

    /**
     * Get the number of nodes with outages.
     * @return the number of nodes with outages.
     */
    int countOutagesByNode();

    /**
     * Get the list of current outages, one per node.  If a node has more than one outage, the
     * oldest outstanding outage is returned.
     * @param rows The maximum number of outages to return.
     * @return A list of outages.
     */
    List<OutageSummary> getNodeOutageSummaries(int rows);

    /**
     * Retrieves heatmap elements for a given combination of database columns.
     *
     * @param entityNameColumn the entity's name column
     * @param entityIdColumn the entity's id column
     * @param restrictionColumn a column used for a restriction of the results
     * @param restrictionValue the value that must match against the restrictionColumn
     * @param groupByColumns columns used for the SQL group-by clause
     * @return the heatmap elements for this query
     */
    List<HeatMapElement> getHeatMapItemsForEntity(String entityNameColumn, String entityIdColumn, String restrictionColumn, String restrictionValue, String... groupByColumns);

    Collection<OnmsOutage> getStatusChangesForApplicationIdBetween(final Date startDate, final Date endDate, final Integer applicationId);
}
