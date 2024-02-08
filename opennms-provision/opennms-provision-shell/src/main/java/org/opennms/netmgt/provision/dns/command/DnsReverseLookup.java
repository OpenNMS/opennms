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
package org.opennms.netmgt.provision.dns.command;

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
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.LocationAwareDnsLookupClient;

@Command(scope = "opennms", name = "dns-reverse-lookup", description = "DNS reverse lookup for the specified ipaddress")
@Service
public class DnsReverseLookup implements Action {

    @Option(name = "-l", aliases = "--location", description = "Location", required = false, multiValued = false)
    String m_location;

    @Option(name = "-s", aliases = "--system-id", description = "System ID")
    String m_systemId;

    @Argument(index = 0, name = "ipAddress", description = "ip-address", required = true, multiValued = false)
    String ipAddress;

    @Reference
    public LocationAwareDnsLookupClient client;

    @Override
    public Object execute() throws Exception {
        final CompletableFuture<String> future = client.reverseLookup(InetAddressUtils.addr(ipAddress), m_location, m_systemId);
        while (true) {
            try {
                try {
                    String hostName = future.get(1, TimeUnit.SECONDS);
                    System.out.printf("\n%s resolves to: %s\n", ipAddress, hostName);
                } catch (InterruptedException e) {
                    System.out.println("\nInterrupted.");
                } catch (ExecutionException e) {
                    System.out.printf("\n DNS reverse lookup failed with: %s\n", e);
                }
                break;
            } catch (TimeoutException e) {
                // pass
            }
            System.out.print(".");
            System.out.flush();
        }
        return null;
    }
}
