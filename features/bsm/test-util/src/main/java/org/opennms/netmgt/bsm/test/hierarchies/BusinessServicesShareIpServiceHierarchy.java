/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
import org.opennms.netmgt.bsm.persistence.api.functions.map.IdentityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.HighestSeverityEntity;
import org.opennms.netmgt.bsm.test.BusinessServiceEntityBuilder;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsMonitoredService;

/**
 * Creates a hierarchy that looks like:
 *             Parent (BS)
 *            /          \
 *     Child 1 (BS)    Child 2 (BS)
 *           |          /
 *          ICMP on Node 2
 */
public class BusinessServicesShareIpServiceHierarchy {
    private final List<BusinessServiceEntity> businessServices = new ArrayList<>();

    public BusinessServicesShareIpServiceHierarchy(DatabasePopulator databasePopulator) {
        Objects.requireNonNull(databasePopulator);

        OnmsMonitoredService icmpServiceNode2 = databasePopulator.getMonitoredServiceDao().get(
                databasePopulator.getNode2().getId(),
                InetAddressUtils.addr("192.168.2.1"),
                "ICMP");

        // Create a simple hierarchy
        BusinessServiceEntity child1 = new BusinessServiceEntityBuilder()
                .name("Child 1")
                .addIpService(icmpServiceNode2, new IdentityEntity())
                .reduceFunction(new HighestSeverityEntity())
                .toEntity();

        BusinessServiceEntity child2 = new BusinessServiceEntityBuilder()
                .name("Child 2")
                .addIpService(icmpServiceNode2, new IdentityEntity())
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
