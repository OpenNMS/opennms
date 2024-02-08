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
package org.opennms.netmgt.dao.mock;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.HeatMapElement;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.model.outage.CurrentOutageDetails;
import org.opennms.netmgt.model.outage.OutageSummary;

public class MockOutageDao extends AbstractMockDao<OnmsOutage, Integer> implements OutageDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final OnmsOutage outage) {
        outage.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsOutage outage) {
        return outage.getId();
    }

    /**
     * When we save an outage, make sure to add the outage to the currentOutages
     * property of {@link OnmsMonitoredService}.
     */
    @Override
    public Integer save(final OnmsOutage entity) {
        Integer retval = super.save(entity);
        if (entity.getIfRegainedService() == null) {
            entity.getMonitoredService().getCurrentOutages().add(entity);
        }
        return retval;
    }

    @Override
    public Integer currentOutageCount() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsOutage> currentOutages() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Map<Integer, Set<OnmsOutage>> currentOutagesByServiceId() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsOutage currentOutageForService(OnmsMonitoredService service) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsOutage currentOutageForServiceFromPerspective(final OnmsMonitoredService service, final OnmsMonitoringLocation perspective) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsOutage> currentOutagesForServiceFromPerspectivePoller(OnmsMonitoredService service) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<CurrentOutageDetails> newestCurrentOutages(final List<String> serviceNames) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsOutage> matchingCurrentOutages(final ServiceSelector selector) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsOutage> findAll(final Integer offset, final Integer limit) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int countOutagesByNode() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OutageSummary> getNodeOutageSummaries(final int rows) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<HeatMapElement> getHeatMapItemsForEntity(String entityNameColumn, String entityIdColumn, String restrictionColumn, String restrictionValue, String... groupByColumns) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsOutage> getStatusChangesForApplicationIdBetween(Date startDate, Date endDate, Integer applicationId) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }
}
