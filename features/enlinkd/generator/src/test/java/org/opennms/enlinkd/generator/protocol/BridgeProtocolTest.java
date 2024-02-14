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
package org.opennms.enlinkd.generator.protocol;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opennms.enlinkd.generator.TopologySettings;


public class BridgeProtocolTest {

    @Test
    public void testAdoptAndVerifySettings() {
        testAmountNodes(11, -1);
        testAmountNodes(11, 0);
        testAmountNodes(11, 10);
        testAmountNodes(11, 11);
        testAmountNodes(21, 12);
        testAmountNodes(21, 20);
        testAmountNodes(21, 21);
        testAmountNodes(31, 22);
    }

    private void testAmountNodes(int expected, int initialSetting) {
        BridgeProtocol protocol = new BridgeProtocol(TopologySettings.builder().build(), null);
        TopologySettings settings = protocol.adoptAndVerifySettings(TopologySettings.builder().amountNodes(initialSetting).build());
        assertEquals(expected, settings.getAmountNodes());
    }
}
