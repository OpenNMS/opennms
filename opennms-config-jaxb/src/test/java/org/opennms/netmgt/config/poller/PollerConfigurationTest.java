/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.poller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.MockLogger;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.core.xml.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Strings;

public class PollerConfigurationTest extends XmlTestNoCastor<PollerConfiguration> {
    private static final Logger LOG = LoggerFactory.getLogger(PollerConfigurationTest.class);

    public PollerConfigurationTest(final PollerConfiguration sampleObject, final Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Override
    public void setUp() {
        super.setUp();
        final Properties props = new Properties();
        props.put(MockLogger.LOG_KEY_PREFIX + "org.opennms.core.xml", "TRACE");
        MockLogAppender.setupLogging(true, props);
    }

    @Override
    protected boolean ignoreNamespace(final String namespace) {
        return "http://xmlns.opennms.org/xsd/config/poller".equals(namespace) || "http://xmlns.opennms.org/xsd/page-sequence".equals(namespace);
    }
    
    @Override
    protected boolean ignorePrefix(final String prefix) {
        return "ps".equals(prefix);
    }

    protected String getSchemaFile() {
        return "target/classes/xsds/poller-configuration.xsd";
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getMinimalPollerConfiguration(),
                    "<poller-configuration threads=\"30\" nextOutageId=\"SELECT nextval('outageNxtId')\" serviceUnresponsiveEnabled=\"false\" pathOutageEnabled=\"false\">"
                    + "   <node-outage status=\"on\" pollAllIfNoCriticalServiceDefined=\"true\"/>"
                    + "   <package name=\"test\">"
                    + "      <filter>abc</filter>"
                    + "      <rrd step=\"3\"><rra>RRA:AVERAGE:0.5:1:2016</rra></rrd>"
                    + "      <service name=\"ICMP\" interval=\"3\" user-defined=\"true\" status=\"on\"/>"
                    + "      <downtime begin=\"0\" delete=\"true\"/>"
                    + "   </package>"
                    + "   <monitor service=\"ICMP\" class-name=\"org.opennms.netmgt.poller.monitors.IcmpMonitor\"/>"
                    + "</poller-configuration>"
                },
                {
                    getSimplePollerConfiguration(),
                    new File(PollerConfigurationTest.class.getResource("simple-poller-configuration.xml").getFile())
                },
                {
                    get18PollerConfiguration(),
                    new File(PollerConfigurationTest.class.getResource("poller-configuration-1.8.xml").getFile())
                }
        });
    }

    @Test
    public void testNms6490() throws IOException {
        final String originalPollerConfigXml = IOUtils.toString(PollerConfigurationTest.class.getResource("poller-configuration-NMS-6490.xml"));

        LOG.debug("original poller config XML: {}", originalPollerConfigXml);
        final PollerConfiguration pollerConfig = JaxbUtils.unmarshal(PollerConfiguration.class, originalPollerConfigXml);
        assertNotNull(pollerConfig);
        assertEquals(2, pollerConfig.getPackages().size());
        final Package pack = pollerConfig.getPackage("example1");
        assertNotNull(pack);
        final Service hyperic = pack.getService("HypericHQ");
        assertNotNull(hyperic);
        final Parameter psmParam = hyperic.getParameter("page-sequence");
        assertNotNull(psmParam);
        final Element ps = psmParam.getAnyObject();
        assertNotNull(ps);
        assertNotNull(ps.getElementsByTagName("page"));
        assertEquals(3, ps.getElementsByTagName("page").getLength());

        final String marshalledPollerConfigXml = JaxbUtils.marshal(pollerConfig);
        LOG.debug("marshalled poller config XML: {}", marshalledPollerConfigXml);

        assertXmlEquals(originalPollerConfigXml, marshalledPollerConfigXml);
    }

    protected static PollerConfiguration getMinimalPollerConfiguration() {
        final PollerConfiguration config = new PollerConfiguration();
        final NodeOutage no = new NodeOutage();
        no.setStatus("on");
        config.setNodeOutage(no);

        final Package pack = new Package("test");
        pack.setFilter("abc");
        pack.setRrd(new Rrd(3, "RRA:AVERAGE:0.5:1:2016"));
        pack.addService(new Service("ICMP", 3, "true", "on"));
        pack.addDowntime(new Downtime(0, true));
        config.addPackage(pack);

        config.addMonitor("ICMP", "org.opennms.netmgt.poller.monitors.IcmpMonitor");
        return config;
    }

    protected static PollerConfiguration getSimplePollerConfiguration() {
        final PollerConfiguration config = new PollerConfiguration();
        config.setThreads(30);
        config.setServiceUnresponsiveEnabled("false");
        config.setNextOutageId("SELECT nextval('outageNxtId')");

        final NodeOutage nodeOutage = new NodeOutage();
        nodeOutage.setStatus("on");
        nodeOutage.setPollAllIfNoCriticalServiceDefined("true");
        nodeOutage.setCriticalService(new CriticalService("ICMP"));
        config.setNodeOutage(nodeOutage);

        final Package example1 = new Package("example1");
        example1.setFilter(new Filter("IPADDR != '0.0.0.0'"));
        example1.addSpecific("0.0.0.0");
        example1.addIncludeRange(new IncludeRange("1.1.1.1", "254.254.254.254"));
        example1.addIncludeUrl("file:/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/etc/include");

        final Rrd example1rrd = new Rrd();
        example1rrd.setStep(300);
        example1rrd.addRra("RRA:AVERAGE:0.5:1:2016");
        example1rrd.addRra("RRA:AVERAGE:0.5:12:1488");
        example1rrd.addRra("RRA:AVERAGE:0.5:288:366");
        example1rrd.addRra("RRA:MAX:0.5:288:366");
        example1rrd.addRra("RRA:MIN:0.5:288:366");
        example1.setRrd(example1rrd);

        example1.addService(new Service("ICMP", 300000, "false", "on",
                                        "retry", "2",
                                        "timeout", "3000",
                                        "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                        "rrd-base-name", "icmp",
                                        "ds-name", "icmp"
                ));
        
        example1.addOutageCalendar("zzz from poll-outages.xml zzz");

        example1.addDowntime(new Downtime(30000, 0, 300000));
        example1.addDowntime(new Downtime(300000, 300000, 43200000));
        example1.addDowntime(new Downtime(600000, 43200000, 432000000));
        example1.addDowntime(new Downtime(432000000, true));

        config.addPackage(example1);

        config.addMonitor("ICMP", "org.opennms.netmgt.poller.monitors.IcmpMonitor");
        
        return config;
    }

    protected static PollerConfiguration get18PollerConfiguration() throws Exception {
        final PollerConfiguration config = new PollerConfiguration();
        config.setThreads(30);
        config.setServiceUnresponsiveEnabled("false");
        config.setNextOutageId("SELECT nextval('outageNxtId')");

        final NodeOutage nodeOutage = new NodeOutage();
        nodeOutage.setStatus("on");
        nodeOutage.setPollAllIfNoCriticalServiceDefined("true");
        nodeOutage.setCriticalService(new CriticalService("ICMP"));
        config.setNodeOutage(nodeOutage);

        final Package example1 = new Package("example1");
        example1.setFilter(new Filter("IPADDR != '0.0.0.0'"));
        example1.addSpecific("0.0.0.0");
        example1.addIncludeRange(new IncludeRange("1.1.1.1", "254.254.254.254"));
        example1.addIncludeUrl("file:/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/etc/include");

        final Rrd example1rrd = new Rrd();
        example1rrd.setStep(300);
        example1rrd.addRra("RRA:AVERAGE:0.5:1:2016");
        example1rrd.addRra("RRA:AVERAGE:0.5:12:1488");
        example1rrd.addRra("RRA:AVERAGE:0.5:288:366");
        example1rrd.addRra("RRA:MAX:0.5:288:366");
        example1rrd.addRra("RRA:MIN:0.5:288:366");
        example1.setRrd(example1rrd);

        example1.addService(new Service("ICMP", 300000, "false", "on",
                                        "retry", "2",
                                        "timeout", "3000",
                                        "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                        "rrd-base-name", "icmp",
                                        "ds-name", "icmp"
                ));

        example1.addService(new Service("DNS", 300000, "false", "on",
                                        "retry", "2",
                                        "timeout", "5000",
                                        "port", "53",
                                        "lookup", "localhost",
                                        "fatal-response-codes", "2,3,5",
                                        "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                        "rrd-base-name", "dns",
                                        "ds-name", "dns"
                ));

        example1.addService(new Service("SMTP", 300000, "false", "on",
                                        "retry", "1",
                                        "timeout", "3000",
                                        "port", "25",
                                        "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                        "rrd-base-name", "smtp",
                                        "ds-name", "smtp"
                ));

        example1.addService(new Service("FTP", 300000, "false", "on",
                                        "retry", "1",
                                        "timeout", "3000",
                                        "port", "21",
                                        "userid", "",
                                        "password", ""
                ));

        example1.addService(new Service("SNMP", 300000, "false", "off",
                                        "oid", ".1.3.6.1.2.1.1.2.0"
                ));

        example1.addService(new Service("HTTP", 300000, "false", "on",
                                        "retry", "1",
                                        "timeout", "3000",
                                        "port", "80",
                                        "url", "/",
                                        "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                        "rrd-base-name", "http",
                                        "ds-name", "http"
                ));

        example1.addService(new Service("HTTP-8080", 300000, "false", "on",
                                        "retry", "1",
                                        "timeout", "3000",
                                        "port", "8080",
                                        "url", "/",
                                        "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                        "rrd-base-name", "http-8080",
                                        "ds-name", "http-8080"
                ));

        example1.addService(new Service("HTTP-8000", 300000, "false", "on",
                                        "retry", "1",
                                        "timeout", "3000",
                                        "port", "8000",
                                        "url", "/",
                                        "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                        "rrd-base-name", "http-8000",
                                        "ds-name", "http-8000"
                ));

        example1.addService(new Service("HTTP-HostExample", 300000, "false", "on",
                                        "retry", "1",
                                        "timeout", "3000",
                                        "port", "80",
                                        "url", "/wiki/Main_Page",
                                        "host-name", "www.opennms.org",
                                        "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                        "rrd-base-name", "http-hostexample",
                                        "ds-name", "http-hostexample"
                ));

        example1.addService(new Service("HTTPS", 300000, "false", "on",
                                        "retry", "1",
                                        "timeout", "5000",
                                        "port", "443",
                                        "url", "/",
                                        "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                        "rrd-base-name", "https",
                                        "ds-name", "responseTime"
                ));

        example1.addService(new Service("HTTP-MGMT", 300000, "false", "on",
                                        "retry", "1",
                                        "timeout", "3000",
                                        "port", "280",
                                        "url", "/"
                ));

        example1.addService(new Service("HypericAgent", 300000, "false", "on",
                                        "retry", "1",
                                        "timeout", "2200",
                                        "port", "2144"
                ));

        final Service hyperichq = new Service("HypericHQ", 300000, "false", "on",
                                        "retry", "1",
                                        "timeout", "3000",
                                        "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                        "rrd-base-name", "hyperic-hq",
                                        "ds-name", "hyperic-hq"
                );
        final Parameter hypericPageSequence = new Parameter();
        hypericPageSequence.setKey("page-sequence");
        hypericPageSequence.setAnyObject(createPageSequence());

        hyperichq.addParameter(hypericPageSequence);
        example1.addService(hyperichq);

        example1.addService(new Service("MySQL", 300000, "false", "on",
                                        "retry", "1",
                                        "timeout", "3000",
                                        "port", "3306",
                                        "banner", "*"
                ));

        example1.addService(new Service("SQLServer", 300000, "false", "on",
                                        "retry", "1",
                                        "timeout", "3000",
                                        "port", "1433",
                                        "banner", "*"
                ));

        example1.addService(new Service("Oracle", 300000, "false", "on",
                                        "retry", "1",
                                        "timeout", "3000",
                                        "port", "1521",
                                        "banner", "*"
                ));

        example1.addService(new Service("Postgres", 300000, "false", "on",
                                        "retry", "1",
                                        "banner", "*",
                                        "port", "5432",
                                        "timeout", "3000"
                ));

        example1.addService(new Service("Sybase", 300000, "false", "on",
                                        "retry", "1",
                                        "timeout", "3000",
                                        "port", "1498",
                                        "banner", "*"
                ));

        example1.addService(new Service("Informix", 300000, "false", "on",
                                        "retry", "1",
                                        "timeout", "3000",
                                        "port", "1536",
                                        "banner", "*"
                ));

        example1.addService(new Service("SSH", 300000, "false", "on",
                                        "retry", "1",
                                        "banner", "SSH",
                                        "port", "22",
                                        "timeout", "3000",
                                        "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                        "rrd-base-name", "ssh",
                                        "ds-name", "ssh"
                ));

        example1.addService(new Service("IMAP", 300000, "false", "on",
                                        "retry", "1",
                                        "port", "143",
                                        "timeout", "3000"
                ));

        example1.addService(new Service("POP3", 300000, "false", "on",
                                        "retry", "1",
                                        "port", "110",
                                        "timeout", "3000",
                                        "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                        "rrd-base-name", "pop3",
                                        "ds-name", "pop3"
                ));

        example1.addService(new Service("NSClient", 300000, "false", "on",
                                        "retry", "2",
                                        "port", "1248",
                                        "timeout", "3000"
                ));

        example1.addService(new Service("NSClientpp", 300000, "false", "on",
                                        "retry", "2",
                                        "port", "12489",
                                        "timeout", "3000"
                ));

        example1.addService(new Service("NRPE", 300000, "false", "on",
                                        "retry", "3",
                                        "timeout", "3000",
                                        "port", "5666",
                                        "command", "_NRPE_CHECK",
                                        "padding", "2",
                                        "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                        "ds-name", "nrpe"
                ));

        example1.addService(new Service("NRPE-NoSSL", 300000, "false", "on",
                                        "retry", "3",
                                        "timeout", "3000",
                                        "port", "5666",
                                        "command", "_NRPE_CHECK",
                                        "usessl", "false",
                                        "padding", "2",
                                        "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                        "ds-name", "nrpe"
                ));

        example1.addService(new Service("Windows-Task-Scheduler", 300000, "false", "on",
                                        "service-name", "Task Scheduler"
                ));

        example1.addService(new Service("OpenNMS-JVM", 300000, "false", "on",
                                        "port", "18980",
                                        "factory", "PASSWORD-CLEAR",
                                        "username", "admin",
                                        "password", "admin",
                                        "retry", "2",
                                        "timeout", "3000",
                                        "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                        "ds-name", "opennms-jvm",
                                        "friendly-name", "opennms-jvm"
                ));

        example1.addService(new Service("DominoIIOP", 300000, "false", "on",
                                        "retry", "2",
                                        "timeout", "3000",
                                        "ior-port", "80",
                                        "port", "63148"
                ));

        example1.addService(new Service("Citrix", 300000, "false", "on",
                                        "retry", "2",
                                        "timeout", "3000"
                ));

        example1.addService(new Service("LDAP", 300000, "false", "on",
                                        "port", "389",
                                        "version", "3",
                                        "searchbase", "DC=example,DC=org,OU=users",
                                        "searchfilter", "CN=testuser",
                                        "dn", "DN=example,DN=org,OU=users,CN=opennms",
                                        "password", "passwordforopennmsuser",
                                        "retry", "2",
                                        "timeout", "3000"
                ));

        example1.addService(new Service("Memcached", 300000, "false", "on",
                                        "port", "11211",
                                        "retry", "2",
                                        "timeout", "3000",
                                        "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                        "ds-name", "memcached",
                                        "rrd-base-name", "memcached"
                ));

        example1.addService(new Service("NTP", 300000, "false", "off",
                                        "retry", "2",
                                        "timeout", "5000",
                                        "port", "123",
                                        "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                        "ds-name", "ntp"
                ));

        example1.addService(new Service("RadiusAuth", 300000, "false", "on",
                                        "retry", "3",
                                        "timeout", "3000",
                                        "user", "TEST",
                                        "password", "test",
                                        "secret", "opennms",
                                        "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                        "ds-name", "radiusauth"
                ));

        example1.addService(new Service("JVM", 300000, "false", "on",
                                        "port", "9003",
                                        "retry", "2",
                                        "timeout", "3000",
                                        "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                        "ds-name", "jmx",
                                        "friendly-name", "jvm"
                ));

        example1.addOutageCalendar("zzz from poll-outages.xml zzz");

        example1.addDowntime(new Downtime(30000, 0, 300000));
        example1.addDowntime(new Downtime(300000, 300000, 43200000));
        example1.addDowntime(new Downtime(600000, 43200000, 432000000));
        example1.addDowntime(new Downtime(432000000, true));

        config.addPackage(example1);

        final Package strafer = new Package("strafer");
        strafer.setFilter("IPADDR != '0.0.0.0'");
        strafer.addIncludeRange("10.1.1.1", "10.1.1.10");
        strafer.setRrd(new Rrd(300, "RRA:AVERAGE:0.5:1:2016", "RRA:AVERAGE:0.5:12:1488", "RRA:AVERAGE:0.5:288:366", "RRA:MAX:0.5:288:366", "RRA:MIN:0.5:288:366"));

        strafer.addService(new Service("StrafePing", 300000, "false", "on",
                                       "retry", "0",
                                       "timeout", "3000",
                                       "ping-count", "20",
                                       "failure-ping-count", "20",
                                       "wait-interval", "50",
                                       "rrd-repository", "/Users/ranger/rcs/opennms-work/target/opennms-1.13.0-SNAPSHOT/share/rrd/response",
                                       "rrd-base-name", "strafeping"));
        strafer.addDowntime(new Downtime(300000, 0, 432000000));
        strafer.addDowntime(new Downtime(432000000, true));

        config.addPackage(strafer);

        config.addMonitor(new Monitor("ICMP", "org.opennms.netmgt.poller.monitors.IcmpMonitor"));
        config.addMonitor("StrafePing", "org.opennms.netmgt.poller.monitors.StrafePingMonitor");
        config.addMonitor("HTTP", "org.opennms.netmgt.poller.monitors.HttpMonitor");
        config.addMonitor("HTTP-8080", "org.opennms.netmgt.poller.monitors.HttpMonitor");
        config.addMonitor("HTTP-8000", "org.opennms.netmgt.poller.monitors.HttpMonitor");
        config.addMonitor("HTTP-HostExample", "org.opennms.netmgt.poller.monitors.HttpMonitor");
        config.addMonitor("HTTPS", "org.opennms.netmgt.poller.monitors.HttpsMonitor");
        config.addMonitor("HypericAgent", "org.opennms.netmgt.poller.monitors.TcpMonitor");
        config.addMonitor("HypericHQ", "org.opennms.netmgt.poller.monitors.PageSequenceMonitor");
        config.addMonitor("SMTP", "org.opennms.netmgt.poller.monitors.SmtpMonitor");
        config.addMonitor("DNS", "org.opennms.netmgt.poller.monitors.DnsMonitor");
        config.addMonitor("FTP", "org.opennms.netmgt.poller.monitors.FtpMonitor");
        config.addMonitor("SNMP", "org.opennms.netmgt.poller.monitors.SnmpMonitor");
        config.addMonitor("Oracle", "org.opennms.netmgt.poller.monitors.TcpMonitor");
        config.addMonitor("Postgres", "org.opennms.netmgt.poller.monitors.TcpMonitor");
        config.addMonitor("MySQL", "org.opennms.netmgt.poller.monitors.TcpMonitor");
        config.addMonitor("SQLServer", "org.opennms.netmgt.poller.monitors.TcpMonitor");
        config.addMonitor("SSH", "org.opennms.netmgt.poller.monitors.SshMonitor");
        config.addMonitor("IMAP", "org.opennms.netmgt.poller.monitors.ImapMonitor");
        config.addMonitor("POP3", "org.opennms.netmgt.poller.monitors.Pop3Monitor");
        config.addMonitor("NRPE", "org.opennms.netmgt.poller.monitors.NrpeMonitor");
        config.addMonitor("NRPE-NoSSL", "org.opennms.netmgt.poller.monitors.NrpeMonitor");
        config.addMonitor("Windows-Task-Scheduler", "org.opennms.netmgt.poller.monitors.Win32ServiceMonitor");
        config.addMonitor("OpenNMS-JVM", "org.opennms.netmgt.poller.monitors.Jsr160Monitor");
        config.addMonitor("DominoIIOP", "org.opennms.netmgt.poller.monitors.DominoIIOPMonitor");
        config.addMonitor("Citrix", "org.opennms.netmgt.poller.monitors.CitrixMonitor");
        config.addMonitor("LDAP", "org.opennms.netmgt.poller.monitors.LdapMonitor");
        config.addMonitor("Memcached", "org.opennms.netmgt.poller.monitors.MemcachedMonitor");
        config.addMonitor("HTTP-MGMT", "org.opennms.netmgt.poller.monitors.HttpMonitor");
        config.addMonitor("JVM", "org.opennms.netmgt.poller.monitors.Jsr160Monitor");
        config.addMonitor("NTP", "org.opennms.netmgt.poller.monitors.NtpMonitor");
        config.addMonitor("Sybase", "org.opennms.netmgt.poller.monitors.TcpMonitor");
        config.addMonitor("Informix", "org.opennms.netmgt.poller.monitors.TcpMonitor");
        config.addMonitor("DbTestExample", "org.opennms.netmgt.poller.monitors.JDBCStoredProcedureMonitor");
        config.addMonitor("DiskUsage-root", "org.opennms.netmgt.poller.monitors.DiskUsageMonitor");
        config.addMonitor("DiskUsage-home", "org.opennms.netmgt.poller.monitors.DiskUsageMonitor");
        config.addMonitor("UnixTime", "org.opennms.netmgt.poller.monitors.TrivialTimeMonitor");
        config.addMonitor("NON-IP", "org.opennms.netmgt.poller.monitors.PassiveServiceMonitor");
        config.addMonitor("MAIL", "org.opennms.netmgt.poller.monitors.MailTransportMonitor");
        config.addMonitor("MSExchangeSA", "org.opennms.netmgt.poller.monitors.Win32ServiceMonitor");
        config.addMonitor("MSExchangeIS", "org.opennms.netmgt.poller.monitors.Win32ServiceMonitor");
        config.addMonitor("MSExchangeMailboxAssistants", "org.opennms.netmgt.poller.monitors.Win32ServiceMonitor");
        config.addMonitor("MSExchangeMailSubmission", "org.opennms.netmgt.poller.monitors.Win32ServiceMonitor");
        config.addMonitor("MSExchangeADTopology", "org.opennms.netmgt.poller.monitors.Win32ServiceMonitor");
        config.addMonitor("MSExchangeAntispamUpdate", "org.opennms.netmgt.poller.monitors.Win32ServiceMonitor");
        config.addMonitor("MSExchangeEdgeSync", "org.opennms.netmgt.poller.monitors.Win32ServiceMonitor");
        config.addMonitor("MSExchangeFDS", "org.opennms.netmgt.poller.monitors.Win32ServiceMonitor");
        config.addMonitor("MSExchangeServiceHost", "org.opennms.netmgt.poller.monitors.Win32ServiceMonitor");
        config.addMonitor("MSExchangeTransport", "org.opennms.netmgt.poller.monitors.Win32ServiceMonitor");
        config.addMonitor("WMI", "org.opennms.netmgt.poller.monitors.WmiMonitor");
        config.addMonitor("DHCP", "org.opennms.netmgt.poller.monitors.DhcpMonitor");
        config.addMonitor("NSClient", "org.opennms.protocols.nsclient.monitor.NsclientMonitor");
        config.addMonitor("NSClientpp", "org.opennms.protocols.nsclient.monitor.NsclientMonitor");
        config.addMonitor("RadiusAuth", "org.opennms.protocols.radius.monitor.RadiusAuthMonitor");
        config.addMonitor("XMP", "org.opennms.protocols.xmp.monitor.XmpMonitor");

        return config;
    }

    private static Element createPageSequence() throws Exception {
        final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document document = documentBuilder.newDocument();

        final Element rootElement = document.createElementNS("http://xmlns.opennms.org/xsd/config/poller", "page-sequence");
        document.appendChild(rootElement);

        final Element page1 = document.createElementNS("http://xmlns.opennms.org/xsd/config/poller", "page");
        page1.setAttribute("method", "GET");
        page1.setAttribute("http-version", "1.1");
        page1.setAttribute("scheme", "http");
        page1.setAttribute("host", "${ipaddr}");
        page1.setAttribute("disable-ssl-verification", "true");
        page1.setAttribute("port", "7080");
        page1.setAttribute("path", "/Login.do");
        page1.setAttribute("successMatch", "(HQ Login)|(Sign in to Hyperic HQ)");
        page1.setAttribute("response-range", "100-399");
        rootElement.appendChild(page1);

        final Element page2 = document.createElementNS("http://xmlns.opennms.org/xsd/config/poller", "page");
        page2.setAttribute("method", "POST");
        page2.setAttribute("http-version", "1.1");
        page2.setAttribute("scheme", "http");
        page2.setAttribute("host", "${ipaddr}");
        page2.setAttribute("disable-ssl-verification", "true");
        page2.setAttribute("port", "7080");
        page2.setAttribute("path", "/j_security_check.do");
        page2.setAttribute("failureMatch", "(?s)(The username or password provided does not match our records)|(You are not signed in)");
        page2.setAttribute("failureMessage", "HQ Login in Failed");
        page2.setAttribute("successMatch", "HQ Dashboard");
        page2.setAttribute("response-range", "100-399");
        rootElement.appendChild(page2);

        final Element parameter1 = document.createElementNS("http://xmlns.opennms.org/xsd/config/poller", "parameter");
        parameter1.setAttribute("key", "j_username");
        parameter1.setAttribute("value", "hqadmin");
        page2.appendChild(parameter1);

        final Element parameter2 = document.createElementNS("http://xmlns.opennms.org/xsd/config/poller", "parameter");
        parameter2.setAttribute("key", "j_password");
        parameter2.setAttribute("value", "hqadmin");
        page2.appendChild(parameter2);

        // This is a work-around existing because the unmarshalling into DOM Elements of formatted XML includes
        // whitespaces as the Element is always handled as an mixed element where whitespaces must be preserved. The
        // whitespaces will be ignored during unmarshalling the DOM Element into the final objects - but as we're
        // comparing the DOM Elements itself, the spaces must be present as they are in the original XML.
        page2.appendChild(document.createTextNode("\n" + Strings.repeat("   ", 5)));

        final Element page3 = document.createElementNS("http://xmlns.opennms.org/xsd/config/poller", "page");
        page3.setAttribute("method", "GET");
        page3.setAttribute("http-version", "1.1");
        page3.setAttribute("scheme", "http");
        page3.setAttribute("host", "${ipaddr}");
        page3.setAttribute("disable-ssl-verification", "true");
        page3.setAttribute("port", "7080");
        page3.setAttribute("path", "/Logout.do");
        page3.setAttribute("successMatch", "HQ Login");
        page3.setAttribute("response-range", "100-399");
        rootElement.appendChild(page3);

        rootElement.appendChild(document.createTextNode("\n" + Strings.repeat("   ", 4)));

        return document.getDocumentElement();
    }
}
