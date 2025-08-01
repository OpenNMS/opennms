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
    private GenericURLStreamHandler m_generGenericURLStreamHandler;

    private Class<? extends URLConnection> m_testClass;

    private int m_defaultPort = 42;

    @Before
    public void setUp() throws Exception {
        m_testClass = StubGenericURLConnection.class;
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
