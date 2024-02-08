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

@Command(scope = "opennms", name = "bsm-generate-hierarchies", description="Generates hierarchies.")
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
        businessServiceManager.triggerDaemonReload();
        return null;
    }

    public void setNumServices(Integer numServices) {
        this.numServices = numServices;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }
}
