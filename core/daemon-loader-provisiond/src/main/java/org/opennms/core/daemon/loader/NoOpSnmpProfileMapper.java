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
package org.opennms.core.daemon.loader;

import java.net.InetAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpProfileMapper;

/**
 * A no-op implementation of {@link SnmpProfileMapper} that always returns
 * empty results. Used in standalone daemon containers where SNMP profile
 * mapping is not needed (profiles are resolved on core).
 */
public class NoOpSnmpProfileMapper implements SnmpProfileMapper {

    private static final CompletableFuture<Optional<SnmpAgentConfig>> EMPTY =
            CompletableFuture.completedFuture(Optional.empty());

    @Override
    public CompletableFuture<Optional<SnmpAgentConfig>> getAgentConfigFromProfiles(
            InetAddress inetAddress, String location, String oid, boolean metaDataInterpolation) {
        return EMPTY;
    }

    @Override
    public CompletableFuture<Optional<SnmpAgentConfig>> getAgentConfigFromProfiles(
            InetAddress inetAddress, String location, boolean metaDataInterpolation) {
        return EMPTY;
    }

    @Override
    public CompletableFuture<Optional<SnmpAgentConfig>> fitProfile(
            String label, InetAddress inetAddress, String location, String oid) {
        return EMPTY;
    }
}
