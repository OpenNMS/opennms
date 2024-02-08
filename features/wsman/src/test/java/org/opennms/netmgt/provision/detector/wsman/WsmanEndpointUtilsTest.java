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
package org.opennms.netmgt.provision.detector.wsman;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;

import org.junit.Test;
import org.opennms.core.wsman.WSManEndpoint;

public class WsmanEndpointUtilsTest {

    @Test
    public void canConvertToAndFromMap() throws MalformedURLException {
        WSManEndpoint expectedEndpoint = new WSManEndpoint.Builder("https://www.opennms.org/wsman")
                .withConnectionTimeout(60)
                .withBasicAuth("x", "y")
                .build();

        WSManEndpoint actualEndpoint = WsmanEndpointUtils.fromMap(WsmanEndpointUtils.toMap(expectedEndpoint));

        assertEquals(expectedEndpoint.getUrl(), actualEndpoint.getUrl());
        assertEquals(expectedEndpoint.isBasicAuth(), actualEndpoint.isBasicAuth());
        assertEquals(expectedEndpoint.getUsername(), actualEndpoint.getUsername());
        assertEquals(expectedEndpoint.getPassword(), actualEndpoint.getPassword());
        assertEquals(expectedEndpoint.getConnectionTimeout(), actualEndpoint.getConnectionTimeout());
    }
}
