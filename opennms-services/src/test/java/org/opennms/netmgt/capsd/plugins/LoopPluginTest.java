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
package org.opennms.netmgt.capsd.plugins;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.monitors.support.LoopPlugin;
public class LoopPluginTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'org.opennms.netmgt.capsd.plugins.LoopPlugin.getProtocolName()'
     */
    public void testGetProtocolName() {
        LoopPlugin plugin = new LoopPlugin();
        assertEquals("LOOP", plugin.getProtocolName());
    }

    /*
     * Test method for 'org.opennms.netmgt.capsd.plugins.LoopPlugin.isProtocolSupported(InetAddress)'
     */
    public void testIsProtocolSupportedInetAddress() throws UnknownHostException {
        LoopPlugin plugin = new LoopPlugin();
        assertFalse(plugin.isProtocolSupported(InetAddressUtils.addr("127.0.0.1")));
    }

    /*
     * Test method for 'org.opennms.netmgt.capsd.plugins.LoopPlugin.isProtocolSupported(InetAddress, Map)'
     */
    public void testIsProtocolSupportedInetAddressMap() throws UnknownHostException {
        Map<String, Object> qualifiers = new HashMap<String, Object>();
        qualifiers.put("ip-match", "127.*.*.1-2");
        qualifiers.put("is-supported", "true");
        LoopPlugin plugin = new LoopPlugin();
        assertTrue(plugin.isProtocolSupported(InetAddressUtils.addr("127.0.0.1"), qualifiers));
        assertFalse(plugin.isProtocolSupported(InetAddressUtils.addr("127.0.0.3"), qualifiers));

    }

}
