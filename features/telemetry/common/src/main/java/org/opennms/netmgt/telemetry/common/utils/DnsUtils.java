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
import java.util.Arrays;
import java.util.List;

import org.minidns.DnsClient;
import org.minidns.dnsserverlookup.AbstractDnsServerLookupMechanism;
import org.minidns.dnsserverlookup.DnsServerLookupMechanism;
import org.minidns.hla.ResolverApi;
import org.minidns.record.PTR;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DnsUtils {
    private static final Logger LOG = LoggerFactory.getLogger(DnsUtils.class);

    private static DnsServerLookupMechanism dnsServerLookupMechanism;

    public static String reverseLookup(final String inetAddress) {
        return reverseLookup(InetAddressUtils.addr(inetAddress));
    }

    public static String reverseLookup(final InetAddress inetAddress) {
        try {
            final PTR ptr = ResolverApi.INSTANCE.reverseLookup(inetAddress).getAnswers().stream().findFirst().orElseGet(null);
            if (ptr != null) {
                return ptr.getTarget().ace;
            }
        } catch (Exception e) {
            LOG.debug("Reverse lookup failed for IP-address {}: {}", inetAddress.getHostAddress(), e.getMessage());
        }
        return null;
    }

    public static void setDnsServers(String... servers) {
        if (dnsServerLookupMechanism != null) {
            DnsClient.removeDNSServerLookupMechanism(dnsServerLookupMechanism);
        }

        if (servers.length == 0) {
            dnsServerLookupMechanism = null;
            return;
        }

        dnsServerLookupMechanism = getLookupMechanism(servers);
        DnsClient.addDnsServerLookupMechanism(dnsServerLookupMechanism);
    }

    private static DnsServerLookupMechanism getLookupMechanism(String... servers) {
        return new AbstractDnsServerLookupMechanism("DnsUtils", 0) {
            @Override
            public List<String> getDnsServerAddresses() {
                return Arrays.asList(servers);
            }

            @Override
            public boolean isAvailable() {
                return true;
            }
        };
    }

    public static String hostnameOrIpAddress(final String inetAddress) {
        return hostnameOrIpAddress(InetAddressUtils.addr(inetAddress));
    }

    public static String hostnameOrIpAddress(final InetAddress inetAddress) {
        final String hostname = reverseLookup(inetAddress);
        return hostname != null ? hostname : inetAddress.getHostAddress();
    }
}
