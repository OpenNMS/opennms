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
