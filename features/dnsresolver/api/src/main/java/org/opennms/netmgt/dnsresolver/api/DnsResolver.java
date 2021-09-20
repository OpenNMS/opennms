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
