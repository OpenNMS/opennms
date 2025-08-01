/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.utils.url;

import org.junit.Assert;
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
