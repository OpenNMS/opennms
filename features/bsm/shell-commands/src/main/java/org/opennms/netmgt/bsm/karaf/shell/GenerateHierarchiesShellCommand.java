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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.karaf.shell;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;
import org.opennms.netmgt.bsm.service.model.functions.reduce.HighestSeverity;

@Command(scope = "bsm", name = "generate-hierarchies", description="Generates hierarchies.")
@Service
public class GenerateHierarchiesShellCommand implements Action {

    private static final int DEFAULT_NUM_SERVICES = 1000;
    private static final int DEFAULT_DEPTH = 10;

    @Argument(index = 0, name = "num-services", description = "The number of business services to generate.", required = false, multiValued = false)
    Integer numServices = null;

    @Argument(index = 1, name = "depth", description = "The depth to use for each hierarchy.", required = false, multiValued = false)
    Integer depth = null;

    @Reference
    public BusinessServiceManager businessServiceManager;

    @Override
    public Object execute() throws Exception {
        final Map<String, BusinessService> businessServicesByName = businessServiceManager.getAllBusinessServices()
                .stream().collect(Collectors.toMap(BusinessService::getName, Function.identity()));

        int showStatusEvery = 100;
        int numServicesToGenerate = numServices != null ? numServices : DEFAULT_NUM_SERVICES;
        int depthPerHierarchy = depth != null ? depth : DEFAULT_DEPTH;

        int currentDepth = 0;
        BusinessService lastBusinessService = null;
        for (int i = 0; i < numServicesToGenerate; i++) {
            if (i % showStatusEvery == 0) {
                System.out.printf("Generating business services %d -> %d\n", i,
                        Math.min(i + showStatusEvery, numServicesToGenerate));
            }
            final String name = "B" + i;
            if (businessServicesByName.containsKey(name)) {
                lastBusinessService = businessServicesByName.get(name);
                continue;
            }

            BusinessService businessService = businessServiceManager.createBusinessService();
            businessService.setName(name);
            businessService.setReduceFunction(new HighestSeverity());
            businessService.getAttributes().put("generated", "true");
            businessServiceManager.saveBusinessService(businessService);

            if (lastBusinessService != null && currentDepth < depthPerHierarchy) {
                businessServiceManager.addChildEdge(lastBusinessService, businessService,
                        new Identity(), 1);
                currentDepth++;
            } else if (currentDepth >= depthPerHierarchy) {
                currentDepth = 0;
            }
            lastBusinessService = businessService;
        }
        return null;
    }

    public void setNumServices(Integer numServices) {
        this.numServices = numServices;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }
}
