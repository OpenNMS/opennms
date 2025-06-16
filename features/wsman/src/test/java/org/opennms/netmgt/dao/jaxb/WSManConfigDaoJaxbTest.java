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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;
import org.opennms.core.wsman.WSManEndpoint;
import org.springframework.core.io.FileSystemResource;

public class WSManConfigDaoJaxbTest {
    @Test
    public void canBuildEndpointForSpecific() throws UnknownHostException {
        WSManConfigDaoJaxb configDao = new WSManConfigDaoJaxb();
        configDao.setConfigResource(new FileSystemResource("src/test/resources/wsman-config.xml"));
        configDao.afterPropertiesSet();
        WSManEndpoint endpoint = configDao.getEndpoint(InetAddress.getByName("172.23.1.2"));
        assertEquals("http://172.23.1.2:5985/ws-man", endpoint.getUrl().toString());
    }
}
