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
    default CompletableFuture<Optional<SnmpAgentConfig>> getAgentConfigFromProfiles(InetAddress inetAddress, String location, String oid) {
        return getAgentConfigFromProfiles(inetAddress, location, oid, true);
    }

    CompletableFuture<Optional<SnmpAgentConfig>> getAgentConfigFromProfiles(InetAddress inetAddress, String location, String oid, boolean metaDataInterpolation);

    /**
     * Get @{@link SnmpAgentConfig} from SNMP profiles.
     *
     * @param inetAddress IP address for which agent config need to be retrieved.
     * @param location    location of IP address.
     * @return snmpAgentConfig, a @{@link SnmpAgentConfig} from the matching profile.
     */
    default CompletableFuture<Optional<SnmpAgentConfig>> getAgentConfigFromProfiles(InetAddress inetAddress, String location) {
        return getAgentConfigFromProfiles(inetAddress, location, true);
    }

    CompletableFuture<Optional<SnmpAgentConfig>> getAgentConfigFromProfiles(InetAddress inetAddress, String location, boolean metaDataInterpolation);

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
