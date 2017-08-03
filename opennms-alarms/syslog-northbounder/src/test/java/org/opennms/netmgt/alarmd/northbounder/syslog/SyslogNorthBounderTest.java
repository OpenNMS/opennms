/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.northbounder.syslog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.graylog2.syslog4j.server.SyslogServer;
import org.graylog2.syslog4j.server.SyslogServerConfigIF;
import org.graylog2.syslog4j.server.SyslogServerEventHandlerIF;
import org.graylog2.syslog4j.server.SyslogServerIF;
import org.graylog2.syslog4j.server.impl.event.printstream.PrintStreamSyslogServerEventHandler;
import org.graylog2.syslog4j.server.impl.net.udp.UDPNetSyslogServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;

/**
 * Tests the Syslog North Bound Interface.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/test-context.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml"
})
@JUnitConfigurationEnvironment
// TODO:Would be great to do something like the following annotation...
// @JUnitSyslogServer(port=8514)
public class SyslogNorthBounderTest {

    /** The Constant SERVER_HOST. */
    private static final String SERVER_HOST = "127.0.0.1";

    /** The Constant MESSAGE_LENGTH. */
    public static final int MESSAGE_LENGTH = 1024;

    /** The Constant SERVER_PORT. */
    private static final int SERVER_PORT = 8514;

    /** The Constant SERVER_PROTOCOL. */
    private static final String SERVER_PROTOCOL = "UDP";

    /** The Constant FACILITY. */
    private static final String FACILITY = "LOCAL0";

    /** The Node DAO. */
    @Autowired
    protected MockNodeDao m_nodeDao;

    @Autowired
    protected MonitoringLocationDao m_locationDao;

    /** The Syslog Server. */
    private SyslogServerIF m_server;

    /** The Test LOG stream. */
    protected TestPrintStream m_logStream;

    /**
     * Needed a String based OutputStream class for the Syslog4j eventhandler interface.
     * 
     * @author <a href="mailto:david@opennms.org">David Hustace</a>
     * 
     */
    class StringOutputStream extends OutputStream {

        /** The string buffer. */
        final StringBuilder m_buf = new StringBuilder(MESSAGE_LENGTH);

        /* (non-Javadoc)
         * @see java.io.OutputStream#write(int)
         */
        @Override
        synchronized public void write(int inByte) throws IOException {
            m_buf.append((char) inByte);
        }

        /**
         * Gets the string.
         *
         * @return the string
         */
        synchronized public String getString() {
            String buffer = m_buf.toString();
            m_buf.setLength(0);
            return buffer;
        }
    }

    /**
     * Handy PrintStream wrapper for implementation in the eventhandler in the
     * Syslog4j server. It wraps the String based OutputStream class, above,
     * such that it makes it handy to retrieve the contents of messages in the
     * Syslog server and avoids having to go to disk for file based ouptut.
     * 
     * @author <a href="mailto:david@opennms.org">David Hustace</a>
     * 
     */
    class TestPrintStream extends PrintStream {

        /** The string output stream. */
        private StringOutputStream m_out;

        /**
         * Instantiates a new test print stream.
         *
         * @param out the out
         */
        public TestPrintStream(StringOutputStream out) {
            super(out);
            m_out = out;
        }

        /**
         * Read stream.
         *
         * @return the string
         */
        public String readStream() {
            return m_out.getString();
        }
    }

    /**
     * Getting ready for tests.
     *
     * @throws InterruptedException the interrupted exception
     */
    @Before
    public void startServer() throws InterruptedException {
        MockLogAppender.setupLogging();

        SyslogServerConfigIF serverConfig = new UDPNetSyslogServerConfig(SERVER_HOST, SERVER_PORT);
        serverConfig.setShutdownWait(0);
        m_logStream = new TestPrintStream(new StringOutputStream());
        SyslogServerEventHandlerIF eventHandler = new PrintStreamSyslogServerEventHandler(m_logStream);
        serverConfig.addEventHandler(eventHandler);
        m_server = SyslogServer.createThreadedInstance("test-udp", serverConfig);
        m_server.initialize("udp", serverConfig);

        //Need this sleep, found a deadlock in the server.
        Thread.sleep(100);
        m_server.run();
    }

    /**
     * Cleans up the Syslog server after each test runs.
     *
     * @throws InterruptedException the interrupted exception
     */
    @After
    public void stopServer() throws InterruptedException {
        m_server.shutdown();
        MockLogAppender.assertNoErrorOrGreater();
    }

    /**
     * This tests forwarding of 7 alarms, one for each OpenNMS severity to
     * verify the LOG_LEVEL agrees with the Severity based on our algorithm.
     *
     * @throws Exception the exception
     */
    @Test
    public void testForwardAlarms() throws Exception {
        String xml = generateConfigXml();
        Resource resource = new ByteArrayResource(xml.getBytes());

        SyslogNorthbounderConfigDao dao = new SyslogNorthbounderConfigDao();
        dao.setConfigResource(resource);
        dao.afterPropertiesSet();

        SyslogNorthbounderConfig config = dao.getConfig();

        List<SyslogNorthbounder> nbis = new LinkedList<>();

        for (SyslogDestination syslogDestination : config.getDestinations()) {
            SyslogNorthbounder nbi = new SyslogNorthbounder(dao, syslogDestination.getName());
            nbi.afterPropertiesSet();
            nbis.add(nbi);
        }

        int j = 7;
        List<NorthboundAlarm> alarms = new LinkedList<>();

        OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "p-brane");
        node.setForeignSource("TestGroup");
        node.setForeignId("1");
        node.setId(m_nodeDao.getNextNodeId());

        OnmsSnmpInterface snmpInterface = new OnmsSnmpInterface(node, 1);
        snmpInterface.setId(1);
        snmpInterface.setIfAlias("Connection to OpenNMS Wifi");
        snmpInterface.setIfDescr("en1");
        snmpInterface.setIfName("en1/0");
        snmpInterface.setPhysAddr("00:00:00:00:00:01");

        Set<OnmsIpInterface> ipInterfaces = new LinkedHashSet<OnmsIpInterface>(j);
        InetAddress address = InetAddress.getByName("10.0.1.1");
        OnmsIpInterface onmsIf = new OnmsIpInterface(address, node);
        onmsIf.setSnmpInterface(snmpInterface);
        onmsIf.setId(1);
        onmsIf.setIfIndex(1);
        onmsIf.setIpHostName("p-brane");
        onmsIf.setIsSnmpPrimary(PrimaryType.PRIMARY);

        ipInterfaces.add(onmsIf);

        node.setIpInterfaces(ipInterfaces);
        m_nodeDao.save(node);
        m_nodeDao.flush();
        for (SyslogNorthbounder nbi : nbis) {
            for (int i = 1; i <=j; ++i) {
                OnmsAlarm onmsAlarm = new OnmsAlarm();
                onmsAlarm.setId(i);
                onmsAlarm.setUei("uei.opennms.org/test/syslogNorthBounder");
                onmsAlarm.setNode(node);
                onmsAlarm.setSeverityId(i);
                onmsAlarm.setIpAddr(InetAddress.getByName("127.0.0.1"));
                onmsAlarm.setCounter(i);
                onmsAlarm.setLogMsg("Node Down");
                onmsAlarm.setX733AlarmType(NorthboundAlarm.x733AlarmType.get(i).name());
                onmsAlarm.setX733ProbableCause(NorthboundAlarm.x733ProbableCause.get(i).getId());
                NorthboundAlarm a = new NorthboundAlarm(onmsAlarm);

                Assert.assertFalse(nbi.accepts(a));
                onmsAlarm.setUei("uei.opennms.org/nodes/nodeDown");
                a = new NorthboundAlarm(onmsAlarm);
                Assert.assertTrue(nbi.accepts(a));

                alarms.add(a);
            }
            nbi.forwardAlarms(alarms);
        }        

        Thread.sleep(100);

        BufferedReader r = new BufferedReader(new StringReader(m_logStream.readStream()));

        List<String> messages = new LinkedList<>();
        String line = null;

        while ((line = r.readLine()) != null) {
            messages.add(line);
            Thread.sleep(10);
        }

        Assert.assertTrue("Log messages sent: 7, Log messages received: " + messages.size(), 7 == messages.size());

        for (String message : messages) {
            System.out.println(message);
        }

        int i = 0;
        for (String message : messages) {
            if (i == 0) {
                i++;
                continue;
            }
            switch (i) {
            case 1:
                Assert.assertTrue("Alarm (OnmsSeverity: "+OnmsSeverity.get(i)+") = LEVEL_INFO.", message.contains("INFO"));
                Assert.assertTrue(message.contains("NODE:p-brane"));
                break;
            case 2:
                Assert.assertTrue("Alarm (OnmsSeverity: "+OnmsSeverity.get(i)+") = LEVEL_NOTICE.", message.contains("NOTICE"));
                Assert.assertTrue(message.contains("NODE:p-brane"));
                break;
            case 3:
                Assert.assertTrue("Alarm (OnmsSeverity: "+OnmsSeverity.get(i)+") = LEVEL_WARN.", message.contains("WARN"));
                Assert.assertTrue(message.contains("NODE:p-brane"));
                break;
            case 4:
                Assert.assertTrue("Alarm (OnmsSeverity: "+OnmsSeverity.get(i)+") = LEVEL_ERROR.", message.contains("ERROR"));
                Assert.assertTrue(message.contains("NODE:p-brane"));
                break;
            case 5:
                Assert.assertTrue("Alarm (OnmsSeverity: "+OnmsSeverity.get(i)+") = LEVEL_ERROR.", message.contains("ERROR"));
                Assert.assertTrue(message.contains("NODE:p-brane"));
                break;
            case 6:
                Assert.assertTrue("Alarm (OnmsSeverity: "+OnmsSeverity.get(i)+") = LEVEL_CRITICAL.", message.contains("CRITICAL"));
                Assert.assertTrue(message.contains("NODE:p-brane"));
                break;
            }
            i++;
        }

    }

    /**
     * Generates configuration XML.
     *
     * @return the string
     */
    private String generateConfigXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
                "<syslog-northbounder-config>\n" + 
                "  <enabled>true</enabled>\n" + 
                "  <nagles-delay>1000</nagles-delay>\n" + 
                "  <batch-size>30</batch-size>\n" + 
                "  <queue-size>30000</queue-size>\n" + 
                "  <message-format>ALARM ID:${alarmId} NODE:${nodeLabel}; PARM-1-NAME: ${parm[name-#1]} PARM-1:${parm[#1]} PARM-2-NAME: ${parm[name-#2]} " +
                "PARM-3-NAME: ${parm[name-#3]} PARM-foreignSource:${parm[foreignSource]} PARM-4-NAME: ${parm[name-#4]} PARM-4: ${parm[#4]} ${logMsg}</message-format>\n" + 
                "  <destination>\n" + 
                "    <destination-name>localTest</destination-name>\n" + 
                "    <host>"+SERVER_HOST+"</host>\n" + 
                "    <port>"+SERVER_PORT+"</port>\n" + 
                "    <ip-protocol>"+SERVER_PROTOCOL+"</ip-protocol>\n" + 
                "    <facility>"+FACILITY+"</facility>\n" + 
                "    <max-message-length>1024</max-message-length>\n" + 
                "    <send-local-name>true</send-local-name>\n" + 
                "    <send-local-time>true</send-local-time>\n" + 
                "    <truncate-message>false</truncate-message>\n" +
                "    <first-occurrence-only>false</first-occurrence-only>" + 
                "  </destination>\n" + 
                "  <uei>uei.opennms.org/nodes/nodeDown</uei>\n" + 
                "  <uei>uei.opennms.org/nodes/nodeUp</uei>\n" + 
                "</syslog-northbounder-config>";
    }

}
