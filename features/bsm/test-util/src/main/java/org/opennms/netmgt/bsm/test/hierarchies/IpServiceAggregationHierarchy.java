/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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
