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
package org.opennms.features.minion.shell.provision;

import java.util.Map;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.provision.detector.registry.api.ServiceDetectorRegistry;

@Command(scope = "opennms", name = "list-detectors", description = "Lists all of the available detectors.")
@Service
public class ListDetectors implements Action {

    @Reference
    ServiceDetectorRegistry serviceDetectorRegistry;

    @Override
    public Object execute() throws Exception {
        serviceDetectorRegistry.getTypes().entrySet().stream()
            .sorted(Map.Entry.<String, String>comparingByKey()) 
            .forEachOrdered(e -> {
                System.out.printf("%s: %s\n", e.getKey(), e.getValue());
            });
        return null;
    }
}
