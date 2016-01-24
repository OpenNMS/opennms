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

package org.opennms.netmgt.bsm.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IdentityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.MostCriticalEntity;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsMonitoredService;

// TODO MVR merge better with BsmDatabasePopulator...
// creates a simple hierarchy with 1 parent and 2 childs.
// Each children has one ip service atached. The parent has not.
public class BsmTestData {

    private final DatabasePopulator databasePopulator;
    private final List<BusinessServiceEntity> businessServices = new ArrayList<>();

    public BsmTestData(DatabasePopulator databasePopulator) {
        this.databasePopulator = Objects.requireNonNull(databasePopulator);
        createSimpleHierarchy();
    }

    private void createSimpleHierarchy() {
        // Create a simple hierarchy
        BusinessServiceEntity child1 = new BusinessServiceEntityBuilder()
                .name("Child 1")
                .addIpService(databasePopulator.getMonitoredServiceDao().get(databasePopulator.getNode1().getId(), InetAddressUtils.addr("192.168.1.1"), "SNMP"))
                .reduceFunction(new MostCriticalEntity())
                .toEntity();

        BusinessServiceEntity child2 = new BusinessServiceEntityBuilder()
                .name("Child 2")
                .addIpService(databasePopulator.getMonitoredServiceDao().get(databasePopulator.getNode1().getId(), InetAddressUtils.addr("192.168.1.2"), "ICMP"))
                .reduceFunction(new MostCriticalEntity())
                .toEntity();

        BusinessServiceEntity root = new BusinessServiceEntityBuilder()
                .name("Parent")
                .addChildren(child1)
                .addChildren(child2)
                .reduceFunction(new MostCriticalEntity())
                .toEntity();

        root.addChildServiceEdge(child1, new IdentityEntity());
        root.addChildServiceEdge(child2, new IdentityEntity());

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

    public OnmsMonitoredService getServiceChild1() {
        return getChild1().getIpServiceEdges().iterator().next().getIpService();
    }

    public OnmsMonitoredService getServiceChild2() {
        return getChild2().getIpServiceEdges().iterator().next().getIpService();
    }

    public int getServiceCount() {
        return businessServices.size();
    }

    public List<BusinessServiceEntity> getServices() {
        return Collections.unmodifiableList(businessServices);
    }
}
