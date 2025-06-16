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

import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsServiceType;

public class MockServiceTypeDao extends AbstractMockDao<OnmsServiceType, Integer> implements ServiceTypeDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final OnmsServiceType service) {
        service.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsServiceType service) {
        return service.getId();
    }

    @Override
    public OnmsServiceType findByName(final String name) {
        if (name == null) return null;
        for (final OnmsServiceType serviceType : findAll()) {
            if (name.equals(serviceType.getName())) {
                return serviceType;
            }
        }
        return null;
    }

}
