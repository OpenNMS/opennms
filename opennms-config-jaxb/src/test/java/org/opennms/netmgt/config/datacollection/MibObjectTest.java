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
package org.opennms.netmgt.config.datacollection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MibObjectTest {

    @Test
    public void testIsInstanceId() {
        for (final String instance : new String[] {"0", "101", "1.5", "1.3.6", " 7 "}) {
            assertTrue(instance, MibObject.isInstanceId(instance));
        }
        for (final String instance : new String[] {null, "", "ifIndex", "hrStorageIndex", "-1", "1.", ".1", "1..5", "1,2"}) {
            assertFalse(String.valueOf(instance), MibObject.isInstanceId(instance));
        }
    }
}
