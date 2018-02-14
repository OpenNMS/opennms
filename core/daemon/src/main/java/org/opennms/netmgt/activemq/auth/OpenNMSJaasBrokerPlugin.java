/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
