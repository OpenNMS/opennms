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

package org.opennms.netmgt.scriptd.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.graylog2.syslog4j.server.SyslogServer;
import org.graylog2.syslog4j.server.SyslogServerConfigIF;
import org.graylog2.syslog4j.server.SyslogServerEventHandlerIF;
import org.graylog2.syslog4j.server.SyslogServerIF;
import org.graylog2.syslog4j.server.impl.event.printstream.PrintStreamSyslogServerEventHandler;
import org.graylog2.syslog4j.server.impl.net.udp.UDPNetSyslogServerConfig;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

/**
 * The Class SyslogEventForwarderTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SyslogEventForwarderTest {

    /** The Constant SERVER_HOST. */
    private static final String SERVER_HOST = "127.0.0.1";

    /** The Constant MESSAGE_LENGTH. */
    public static final int MESSAGE_LENGTH = 1024;

    /** The Constant SERVER_PORT. */
    private static final int SERVER_PORT = 8514;

    /** The Syslog Server. */
    private SyslogServerIF m_server;

    /** The Test LOG stream. */
    protected TestPrintStream m_logStream;

    /**
     * The Class StringOutputStream.
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
     * The Class TestPrintStream.
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
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        System.setProperty("opennms.home", "src/test/resources");

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
     * Test forward event.
     *
     * @throws Exception the exception
     */
    @Test
    public void testForwardEvent() throws Exception {
        SyslogEventForwarder forwarder = new SyslogEventForwarder();
        forwarder.initialize("localTest1");

        OnmsNode node = new OnmsNode();
        node.setForeignSource("TestGroup");
        node.setForeignId("1");
        node.setId(1);
        node.setLabel("p-brane");

        Event event = new Event();
        event.setUei("uei.opennms.org/junit/testEvent");
        event.setNodeid(1l);
        event.setDbid(100);
        event.setLogmsg(new Logmsg());
        event.getLogmsg().setContent("something is wrong");
        Parm param = new Parm();
        param.setParmName("forwardEventToActivitySyslog");
        Value value = new Value();
        value.setContent("true");
        param.setValue(value);
        event.setParmCollection(new ArrayList<Parm>());
        event.getParmCollection().add(param);
        event.setSeverity("Major");

        forwarder.forward(event, node);

        Thread.sleep(100);

        BufferedReader r = new BufferedReader(new StringReader(m_logStream.readStream()));

        List<String> messages = new LinkedList<>();
        String line = null;

        while ((line = r.readLine()) != null) {
            messages.add(line);
            Thread.sleep(10);
        }

        Assert.assertTrue("Log messages sent: 1, Log messages received: " + messages.size(), 1 == messages.size());
        messages.forEach(System.out::println);
        Assert.assertTrue(messages.get(0).contains("EVENT 100 FOR NODE p-brane IN TestGroup: something is wrong"));

        forwarder.reload();
        forwarder.shutdown();
    }

}
