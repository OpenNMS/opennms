/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.minion.status;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.minion.status.MinionStatus.State;

public class MinionStatusTest {
    @Test
    public void testServiceUp() {
        assertTrue("'now' up status should be up", MinionServiceStatus.up().isUp());
    }

    @Test
    public void testServiceDown() {
        assertFalse("'now' down status should be down", MinionServiceStatus.down().isUp());
    }

    @Test
    public void testAggregate() {
        for (final State heartbeat : State.values()) {
            for (final State rpc : State.values()) {
                final MinionServiceStatus heartbeatStatus = new MinionServiceStatus(heartbeat);
                final MinionServiceStatus rpcStatus = new MinionServiceStatus(rpc);

                final String message = "heartbeat=" + heartbeat + ", rpc=" + rpc;
                final AggregateMinionStatus aggregateStatus = AggregateMinionStatus.create(heartbeatStatus, rpcStatus);

                if (heartbeat == State.UP
                        && rpc == State.UP) {
                    assertTrue(message, aggregateStatus.isUp());
                } else {
                    assertFalse(message, aggregateStatus.isUp());
                }
            }
        }
    }
}
