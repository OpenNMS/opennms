/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.config.SyslogdConfigFactory;

public class SyslogMessageTest {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogMessageTest.class);
    public SyslogMessageTest() throws Exception {
        InputStream stream = null;
        try {
            stream = ConfigurationTestUtils.getInputStreamForResource(this, "/etc/syslogd-configuration.xml");
            final SyslogdConfigFactory factory = new SyslogdConfigFactory(stream);
            SyslogdConfigFactory.setInstance(factory);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }
    
    @Before
    public void setUp() {
        MockLogAppender.setupLogging(true, "TRACE");
    }

    @Test
    public void testCustomParserWithProcess() throws Exception {
        final SyslogParser parser = CustomSyslogParser.getParser("<6>test: 2007-01-01 127.0.0.1 OpenNMS[1234]: A SyslogNG style message");
        assertTrue(parser.find());
        final SyslogMessage message = parser.parse();

        assertEquals(SyslogFacility.KERNEL, message.getFacility());
        assertEquals(SyslogSeverity.INFO, message.getSeverity());
        assertEquals("test", message.getMessageID());
        assertEquals("127.0.0.1", message.getHostName());
        assertEquals("OpenNMS", message.getProcessName());
        assertEquals(1234, message.getProcessId().intValue());
        assertEquals("A SyslogNG style message", message.getMessage());
    }

    @Test
    public void testCustomParserWithSimpleForwardingRegexAndSyslog21Message() throws Exception {
        // see: http://searchdatacenter.techtarget.com/tip/Turn-aggregated-syslog-messages-into-OpenNMS-events

        final InputStream stream = new ByteArrayInputStream(("<syslogd-configuration>" +
                                                        "<configuration " +
                                                        "syslog-port=\"10514\" " +
                                                        "new-suspect-on-message=\"false\" " +
                                                        "forwarding-regexp=\"^((.+?) (.*))\\r?\\n?$\" " +
                                                        "matching-group-host=\"2\" " +
                                                        "matching-group-message=\"3\" " +
                                                        "discard-uei=\"DISCARD-MATCHING-MESSAGES\" " +
                                                        "/></syslogd-configuration>").getBytes());
        final SyslogdConfigFactory factory = new SyslogdConfigFactory(stream);
        SyslogdConfigFactory.setInstance(factory);

        final SyslogParser parser = CustomSyslogParser.getParser("<173>Dec  7 12:02:06 10.13.110.116 mgmtd[8326]: [mgmtd.NOTICE]: Configuration saved to database initial");
        assertTrue(parser.find());
        final SyslogMessage message = parser.parse();
        final Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.MONTH, 11);
        calendar.set(Calendar.DATE, 7);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 2);
        calendar.set(Calendar.SECOND, 6);
        calendar.set(Calendar.MILLISECOND, 0);
        final Date date = calendar.getTime();

        LOG.debug("got message: {}", message);

        assertEquals(SyslogFacility.LOCAL5, message.getFacility());
        assertEquals(SyslogSeverity.NOTICE, message.getSeverity());
        assertEquals(null, message.getMessageID());
        assertEquals(date, message.getDate());
        assertEquals("10.13.110.116", message.getHostName());
        assertEquals("mgmtd", message.getProcessName());
        assertEquals(8326, message.getProcessId().intValue());
        assertEquals("[mgmtd.NOTICE]: Configuration saved to database initial", message.getMessage());
    }
    
    @Test
    public void testCustomParserNms5242() throws Exception {
        final Locale startLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.FRANCE);
            final InputStream stream = new ByteArrayInputStream(
               (
                   "<?xml version=\"1.0\"?>\n" +
                   "<syslogd-configuration>\n" +
                   "    <configuration\n" +
                   "            syslog-port=\"10514\"\n" +
                   "            new-suspect-on-message=\"false\"\n" +
                   "            parser=\"org.opennms.netmgt.syslogd.CustomSyslogParser\"\n" +
                   "            forwarding-regexp=\"^((.+?) (.*))\\n?$\"\n" +
                   "            matching-group-host=\"2\"\n" +
                   "            matching-group-message=\"3\"\n" +
                   "            discard-uei=\"DISCARD-MATCHING-MESSAGES\"\n" +
                   "            />\n" +
                   "\n" +
                   "    <hideMessage>\n" +
                   "        <hideMatch>\n" +
                   "            <match type=\"substr\" expression=\"TEST\"/>\n" +
                   "        </hideMatch>\n" +
                   "    </hideMessage>\n" +
                   "</syslogd-configuration>\n"
                ).getBytes()
            );
            final SyslogdConfigFactory factory = new SyslogdConfigFactory(stream);
            SyslogdConfigFactory.setInstance(factory);
            final SyslogParser parser = CustomSyslogParser.getParser("<0>Mar 14 17:10:25 petrus sudo:  cyrille : user NOT in sudoers ; TTY=pts/2 ; PWD=/home/cyrille ; USER=root ; COMMAND=/usr/bin/vi /etc/aliases");
            assertTrue(parser.find());
            final SyslogMessage message = parser.parse();
            LOG.debug("message = {}", message);
            final Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, Calendar.MARCH);
            cal.set(Calendar.DAY_OF_MONTH, 14);
            cal.set(Calendar.HOUR_OF_DAY, 17);
            cal.set(Calendar.MINUTE, 10);
            cal.set(Calendar.SECOND, 25);
            cal.set(Calendar.MILLISECOND, 0);
            assertEquals(SyslogFacility.KERNEL, message.getFacility());
            assertEquals(SyslogSeverity.EMERGENCY, message.getSeverity());
            assertNull(message.getMessageID());
            assertEquals(cal.getTime(), message.getDate());
            assertEquals("petrus", message.getHostName());
            assertEquals("sudo", message.getProcessName());
            assertEquals(0, message.getProcessId().intValue());
            assertEquals("cyrille : user NOT in sudoers ; TTY=pts/2 ; PWD=/home/cyrille ; USER=root ; COMMAND=/usr/bin/vi /etc/aliases", message.getMessage());
        } finally {
            Locale.setDefault(startLocale);
        }
    }
    
    @Test
    public void testSyslogNGParserWithProcess() throws Exception {
        final SyslogParser parser = SyslogNGParser.getParser("<6>test: 2007-01-01 127.0.0.1 OpenNMS[1234]: A SyslogNG style message");
        assertTrue(parser.find());
        final SyslogMessage message = parser.parse();
        final Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, 2007);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        final Date date = calendar.getTime();

        assertEquals(SyslogFacility.KERNEL, message.getFacility());
        assertEquals(SyslogSeverity.INFO, message.getSeverity());
        assertEquals("test", message.getMessageID());
        assertEquals(date, message.getDate());
        assertEquals("127.0.0.1", message.getHostName());
        assertEquals("OpenNMS", message.getProcessName());
        assertEquals(1234, message.getProcessId().intValue());
        assertEquals("A SyslogNG style message", message.getMessage());
    }

    @Test
    public void testSyslogNGParserWithoutProcess() throws Exception {
        final SyslogParser parser = SyslogNGParser.getParser("<6>test: 2007-01-01 127.0.0.1 A SyslogNG style message");
        assertTrue(parser.find());
        final SyslogMessage message = parser.parse();
        final Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, 2007);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        final Date date = calendar.getTime();

        assertEquals(SyslogFacility.KERNEL, message.getFacility());
        assertEquals(SyslogSeverity.INFO, message.getSeverity());
        assertEquals("test", message.getMessageID());
        assertEquals(date, message.getDate());
        assertEquals("127.0.0.1", message.getHostName());
        assertEquals(null, message.getProcessName());
        assertEquals(null, message.getProcessId());
        assertEquals("A SyslogNG style message", message.getMessage());
    }

    @Test
    public void testSyslogNGParserWithSyslog21Message() throws Exception {
        final SyslogParser parser = SyslogNGParser.getParser("<173>Dec  7 12:02:06 10.13.110.116 mgmtd[8326]: [mgmtd.NOTICE]: Configuration saved to database initial");
        assertTrue(parser.find());
        final SyslogMessage message = parser.parse();
        final Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.DATE, 7);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 2);
        calendar.set(Calendar.SECOND, 6);
        calendar.clear(Calendar.MILLISECOND);
        final Date date = calendar.getTime();

        assertEquals(SyslogFacility.LOCAL5, message.getFacility());
        assertEquals(SyslogSeverity.NOTICE, message.getSeverity());
        assertEquals(null, message.getMessageID());
        assertEquals(date, message.getDate());
        assertEquals("10.13.110.116", message.getHostName());
        assertEquals("mgmtd", message.getProcessName());
        assertEquals(8326, message.getProcessId().intValue());
        assertEquals("[mgmtd.NOTICE]: Configuration saved to database initial", message.getMessage());
    }

    @Test
    public void testRfc5424ParserExample1() throws Exception {
        final SyslogParser parser = Rfc5424SyslogParser.getParser("<34>1 2003-10-11T22:14:15.000Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8");
        assertTrue(parser.find());
        final SyslogMessage message = parser.parse();
        final Date date = new Date(1065910455000L);

        assertEquals(1, message.getVersion().intValue());
        assertEquals(SyslogFacility.AUTH, message.getFacility());
        assertEquals(SyslogSeverity.CRITICAL, message.getSeverity());
        assertEquals(date, message.getDate());
        assertEquals("mymachine.example.com", message.getHostName());
        assertEquals("su", message.getProcessName());
        assertEquals("ID47", message.getMessageID());
        assertEquals("'su root' failed for lonvick on /dev/pts/8", message.getMessage());
    }
    
    @Test
    public void testRfc5424ParserExample2() throws Exception {
        final SyslogParser parser = Rfc5424SyslogParser.getParser("<165>1 2003-10-11T22:14:15.000003-00:00 192.0.2.1 myproc 8710 - - %% It's time to make the do-nuts.");
        assertTrue(parser.find());
        final SyslogMessage message = parser.parse();
        final Date date = new Date(1065910455003L);

        assertEquals(SyslogFacility.LOCAL4, message.getFacility());
        assertEquals(SyslogSeverity.NOTICE, message.getSeverity());
        assertEquals(1, message.getVersion().intValue());
        assertEquals(date, message.getDate());
        assertEquals("192.0.2.1", message.getHostName());
        assertEquals("myproc", message.getProcessName());
        assertEquals(8710, message.getProcessId().intValue());
        assertEquals(null, message.getMessageID());
        assertEquals("%% It's time to make the do-nuts.", message.getMessage());
    }
    
    @Test
    public void testRfc5424ParserExample3() throws Exception {
        final SyslogParser parser = Rfc5424SyslogParser.getParser("<165>1 2003-10-11T22:14:15.003Z mymachine.example.com evntslog - ID47 [exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"] BOMAn application event log entry...");
        assertTrue(parser.find());
        final SyslogMessage message = parser.parse();
        assertEquals(SyslogFacility.LOCAL4, message.getFacility());
        assertEquals(SyslogSeverity.NOTICE, message.getSeverity());
        assertEquals(1, message.getVersion().intValue());
        assertEquals("mymachine.example.com", message.getHostName());
        assertEquals("evntslog", message.getProcessName());
        assertEquals(null, message.getProcessId());
        assertEquals("ID47", message.getMessageID());
        assertEquals("An application event log entry...", message.getMessage());
    }

    @Test
    public void testRfc5424ParserExample4() throws Exception {
        final SyslogParser parser = Rfc5424SyslogParser.getParser("<165>1 2003-10-11T22:14:15.003Z mymachine.example.com evntslog - ID47 [exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"][examplePriority@32473 class=\"high\"]");
        assertTrue(parser.find());
        final SyslogMessage message = parser.parse();
        assertEquals(SyslogFacility.LOCAL4, message.getFacility());
        assertEquals(SyslogSeverity.NOTICE, message.getSeverity());
        assertEquals(1, message.getVersion().intValue());
        assertEquals("mymachine.example.com", message.getHostName());
        assertEquals("evntslog", message.getProcessName());
        assertEquals(null, message.getProcessId());
        assertEquals("ID47", message.getMessageID());
    }
    
    @Test
    public void testRfc5424Nms5051() throws Exception {
        final SyslogParser parser = Rfc5424SyslogParser.getParser("<85>1 2011-11-15T14:42:18+01:00 hostname sudo - - - pam_unix(sudo:auth): authentication failure; logname=username uid=0 euid=0 tty=/dev/pts/0 ruser=username rhost= user=username");
        assertTrue(parser.find());
        final SyslogMessage message = parser.parse();
        assertEquals(SyslogFacility.AUTHPRIV, message.getFacility());
        assertEquals(SyslogSeverity.NOTICE, message.getSeverity());
        assertEquals(1, message.getVersion().intValue());
        assertEquals("hostname", message.getHostName());
        assertEquals("sudo", message.getProcessName());
        assertEquals(null, message.getProcessId());
        assertEquals(null, message.getMessageID());
    }

    @Test
    public void testJuniperCFMFault() throws Exception {
        final SyslogParser parser = Rfc5424SyslogParser.getParser("<27>1 2012-04-20T12:33:13.946Z junos-mx80-2-space cfmd 1317 CFMD_CCM_DEFECT_RMEP - CFM defect: Remote CCM timeout detected by MEP on Level: 6 MD: MD_service_level MA: PW_126 Interface: ge-1/3/2.1");
        assertTrue(parser.find());
        final SyslogMessage message = parser.parse();
        assertNotNull(message);
        assertEquals(SyslogFacility.SYSTEM, message.getFacility());
        assertEquals(SyslogSeverity.ERROR, message.getSeverity());
        assertEquals("junos-mx80-2-space", message.getHostName());
        assertEquals("cfmd", message.getProcessName());
        assertEquals(Integer.valueOf(1317), message.getProcessId());
        assertEquals("CFMD_CCM_DEFECT_RMEP", message.getMessageID());
    }
}
