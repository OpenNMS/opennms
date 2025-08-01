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
package org.opennms.netmgt.provision.service;

import java.net.InetAddress;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.LocationAwareDnsLookupClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DefaultHostnameResolver implements HostnameResolver {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultHostnameResolver.class);

    private final LocationAwareDnsLookupClient m_locationAwareDnsLookupClient;

    public DefaultHostnameResolver(LocationAwareDnsLookupClient locationAwareDnsLookupClient) {
        m_locationAwareDnsLookupClient = Objects.requireNonNull(locationAwareDnsLookupClient);
    }

    @Override
    public CompletableFuture<String> getHostnameAsync(final InetAddress addr, final String location) {
        LOG.debug("Performing reverse lookup on {} at location {}", addr, location);
        return m_locationAwareDnsLookupClient.reverseLookup(addr, location).handle((result, e) -> {
            if (e == null) {
                LOG.debug("Reverse lookup returned {} for {} at location {}", result, addr, location);
                return result;
            } else {
                LOG.warn("Reverse lookup failed for {} at location {}. Using IP address as hostname.", addr, location);
                return InetAddressUtils.str(addr);
            }
        });
    }
}
