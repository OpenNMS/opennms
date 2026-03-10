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
import java.util.concurrent.CompletableFuture;

import org.opennms.netmgt.provision.LocationAwareDnsLookupClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A local implementation of {@link LocationAwareDnsLookupClient} for standalone
 * daemon containers. Delegates to {@link InetAddress#getByName(String)} for
 * forward lookups and {@link InetAddress#getCanonicalHostName()} for reverse
 * lookups. The location parameter is ignored.
 */
public class LocalDnsLookupClient implements LocationAwareDnsLookupClient {

    private static final Logger LOG = LoggerFactory.getLogger(LocalDnsLookupClient.class);

    @Override
    public CompletableFuture<String> lookup(String hostName, String location) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return InetAddress.getByName(hostName).getHostAddress();
            } catch (Exception e) {
                throw new java.util.concurrent.CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture<String> lookup(String hostName, String location, String systemId) {
        return lookup(hostName, location);
    }

    @Override
    public CompletableFuture<String> reverseLookup(InetAddress ipAddress, String location) {
        return CompletableFuture.supplyAsync(ipAddress::getCanonicalHostName);
    }

    @Override
    public CompletableFuture<String> reverseLookup(InetAddress ipAddress, String location, String systemId) {
        return reverseLookup(ipAddress, location);
    }
}
