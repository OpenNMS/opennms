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
package org.opennms.netmgt.snmp.commands;

import java.net.InetAddress;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;

import com.google.common.base.Strings;

@Command(scope = "opennms", name = "snmp-remove-from-definition", description = "Remove an IP address from a definition")
@Service
public class RemoveFromDefinitionsCommand implements Action {

    @Reference
    private SnmpAgentConfigFactory snmpAgentConfigFactory;

    @Option(name = "-l", aliases = "--location", description = "Location")
    String location;

    @Argument(name = "ipAddress", description = "IP address to remove from definition", required = true)
    String ipAddress;

    @Override
    public Object execute() throws Exception {
        boolean succeeded = snmpAgentConfigFactory.removeFromDefinition(InetAddress.getByName(ipAddress), location, "karaf-shell");
        if (Strings.isNullOrEmpty(location)) {
            location = "Default";
        }
        if (succeeded) {
            System.out.printf("IP address '%s' at location '%s' removed from SNMP Definitions \n", ipAddress, location);
        } else {
            System.out.printf("Failed to remove IP address '%s' at location '%s' from SNMP Definitions \n ", ipAddress, location);
        }
        return null;
    }
}
