/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.nsclient.detector;

import java.io.IOException;
import java.io.OutputStream;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.opennms.netmgt.provision.server.exchange.RequestHandler;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * <p>JUnit Test Class for NsclientDetector.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:/META-INF/opennms/detectors.xml"})
public class NsclientDetectorTest implements InitializingBean {

    @Autowired
    private NsclientDetector m_detector;

    private SimpleServer m_server = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @After
    public void tearDown() throws Exception{
        if (m_server != null) {
            m_server.stopServer();
            m_server = null;
        }
    }

    @Before
    public void setUp() throws Exception{
        MockLogAppender.setupLogging();
        // Initialize Mock NSClient Server
        m_server  = new SimpleServer() {
            @Override
            public void onInit() {
                addResponseHandler(startsWith("None&1"), new RequestHandler() {
                    @Override
                    public void doRequest(OutputStream out) throws IOException {
                        out.write(String.format("%s\r\n", "NSClient++ 0.3.8.75 2010-05-27").getBytes());
                    }
                });
            }
        };
        m_server.init();
        m_server.startServer();
        Thread.sleep(100); // make sure the server is really started
        // Initialize Detector
        m_detector.setServiceName("NSclient++");
        m_detector.setPort(m_server.getLocalPort());
        m_detector.setTimeout(2000);
        m_detector.setRetries(3);
    }

    @Test(timeout=90000)
    public void testServerSuccess() throws Exception{
        m_detector.setCommand("CLIENTVERSION");
        m_detector.init();
        Assert.assertTrue(m_detector.isServiceDetected(m_server.getInetAddress()));
    }

    @Test(timeout=90000)
    public void testBadCommand() throws Exception{
        m_detector.setCommand("UNKNOWN");
        m_detector.init();
        Assert.assertFalse(m_detector.isServiceDetected(m_server.getInetAddress()));
    }

    @Test(timeout=90000)
    public void testNoCommand() throws Exception{
        m_detector.init(); // Assumes CLIENTVERSION
        Assert.assertTrue(m_detector.isServiceDetected(m_server.getInetAddress()));
    }

}
