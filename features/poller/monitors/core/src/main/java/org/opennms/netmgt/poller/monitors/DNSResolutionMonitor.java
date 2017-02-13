/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * DNSResolutionMonitor
 *
 * @author brozow, fooker
 */
@Distributable
public class DNSResolutionMonitor extends AbstractServiceMonitor {
    public static final Logger LOG = LoggerFactory.getLogger(DNSResolutionMonitor.class);

    public static final String PARM_RESOLUTION_TYPE = "resolution-type";
    public static final String PARM_RESOLUTION_TYPE_V4 = "v4";
    public static final String PARM_RESOLUTION_TYPE_V6 = "v6";
    public static final String PARM_RESOLUTION_TYPE_BOTH = "both";
    public static final String PARM_RESOLUTION_TYPE_EITHER = "either";
    public static final String PARM_RESOLUTION_TYPE_DEFAULT = PARM_RESOLUTION_TYPE_EITHER;

    public static final String PARM_NAMESERVER = "nameserver";

    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        // Get the name to query for
        final Name name;
        try {
            name = new Name(svc.getNodeLabel());

        } catch (final TextParseException e) {
            return PollStatus.unavailable("Invalid record name '" + svc.getNodeLabel() + "': " + e.getMessage());
        }

        // Determine if records for IPv4 and/or IPv6 re required
        final String resolutionType = ParameterMap.getKeyedString(parameters,
                                                                  PARM_RESOLUTION_TYPE,
                                                                  PARM_RESOLUTION_TYPE_DEFAULT);
        final boolean ipv4Required = PARM_RESOLUTION_TYPE_V4.equalsIgnoreCase(resolutionType) ||
                                     PARM_RESOLUTION_TYPE_BOTH.equals(resolutionType);
        final boolean ipv6Required = PARM_RESOLUTION_TYPE_V6.equalsIgnoreCase(resolutionType) ||
                                     PARM_RESOLUTION_TYPE_BOTH.equals(resolutionType);

        // Build a resolver object used for lookups
        final String nameserver = ParameterMap.getKeyedString(parameters,
                                                              PARM_NAMESERVER,
                                                              null);

        final Resolver resolver;
        try {
            if (nameserver == null) {
                // Use system-defined resolvers
                resolver = new ExtendedResolver();
            } else {
                resolver = new SimpleResolver(nameserver);
            }

        } catch (final UnknownHostException e) {
            return PollStatus.unavailable("Unable to resolve nameserver '" + nameserver + "': " + e.getMessage());
        }

        // Start resolving the records
        final long start = System.currentTimeMillis();

        // Resolve the name
        final boolean ipv4Found = resolve(name, resolver, Type.A);
        final boolean ipv6Found = resolve(name, resolver, Type.AAAA);

        // Resolving succeeded - checking results
        final long end = System.currentTimeMillis();

        // Check if result is valid
        if (!ipv4Found && !ipv6Found) {
            return PollStatus.unavailable("Unable to resolve host '" + name + "'");

        } else  if (ipv4Required && !ipv4Found) {
            return PollStatus.unavailable("'" + name + "' could be resolved to an IPv6 address (AAAA record) but not an IPv4 address (A record)");

        } else if (ipv6Required && !ipv6Found) {
            return PollStatus.unavailable("'" + name + "' could be resolved to an IPv4 address (A record) but not an IPv6 address (AAAA record)");

        } else {
            return PollStatus.available((double) (end - start));
        }
    }

    private static boolean resolve(final Name name,
                                   final Resolver resolver,
                                   final int type) {
        final Lookup lookup = new Lookup(name, type);
        lookup.setResolver(resolver);

        final Record[] records = lookup.run();

        if (records == null) {
            return false;
        }

        return Arrays.stream(records)
                     .filter(r -> r.getType() == type)
                     .count() > 0;
    }
}
