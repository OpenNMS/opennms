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
package org.opennms.netmgt.passive;

import org.opennms.netmgt.passive.PassiveStatusKey;

import junit.framework.TestCase;

public class PassiveStatusTest extends TestCase {

    /*
     * Test method for 'org.opennms.netmgt.config.PassiveStatus.equals(Object)'
     */
    public void testEqualsObject() {
        PassiveStatusKey ps = new PassiveStatusKey("node1", "1.1.1.1", "ICMP");
        PassiveStatusKey ps2 = new PassiveStatusKey("node1", "2.1.1.1", "HTTP");
        PassiveStatusKey ps3 = new PassiveStatusKey("node1", "1.1.1.1", "ICMP");
        
        assertEquals(ps, ps3);
        assertFalse(ps.equals(ps2));
        assertFalse(ps2.equals(ps3));


    }

}
