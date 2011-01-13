package org.opennms.core.test;

import java.net.InetAddress;

import org.opennms.core.test.annotations.DNSEntry;
import org.opennms.core.test.annotations.DNSZone;
import org.opennms.core.test.annotations.JUnitDNSServer;
import org.opennms.core.test.dns.DNSServer;
import org.opennms.core.utils.LogUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
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
    public void beforeTestMethod(TestContext testContext) throws Exception {
        super.beforeTestMethod(testContext);

        final JUnitDNSServer config = findTestAnnotation(JUnitDNSServer.class, testContext);

        if (config == null)
            return;

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
                    new NSRecord(zoneName, DClass.IN, DEFAULT_TTL, Name.fromString("resolver2.opendns.com."))
            });
            LogUtils.debugf(this, "zone = %s", zone);

            for (final DNSEntry entry : dnsZone.entries()) {
                LogUtils.debugf(this, "adding entry: %s", entry);
                String hostname = entry.hostname();
                final Name recordName = Name.fromString(hostname, zoneName);
                LogUtils.debugf(this, "name = %s", recordName);
                zone.addRecord(new ARecord(recordName, DClass.IN, DEFAULT_TTL, InetAddress.getByName(entry.address())));
            }

            m_server.addZone(zone);
        }
        
        LogUtils.debugf(this, "starting DNS server");
        m_server.start();
    }

    /** {@inheritDoc} */
    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        super.afterTestMethod(testContext);

        LogUtils.infof(this, "stopping DNS server");
        m_server.stop();
    }

}
