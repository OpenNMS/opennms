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
package org.opennms.features.enlinkd.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.enlinkd.generator.TopologyGenerator;
import org.opennms.enlinkd.generator.TopologyPersister;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.enlinkd.api.ReloadableTopologyDaemon;

/**
 * Deletes the generated topology via karaf command. The topology is identified as it belongs to the category "GeneratedNode"
 * Install: feature:install opennms-enlinkd-shell
 * Usage: type opennms:delete-topology in karaf console
 */
@Command(scope = "opennms", name = "delete-topology",
        description = "Delete generated topology (OnmsNodes, Elements, Links, SnmpInterfaces, IpInterfaces." +
                "The topology is identified by category 'GeneratedNode'")
@Service
public class DeleteTopologyCommand implements Action {

    @Reference
    private GenericPersistenceAccessor genericPersistenceAccessor;

    @Reference
    private ReloadableTopologyDaemon reloadableTopologyDaemon;

    @Override
    public Object execute() {
        // We print directly to System.out so it will appear in the console
        TopologyGenerator.ProgressCallback progressCallback = new TopologyGenerator.ProgressCallback(System.out::println);

        TopologyGenerator generator = TopologyGenerator.builder()
                .persister(new TopologyPersister(genericPersistenceAccessor, progressCallback))
                .progressCallback(progressCallback)
                .build();
        generator.deleteTopology();
        reloadableTopologyDaemon.reloadTopology();
        return null;
    }
}
