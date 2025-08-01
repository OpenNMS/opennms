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

import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>LocationMonitorDaoHibernate class.</p>
 *
 * @author Seth
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class MonitoringLocationDaoHibernate extends AbstractDaoHibernate<OnmsMonitoringLocation, String> implements MonitoringLocationDao {

    private static final Logger LOG = LoggerFactory.getLogger(MonitoringLocationDaoHibernate.class);

    public MonitoringLocationDaoHibernate() {
        super(OnmsMonitoringLocation.class);
    }

    public OnmsMonitoringLocation getDefaultLocation() {
        return get(DEFAULT_MONITORING_LOCATION_ID);
    }
}
