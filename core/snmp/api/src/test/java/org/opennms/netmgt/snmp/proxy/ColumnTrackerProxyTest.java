/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
import org.opennms.netmgt.snmp.ColumnTracker;
import org.opennms.netmgt.snmp.GatheringTracker;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;

public class ColumnTrackerProxyTest {
    private GatheringTracker gatherer = new GatheringTracker();
    private SnmpObjId base = SnmpObjId.get(".1.3.6.1.2.1");
    private ColumnTracker tracker = new ColumnTracker(gatherer, base);

    @Before
    public void setUp() {
        // Verify the generated request
        WalkRequest expectedRequest = new WalkRequest(base);
        expectedRequest.setMaxRepetitions(2);
        assertThat(tracker.getWalkRequests(), contains(expectedRequest));

        // We shouldn't be finished yet
        assertThat(tracker.isFinished(), equalTo(false));
        assertThat(gatherer.getResults(), hasSize(0));
    }

    @Test
    public void canHandleValidResponses() {
        // Build a response with the requested OID
        SnmpValue value = mock(SnmpValue.class);
        List<SnmpResult> results = new ArrayList<>();
        results.add(new SnmpResult(base, SnmpInstId.INST_ZERO, value));
        results.add(new SnmpResult(SnmpObjId.get(".1.3.6.1.2.2"), SnmpInstId.INST_ZERO, null)); // Outside of base
        WalkResponse response = new WalkResponse(results);

        // Resolve the walker
        tracker.handleWalkResponses(Collections.singletonList(response));

        // We should be finished, and have captured the expected value
        assertThat(tracker.isFinished(), equalTo(true));
        assertThat(gatherer.getResults(), hasSize(1));
        assertThat(gatherer.getResults().get(0).getValue(), equalTo(value));
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
