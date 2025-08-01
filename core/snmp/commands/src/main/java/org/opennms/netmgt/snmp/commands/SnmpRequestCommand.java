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

import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;

public class SnmpRequestCommand implements Action {

    @Reference
    public SnmpAgentConfigFactory snmpAgentConfigFactory;
    @Reference
    public LocationAwareSnmpClient locationAwareSnmpClient;
    @Option(name = "-l", aliases = "--location", description = "Location", required = false, multiValued = false)
    protected String m_location;
    @Option(name = "-s", aliases = "--system-id", description = "System ID")
    protected String m_systemId = null;
    @Argument(index = 0, name = "host", description = "Hostname or IP Address of the target agent", required = true, multiValued = false)
    protected String m_host;
    @Argument(index = 1, name = "oids", description = "List of OIDs to retrieve from the agent", required = true, multiValued = true)
    protected List<String> m_oids;

    public SnmpRequestCommand() {
        super();
    }

    @Override
    public Object execute() throws Exception {
        return null;
    }
    
}
