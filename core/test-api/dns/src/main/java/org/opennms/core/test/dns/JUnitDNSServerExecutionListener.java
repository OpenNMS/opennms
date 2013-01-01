/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.test.dns;

import org.opennms.core.test.OpenNMSAbstractTestExecutionListener;
import org.opennms.core.test.dns.annotations.DNSEntry;
import org.opennms.core.test.dns.annotations.DNSZone;
import org.opennms.core.test.dns.annotations.JUnitDNSServer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.SOARecord;
import org.xbill.DNS.Zone;

/**
 * This {@link TestExecutionListener} looks for the {@link JUnitDNSServer} annotation
 * and uses attributes on it to launch a temporary HTTP server for use during unit tests.
 */
public class JUnitDNSServerExecutionListener extends OpenNMSAbstractTestExecutionListener {
    private static final int DEFAULT_TTL = 3600;
    private DNSServer m_server;

    /** {@inheritDoc} */
    @Override
    public void beforeTestMethod(final TestContext testContext) throws Exception {
        super.beforeTestMethod(testContext);

        final JUnitDNSServer config = findTestAnnotation(JUnitDNSServer.class, testContext);

        if (config == null) {
            return;
        }

        LogUtils.infof(this, "initializing DNS on port %d", config.port());

        m_server = new DNSServer();
        m_server.addPort(config.port());

        for (final DNSZone dnsZone : config.zones()) {
            String name = dnsZone.name();
            if (!name.endsWith(".")) {
                name = name + ".";
            }
            final Name zoneName = Name.fromString(name, Name.root);
            LogUtils.debugf(this, "zoneName = %s", zoneName);
            final Zone zone = new Zone(zoneName, new Record[] {
                    new SOARecord(zoneName, DClass.IN, DEFAULT_TTL, zoneName, Name.fromString("admin." + name), 1, DEFAULT_TTL, DEFAULT_TTL, DEFAULT_TTL, DEFAULT_TTL),
                    new NSRecord(zoneName, DClass.IN, DEFAULT_TTL, Name.fromString("resolver1.opendns.com.")),
                    new NSRecord(zoneName, DClass.IN, DEFAULT_TTL, Name.fromString("resolver2.opendns.com.")),
                    new ARecord(zoneName, DClass.IN, DEFAULT_TTL, InetAddressUtils.addr(dnsZone.v4address())),
                    new AAAARecord(zoneName, DClass.IN, DEFAULT_TTL, InetAddressUtils.addr(dnsZone.v6address()))
            });
            LogUtils.debugf(this, "zone = %s", zone);

            for (final DNSEntry entry : dnsZone.entries()) {
                LogUtils.debugf(this, "adding entry: %s", entry);
                String hostname = entry.hostname();
                final Name recordName = Name.fromString(hostname, zoneName);
                LogUtils.debugf(this, "name = %s", recordName);
                if (entry.ipv6()) {
                    zone.addRecord(new AAAARecord(recordName, DClass.IN, DEFAULT_TTL, InetAddressUtils.addr(entry.address())));
                } else {
                    zone.addRecord(new ARecord(recordName, DClass.IN, DEFAULT_TTL, InetAddressUtils.addr(entry.address())));
                }
            }

            m_server.addZone(zone);
        }

        LogUtils.debugf(this, "starting DNS server");
        m_server.start();
        try {
            Thread.sleep(50);
        } catch (final InterruptedException e) {
            LogUtils.debugf(this, e, "interrupted while waiting for server to come up");
            Thread.currentThread().interrupt();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        super.afterTestMethod(testContext);

        if (m_server != null) {
            LogUtils.infof(this, "stopping DNS server");
            m_server.stop();
        }
    }

}
