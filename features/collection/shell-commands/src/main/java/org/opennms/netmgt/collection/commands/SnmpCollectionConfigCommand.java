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
package org.opennms.netmgt.collection.commands;

import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.MibObject;
import org.opennms.netmgt.config.datacollection.SnmpCollection;

@Command(scope = "opennms", name = "snmp-collection-config",
        description = "Display SNMP data collection configuration status or lookup MIB objects for a sysoid.")
@Service
public class SnmpCollectionConfigCommand implements Action {

    @Reference
    private DataCollectionConfigDao dataCollectionConfigDao;

    @Argument(index = 0, name = "sysoid", required = false,
            description = "The sysObjectID to lookup. If omitted, shows config status summary.")
    private String sysoid;

    @Option(name = "-c", aliases = "--collection", description = "Collection name (default: 'default')")
    private String collection = "default";

    @Option(name = "-a", aliases = "--address", description = "IP address for matching (default: 127.0.0.1)")
    private String address = "127.0.0.1";

    @Option(name = "-i", aliases = "--ifType", description = "Interface type: -1=node, -2=all-if, or specific ifType number")
    private int ifType = -2;

    @Override
    public Object execute() {
        if (sysoid == null || sysoid.isEmpty()) {
            printStatus();
        } else {
            printLookup();
        }
        return null;
    }

    private void printStatus() {
        System.out.println("=== SNMP Data Collection Config Status ===");
        System.out.println();

        System.out.println("Collection Groups: " + dataCollectionConfigDao.getAvailableDataCollectionGroups().size());
        System.out.println("System Definitions: " + dataCollectionConfigDao.getAvailableSystemDefs().size());
        System.out.println("MIB Groups: " + dataCollectionConfigDao.getAvailableMibGroups().size());
        System.out.println("Resource Types: " + dataCollectionConfigDao.getConfiguredResourceTypes().size());
        System.out.println("Last Update: " + dataCollectionConfigDao.getLastUpdate());
        System.out.println();

        final DatacollectionConfig config = dataCollectionConfigDao.getRootDataCollection();
        if (config != null) {
            System.out.println("SNMP Collections:");
            for (final SnmpCollection coll : config.getSnmpCollections()) {
                if ("__resource_type_collection".equals(coll.getName())) continue;
                final int groups = coll.getGroups() != null ? coll.getGroups().getGroups().size() : 0;
                final int systemDefs = coll.getSystems() != null ? coll.getSystems().getSystemDefs().size() : 0;
                System.out.printf("  %-20s step=%-4d flag=%-6s groups=%-4d systemDefs=%-4d%n",
                        coll.getName(),
                        coll.getRrd() != null ? coll.getRrd().getStep() : 0,
                        coll.getSnmpStorageFlag(),
                        groups, systemDefs);
            }
        }

        System.out.println();
        System.out.println("Available Data Collection Groups:");
        for (final String group : dataCollectionConfigDao.getAvailableDataCollectionGroups()) {
            System.out.println("  " + group);
        }
    }

    private void printLookup() {
        System.out.printf("=== Lookup: sysoid=%s collection=%s address=%s ifType=%d ===%n",
                sysoid, collection, address, ifType);
        System.out.println();

        final List<MibObject> mibObjects = dataCollectionConfigDao.getMibObjectList(collection, sysoid, address, ifType);

        if (mibObjects.isEmpty()) {
            System.out.println("No matching MIB objects found.");
            System.out.println("Verify the sysoid matches a systemDef in the loaded config.");
            return;
        }

        System.out.printf("Matched %d MIB objects:%n%n", mibObjects.size());
        System.out.printf("  %-25s %-40s %-20s %-12s %s%n", "GROUP", "OID", "ALIAS", "TYPE", "INSTANCE");
        System.out.printf("  %-25s %-40s %-20s %-12s %s%n", "-----", "---", "-----", "----", "--------");

        for (final MibObject obj : mibObjects) {
            System.out.printf("  %-25s %-40s %-20s %-12s %s%n",
                    obj.getGroupName(),
                    obj.getOid(),
                    obj.getAlias(),
                    obj.getType(),
                    obj.getInstance());
        }
    }
}
