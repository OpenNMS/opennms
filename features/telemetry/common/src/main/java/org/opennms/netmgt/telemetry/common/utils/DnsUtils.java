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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.Type;

import com.google.common.base.Strings;

public class DnsUtils {
    private static final Logger LOG = LoggerFactory.getLogger(DnsUtils.class);
    public static final String DNS_PRIMARY_SERVER = "org.opennms.features.telemetry.dns.primaryServer";
    public static final String DNS_SECONDARY_SERVER = "org.opennms.features.telemetry.dns.secondaryServer";
    public static final String DNS_ENABLE = "org.opennms.features.telemetry.dns.enable";
    private static ExtendedResolver resolver;

    private static String primaryServer = null, secondaryServer = null;
    private static boolean enable = false;
    static BundleContext bundleContext;

    static {
        try {
            resolver = new ExtendedResolver();
        } catch (UnknownHostException e) {
            LOG.debug("Cannot create resolver: {}", e.getMessage());
        }

        try {
            bundleContext = FrameworkUtil.getBundle(DnsUtils.class).getBundleContext();
        } catch (NullPointerException e) {
            LOG.debug("BundleContext not available: {}", e.getMessage());
        }
    }


    private static void checkSystemProperties() {
        if (bundleContext == null) {
            return;
        }

        final String primaryServer = bundleContext.getProperty(DNS_PRIMARY_SERVER);
        final String secondaryServer = bundleContext.getProperty(DNS_SECONDARY_SERVER);
        final boolean enable = Boolean.parseBoolean(bundleContext.getProperty(DNS_ENABLE));

        if (enable != DnsUtils.enable || !Objects.equals(primaryServer, DnsUtils.primaryServer) || !Objects.equals(secondaryServer, DnsUtils.secondaryServer)) {
            DnsUtils.enable = enable;
            DnsUtils.primaryServer = primaryServer;
            DnsUtils.secondaryServer = secondaryServer;
            setDnsServers(DnsUtils.primaryServer, DnsUtils.secondaryServer);
        }
    }

    public static synchronized void setDnsServers(String... dnsServers) {
        final String[] notNullDnsServers = (dnsServers == null ? new String[]{} : Arrays.stream(dnsServers)
                .filter(e -> !Strings.isNullOrEmpty(e))
                .collect(Collectors.toList())
                .toArray(new String[]{})
        );

        try {
            if (notNullDnsServers.length == 0) {
                resolver = new ExtendedResolver();
            } else {
                resolver = new ExtendedResolver(notNullDnsServers);
            }
        } catch (UnknownHostException e) {
            LOG.debug("Cannot create resolver for given servers {}: {}", dnsServers, e.getMessage());
        }
    }

    static ExtendedResolver getResolver() {
        return resolver;
    }

    public static Optional<String> reverseLookup(final String inetAddress) {
        return reverseLookup(InetAddressUtils.addr(inetAddress));
    }

    public static Optional<String> reverseLookup(final InetAddress inetAddress) {
        checkSystemProperties();

        if (!DnsUtils.enable) {
            return Optional.empty();
        }

        final Lookup lookup = new Lookup(ReverseMap.fromAddress(inetAddress), Type.PTR);
        lookup.setResolver(resolver);

        final Record records[] = lookup.run();
        if (lookup.getResult() == Lookup.SUCCESSFUL) {
            return Arrays.stream(records)
                    .filter(PTRRecord.class::isInstance)
                    .reduce((first, other) -> {
                        LOG.warn("Reverse lookup of hostname got multiple results: {}", inetAddress);
                        return first;
                    })
                    .map(rr -> ((PTRRecord) rr).getTarget().toString())
                    // Strip of the trailing dot
                    .map(hostname -> hostname.substring(0, hostname.length() - 1));
        } else {
            LOG.warn("Reverse lookup of hostname failed: {}", inetAddress);
        }

        return Optional.empty();
    }
}
