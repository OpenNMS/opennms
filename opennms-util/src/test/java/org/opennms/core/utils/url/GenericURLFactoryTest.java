/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
import org.junit.Test;

/**
 * <p>GenericURLFactoryTest class.</p>
 *
 * @author Ronny Trommer <ronny@opennms.org>
 * @version $Id: $
 * @since 1.8.1
 */
public class GenericURLFactoryTest {

    /**
     * URL factory to test
     */
    GenericURLFactory m_genericURLFactory;

    /**
     * <p>setUp</p>
     * <p/>
     * Initialize an instance for the test
     */
    @Before
    public void setUp() {
        m_genericURLFactory = GenericURLFactory.getInstance();
    }

    /**
     * <p>testGetInstance</p>
     * <p/>
     * Try to get a singleton instance of the factory
     */
    @Test
    public void testGetInstance() {
        Assert.assertNotNull("Test generic ", m_genericURLFactory);
    }

    /**
     * <p>testAddURLConnection</p>
     * <p/>
     * Try to add a new protocol with class mapping
     */
    @Test
    public void testAddURLConnection() {
        m_genericURLFactory.addURLConnection("myProtocol", "org.opennms.test.MyProtocolImplementation");
        Assert.assertEquals("Test add URL connection", m_genericURLFactory.getURLConnections().get("myProtocol"), "org.opennms.test.MyProtocolImplementation");
    }

    /**
     * <p>testRemoveURLConnection</p>
     * <p/>
     * Try to remove a previously added protocol
     */
    @Test
    public void testRemoveURLConnection() {
        m_genericURLFactory.removeURLConnection("myProtocol");
        Assert.assertNull("Test add URL connection", m_genericURLFactory.getURLConnections().get("myProtocol"));
    }

    /**
     * <p>testCreateURLStreamHandler</p>
     * <p/>
     * Try to create a URL stream handler.
     */
    @Test
    public void testCreateURLStreamHandler() {
        m_genericURLFactory.addURLConnection("testprotocol", "org.opennms.core.utils.url.StubGenericURLConnection");
        m_genericURLFactory.addURLConnection("noclass", "org.opennms.bkd.class.not.found");
        Assert.assertEquals("Test get test protocol class", m_genericURLFactory.createURLStreamHandler("testprotocol").getClass().getName(), "org.opennms.core.utils.url.GenericURLStreamHandler");
        Assert.assertNull(m_genericURLFactory.createURLStreamHandler("undefined_protocol"));
        Assert.assertNull(m_genericURLFactory.createURLStreamHandler("noclass"));
    }
}
