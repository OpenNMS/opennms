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

import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IdentityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IncreaseEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.HighestSeverityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.ThresholdEntity;
import org.opennms.netmgt.bsm.test.BusinessServiceEntityBuilder;

// Creates a Business Service Hierarchy as defined here: http://www.opennms.org/wiki/BusinessServiceMonitoring#State_Machine
public class BambooTestHierarchy {

    public static final String DISK_USAGE_THRESHOLD_BAMBO_REDUCTION_KEY = "90% Disk Usage Threshold on bamboo";
    public static final String HTTP_8085_BAMBOO_REDUCTION_KEY = "HTTP-8085 on bamboo";
    public static final String BAMBOO_AGENT_DUKE_REDUCTION_KEY = "Bamboo-Agent on duke";
    public static final String BAMBOO_AGENT_CAROLINA_REDUCTION_KEY = "Bamboo-Agent on carolina";
    public static final String BAMBOO_AGENT_NCSTATE_REDUCTION_KEY = "Bamboo-Agent on ncstate";

    private final List<BusinessServiceEntity> businessServices = new ArrayList<>();

    public BambooTestHierarchy() {
        BusinessServiceEntity master = new BusinessServiceEntityBuilder()
                .name("Master")
                .addReductionKey(HTTP_8085_BAMBOO_REDUCTION_KEY, new IncreaseEntity())
                .addReductionKey(DISK_USAGE_THRESHOLD_BAMBO_REDUCTION_KEY, new IdentityEntity())
                .reduceFunction(new HighestSeverityEntity())
                .toEntity();

        BusinessServiceEntity agents = new BusinessServiceEntityBuilder()
                .name("Agents")
                .addReductionKey(BAMBOO_AGENT_DUKE_REDUCTION_KEY, new IncreaseEntity(), 2)
                .addReductionKey(BAMBOO_AGENT_CAROLINA_REDUCTION_KEY, new IncreaseEntity(), 2)
                .addReductionKey(BAMBOO_AGENT_NCSTATE_REDUCTION_KEY, new IncreaseEntity(), 1)
                .reduceFunction(new ThresholdEntity(0.75f))
                .toEntity();

        BusinessServiceEntity root = new BusinessServiceEntityBuilder()
                .name("Bamboo")
                .addChildren(master, new IdentityEntity())
                .addChildren(agents, new IdentityEntity())
                .reduceFunction(new HighestSeverityEntity())
                .toEntity();

        businessServices.add(master);
        businessServices.add(agents);
        businessServices.add(root);
    }

    public List<BusinessServiceEntity> getServices() {
        return Collections.unmodifiableList(businessServices);
    }

    public BusinessServiceEntity getMasterService() {
        return businessServices.get(0);
    }

    public BusinessServiceEntity getAgentsService() {
        return businessServices.get(1);
    }

    public BusinessServiceEntity getBambooService() {
        return businessServices.get(2);
    }
}
