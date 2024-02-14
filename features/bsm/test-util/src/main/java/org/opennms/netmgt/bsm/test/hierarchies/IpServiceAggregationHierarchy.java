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
import org.opennms.netmgt.bsm.persistence.api.functions.map.SetToEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.HighestSeverityEntity;
import org.opennms.netmgt.bsm.test.BusinessServiceEntityBuilder;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsSeverity;

import com.google.common.collect.Iterators;

/**
 * Creates a hierarchy that looks like:
 *                 Parent (BS)
 *            /         |                  \
 *   ICMP on Node 1  ICMP on Node 2    ICMP on Node 3
 */
public class IpServiceAggregationHierarchy {

    private final List<BusinessServiceEntity> businessServices = new ArrayList<>();

    public IpServiceAggregationHierarchy(DatabasePopulator databasePopulator) {
        Objects.requireNonNull(databasePopulator);

        OnmsMonitoredService icmpServiceNode1 = databasePopulator.getMonitoredServiceDao().get(
                databasePopulator.getNode1().getId(),
                InetAddressUtils.addr("192.168.1.1"),
                "ICMP");

        OnmsMonitoredService icmpServiceNode2 = databasePopulator.getMonitoredServiceDao().get(
                databasePopulator.getNode2().getId(),
                InetAddressUtils.addr("192.168.2.1"),
                "ICMP");

        OnmsMonitoredService icmpServiceNode3 = databasePopulator.getMonitoredServiceDao().get(
                databasePopulator.getNode3().getId(),
                InetAddressUtils.addr("192.168.3.1"),
                "ICMP");

        // Create the hierarchy
        BusinessServiceEntity root = new BusinessServiceEntityBuilder()
                .name("Parent")
                .addIpService(icmpServiceNode1, new SetToEntity(OnmsSeverity.MINOR.getId()))
                .addIpService(icmpServiceNode2, new SetToEntity(OnmsSeverity.MAJOR.getId()))
                .addIpService(icmpServiceNode3, new SetToEntity(OnmsSeverity.CRITICAL.getId()))
                .reduceFunction(new HighestSeverityEntity())
                .toEntity();

        businessServices.add(root);
    }

    public BusinessServiceEntity getRoot() {
        return businessServices.get(0);
    }

    public IPServiceEdgeEntity getIpSvcNode1() {
        return Iterators.get(getRoot().getIpServiceEdges().iterator(), 0);
    }

    public IPServiceEdgeEntity getIpSvcNode2() {
        return Iterators.get(getRoot().getIpServiceEdges().iterator(), 1);
    }

    public IPServiceEdgeEntity getIpSvcNode3() {
        return Iterators.get(getRoot().getIpServiceEdges().iterator(), 2);
    }

    public List<BusinessServiceEntity> getServices() {
        return Collections.unmodifiableList(businessServices);
    }
}
