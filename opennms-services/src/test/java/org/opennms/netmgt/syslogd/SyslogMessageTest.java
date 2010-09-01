package org.opennms.netmgt.syslogd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.test.ConfigurationTestUtils;

public class SyslogMessageTest {
    public SyslogMessageTest() throws Exception {
        InputStream stream = null;
        try {
            stream = ConfigurationTestUtils.getInputStreamForResource(this, "/etc/syslogd-configuration.xml");
            SyslogdConfigFactory factory = new SyslogdConfigFactory(stream);
            SyslogdConfigFactory.setInstance(factory);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }
    
    @Test
    public void testCustomParserWithProcess() throws Exception {
        SyslogParser parser = CustomSyslogParser.getParser("<6>test: 2007-01-01 127.0.0.1 OpenNMS[1234]: A SyslogNG style message");
        assertTrue(parser.find());
        SyslogMessage message = parser.parse();
        final Date date = new Date(1167609600000L);

        assertEquals(0, message.getFacility());
        assertEquals(6, message.getSeverity());
        assertEquals("test", message.getMessageID());
        assertEquals(date, message.getDate());
        assertEquals("127.0.0.1", message.getHostName());
        assertEquals("OpenNMS", message.getProcessName());
        assertEquals(1234, message.getProcessId().intValue());
        assertEquals("A SyslogNG style message", message.getMessage());
    }

    @Test
    public void testSyslogNGParserWithProcess() throws Exception {
        SyslogParser parser = SyslogNGParser.getParser("<6>test: 2007-01-01 127.0.0.1 OpenNMS[1234]: A SyslogNG style message");
        assertTrue(parser.find());
        SyslogMessage message = parser.parse();
        final Date date = new Date(1167609600000L);

        assertEquals(0, message.getFacility());
        assertEquals(6, message.getSeverity());
        assertEquals("test", message.getMessageID());
        assertEquals(date, message.getDate());
        assertEquals("127.0.0.1", message.getHostName());
        assertEquals("OpenNMS", message.getProcessName());
        assertEquals(1234, message.getProcessId().intValue());
        assertEquals("A SyslogNG style message", message.getMessage());
    }

    @Test
    public void testSyslogNGParserWithoutProcess() throws Exception {
        SyslogParser parser = SyslogNGParser.getParser("<6>test: 2007-01-01 127.0.0.1 A SyslogNG style message");
        assertTrue(parser.find());
        SyslogMessage message = parser.parse();
        final Date date = new Date(1167609600000L);

        assertEquals(0, message.getFacility());
        assertEquals(6, message.getSeverity());
        assertEquals("test", message.getMessageID());
        assertEquals(date, message.getDate());
        assertEquals("127.0.0.1", message.getHostName());
        assertEquals(null, message.getProcessName());
        assertEquals(null, message.getProcessId());
        assertEquals("A SyslogNG style message", message.getMessage());
    }

    @Test
    public void testRfc5424ParserExample1() throws Exception {
        SyslogParser parser = Rfc5424SyslogParser.getParser("<34>1 2003-10-11T22:14:15.000Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8");
        assertTrue(parser.find());
        SyslogMessage message = parser.parse();
        final Date date = new Date(1065910455000L);

        assertEquals(1, message.getVersion().intValue());
        assertEquals(4, message.getFacility());
        assertEquals(2, message.getSeverity());
        assertEquals(date, message.getDate());
        assertEquals("mymachine.example.com", message.getHostName());
        assertEquals("su", message.getProcessName());
        assertEquals("ID47", message.getMessageID());
        assertEquals("'su root' failed for lonvick on /dev/pts/8", message.getMessage());
    }
    
    @Test
    public void testRfc5424ParserExample2() throws Exception {
        SyslogParser parser = Rfc5424SyslogParser.getParser("<165>1 2003-10-11T22:14:15.000003-00:00 192.0.2.1 myproc 8710 - - %% It's time to make the do-nuts.");
        assertTrue(parser.find());
        SyslogMessage message = parser.parse();
        final Date date = new Date(1065910455003L);

        assertEquals(20, message.getFacility());
        assertEquals(5, message.getSeverity());
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
        SyslogParser parser = Rfc5424SyslogParser.getParser("<165>1 2003-10-11T22:14:15.003Z mymachine.example.com evntslog - ID47 [exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"] BOMAn application event log entry...");
        assertTrue(parser.find());
        SyslogMessage message = parser.parse();
        assertEquals(20, message.getFacility());
        assertEquals(5, message.getSeverity());
        assertEquals(1, message.getVersion().intValue());
        assertEquals("mymachine.example.com", message.getHostName());
        assertEquals("evntslog", message.getProcessName());
        assertEquals(null, message.getProcessId());
        assertEquals("ID47", message.getMessageID());
        assertEquals("An application event log entry...", message.getMessage());
    }

    @Test
    public void testRfc5424ParserExample4() throws Exception {
        SyslogParser parser = Rfc5424SyslogParser.getParser("<165>1 2003-10-11T22:14:15.003Z mymachine.example.com evntslog - ID47 [exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"][examplePriority@32473 class=\"high\"]");
        assertTrue(parser.find());
        SyslogMessage message = parser.parse();
        assertEquals(20, message.getFacility());
        assertEquals(5, message.getSeverity());
        assertEquals(1, message.getVersion().intValue());
        assertEquals("mymachine.example.com", message.getHostName());
        assertEquals("evntslog", message.getProcessName());
        assertEquals(null, message.getProcessId());
        assertEquals("ID47", message.getMessageID());
    }

}
