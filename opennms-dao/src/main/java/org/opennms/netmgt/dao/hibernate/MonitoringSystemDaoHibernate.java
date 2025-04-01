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
package org.opennms.netmgt.dao.hibernate;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MonitoringSystemDao;
import org.opennms.netmgt.model.OnmsMonitoringSystem;

import java.util.Optional;

public class MonitoringSystemDaoHibernate extends AbstractDaoHibernate<OnmsMonitoringSystem, String>
        implements MonitoringSystemDao {
    public MonitoringSystemDaoHibernate() {
        super(OnmsMonitoringSystem.class);
    }

    /**
     * Get the number of monitoring systems of the given type.
     * @param type an OnmsMonitoringSystem.TYPE_ string.
     */
    @Override
    public long getNumMonitoringSystems(String type) {
        CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsMonitoringSystem.class);
        criteriaBuilder.eq("type", type);
        return countMatching(criteriaBuilder.toCriteria());
    }

    /**
     * Get the "main" monitoring system. This has type OnmsMonitoringSystem.TYPE_OPENNMS.
     */
    @Override
    public OnmsMonitoringSystem getMainMonitoringSystem() {
        CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsMonitoringSystem.class);
        criteriaBuilder.eq("type", OnmsMonitoringSystem.TYPE_OPENNMS);

        Optional<OnmsMonitoringSystem> system = this.findMatching(criteriaBuilder.toCriteria()).stream().findFirst();

        return system.orElse(null);
    }
}
