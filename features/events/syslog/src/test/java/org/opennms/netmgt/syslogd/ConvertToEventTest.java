/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.hibernate.InterfaceToNodeCacheDaoImpl;
import org.opennms.netmgt.dao.mock.MockInterfaceToNodeCache;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
  * Convert to event junit test file to test the performance of Syslogd ConvertToEvent processor
 * @author ms043660
 */
public class ConvertToEventTest {

    private static final Logger LOG = LoggerFactory.getLogger(ConvertToEventTest.class);

    /**
     * Test method which calls the ConvertToEvent constructor.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException
     */
    @Test
    public void testConvertToEvent() throws MarshalException, ValidationException, IOException {

        InterfaceToNodeCacheDaoImpl.setInstance(new MockInterfaceToNodeCache());

        // 10000 sample syslogmessages from xml file are taken and passed as
        // Inputstream to create syslogdconfiguration
        InputStream stream = ConfigurationTestUtils.getInputStreamForResource(this,
                                                                              "/etc/syslogd-loadtest-configuration.xml");
        SyslogdConfig config = new SyslogdConfigFactory(stream);

        // Sample message which is embedded in packet and passed as parameter
        // to
        // ConvertToEvent constructor
        byte[] bytes = "<34> 2010-08-19 localhost foo10000: load test 10000 on tty1".getBytes();

        // Datagram packet which is passed as parameter for ConvertToEvent
        // constructor
        DatagramPacket pkt = new DatagramPacket(bytes, bytes.length,
                                                InetAddress.getLocalHost(),
                                                SyslogClient.PORT);
        String data = StandardCharsets.US_ASCII.decode(ByteBuffer.wrap(pkt.getData())).toString();

        // ConvertToEvent takes 4 parameter
        // @param addr The remote agent's address.
        // @param port The remote agent's port
        // @param data The XML data in US-ASCII encoding.
        // @param len The length of the XML data in the buffer
        try {
            ConvertToEvent convertToEvent = new ConvertToEvent(
                DistPollerDao.DEFAULT_DIST_POLLER_ID,
                MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
                pkt.getAddress(),
                pkt.getPort(),
                data,
                config
            );
            LOG.info("Generated event: {}", convertToEvent.getEvent().toString());
        } catch (MessageDiscardedException e) {
            LOG.error("Message Parsing failed", e);
            fail("Message Parsing failed: " + e.getMessage());
        }
    }

    @Test
    public void testCiscoEventConversion() throws MarshalException, ValidationException, IOException {

        InputStream stream = ConfigurationTestUtils.getInputStreamForResource(this, "/etc/syslogd-cisco-configuration.xml");
        SyslogdConfig config = new SyslogdConfigFactory(stream);

        try {
            ConvertToEvent convertToEvent = new ConvertToEvent(
                DistPollerDao.DEFAULT_DIST_POLLER_ID,
                MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
                InetAddressUtils.ONE_TWENTY_SEVEN,
                9999,
                "<190>Mar 11 08:35:17 aaa_host 30128311: Mar 11 08:35:16.844 CST: %SEC-6-IPACCESSLOGP: list in110 denied tcp 192.168.10.100(63923) -> 192.168.11.128(1521), 1 packet", 
                config
            );
            LOG.info("Generated event: {}", convertToEvent.getEvent().toString());
        } catch (MessageDiscardedException e) {
            LOG.error("Message Parsing failed", e);
            fail("Message Parsing failed: " + e.getMessage());
        }
    }

    @Test
    public void testNms5984() throws MarshalException, ValidationException, IOException {

        InputStream stream = ConfigurationTestUtils.getInputStreamForResource(this, "/etc/syslogd-nms5984-configuration.xml");
        SyslogdConfig config = new SyslogdConfigFactory(stream);

        try {
            ConvertToEvent convertToEvent = new ConvertToEvent(
                DistPollerDao.DEFAULT_DIST_POLLER_ID,
                MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
                InetAddressUtils.ONE_TWENTY_SEVEN,
                9999,
                "<11>Jul 19 15:55:21 otrs-test OTRS-CGI-76[14364]: [Error][Kernel::System::ImportExport::ObjectBackend::CI2CILink::ImportDataSave][Line:468]: CILink: Could not create link between CIs!", 
                config
            );
            LOG.info("Generated event: {}", convertToEvent.getEvent().toString());
        } catch (MessageDiscardedException e) {
            LOG.error("Message Parsing failed", e);
            fail("Message Parsing failed: " + e.getMessage());
        }
    }

    /**
     * This test configures each {@link SyslogParser} type and then
     * executes a variety of syslog messages against all of the parsers.
     * Successful parses are compared against one another to determine if
     * fields are parsed properly.
     * 
     * @throws IOException
     */
    @Test
    public void testCompareImplementations() throws IOException {
        SyslogConfigBean defaultConfig = new SyslogConfigBean();
        defaultConfig.setParser("org.opennms.netmgt.syslogd.CustomSyslogParser");
        defaultConfig.setForwardingRegexp("^.*\\s(19|20)\\d\\d([-/.])(0[1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01])(\\s+)(\\S+)(\\s)(\\S.+)");
        defaultConfig.setMatchingGroupHost(6);
        defaultConfig.setMatchingGroupMessage(8);
        defaultConfig.setDiscardUei("DISCARD-MATCHING-MESSAGES");

        SyslogConfigBean juniperConfig = new SyslogConfigBean();
        juniperConfig.setParser("org.opennms.netmgt.syslogd.JuniperSyslogParser");
        juniperConfig.setForwardingRegexp("^.*\\s(19|20)\\d\\d([-/.])(0[1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01])(\\s+)(\\S+)(\\s)(\\S.+)");
        juniperConfig.setMatchingGroupHost(6);
        juniperConfig.setMatchingGroupMessage(8);
        juniperConfig.setDiscardUei("DISCARD-MATCHING-MESSAGES");

        SyslogConfigBean rfc5424Config = new SyslogConfigBean();
        rfc5424Config.setParser("org.opennms.netmgt.syslogd.Rfc5424SyslogParser");
        rfc5424Config.setDiscardUei("DISCARD-MATCHING-MESSAGES");

        SyslogConfigBean syslogNgConfig = new SyslogConfigBean();
        syslogNgConfig.setParser("org.opennms.netmgt.syslogd.SyslogNGParser");
        syslogNgConfig.setDiscardUei("DISCARD-MATCHING-MESSAGES");

        final List<String> results = new ArrayList<>();
        Files.lines(ConfigurationTestUtils.getFileForResource(this, "/syslogMessages.txt").toPath()).forEach(syslog -> {
            // Ignore comments and blank lines
            if (syslog == null || syslog.trim().length() == 0 || syslog.trim().startsWith("#")) {
                return;
            }

            // Replace the "\x00" tokens with null characters. This allows us to store the 
            // syslogMessages.txt file as text instead of binary in git
            syslog = syslog.replaceAll("\\x00", "\0");

            final Event[] events = new Event[4];
            try {
                events[0] = parseSyslog("default", defaultConfig, syslog);
                events[1] = parseSyslog("juniper", juniperConfig, syslog);
                events[2] = parseSyslog("rfc5424", rfc5424Config, syslog);
                events[3] = parseSyslog("syslog-ng", syslogNgConfig, syslog);

                results.add(syslog);
                if (events[0] != null || events[1] != null || events[2] != null || events[3] != null) {
                    results.add(String.format("%s\t%s\t%s\t%s", events[0] != null, events[1] != null, events[2] != null, events[3] != null));
                } else {
                    results.add("PARSING FAILURE");
                }

                List<String> ueis = new ArrayList<>();
                List<Date> times = new ArrayList<>();
                List<Long> nodeIds = new ArrayList<>();
                List<String> interfaces = new ArrayList<>();
                List<String> logmsgs = new ArrayList<>();
                List<String> syslogmessages = new ArrayList<>();
                List<String> severities = new ArrayList<>();
                List<String> timestamps = new ArrayList<>();
                List<String> processes = new ArrayList<>();
                List<String> services = new ArrayList<>();
                List<String> processids = new ArrayList<>();
                List<Long> parmcounts = new ArrayList<>();

                for (Event event : events) {
                    if (event != null) {
                        ueis.add(event.getUei());
                        times.add(event.getTime());
                        nodeIds.add(event.getNodeid());
                        interfaces.add(event.getInterface());
                        logmsgs.add(event.getLogmsg().getContent());
                        syslogmessages.add(event.getParm("syslogmessage").getValue().getContent());
                        timestamps.add(event.getParm("timestamp").getValue().getContent());
                        // Facility
                        services.add(event.getParm("service").getValue().getContent());
                        // Priority
                        severities.add(event.getParm("severity").getValue().getContent());
                        processes.add(event.getParm("process") == null ? null : event.getParm("process").getValue().getContent());
                        processids.add(event.getParm("processid") == null ? null : event.getParm("processid").getValue().getContent());
                        parmcounts.add((long)event.getParmCollection().size());
                    }
                }

                // Make sure that all parsers that match are emitting the same events
                assertTrue("UEIs do not match", compare("uei", ueis.toArray(new String[0])));
                assertTrue("times do not match", compare("time", times.toArray(new Date[0])));
                assertTrue("nodeIds do not match", compare("nodeId", nodeIds.toArray(new Long[0])));
                assertTrue("interfaces do not match", compare("interface", interfaces.toArray(new String[0])));
                assertTrue("logmsgs do not match", compare("logmsg", logmsgs.toArray(new String[0])));
                assertTrue("syslogmessage parms do not match", compare("syslogmessage", syslogmessages.toArray(new String[0])));
                assertTrue("severity parms do not match", compare("severity", severities.toArray(new String[0])));
                assertTrue("timestamp parms do not match", compare("timestamp", timestamps.toArray(new String[0])));
                assertTrue("process parms do not match", compare("process", processes.toArray(new String[0])));
                assertTrue("service parms do not match", compare("service", services.toArray(new String[0])));
                assertTrue("processid parms do not match", compare("processid", processids.toArray(new String[0])));
                assertTrue("parm counts do not match", compare("parm count", parmcounts.toArray(new Long[0])));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                fail("Unexpected exception: " + e.getMessage());
            }
        });

        System.out.println("default\tjuniper\trfc5424\tsyslog-ng");
        results.stream().forEach(System.out::println);
    }

    private static Event parseSyslog(final String name, final SyslogdConfig config, final String syslog) throws UnsupportedEncodingException {
        try {
            ConvertToEvent convert = new ConvertToEvent(
                DistPollerDao.DEFAULT_DIST_POLLER_ID,
                MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
                InetAddressUtils.ONE_TWENTY_SEVEN,
                9999,
                syslog,
                config
            );
            Event event = convert.getEvent();
            LOG.info("Generated event ({}): {}", name, event.toString());
            return event;

        } catch (MessageDiscardedException e) {
            LOG.error("Message Parsing failed ({})", name);
            return null;
        }
    }

    private static boolean compare(final String value, final String...strings) {
        String first = null;
        for (String string : strings) {
            if (first == null) {
                first = string;
            } else {
//                System.err.println("Comparing: " + first + " ?= " + string);
                if (!first.equals(string)) {
                    LOG.warn("Different values for {}: {}", value, String.join(", ", strings));
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean compare(final String value, final Long...strings) {
        Long first = null;
        for (Long string : strings) {
            if (first == null) {
                first = string;
            } else {
//                System.err.println("Comparing: " + first + " ?= " + string);
                if (!first.equals(string)) {
                    LOG.warn("Different values for {}: {}", value, Arrays.stream(strings).map(String::valueOf).collect(Collectors.joining(", ")));
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean compare(final String value, final Date...strings) {
        Date first = null;
        for (Date string : strings) {
            if (first == null) {
                first = string;
            } else {
//                System.err.println("Comparing: " + first + " ?= " + string);
                if (!first.equals(string)) {
                    LOG.warn("Different values for {}: {}", value, Arrays.stream(strings).map(String::valueOf).collect(Collectors.joining(", ")));
                    return false;
                }
            }
        }
        return true;
    }
}
