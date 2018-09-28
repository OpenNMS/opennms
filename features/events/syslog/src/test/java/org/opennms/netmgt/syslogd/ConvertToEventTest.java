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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.dao.api.AbstractInterfaceToNodeCache;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.hibernate.InterfaceToNodeCacheDaoImpl;
import org.opennms.netmgt.dao.mock.MockInterfaceToNodeCache;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the performance of Syslogd's {@link ConvertToEvent} processor.
 *
 * @author ms043660
 */
public class ConvertToEventTest {

    private static final Logger LOG = LoggerFactory.getLogger(ConvertToEventTest.class);
    private static final SyslogConfigBean radixConfig = new SyslogConfigBean();

    /**
     * Set up before any tests are executed.
     */
    @BeforeClass
    public static void setup() {
        // This is shared be a few tests in this class
        radixConfig.setParser("org.opennms.netmgt.syslogd.RadixTreeSyslogParser");
        radixConfig.setDiscardUei("DISCARD-MATCHING-MESSAGES");
    }

    /**
     * Test method which calls the ConvertToEvent constructor.
     *
     * @throws IOException
     */
    @Test
    public void testConvertToEvent() throws IOException {

        InterfaceToNodeCacheDaoImpl.setInstance(new MockInterfaceToNodeCache());

        // 10000 sample syslogmessages from xml file are taken and passed as
        // Inputstream to create syslogdconfiguration
        InputStream stream = ConfigurationTestUtils.getInputStreamForResource(this,
                                                                              "/etc/syslogd-loadtest-configuration.xml");
        SyslogdConfig config = new SyslogdConfigFactory(stream);

        // Sample message which is embedded in packet and passed as parameter
        // to
        // ConvertToEvent constructor
        byte[] bytes = "<34> 2010-08-19 localhost foo10000: load test 10000 on tty1".getBytes(StandardCharsets.US_ASCII);

        // Datagram packet which is passed as parameter for ConvertToEvent
        // constructor
        DatagramPacket pkt = new DatagramPacket(bytes, bytes.length,
                                                InetAddress.getLocalHost(),
                                                SyslogClient.PORT);
        ByteBuffer data = ByteBuffer.wrap(pkt.getData());

        // ConvertToEvent takes 4 parameter
        // @param addr The remote agent's address.
        // @param port The remote agent's port
        // @param data The XML data in {@link StandardCharsets#US_ASCII} encoding.
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
    public void testCiscoEventConversion() throws IOException {

        InputStream stream = ConfigurationTestUtils.getInputStreamForResource(this, "/etc/syslogd-cisco-configuration.xml");
        SyslogdConfig config = new SyslogdConfigFactory(stream);

        try {
            ConvertToEvent convertToEvent = new ConvertToEvent(
                DistPollerDao.DEFAULT_DIST_POLLER_ID,
                MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
                InetAddressUtils.ONE_TWENTY_SEVEN,
                9999,
                SyslogdTestUtils.toByteBuffer("<190>Mar 11 08:35:17 aaa_host 30128311: Mar 11 08:35:16.844 CST: %SEC-6-IPACCESSLOGP: list in110 denied tcp 192.168.10.100(63923) -> 192.168.11.128(1521), 1 packet"),
                config
            );
            LOG.info("Generated event: {}", convertToEvent.getEvent().toString());
        } catch (MessageDiscardedException e) {
            LOG.error("Message Parsing failed", e);
            fail("Message Parsing failed: " + e.getMessage());
        }
    }

    @Test
    public void testNms5984() throws IOException {

        InputStream stream = ConfigurationTestUtils.getInputStreamForResource(this, "/etc/syslogd-nms5984-configuration.xml");
        SyslogdConfig config = new SyslogdConfigFactory(stream);

        try {
            ConvertToEvent convertToEvent = new ConvertToEvent(
                DistPollerDao.DEFAULT_DIST_POLLER_ID,
                MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
                InetAddressUtils.ONE_TWENTY_SEVEN,
                9999,
                SyslogdTestUtils.toByteBuffer("<11>Jul 19 15:55:21 otrs-test OTRS-CGI-76[14364]: [Error][Kernel::System::ImportExport::ObjectBackend::CI2CILink::ImportDataSave][Line:468]: CILink: Could not create link between CIs!"),
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
        final Path resource = ConfigurationTestUtils.getFileForResource(this, "/syslogMessages.txt").toPath();
        assertNotNull(resource);
        Files.lines(resource).forEach(syslog -> {
            // Ignore comments and blank lines
            if (syslog == null || syslog.trim().length() == 0 || syslog.trim().startsWith("#")) {
                return;
            }

            // Replace the "\u0000" tokens with null characters. This allows us to store the
            // syslogMessages.txt file as text instead of binary in git.
            syslog = syslog.replaceAll("\\\\u0000", "\u0000");
            // Similarly, replace the "\uFEFF" tokens with UTF-16 byte order marker characters.
            syslog = syslog.replaceAll("\\\\uFEFF", "\uFEFF");

            final Event[] events = new Event[5];
            try {
                events[0] = parseSyslog("default", defaultConfig, syslog);
                events[1] = parseSyslog("juniper", juniperConfig, syslog);
                events[2] = parseSyslog("rfc5424", rfc5424Config, syslog);
                events[3] = parseSyslog("syslogNg", syslogNgConfig, syslog);
                events[4] = parseSyslog("radixTree", radixConfig, syslog);

                results.add(syslog);
                if (events[0] != null || events[1] != null || events[2] != null || events[3] != null || events[4] != null) {
                    results.add(String.format("%s\t%s\t%s\t%s\t%s", events[0] != null, events[1] != null, events[2] != null, events[3] != null, events[4] != null));
                } else {
                    results.add("PARSING FAILURE");
                }

                if (events[4] == null) {
                    fail("Grok parsing failure: " + syslog);
                }

                List<String> ueis = new ArrayList<>();
                List<Date> times = new ArrayList<>();
                List<Long> nodeIds = new ArrayList<>();
                List<String> interfaces = new ArrayList<>();
                List<String> messageids = new ArrayList<>();
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
                        messageids.add(event.getParm("messageid") == null ? null : event.getParm("messageid").getValue().getContent());
                        logmsgs.add(event.getLogmsg().getContent());
                        syslogmessages.add(event.getParm("syslogmessage").getValue().getContent());
                        timestamps.add(event.getParm("timestamp") == null ? null : event.getParm("timestamp").getValue().getContent());
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
                assertTrue("messageid parms do not match", compare("messageid", messageids.toArray(new String[0])));
                assertTrue("severity parms do not match", compare("severity", severities.toArray(new String[0])));
                assertTrue("timestamp parms do not match", compare("timestamp", timestamps.toArray(new String[0])));
                assertTrue("process parms do not match", compare("process", processes.toArray(new String[0])));
                assertTrue("service parms do not match", compare("service", services.toArray(new String[0])));
                assertTrue("processid parms do not match", compare("processid", processids.toArray(new String[0])));
                assertTrue("parm counts do not match", compare("parm count", parmcounts.toArray(new Long[0])));
                assertTrue("logmsgs do not match", compare("logmsg", logmsgs.toArray(new String[0])));
                assertTrue("syslogmessage parms do not match", compare("syslogmessage", syslogmessages.toArray(new String[0])));
            } catch (Throwable e) {
                e.printStackTrace();
                fail("Unexpected exception: " + e.getMessage());
            }
        });

        System.out.println("default\tjuniper\trfc5424\tsys-ng\tradix");
        results.stream().forEach(System.out::println);
    }

    private static Event parseSyslog(final String name, final SyslogdConfig config, final String syslog) {
        return parseSyslog(name, config, syslog, null);
    }

    private static Event parseSyslog(final String name, final SyslogdConfig config, final String syslog, Date receivedTimestamp) {
        try {
            ConvertToEvent convert = new ConvertToEvent(
                    DistPollerDao.DEFAULT_DIST_POLLER_ID,
                    MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
                    InetAddressUtils.ONE_TWENTY_SEVEN,
                    9999,
                    SyslogdTestUtils.toByteBuffer(syslog),
                    receivedTimestamp,
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

    @SafeVarargs
    private static <T> boolean compare(final String field, final T... values) {
        T first = null;
        boolean needsFirst = true;
        for (T value : values) {
            if (needsFirst) {
                first = value;
                needsFirst = false;
            } else {
//                System.err.println("Comparing: " + first + " ?= " + string);
                if (first == null) {
                    if (value != null) {
                        LOG.warn("Different values for {}: {}", field, Arrays.stream(values).map(String::valueOf).collect(Collectors.joining(", ")));
                        return false;
                    }
                } else {
                    if (value instanceof Date) {
                        if (Math.abs(((Date)first).getTime() - ((Date)value).getTime()) > 1000) {
                            LOG.warn("Time values for {} differ by more than 1 second: {}", field, Arrays.stream(values).map(String::valueOf).collect(Collectors.joining(", ")));
                            return false;
                        } else {
                            continue;
                        }
                    } else if (!first.equals(value)) {
                        LOG.warn("Different values for {}: {}", field, Arrays.stream(values).map(String::valueOf).collect(Collectors.joining(", ")));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Test
    public void testTrimTrailingNulls() {
        ByteBuffer allNulls = ByteBuffer.wrap(new byte[] { 0, 0, 0, 0, 0 });
        ByteBuffer allNullsTrimmed = ConvertToEvent.trimTrailingNulls(allNulls);

        allNulls.position(3);

        // Make sure that the original buffer's position and limit is unchanged
        assertEquals(3, allNulls.position());
        assertEquals(5, allNulls.limit());
        assertEquals(2, allNulls.remaining());

        assertEquals(0, allNullsTrimmed.position());
        assertEquals(0, allNullsTrimmed.limit());
        assertEquals(0, allNullsTrimmed.remaining());

        ByteBuffer middleByte = ByteBuffer.wrap(new byte[] { 0, 0, 4, 0, 0 });
        ByteBuffer middleByteTrimmed = ConvertToEvent.trimTrailingNulls(middleByte);

        middleByte.position(3);

        // Make sure that the original buffer's position and limit is unchanged
        assertEquals(3, middleByte.position());
        assertEquals(5, middleByte.limit());
        assertEquals(2, middleByte.remaining());

        assertEquals(0, middleByteTrimmed.position());
        assertEquals(3, middleByteTrimmed.limit());
        assertEquals(3, middleByteTrimmed.remaining());
    }

    /**
     * Make sure that a syslog message with no host provided uses the source
     * address of the message log and that the node is correctly populated as a result.
     */
    @Test
    public void testNoHostNameProvided() {
        String syslogMessage = "<14> Mar 29 2004 09:57:04: %PIX-5-304001: 192.168.0.2 Accessed URL " +
                "212.227.109.224:/scriptlib/ClientStdScripts.js";

        InetAddress localHostIP = InetAddressUtils.ONE_TWENTY_SEVEN;
        Integer nodeId = 1;

        // Mock the cache hit for the IP Address to Node ID lookup
        InterfaceToNodeCache originalCache = AbstractInterfaceToNodeCache.getInstance();
        InterfaceToNodeCache mockCache = Mockito.mock(InterfaceToNodeCache.class);
        AbstractInterfaceToNodeCache.setInstance(mockCache);
        when(mockCache.getFirstNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
                localHostIP)).thenReturn(Optional.of(nodeId));

        Event event = parseSyslog("testNoHostNameProvided", radixConfig, syslogMessage);
        String eventHostname = event.getParm("hostname").getValue().getContent();

        // Stop using the mock cache so we don't interfere with other tests
        AbstractInterfaceToNodeCache.setInstance(originalCache);

        assertEquals(localHostIP, InetAddressUtils.addr(eventHostname));
        assertEquals(nodeId.intValue(), event.getNodeid().intValue());
    }

    /**
     * Make sure the host name parameter is not overwritten by the source IP
     * address.
     */
    @Test
    public void testHostNameProvided() {
        String hostname = "testhost";
        String syslogMessage = "<11>Mar 22 14:24:49 " + hostname + " last message repeated 30 times";

        Event event = parseSyslog("testHostNameProvided", radixConfig, syslogMessage);
        String eventHostname = event.getParm("hostname").getValue().getContent();
        assertEquals(hostname, eventHostname);
    }

    /**
     * Make sure we use the received timestamp if there is no timestamp included in the syslog message.
     */
    @Test
    public void testNoTimestampInMessage() {
        RadixTreeParser oldRadixParser = RadixTreeSyslogParser.getRadixParser();

        // Create a custom radix tree parser that can handle our message that contains no timestamp
        RadixTreeParser radixParser = new RadixTreeParser();
        radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("%{STRING:message}").toArray(new ParserStage[0]));
        RadixTreeSyslogParser.setRadixParser(radixParser);

        String syslogMessage = "%CDP-4-NATIVE_VLAN_MISMATCH: Native VLAN mismatch discovered on GigabitEthernet5/9 " +
                "(75), with switch.fqdn.com GigabitEthernet2/4/21 (2)";
        Date now = new Date();
        Event event = parseSyslog("testNoTimestampInMessage", radixConfig, syslogMessage, now);
        RadixTreeSyslogParser.setRadixParser(oldRadixParser);

        Date eventTime = event.getTime();
        assertEquals(now, eventTime);
    }

    /**
     * Make sure that we use the timestamp in the message if provided.
     */
    @Test
    public void testTimestampInMessage() {
        String dateString = "2018-01-01";
        Date messageTime = null;

        try {
            messageTime = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
        } catch (ParseException e) {
            LOG.error("Time parsing failed ({})", dateString);
        }

        String syslogMessage = "<11> " + dateString + " localhost test message";
        Event event = parseSyslog("testNoTimestampInMessage", radixConfig, syslogMessage, new Date());

        Date eventTime = event.getTime();
        assertEquals(messageTime, eventTime);
    }

    /**
     * Tests a message that looks like this:
     * <189>: 2018 Jul 16 23:56:11 CDT: %ETHPORT-5-IF_DOWN_LINK_FAILURE: Interface Ethernet119/1/36 is down (Link failure)
     */
    @Test
    public void testCiscoPatternA() throws ParseException {
        int messageFacilityPriority = 189;
        String messageContent = "%ETHPORT-5-IF_DOWN_LINK_FAILURE: Interface Ethernet119/1/36 is down (Link failure)";
        String timeZone = "CDT";
        String date = "2018 Jul 16 23:56:11";
        String syslogMessage = "<" + messageFacilityPriority + ">: " + date + " " + timeZone + ": " + messageContent;
        Event event = parseSyslog("testCiscoPatternA", radixConfig, syslogMessage, new Date());
        assertEquals(SyslogSeverity.getSeverityForCode(messageFacilityPriority).toString(),
                event.getParm("severity").getValue().getContent());
        assertEquals(new SimpleDateFormat("yyyy MMM dd hh:mm:ss Z").parse(date + " " + timeZone),
                event.getTime());
        assertEquals(messageContent, event.getLogmsg().getContent());
    }

    /**
     * Tests a message that looks like this:
     * %CDP-4-NATIVE_VLAN_MISMATCH: Native VLAN mismatch discovered on GigabitEthernet5/9 (75), with switch.fqdn.com GigabitEthernet2/4/21 (2)
     */
    @Test
    public void testCiscoPatternB() {
        String syslogMessage = "%CDP-4-NATIVE_VLAN_MISMATCH: Native VLAN mismatch discovered on GigabitEthernet5/9 " +
                "(75), with switch.fqdn.com GigabitEthernet2/4/21 (2)";
        Event event = parseSyslog("testCiscoPatternB", radixConfig, syslogMessage, new Date());
        assertEquals(syslogMessage, event.getLogmsg().getContent());
    }

    /**
     * Tests a message that looks like this:
     * <189>71863: LC/0/0/CPU0:Jul 16 23:49:47.738 : ifmgr[217]: %PKT_INFRA-LINEPROTO-5-UPDOWN : Line protocol on Interface Serial0/0/0/0/1/2/2:0, changed state to Up
     */
    @Test
    public void testCiscoPatternC() throws ParseException {
        int messageFacilityPriority = 189;
        String sequenceNum = "71863";
        String componentId = "LC/0/0/CPU0";
        String processName = "ifmgr";
        String processId = "217";
        String messageContent = "%PKT_INFRA-LINEPROTO-5-UPDOWN : Line protocol on Interface Serial0/0/0/0/1/2/2:0, " +
                "changed state to Up";
        String date = "Jul 16 23:49:47.738";
        String syslogMessage = "<" + messageFacilityPriority + ">" + sequenceNum + ": " + componentId +
                ":" + date + " : " + processName + "[" + processId + "]: " + messageContent;
        Event event = parseSyslog("testCiscoPatternC", radixConfig, syslogMessage, new Date());
        assertEquals(SyslogSeverity.getSeverityForCode(messageFacilityPriority).toString(),
                event.getParm("severity").getValue().getContent());
        assertEquals(sequenceNum, event.getParm("SequenceNum").getValue()
                .getContent());
        assertEquals(componentId, event.getParm("ComponentId").getValue()
                .getContent());
        Date expectedDate = new SimpleDateFormat("yyyy MMM dd hh:mm:ss").parse(Calendar.getInstance()
                .get(Calendar.YEAR) + " " + date);
        assertEquals(expectedDate, event.getTime());
        assertEquals(processName, event.getParm("process").getValue().getContent());
        assertEquals(processId, event.getParm("processid").getValue().getContent());
        assertEquals(messageContent, event.getLogmsg().getContent());
    }

    /**
     * Tests a message that looks like this:
     * <188>1421602: Jul 17 04:36:01.993: %CDP-4-NATIVE_VLAN_MISMATCH: Native VLAN mismatch discovered on GigabitEthernet0/43 (503), with Switch GigabitEthernet1/0/24 (1).
     */
    @Test
    public void testCiscoPatternD() throws ParseException {
        int messageFacilityPriority = 188;
        String sequenceNum = "1421602";
        String messageContent = "%CDP-4-NATIVE_VLAN_MISMATCH: Native VLAN mismatch discovered on GigabitEthernet0/43 " +
                "(503), with Switch GigabitEthernet1/0/24 (1).";
        String date = "Jul 17 04:36:01.993";
        String syslogMessage = "<" + messageFacilityPriority + ">" + sequenceNum + ": " + date + ": " +
                messageContent;
        Event event = parseSyslog("testCiscoPatternD", radixConfig, syslogMessage, new Date());
        assertEquals(SyslogSeverity.getSeverityForCode(messageFacilityPriority).toString(),
                event.getParm("severity").getValue().getContent());
        assertEquals(sequenceNum, event.getParm("SequenceNum").getValue()
                .getContent());
        Date expectedDate = new SimpleDateFormat("yyyy MMM dd hh:mm:ss").parse(Calendar.getInstance()
                .get(Calendar.YEAR) + " " + date);
        assertEquals(expectedDate, event.getTime());
        assertEquals(messageContent, event.getLogmsg().getContent());
    }

    /**
     * Tests a message that looks like this:
     * <12> 2017 Jul 6 08:42:31 CDT *host1-2-3-4* 1,2017/06/02 01:59:06,123ABC456, THREAT
     */
    @Test
    public void testCiscoPatternE() throws ParseException {
        int messageFacilityPriority = 12;
        String messageContent = "1,2017/06/02 01:59:06,123ABC456, THREAT";
        String hostName = "host1-2-3-4";
        String date = "2017 Jul 6 08:42:31";
        String timeZone = "CDT";
        String syslogMessage = "<" + messageFacilityPriority + "> " + date + " " + timeZone + " *" + hostName + "* " +
                messageContent;
        Event event = parseSyslog("testCiscoPatternE", radixConfig, syslogMessage, new Date());
        assertEquals(SyslogSeverity.getSeverityForCode(messageFacilityPriority).toString(),
                event.getParm("severity").getValue().getContent());
        assertEquals(new SimpleDateFormat("yyyy MMM dd hh:mm:ss Z").parse(date + " " + timeZone),
                event.getTime());
        assertEquals(hostName, event.getParm("hostname").getValue().getContent());
        assertEquals(messageContent, event.getLogmsg().getContent());
    }

    /**
     * Tests a message that looks like this:
     * <12> 2017 Jul 6 08:42:31 CDT 1,2017/06/02 01:59:06,123ABC456, THREAT
     */
    @Test
    public void testCiscoPatternF() throws ParseException {
        int messageFacilityPriority = 12;
        String messageContent = "1,2017/06/02 01:59:06,123ABC456, THREAT";
        String date = "2017 Jul 6 08:42:31";
        String timeZone = "CDT";
        String syslogMessage = "<" + messageFacilityPriority + "> " + date + " " + timeZone + " " + messageContent;
        Event event = parseSyslog("testCiscoPatternF", radixConfig, syslogMessage, new Date());
        assertEquals(SyslogSeverity.getSeverityForCode(messageFacilityPriority).toString(),
                event.getParm("severity").getValue().getContent());
        assertEquals(new SimpleDateFormat("yyyy MMM dd hh:mm:ss Z").parse(date + " " + timeZone),
                event.getTime());
        assertEquals(messageContent, event.getLogmsg().getContent());
    }

    /**
     * Make sure the terminal case works where a hostname is the last grok pattern.
     */
    @Test
    public void testHostnameLastPattern() {
        RadixTreeParser oldRadixParser = RadixTreeSyslogParser.getRadixParser();

        // Create a custom radix tree parser that can handle our simple test message
        RadixTreeParser radixParser = new RadixTreeParser();
        radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("%{STRING:message}: %{HOSTNAME:hostname}")
                .toArray(new ParserStage[0]));
        RadixTreeSyslogParser.setRadixParser(radixParser);
        String testPrefix = "%TEST-123: ";

        String testHostName = "my-test-host";
        String syslogMessage = testPrefix + testHostName;
        Event event = parseSyslog("testHostnameLastPattern", radixConfig, syslogMessage);
        assertEquals(testHostName, event.getParm("hostname").getValue().getContent());

        String testIp = "10.0.0.1";
        syslogMessage = testPrefix + testIp;
        event = parseSyslog("testHostnameLastPattern", radixConfig, syslogMessage);
        assertEquals(testIp, event.getParm("hostname").getValue().getContent());

        RadixTreeSyslogParser.setRadixParser(oldRadixParser);
    }
}
