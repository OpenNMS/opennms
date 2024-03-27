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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.snmp.GatheringTracker;
import org.opennms.netmgt.snmp.SingleInstanceTracker;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;

public class SingleInstanceTrackerProxyTest {
    private GatheringTracker gatherer = new GatheringTracker();
    private SnmpObjId base = SnmpObjId.get(".1.3.6.1.2.1.1");
    private SnmpInstId instance = new SnmpInstId(2);
    private SingleInstanceTracker tracker = new SingleInstanceTracker(base, instance, gatherer);

    @Before
    public void setUp() {
        // Verify the generated request
        WalkRequest expectedRequest = new WalkRequest(base);
        expectedRequest.setInstance(instance);
        expectedRequest.setMaxRepetitions(1);
        assertThat(tracker.getWalkRequests(), contains(expectedRequest));

        // We shouldn't be finished yet
        assertThat(tracker.isFinished(), equalTo(false));
        assertThat(gatherer.getResults(), hasSize(0));
    }

    @Test
    public void canHandleValidResponses() {
        // Build a response with the requested OID
        SnmpValue value = mock(SnmpValue.class);
        SnmpResult result = new SnmpResult(base, instance, value);
        WalkResponse response = new WalkResponse(Collections.singletonList(result));

        // Resolve the walker
        tracker.handleWalkResponses(Collections.singletonList(response));

        // We should be finished, and have captured the expected value
        assertThat(tracker.isFinished(), equalTo(true));
        assertThat(gatherer.getResults(), hasSize(1));
        assertThat(gatherer.getResults().get(0).getValue(), equalTo(value));
    }

    @Test
    public void canHandleResponsesForOtherOids() {
        // Build a response for another OID
        SnmpValue value = mock(SnmpValue.class);
        SnmpResult result = new SnmpResult(SnmpObjId.get(".1.3.6.1.2.1.1.1"), instance, value);
        WalkResponse response = new WalkResponse(Collections.singletonList(result));

        // Resolve the walker
        tracker.handleWalkResponses(Collections.singletonList(response));

        // We should be finished, without any captured values
        assertThat(tracker.isFinished(), equalTo(true));
        assertThat(gatherer.getResults(), hasSize(0));
    }

    @Test
    public void canHandleEmptyResponses() {
        // Resolve the walker
        tracker.handleWalkResponses(Collections.emptyList());

        // We should be finished, without any captured values
        assertThat(tracker.isFinished(), equalTo(true));
        assertThat(gatherer.getResults(), hasSize(0));
    }
}
