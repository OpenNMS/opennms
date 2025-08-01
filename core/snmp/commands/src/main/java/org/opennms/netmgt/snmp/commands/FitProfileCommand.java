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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpProfileMapper;

import com.google.common.base.Strings;


@Command(scope = "opennms", name = "snmp-fit", description = "Fit a profile for a given IP address")
@Service
public class FitProfileCommand implements Action {

    @Reference
    private SnmpProfileMapper snmpProfileMapper;

    @Reference
    private SnmpAgentConfigFactory agentConfigFactory;
    
    @Reference
    private EventForwarder eventForwarder;

    @Option(name = "-l", aliases = "--location", description = "Location of IP address that needs fitting")
    String location;

    @Option(name = "-s", aliases = "--save", description = "Save the resulting definition")
    boolean save = false;

    @Option(name = "-o", aliases = "--oid", description = "Custom OID used to fit profile")
    String oid;
    
    @Option(name = "-n", aliases = "--new-suspect", description = "Send newSuspect event on successful fit")
    boolean sendNewSuspect;
    
    @Option(name = "-f", aliases = "--foreign-source", description = "Foreign-source name for newSuspect event, if enabled")
    String foreignSource;

    @Argument(name = "host", description = "IP address to fit", required = true)
    String ipAddress;

    @Argument(index = 1, name = "label", description = "Label of the Snmp Profile used to fit")
    String label;


    @Override
    public Object execute() {

        InetAddress inetAddress;
        CompletableFuture<Optional<SnmpAgentConfig>> future;
        try {
            inetAddress = InetAddress.getByName(ipAddress);
            future = snmpProfileMapper.fitProfile(label, inetAddress, location, oid);
        } catch (UnknownHostException e) {
            System.err.printf("Unknown host '%s' at location '%s': %s%n", ipAddress, location, e.getMessage());
            return null;
        }
        while (future != null) {
            try {
                Optional<SnmpAgentConfig> agentConfig = future.get(1, TimeUnit.SECONDS);
                if (agentConfig.isPresent()) {
                    System.out.printf("Fitted IP address '%s' with profile '%s', agent config: %n", ipAddress, agentConfig.get().getProfileLabel());
                    ShowConfigCommand.prettyPrint(agentConfig.get());
                    if (save) {
                        agentConfigFactory.saveAgentConfigAsDefinition(agentConfig.get(), location, "karaf-shell");
                        System.out.println("*** Saved above config in definitions ***");
                    }
                    if (sendNewSuspect) {
                        EventBuilder eventBuilder = new EventBuilder(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "KarafShell_snmp-fit")
                                .setInterface(inetAddress);
                        if (! Strings.isNullOrEmpty(foreignSource)) {
                            eventBuilder.addParam(EventConstants.PARM_FOREIGN_SOURCE, foreignSource);
                        }
                        if (! Strings.isNullOrEmpty(location)) {
                            eventBuilder.addParam(EventConstants.PARM_LOCATION, location);
                        }
                        eventForwarder.sendNow(eventBuilder.getEvent());
                        System.out.printf("Sent newSuspect event for %s, location '%s', foreign-source '%s'%n", ipAddress, location, foreignSource);
                    }
                    break;
                } else {
                    if (Strings.isNullOrEmpty(label)) {
                        System.out.printf("%nDidn't find any matching profile for IP address '%s' %n", ipAddress);
                    } else {
                        System.out.printf("%nProfile with label '%s' didn't fit for IP address '%s'%n", label, ipAddress);
                    }
                    break;
                }

            } catch (TimeoutException e) {
                //pass
                System.out.print(".");
            } catch (InterruptedException | ExecutionException e1) {
                System.err.printf("\n %s: %s%n", ipAddress, e1.getMessage());
                break;
            }
        }

        return null;
    }


}
