/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.rrd.tcp;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.FileAnticipator;

/*
import org.python.core.PyException;
import org.python.core.PyDictionary;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
 */

/**
 * Unit tests for the TcpRrdStrategy.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class TcpRrdStrategyTest {

    private RrdStrategy<Object,Object> m_strategy;
    private FileAnticipator m_fileAnticipator;
    private static Thread m_listenerThread;
    private static String m_tempDir;

    @BeforeClass
    public static void startListenerThread() throws Exception {
        m_listenerThread = new Thread() {
            @Override
            public void run() {
                this.setName("fail");
                try {
                    ServerSocket ssocket = new ServerSocket(8999);
                    ssocket.setSoTimeout(500);
                    while (true) {
                        try {
                            /*
                             * This python code is not working properly under Jython. My
                             * hunch is that it would be better under the new Jython 2.5.1
                             * but that version is not easy to use under Maven, see:
                             * 
                             * http://bugs.jython.org/issue1512
                             * http://bugs.jython.org/issue1513
                             * 
                            PythonInterpreter python = new PythonInterpreter();
                            python.execfile(
                                    // Load the python path parser script from the classpath
                                    Thread.currentThread().getContextClassLoader().getResourceAsStream(
                                            "rrdPathParser.py"
                                    )
                            );
                            python.eval("configureRrdPaths('" + m_tempDir + "')");
                             */

                            Socket socket = ssocket.accept();
                            PerformanceDataProtos.PerformanceDataReadings messages = PerformanceDataProtos.PerformanceDataReadings.parseFrom(socket.getInputStream());
                            System.out.println("Number of messages in current packet: " + messages.getMessageCount());
                            for (PerformanceDataProtos.PerformanceDataReading message : messages.getMessageList()) {
                                StringBuffer values = new StringBuffer();
                                values.append("{ ");
                                for (int i = 0; i < message.getValueCount(); i++) {
                                    if (i != 0) { values.append(", "); }
                                    values.append(message.getValue(i));
                                }
                                values.append(" }");
                                System.out.println("Message received: { " + 
                                        "path: \"" + message.getPath() + "\", " + 
                                        "owner: \"" + message.getOwner() + "\", " + 
                                        "timestamp: \"" + message.getTimestamp() + "\", " + 
                                        "values: " + values.toString() + " }");

                                /*
                                 * See comments above re: Jython
                                PyDictionary attributes = (PyDictionary)python.eval("parseRrdPath('" + message.getPath() + "')");
                                System.out.println(attributes.getClass().getName());
                                 */
                            }
                        } catch (SocketTimeoutException e) {
                            if (this.isInterrupted()) {
                                this.setName("notfailed");
                                return;
                            }
                        } catch (IOException e) {
                            ThreadCategory.getInstance(this.getClass()).error(e.getMessage(), e);
                        }
                    }
                } catch (IOException e) {
                    ThreadCategory.getInstance(this.getClass()).error(e.getMessage(), e);
                } catch (Throwable e) {
                    ThreadCategory.getInstance(this.getClass()).error(e.getMessage(), e);
                }
            }
        };

        m_listenerThread.start();
    }

    @Before
    public void setUp() throws Exception {

        MockLogAppender.setupLogging();

        m_strategy = RrdUtils.getStrategy();
        // m_strategy = new TcpRrdStrategy();
        // ((TcpRrdStrategy)m_strategy).setHost("127.0.0.1");
        // ((TcpRrdStrategy)m_strategy).setPort(8999);

        // Don't initialize by default since not all tests need it.
        m_fileAnticipator = new FileAnticipator(false);
    }

    @After
    public void tearDown() throws Exception {
        if (m_fileAnticipator.isInitialized()) {
            m_fileAnticipator.deleteExpected();
        }
        m_fileAnticipator.tearDown();
    }

    @AfterClass
    public static void stopListenerThread() throws Exception {
        m_listenerThread.interrupt();
        m_listenerThread.join();
        assertFalse("Listener thread encountered errors", "fail".equals(m_listenerThread.getName()));
    }

    @Test
    public void testInitialize() {
        // Don't do anything... just check that setUp works 
    }

    @Test
    public void testCreate() throws Exception {
        File rrdFile = createRrdFile();

        Object openedFile = m_strategy.openFile(rrdFile.getAbsolutePath());
        //m_strategy.updateFile(openedFile, "huh?", "N:1,234234");

        m_strategy.closeFile(openedFile);
    }

    @Test
    public void testUpdate() throws Exception {
        File rrdFile = createRrdFile();

        Object openedFile = m_strategy.openFile(rrdFile.getAbsolutePath());
        long currentTimeInSeconds = (long)(new Date().getTime() / 100);
        m_strategy.updateFile(openedFile, "huh?", String.valueOf(currentTimeInSeconds - 9) + ":1.234234");
        m_strategy.updateFile(openedFile, "oh  ", String.valueOf(currentTimeInSeconds - 8) + ":1.234234");
        m_strategy.updateFile(openedFile, "ok  ", String.valueOf(currentTimeInSeconds - 7) + ":1.234234");
        // Sleep in between updates so that we don't underrun the 1-second step size
        Thread.sleep(5000);
        currentTimeInSeconds = (long)(new Date().getTime() / 100);
        m_strategy.updateFile(openedFile, "lol ", String.valueOf(currentTimeInSeconds - 6) + ":1.234234");
        m_strategy.updateFile(openedFile, "lolz", String.valueOf(currentTimeInSeconds - 5) + ":1.234234");
        m_strategy.updateFile(openedFile, "lolz", String.valueOf(currentTimeInSeconds - 4) + ":1.234234");
        m_strategy.updateFile(openedFile, "zzzz", String.valueOf(currentTimeInSeconds - 3) + ":1.234234");
        m_strategy.closeFile(openedFile);
        Thread.sleep(1000);
    }

    public File createRrdFile() throws Exception {
        String rrdFileBase = "foo";
        String rrdExtension = RrdUtils.getExtension();

        m_fileAnticipator.initialize();

        // This is so the RrdUtils.getExtension() call in the strategy works
        // Properties properties = new Properties();
        // properties.setProperty("org.opennms.rrd.fileExtension", rrdExtension);
        // RrdConfig.getInstance().setProperties(properties);

        List<RrdDataSource> dataSources = new ArrayList<RrdDataSource>();
        dataSources.add(new RrdDataSource("bar", "GAUGE", 3000, "U", "U"));
        List<String> rraList = new ArrayList<String>();
        rraList.add("RRA:AVERAGE:0.5:1:2016");
        File tempDir = m_fileAnticipator.getTempDir(); 
        m_tempDir = tempDir.getAbsolutePath();
        // Create an '/rrd/snmp/1' directory in the temp directory so that the
        // RRDs created by the test will have a realistic path
        File rrdDir = m_fileAnticipator.tempDir(m_fileAnticipator.tempDir(m_fileAnticipator.tempDir(tempDir, "rrd"), "snmp"), "1");
        Object def = m_strategy.createDefinition("hello!", rrdDir.getAbsolutePath(), rrdFileBase, 300, dataSources, rraList);
        m_strategy.createFile(def, null);

        return m_fileAnticipator.expecting(rrdDir, rrdFileBase + rrdExtension);
    }
}
