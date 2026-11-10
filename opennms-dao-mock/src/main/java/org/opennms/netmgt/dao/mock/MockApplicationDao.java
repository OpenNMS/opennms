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

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.ApplicationStatus;
import org.opennms.netmgt.dao.api.MonitoredServiceStatusEntity;
import org.opennms.netmgt.dao.api.ServicePerspective;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

public class MockApplicationDao extends AbstractMockDao<OnmsApplication, Integer> implements ApplicationDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final OnmsApplication app) {
        app.setId(m_id.incrementAndGet());
    }

    @Override
    public Integer getId(final OnmsApplication app) {
        return app.getId();
    }

    @Override
    public OnmsApplication findByName(final String label) {
        if (label == null) return null;
        for (final OnmsApplication app : findAll()) {
            if (label.equals(app.getName())) {
                return app;
            }
        }
        return null;
    }

    @Override
    public List<ApplicationStatus> getApplicationStatus() {
        return null;
    }

    @Override
    public List<ApplicationStatus> getApplicationStatus(List<OnmsApplication> applications) {
        return null;
    }

    @Override
    public List<MonitoredServiceStatusEntity> getAlarmStatus() {
        return null;
    }

    @Override
    public List<MonitoredServiceStatusEntity> getAlarmStatus(List<OnmsApplication> applications) {
        return null;
    }

    @Override
    public List<OnmsMonitoringLocation> getPerspectiveLocationsForService(final int nodeId, final InetAddress ipAddress, final String serviceName) {
        return null;
    }

    @Override
    public List<ServicePerspective> getServicePerspectives() {
        return null;
    }
}
