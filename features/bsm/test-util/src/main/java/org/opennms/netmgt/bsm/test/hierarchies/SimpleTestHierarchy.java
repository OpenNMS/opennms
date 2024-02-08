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
package org.opennms.netmgt.bsm.test.hierarchies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.persistence.api.IPServiceEdgeEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IdentityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.HighestSeverityEntity;
import org.opennms.netmgt.bsm.test.BusinessServiceEntityBuilder;
import org.opennms.netmgt.dao.DatabasePopulator;

/**
 * Creates a simple  hierarchy that looks like:
 *                Parent (BS)
 *            /                \
 *     Child 1 (BS)        Child 2 (BS)
 *         |                    |
 *     SNMP on Node 1     ICMP on Node 2
 */
public class SimpleTestHierarchy {

    private final List<BusinessServiceEntity> businessServices = new ArrayList<>();

    public SimpleTestHierarchy(DatabasePopulator databasePopulator) {
        Objects.requireNonNull(databasePopulator);

        // Create a simple hierarchy
        BusinessServiceEntity child1 = new BusinessServiceEntityBuilder()
                .name("Child 1")
                .addIpService(databasePopulator.getMonitoredServiceDao().get(databasePopulator.getNode1().getId(), InetAddressUtils.addr("192.168.1.1"), "SNMP"), new IdentityEntity())
                .reduceFunction(new HighestSeverityEntity())
                .toEntity();

        BusinessServiceEntity child2 = new BusinessServiceEntityBuilder()
                .name("Child 2")
                .addIpService(databasePopulator.getMonitoredServiceDao().get(databasePopulator.getNode2().getId(), InetAddressUtils.addr("192.168.2.1"), "ICMP"), new IdentityEntity())
                .reduceFunction(new HighestSeverityEntity())
                .toEntity();

        BusinessServiceEntity root = new BusinessServiceEntityBuilder()
                .name("Parent")
                .addChildren(child1, new IdentityEntity())
                .addChildren(child2, new IdentityEntity())
                .reduceFunction(new HighestSeverityEntity())
                .toEntity();

        businessServices.add(child1);
        businessServices.add(child2);
        businessServices.add(root);
    }

    public BusinessServiceEntity getRoot() {
        return businessServices.get(2);
    }

    public BusinessServiceEntity getChild1() {
        return businessServices.get(0);
    }

    public BusinessServiceEntity getChild2() {
        return businessServices.get(1);
    }

    public IPServiceEdgeEntity getServiceChild1() {
        return getChild1().getIpServiceEdges().iterator().next();
    }

    public IPServiceEdgeEntity getServiceChild2() {
        return getChild2().getIpServiceEdges().iterator().next();
    }

    public List<BusinessServiceEntity> getServices() {
        return Collections.unmodifiableList(businessServices);
    }
}
