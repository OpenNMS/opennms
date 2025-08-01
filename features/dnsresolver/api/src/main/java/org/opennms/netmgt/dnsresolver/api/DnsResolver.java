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
package org.opennms.netmgt.dnsresolver.api;

import java.net.InetAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous DNS resolution.
 *
 * @author jwhite
 */
public interface DnsResolver {

    /**
     * Perform a DNS lookup for the given hostname.
     *
     * Returns a future that contains the lookup results.
     * If the optional is empty the lookup was completed but no result was found.
     *
     * @param hostname hostname to lookup
     * @return a future
     */
    CompletableFuture<Optional<InetAddress>> lookup(final String hostname);

    /**
     * Perform a reverse DNS lookup for the given IP address.
     *
     * Returns a future that contains the lookup results.
     * If the optional is empty the lookup was completed but no result was found.
     *
     * @param inetAddress IP address to lookup
     * @return a future
     */
    CompletableFuture<Optional<String>> reverseLookup(final InetAddress inetAddress);

}
