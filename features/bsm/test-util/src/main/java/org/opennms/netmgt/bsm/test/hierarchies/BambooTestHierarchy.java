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
