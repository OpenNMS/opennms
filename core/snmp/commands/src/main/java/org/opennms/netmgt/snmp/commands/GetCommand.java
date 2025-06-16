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
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "opennms", name = "snmp-get", description = "Request one or more fully-qualified MIB objects from the agent on the specified host and print the results.")
@Service
public class GetCommand extends SnmpRequestCommand implements Action {

    private static final Logger LOG = LoggerFactory.getLogger(GetCommand.class);

    @Override
    public Object execute() {
        LOG.debug("snmp:get {} {} {}", m_location != null ? "-l " + m_location : "", m_host, m_oids);
        final List<SnmpObjId> snmpObjIds = m_oids.stream()
                .map(SnmpObjId::get)
                .collect(Collectors.toList());
        SnmpAgentConfig agent;
        try {
            agent = snmpAgentConfigFactory.getAgentConfig(InetAddress.getByName(m_host), m_location);
        } catch (UnknownHostException uhe) {
            System.out.println(String.format("Unknown host '%s' at location '%s': %s", m_host, m_location, uhe.getMessage()));
            return null;
        }
        final CompletableFuture<List<SnmpValue>> future = locationAwareSnmpClient.get(agent, snmpObjIds)
                .withDescription("snmp:get")
                .withLocation(m_location)
                .withSystemId(m_systemId)
                .execute();

        while (true) {
            try {
                future.get(1, TimeUnit.SECONDS).stream()
                        .forEach(res -> {
                            if (res.isError()) {
                                System.out.println(String.format("ERROR: %s", res));
                            } else {
                                System.out.println(String.format("%s%n", res));
                            }
                        });
                break;
            } catch (TimeoutException e) {
                // pass
                System.out.print(".");
            } catch (InterruptedException | ExecutionException e) {
                System.out.println(String.format("\n %s: %s", m_host, e.getMessage()));
                break;
            }
        }
        return null;
    }
}
