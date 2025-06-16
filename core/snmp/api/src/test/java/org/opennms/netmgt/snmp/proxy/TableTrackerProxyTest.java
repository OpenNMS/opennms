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
package org.opennms.netmgt.snmp.proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TableTracker;

public class TableTrackerProxyTest {
    private List<SnmpRowResult> rows = new ArrayList<>();
    private SnmpObjId table = SnmpObjId.get(".1.3.6.1.2.1");
    private TableTracker tracker = new TableTracker(table) {
        @Override
        public void rowCompleted(SnmpRowResult row) {
            rows.add(row);
        }
    };

    @Before
    public void setUp() {
        // Verify the generated request
        WalkRequest expectedRequest = new WalkRequest(table);
        expectedRequest.setMaxRepetitions(2);
        assertThat(tracker.getWalkRequests(), contains(expectedRequest));

        // We shouldn't be finished yet
        assertThat(tracker.isFinished(), equalTo(false));
        assertThat(rows, hasSize(0));
    }

    @Test
    public void canHandleAValidResponse() {
        // Build a response with an OID from the requested table
        SnmpValue value = mock(SnmpValue.class);
        SnmpResult result = new SnmpResult(table, SnmpInstId.INST_ZERO, value);
        WalkResponse response = new WalkResponse(Collections.singletonList(result));

        // Resolve the walker
        tracker.handleWalkResponses(Collections.singletonList(response));

        // We should be finished, and have captured the expected value
        assertThat(tracker.isFinished(), equalTo(true));
        assertThat(rows, hasSize(1));
        assertThat(rows.get(0).getValue(table), equalTo(value));
    }

    @Test
    public void canHandleAnEmptyResponse() {
        WalkResponse response = new WalkResponse(Collections.emptyList());

        // Resolve the walker
        tracker.handleWalkResponses(Collections.singletonList(response));

        // We should be finished, without any captured values
        assertThat(tracker.isFinished(), equalTo(true));
        assertThat(rows, hasSize(0));
    }
}
