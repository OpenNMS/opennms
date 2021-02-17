/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter.stats;

import java.io.IOException;
import java.util.Optional;

import org.apache.commons.net.whois.WhoisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BmpWhoIsClient {

    private static final Logger LOG = LoggerFactory.getLogger(BmpWhoIsClient.class);

    private static String[] hosts = {"whois.arin.net",
            "whois.ripe.net",
            "whois.apnic.net",
            "whois.afrinic.net",
            "whois.lacnic.net",
            "rr.ntt.net"};


    public static Optional<AsnInfo> getAsnInfo(Long asn) {
        WhoisClient whoisClient = new WhoisClient();
        Optional<AsnInfo> asnInfo = Optional.empty();
        for (String host : hosts) {
            try {
                whoisClient.connect(host, 43);
                String output = whoisClient.query("AS" + asn);
                whoisClient.disconnect();
                if (output.contains("ASName") || output.contains("as-name")) {
                    asnInfo = Optional.of(AsnInfo.parseOutput(asn, host, output));
                    break;
                }
            } catch (IOException e) {
                LOG.warn("Exception while fetching whois info with host `{}` for asn = {} ", host, asn, e);
            }
        }
        return asnInfo;
    }

    static Optional<RouteInfo> getRouteInfo(String prefix) {
        WhoisClient whoisClient = new WhoisClient();
        Optional<RouteInfo> routeInfo = Optional.empty();
        for (String host : hosts) {
            try {
                whoisClient.connect(host, 43);
                String rawOutput = whoisClient.query(prefix);
                whoisClient.disconnect();
                if (rawOutput.contains("route")) {
                    RouteInfo parsed = RouteInfo.parseOneRecord(rawOutput);
                    if(parsed.getPrefix() != null && parsed.getPrefixLen() != null
                            && parsed.getOriginAs() != null) {
                        routeInfo = Optional.of(parsed);
                        break;
                    } else {
                        LOG.warn("Not able to parse RouteInfo from {}", rawOutput);
                    }
                }
            } catch (IOException e) {
                LOG.warn("Exception while fetching whois info with host `{}` for prefix = {} ", host, prefix, e);
            }
        }
        return routeInfo;
    }


}
