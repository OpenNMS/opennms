/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

import java.net.InetAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SnmpProfileMapper {

    /**
     * Get @{@link SnmpAgentConfig} from SNMP profiles.
     *
     * @param inetAddress IP address for which agent config need to be retrieved.
     * @param location    location of Ip address.
     * @param oid         OID with which SNMP get needs to be performed.
     * @return snmpAgentConfig, a @{@link SnmpAgentConfig} from the matching profile.
     */
    CompletableFuture<Optional<SnmpAgentConfig>> getAgentConfigFromProfiles(InetAddress inetAddress, String location, String oid);

    /**
     * Get @{@link SnmpAgentConfig} from SNMP profiles.
     *
     * @param inetAddress IP address for which agent config need to be retrieved.
     * @param location    location of IP address.
     * @return snmpAgentConfig, a @{@link SnmpAgentConfig} from the matching profile.
     */
    CompletableFuture<Optional<SnmpAgentConfig>> getAgentConfigFromProfiles(InetAddress inetAddress, String location);

    /**
     * Fit a profile with specified IP address and label. If no label is specified, it will try to find first matching profile.
     *
     * @param label       label of profile with which SNMP profile will be retrieved.
     * @param inetAddress IP address that needs fitting.
     * @param location    location of IP address.
     * @param oid         OID with which SNMP get needs to be performed
     * @return snmpAgentConfig, a @{@link SnmpAgentConfig} from the matching profile.
     */
    CompletableFuture<Optional<SnmpAgentConfig>> fitProfile(String label, InetAddress inetAddress, String location, String oid);

}
