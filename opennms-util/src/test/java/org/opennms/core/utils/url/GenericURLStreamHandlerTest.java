/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.core.utils.url;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


/**
 * <p>GenericURLStreamHandlerTest class.</p>
 *
 * @author Ronny Trommer <ronny@opennms.org>
 * @version $Id: $
 * @since 1.8.1
 */
public class GenericURLStreamHandlerTest {
    private GenericURLFactory m_genericURLFactory = GenericURLFactory.getInstance();

    private GenericURLStreamHandler m_generGenericURLStreamHandler;

    private GenericURLStreamHandler m_generGenericURLStreamHandler_port;

    private Class<? extends URLConnection> m_testClass;

    private int m_defaultPort = 42;

    @Before
    public void setUp() throws Exception {
        m_testClass = StubGenericURLConnection.class;
    }

    /**
     * Test should expect a NoSuchMethodException and this exception is thrown. I don't know why this test will not work.
     * <p/>
     * TODO indigo: Fix this test to verify NoSuchMethodException
     * <p/>
     * java.lang.AssertionError: Expected exception: java.lang.NoSuchMethodException
     * java.lang.NoSuchMethodException: org.opennms.core.utils.url.ProtectedStubGenericURLConnection.<init>(java.net.URL)
     */
    @Ignore
    @Test(expected = NoSuchMethodException.class)
    public void testFailToCreateURLStreamHandler() {
        URL testUrl = null;
        try {
            testUrl = new URL("http://myhost");
        } catch (MalformedURLException e) {
            Assert.fail("Test URL in testCreateURLStreamHandler test is incorrect. Error message: " + e.getMessage());
        }

        try {
            Class<? extends URLConnection> c = org.opennms.core.utils.url.ProtectedStubGenericURLConnection.class;
            m_generGenericURLStreamHandler = new GenericURLStreamHandler(c);
            m_generGenericURLStreamHandler.openConnection(testUrl);
        } catch (IOException e) {
            Assert.fail("Could not open connection. Error message: " + e.getMessage());
        }
    }

    @Test
    public void testGetDefaultPort() {
        Assert.assertEquals("Default should be -1", new GenericURLStreamHandler(m_testClass).getDefaultPort(), -1);
        Assert.assertEquals("Default should be 42", new GenericURLStreamHandler(m_testClass, m_defaultPort).getDefaultPort(), m_defaultPort);
    }

    @Test
    public void testOpenUrlConnection() {

        try {
            URL testUrl = new URL("http://myhost");
            Assert.assertNotNull(new GenericURLStreamHandler(m_testClass).openConnection(testUrl));
        } catch (MalformedURLException e) {
            Assert.fail("Test URL in testOpenUrlConnection test is incorrect. Error message: " + e.getMessage());
        } catch (IOException e) {
            Assert.fail("Could open connection in testOpenUrlConnection. Error message: " + e.getMessage());
        }
    }
}
