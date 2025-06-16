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
package org.opennms.netmgt.jmx.connection;

import java.net.MalformedURLException;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;

public class JmxConnectionConfigTest {

    @Test
    public void verifyIsLocalConnection() throws MalformedURLException {
        JmxConnectionConfig config = new JmxConnectionConfigBuilder()
                .withUrl("service:jmx:rmi://localhost:18980")
                .withUsername("admin")
                .withPassword("admin")
                .build();
        Assert.assertEquals(Boolean.TRUE, config.isLocalConnection());

        // Try with substitution
        config.setUrl("service:jmx:rmi://${ipaddr}:18980");
        config.setIpAddress(InetAddressUtils.getInetAddress("localhost"));
        Assert.assertEquals(Boolean.TRUE, config.isLocalConnection());
    }
}