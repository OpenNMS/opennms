/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
import java.util.Set;
import java.util.TreeSet;

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
    public static final String PARM_RECORD_TYPES = "record-types";
    public static final String PARM_RECORD_TYPE_A = "A";
    public static final String PARM_RECORD_TYPE_AAAA = "AAAA";
    public static final String PARM_RECORD_TYPE_CNAME = "CNAME";
    public static final String PARM_RECORD_TYPE_NS = "NS";
    public static final String PARM_RECORD_TYPE_MX = "MX";
    public static final String PARM_RECORD_TYPE_PTR = "PTR";
    public static final String PARM_RECORD_TYPE_SOA = "SOA";
    public static final String PARM_RECORD_TYPE_SRV = "SRV";
    public static final String PARM_RECORD_TYPE_TXT = "TXT";

    public static final String PARM_NAMESERVER = "nameserver";
    public static final String PARM_LOOKUP = "lookup";

    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        // Get the name to query for
        final Name name;
        final String lookup = ParameterMap.getKeyedString(parameters, PARM_LOOKUP, svc.getNodeLabel());
        try {
            name = new Name(lookup);

        } catch (final TextParseException e) {
            return PollStatus.unavailable("Invalid record name '" + lookup + "': " + e.getMessage());
        }

        Set<Integer> recordTypes = new TreeSet<>();
        // Determine if records for IPv4 and/or IPv6 re required
        final String resolutionType = ParameterMap.getKeyedString(parameters, PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_DEFAULT);
        final String recordTypesParam = ParameterMap.getKeyedString(parameters, PARM_RECORD_TYPES, "");
        Boolean matchAll = Boolean.TRUE;

        if ("".equals(recordTypesParam)) {
            if (PARM_RESOLUTION_TYPE_V4.equalsIgnoreCase(resolutionType) || PARM_RESOLUTION_TYPE_BOTH.equalsIgnoreCase(resolutionType) || PARM_RESOLUTION_TYPE_EITHER.equalsIgnoreCase(resolutionType)) {
                recordTypes.add(Type.A);
            }
            if (PARM_RESOLUTION_TYPE_V6.equalsIgnoreCase(resolutionType) || PARM_RESOLUTION_TYPE_BOTH.equalsIgnoreCase(resolutionType) || PARM_RESOLUTION_TYPE_EITHER.equalsIgnoreCase(resolutionType)) {
                recordTypes.add(Type.AAAA);
            }
            if (PARM_RESOLUTION_TYPE_EITHER.equals(resolutionType)) {
                matchAll = Boolean.FALSE;
            }
        } else {
            for (final String type : recordTypesParam.split(",")) {
                final Integer typeValue = Type.value(type);
                if (typeValue == -1) {
                    LOG.error("Invalid record type '{}' specified in record-types list.", type);
                } else {
                    recordTypes.add(typeValue);
                }
            }
        }

        // Build a resolver object used for lookups
        final String nameserver = ParameterMap.getKeyedString(parameters, PARM_NAMESERVER, null);

        final Resolver resolver;
        try {
            if (nameserver == null) {
                // Use system-defined resolvers
                resolver = new ExtendedResolver();
            } else {
                if ("::1".equals(nameserver)) {
                    resolver = new SimpleResolver(nameserver);
                } else if (nameserver.matches("^\\[[\\d:]+\\]:\\d+$")) {
                    // IPv6 address with port number
                    final Integer pos = nameserver.lastIndexOf(":");
                    String hostname = nameserver.substring(0, pos);
                    hostname = hostname.substring(1, hostname.length() - 1);
                    final Integer port = Integer.valueOf(nameserver.substring(pos + 1));
                    LOG.debug("nameserver: hostname={}, port={}", hostname, port);
                    resolver = new SimpleResolver(hostname);
                    resolver.setPort(port);
                } else if (nameserver.matches("^\\S+:\\d+$")) {
                    // hostname with port number
                    final Integer pos = nameserver.lastIndexOf(":");
                    final String hostname = nameserver.substring(0, pos);
                    final String port = nameserver.substring(pos + 1);
                    LOG.debug("nameserver: hostname={}, port={}", hostname, port);
                    resolver = new SimpleResolver(hostname);
                    resolver.setPort(Integer.getInteger(port));

                } else {
                    // hostname or ip address
                    resolver = new SimpleResolver(nameserver);
                }
            }

        } catch (final UnknownHostException e) {
            return PollStatus.unavailable("Unable to resolve nameserver '" + nameserver + "': " + e.getMessage());
        }

        // Start resolving the records
        final long start = System.currentTimeMillis();

        // Resolve the name
        Set<String> foundTypes = new TreeSet<>();
        Set<String> notFoundTypes = new TreeSet<>();
        recordTypes.forEach((type) -> {
            if (resolve(name, resolver, type)) {
                foundTypes.add(Type.string(type));
            } else {
                LOG.warn("Unable to resolve host '{}' for type '{}'", name, Type.string(type));
                notFoundTypes.add(Type.string(type));
            }
        });

        // Resolving succeeded - checking results
        final long end = System.currentTimeMillis();

        LOG.debug("foundTypes: {}", foundTypes);
        LOG.debug("notFoundTypes: {}", notFoundTypes);
        // Check if result is valid
        if (foundTypes.isEmpty() && !notFoundTypes.isEmpty()) {
            return PollStatus.unavailable("Unable to resolve host '" + name + "'");

        } else if (!foundTypes.isEmpty() && !notFoundTypes.isEmpty() && matchAll) {
            return PollStatus.unavailable("'" + name + "' could be resolved to types [" + foundTypes + "], but not for types [" + notFoundTypes + "]");

        } else {
            return PollStatus.available((double) (end - start));
        }
    }

    private static boolean resolve(final Name name,
                                   final Resolver resolver,
                                   final int type) {
        final Lookup lookup = new Lookup(name, type);
        // NMS-9238: Do not use a cache when looking up the record,
        // that kind of defeats the purpose of this monitor :)
        lookup.setCache(null);
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
