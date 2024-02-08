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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.hamcrest.Matchers.contains;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.ColumnTracker;
import org.opennms.netmgt.snmp.GatheringTracker;
import org.opennms.netmgt.snmp.SingleInstanceTracker;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;

import com.google.common.collect.Lists;

public class AggregateTrackerProxyTest {
    private GatheringTracker gatherer = new GatheringTracker();
    private SnmpObjId[] baseOids = new SnmpObjId[] {
            SnmpObjId.get(".1.3.6.1.2.1"),
            SnmpObjId.get(".1.3.6.1.2.2"),
            SnmpObjId.get(".1.3.6.1.2.3"),
            SnmpObjId.get(".1.3.6.1.2.4")
    };
    private ColumnTracker[] columnTrackers;
    private SingleInstanceTracker singleInstanceTracker;
    private AggregateTracker childAggregateTracker;
    private AggregateTracker parentAggregateTracker;

    @Before
    public void setUp() {
        // Create a hierarchy of aggregated trackers
        columnTrackers = new ColumnTracker[] {
                new ColumnTracker(baseOids[0]),
                new ColumnTracker(baseOids[1]),
                new ColumnTracker(baseOids[2]),
        };
        singleInstanceTracker = new SingleInstanceTracker(baseOids[3], SnmpInstId.INST_ZERO);
        childAggregateTracker = new AggregateTracker(columnTrackers);
        parentAggregateTracker = new AggregateTracker(Lists.newArrayList(childAggregateTracker, singleInstanceTracker), gatherer);

        // Verify the generated requests
        List<WalkRequest> expectedRequests = new ArrayList<>();

        // Column tracker requests
        WalkRequest expectedRequest = new WalkRequest(baseOids[0]);
        expectedRequest.setCorrelationId("0-0");
        expectedRequest.setMaxRepetitions(2);
        expectedRequests.add(expectedRequest);

        expectedRequest = new WalkRequest(baseOids[1]);
        expectedRequest.setCorrelationId("0-1");
        expectedRequest.setMaxRepetitions(2);
        expectedRequests.add(expectedRequest);

        expectedRequest = new WalkRequest(baseOids[2]);
        expectedRequest.setCorrelationId("0-2");
        expectedRequest.setMaxRepetitions(2);
        expectedRequests.add(expectedRequest);

        // Single instance tracker request
        expectedRequest = new WalkRequest(baseOids[3]);
        expectedRequest.setCorrelationId("1");
        expectedRequest.setInstance(SnmpInstId.INST_ZERO);
        expectedRequest.setMaxRepetitions(1);
        expectedRequests.add(expectedRequest);

        assertThat(parentAggregateTracker.getWalkRequests(), hasSize(4));
        assertThat(parentAggregateTracker.getWalkRequests(), contains(expectedRequests.toArray()));

        // We shouldn't be finished yet
        assertThat(parentAggregateTracker.isFinished(), equalTo(false));
        assertThat(gatherer.getResults(), hasSize(0));
    }

    @Test
    public void canHandleValidResponses() {
        // Build responses
        SnmpValue value = mock(SnmpValue.class);
        List<WalkResponse> responses = Lists.newArrayList(
                new WalkResponse(Collections.singletonList(
                        new SnmpResult(baseOids[0], SnmpInstId.INST_ZERO, value)),
                        "0-0"),
                new WalkResponse(Collections.singletonList(
                        new SnmpResult(baseOids[1], SnmpInstId.INST_ZERO, value)),
                        "0-1"),
                new WalkResponse(Collections.singletonList(
                        new SnmpResult(baseOids[2], SnmpInstId.INST_ZERO, value)),
                        "0-2"),
                new WalkResponse(Collections.singletonList(
                        new SnmpResult(baseOids[3], SnmpInstId.INST_ZERO, value)),
                        "1")
                );

        // Resolve the walker
        parentAggregateTracker.handleWalkResponses(responses);

        // We should be finished, and have captured the expected results
        assertThat(parentAggregateTracker.isFinished(), equalTo(true));
        assertThat(gatherer.getResults(), hasSize(4));
    }

    @Test
    public void canHandleEmptyResponses() {
        // Resolve the walker
        parentAggregateTracker.handleWalkResponses(Collections.emptyList());

        // We should be finished, without any captured values
        assertThat(parentAggregateTracker.isFinished(), equalTo(true));
        assertThat(gatherer.getResults(), hasSize(0));
    }
}
