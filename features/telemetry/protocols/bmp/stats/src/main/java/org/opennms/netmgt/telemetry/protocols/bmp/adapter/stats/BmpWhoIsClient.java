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
