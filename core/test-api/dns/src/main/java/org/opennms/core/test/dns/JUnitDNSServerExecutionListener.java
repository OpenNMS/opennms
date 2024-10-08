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
package org.opennms.core.test.dns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opennms.core.test.OpenNMSAbstractTestExecutionListener;
import org.opennms.core.test.dns.annotations.DNSEntry;
import org.opennms.core.test.dns.annotations.DNSZone;
import org.opennms.core.test.dns.annotations.JUnitDNSServer;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.SOARecord;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.Zone;

/**
 * This {@link TestExecutionListener} looks for the {@link JUnitDNSServer} annotation
 * and uses attributes on it to launch a temporary HTTP server for use during unit tests.
 */
public class JUnitDNSServerExecutionListener extends OpenNMSAbstractTestExecutionListener {

	private static final Logger LOG = LoggerFactory.getLogger(JUnitDNSServerExecutionListener.class);

    private static final int DEFAULT_TTL = 3600;
    private DNSServer m_server;

    private static final Pattern s_mxPattern = Pattern.compile("^(\\d+)\\s+(.*)$");
    private static final Pattern s_soaPattern = Pattern.compile("^(\\S+) (\\S+) (\\d+) (\\d+) (\\d+) (\\d+) (\\d+)$");
    private static final Pattern s_srvPattern = Pattern.compile("^(\\d+) (\\d+) (\\d+) (\\S+)$");

    /** {@inheritDoc} */
    @Override
    public void beforeTestMethod(final TestContext testContext) throws Exception {
        super.beforeTestMethod(testContext);

        final JUnitDNSServer config = findTestAnnotation(JUnitDNSServer.class, testContext);

        if (config == null) {
            return;
        }

        LOG.info("initializing DNS on port {}", config.port());

        m_server = new DNSServer();
        m_server.addPort(config.port());

        for (final DNSZone dnsZone : config.zones()) {
            String name = dnsZone.name();
            if (!name.endsWith(".")) {
                name = name + ".";
            }
            final Name zoneName = Name.fromString(name, Name.root);
            LOG.debug("zoneName = {}", zoneName);
            final Zone zone = new Zone(zoneName, new Record[] {
                    new SOARecord(zoneName, DClass.IN, DEFAULT_TTL, zoneName, Name.fromString("admin." + name), 1, DEFAULT_TTL, DEFAULT_TTL, DEFAULT_TTL, DEFAULT_TTL),
                    new NSRecord(zoneName, DClass.IN, DEFAULT_TTL, Name.fromString("resolver1.opendns.com.")),
                    new NSRecord(zoneName, DClass.IN, DEFAULT_TTL, Name.fromString("resolver2.opendns.com.")),
                    new ARecord(zoneName, DClass.IN, DEFAULT_TTL, InetAddressUtils.addr(dnsZone.v4address())),
                    new AAAARecord(zoneName, DClass.IN, DEFAULT_TTL, InetAddressUtils.addr(dnsZone.v6address()))
            });
            LOG.debug("zone = {}", zone);

            Matcher m;
            for (final DNSEntry entry : dnsZone.entries()) {
                LOG.debug("adding entry: {}", entry);
                String hostname = entry.hostname();
                final Name recordName = Name.fromString(hostname, zoneName);
                LOG.debug("name = {}", recordName);
                switch (entry.type()) {
                    case "A":
                        zone.addRecord(new ARecord(recordName, DClass.value(entry.dclass()), DEFAULT_TTL, InetAddressUtils.addr(entry.data())));
                        break;
                    case "AAAA":
                        zone.addRecord(new AAAARecord(recordName, DClass.value(entry.dclass()), DEFAULT_TTL, InetAddressUtils.addr(entry.data())));
                        break;
                    case "CNAME":
                        zone.addRecord(new CNAMERecord(recordName, DClass.value(entry.dclass()), DEFAULT_TTL, Name.fromString(entry.data())));
                        break;
                    case "NS":
                        zone.addRecord(new NSRecord(recordName, DClass.value(entry.dclass()), DEFAULT_TTL, Name.fromString(entry.data())));
                        break;
                    case "MX":
                        m = s_mxPattern.matcher(entry.data());
                        if (m.matches()) {
                            zone.addRecord(new MXRecord(recordName, DClass.value(entry.dclass()), DEFAULT_TTL, Integer.valueOf(m.group(1)), Name.fromString(m.group(2))));
                        } else {
                            LOG.error("Entry data '{}' does not match MX pattern", entry.data());
                        }
                        break;
                    case "PTR":
                        zone.addRecord(new PTRRecord(recordName, DClass.value(entry.dclass()), DEFAULT_TTL, Name.fromString(entry.data())));
                        break;
                    case "SOA":
                        m = s_soaPattern.matcher(entry.data());
                        if (m.matches()) {
                            zone.addRecord(new SOARecord(recordName, DClass.value(entry.dclass()), DEFAULT_TTL, Name.fromString(m.group(1)), Name.fromString(m.group(2)),
                              Long.valueOf(m.group(3)), Long.valueOf(m.group(4)), Long.valueOf(m.group(5)), Long.valueOf(m.group(6)), Long.valueOf(m.group(7))));
                        } else {
                            LOG.error("Entry data '{}' does not match SOA pattern", entry.data());
                        }
                        break;
                    case "SRV":
                        m = s_srvPattern.matcher(entry.data());
                        if (m.matches()) {
                            zone.addRecord(new SRVRecord(recordName, DClass.value(entry.dclass()), DEFAULT_TTL, Integer.valueOf(m.group(1)), Integer.valueOf(m.group(2)),
                              Integer.valueOf(m.group(3)), Name.fromString(m.group(4))));
                        } else {
                            LOG.error("Entry data '{}' does not match MX pattern", entry.data());
                        }
                        break;
                    case "TXT":
                        zone.addRecord(new TXTRecord(recordName, DClass.value(entry.dclass()), DEFAULT_TTL, entry.data()));
                        break;
                    default:
                        LOG.error("DNS entry type {} not supported.", entry.type());
                        break;
                }
            }

            m_server.addZone(zone);
        }

        LOG.debug("starting DNS server");
        m_server.start();
    }

    /** {@inheritDoc} */
    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        super.afterTestMethod(testContext);

        if (m_server != null) {
            LOG.info("stopping DNS server");
            m_server.stop();
        }
    }

}
