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
package org.opennms.netmgt.dao.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;
import org.opennms.core.wsman.WSManEndpoint;
import org.springframework.core.io.FileSystemResource;

public class WSManConfigDaoJaxbTest {
    @Test
    public void canBuildEndpointForSpecific() throws UnknownHostException {
        WSManConfigDaoJaxb configDao = load("src/test/resources/wsman-config.xml");
        WSManEndpoint endpoint = configDao.getEndpoint(InetAddress.getByName("172.23.1.2"));
        assertEquals("http://172.23.1.2:5985/ws-man", endpoint.getUrl().toString());
        assertTrue(endpoint.isBasicAuth());
        assertFalse(endpoint.isGSSAuth());
        assertFalse(endpoint.isKerberosEncryption());
    }

    @Test
    public void canBuildKerberosEncryptedEndpoint() throws UnknownHostException {
        WSManConfigDaoJaxb configDao = load("src/test/resources/wsman-config-endpoints.xml");
        WSManEndpoint endpoint = configDao.getEndpoint(InetAddress.getByName("127.0.0.1"));
        assertTrue(endpoint.isKerberosEncryption());
        // Kerberos encryption implies GSS authentication
        assertTrue(endpoint.isGSSAuth());
        assertFalse(endpoint.isBasicAuth());
        // The canonical host name is used, as with gss-auth, so the URL must not contain the bare address
        assertEquals("http", endpoint.getUrl().getProtocol());
        assertEquals(5985, endpoint.getUrl().getPort());
        assertEquals(InetAddress.getByName("127.0.0.1").getCanonicalHostName(), endpoint.getUrl().getHost());
    }

    @Test
    public void gssAuthAloneDoesNotEnableKerberosEncryption() throws UnknownHostException {
        WSManConfigDaoJaxb configDao = load("src/test/resources/wsman-config-endpoints.xml");
        WSManEndpoint endpoint = configDao.getEndpoint(InetAddress.getByName("127.0.0.2"));
        assertTrue(endpoint.isGSSAuth());
        assertFalse(endpoint.isKerberosEncryption());
    }

    private static WSManConfigDaoJaxb load(String path) {
        WSManConfigDaoJaxb configDao = new WSManConfigDaoJaxb();
        configDao.setConfigResource(new FileSystemResource(path));
        configDao.afterPropertiesSet();
        return configDao;
    }
}
