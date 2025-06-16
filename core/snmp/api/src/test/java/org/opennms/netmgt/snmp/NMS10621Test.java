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
package org.opennms.netmgt.snmp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class NMS10621Test {

    /**
     * Used to validate {@link SnmpObjId#isSuccessorOf(SnmpObjId)}.
     *
     * See NMS-10621 for details.
     */
    @Test
    public void canVerifySuccessor() {
        SnmpObjId ifIndex = SnmpObjId.get("1.3.6.1.2.1.2.2.1.1");
        SnmpObjId ifIndex2 = SnmpObjId.get("1.3.6.1.2.1.2.2.1.1.2");
        SnmpObjId ifIndex3 = SnmpObjId.get("1.3.6.1.2.1.2.2.1.1.3");

        assertThat(ifIndex.isSuccessorOf(ifIndex), equalTo(false));
        assertThat(ifIndex2.isSuccessorOf(ifIndex), equalTo(true));
        assertThat(ifIndex3.isSuccessorOf(ifIndex), equalTo(true));

        assertThat(ifIndex2.isSuccessorOf(ifIndex2), equalTo(false));
        assertThat(ifIndex2.isSuccessorOf(ifIndex3), equalTo(false));

        assertThat(ifIndex3.isSuccessorOf(ifIndex3), equalTo(false));
        assertThat(ifIndex3.isSuccessorOf(ifIndex2), equalTo(true));

        SnmpObjId mib2 = SnmpObjId.get("1.3.6.1.2.1");
        assertThat(ifIndex.isSuccessorOf(mib2), equalTo(true));
        assertThat(mib2.isSuccessorOf(ifIndex), equalTo(false));
    }
}
