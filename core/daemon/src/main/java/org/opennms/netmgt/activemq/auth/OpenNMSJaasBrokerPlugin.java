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
package org.opennms.netmgt.activemq.auth;

import java.net.InetAddress;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.jaas.GroupPrincipal;

public class OpenNMSJaasBrokerPlugin implements BrokerPlugin {

    /**
     * A list of hosts from which connections will be automatically trusted,
     * and will be authenticated as 'trustedUsername' with the principals listed
     * in 'trustedPrincipals'.
     */
    private List<String> trustedHosts = new ArrayList<>();

    private String usernameForTrustedHosts = "admin";

    private List<String> groupsForTrustedHosts = Arrays.asList("admin");

    @Override
    public Broker installPlugin(Broker broker) throws Exception {
        final Set<InetAddress> trustedHostAddresses = new HashSet<>();
        for (String trustedHost : trustedHosts) {
            trustedHostAddresses.add(InetAddress.getByName(trustedHost));
        }
        final Set<Principal> principalsForTrustedHosts = groupsForTrustedHosts.stream()
                .map(GroupPrincipal::new)
                .collect(Collectors.toSet());
        return new OpenNMSJaasAuthenticationBroker(broker, trustedHostAddresses, usernameForTrustedHosts, principalsForTrustedHosts);
    }

    public List<String> getTrustedHosts() {
        return trustedHosts;
    }

    public void setTrustedHosts(List<String> trustedHosts) {
        this.trustedHosts = trustedHosts;
    }

    public String getUsernameForTrustedHosts() {
        return usernameForTrustedHosts;
    }

    public void setUsernameForTrustedHosts(String usernameForTrustedHosts) {
        this.usernameForTrustedHosts = usernameForTrustedHosts;
    }

    public List<String> getGroupsForTrustedHosts() {
        return groupsForTrustedHosts;
    }

    public void setGroupsForTrustedHosts(List<String> groupsForTrustedHosts) {
        this.groupsForTrustedHosts = groupsForTrustedHosts;
    }

}
