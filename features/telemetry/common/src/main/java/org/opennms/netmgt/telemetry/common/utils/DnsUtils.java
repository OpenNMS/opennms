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

package org.opennms.netmgt.telemetry.common.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.Type;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DnsUtils {
    private static final Logger LOG = LoggerFactory.getLogger(DnsUtils.class);
    private static ExtendedResolver resolver;

    static {
        try {
            resolver = new ExtendedResolver();
        } catch (UnknownHostException e) {
            LOG.debug("Cannot create resolver: {}", e.getMessage());
        }
    }

    public static synchronized void setDnsServers(String ... dnsServers) {
        try {
            if (dnsServers == null || dnsServers.length == 0) {
                resolver = new ExtendedResolver();
            } else {
                resolver = new ExtendedResolver(dnsServers);
            }
        } catch (UnknownHostException e) {
            LOG.debug("Cannot create resolver for given servers {}: {}", dnsServers, e.getMessage());
        }
    }

    static ExtendedResolver getResolver() {
        return resolver;
    }

    public static String reverseLookup(final String inetAddress) {
        return reverseLookup(InetAddressUtils.addr(inetAddress));
    }

    public static String reverseLookup(final InetAddress inetAddress) {
        final Lookup lookup = new Lookup(ReverseMap.fromAddress(inetAddress), Type.PTR);

        lookup.setResolver(resolver);

        final Record records[] = lookup.run();

        if (lookup.getResult() == Lookup.SUCCESSFUL) {

            final PTRRecord ptrRecord = (PTRRecord) Arrays.stream(records)
                    .filter(PTRRecord.class::isInstance)
                    .findFirst()
                    .orElseGet(null);

            if (ptrRecord != null && ptrRecord.getTarget() != null) {
                final String hostname = ptrRecord.getTarget().toString();
                return hostname.substring(0, hostname.length() - 1);
            }
        }

        return null;
    }

    public static String hostnameOrIpAddress(final String inetAddress) {
        return hostnameOrIpAddress(InetAddressUtils.addr(inetAddress));
    }

    public static String hostnameOrIpAddress(final InetAddress inetAddress) {
        final String hostname = reverseLookup(inetAddress);
        return hostname != null ? hostname : inetAddress.getHostAddress();
    }
}
