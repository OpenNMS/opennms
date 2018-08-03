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

import java.util.Date;

import org.junit.Test;
import org.opennms.minion.status.MinionStatus.State;

public class MinionStatusTest {
    private static final int PERIOD = 2 * 30 * 1000;

    @Test
    public void testServiceUp() {
        assertTrue("'now' up status should be up", MinionServiceStatus.up().isUp(PERIOD));
        assertTrue("recent up status should be up", MinionServiceStatus.up(new Date(System.currentTimeMillis() - 50)).isUp(PERIOD));
        assertFalse("barely old up status should be down", MinionServiceStatus.up(new Date(System.currentTimeMillis() - PERIOD - 10)).isUp(PERIOD));
        assertFalse("old up status should be down", MinionServiceStatus.up(new Date(1)).isUp(PERIOD));
    }

    @Test
    public void testServiceDown() {
        assertFalse("'now' down status should be down", MinionServiceStatus.down().isUp(PERIOD));
        assertFalse("recent down status should be down", MinionServiceStatus.down(new Date(System.currentTimeMillis() - 50)).isUp(PERIOD));
        assertFalse("old down status should be down", MinionServiceStatus.down(new Date(1)).isUp(PERIOD));
    }

    @Test
    public void testAggregate() {
        final long now = System.currentTimeMillis();
        final long diff = now - 100;

        final long[] dateCombinations = new long[] {
                now,
                diff,
                diff - PERIOD,
                1
        };

        for (final State heartbeat : State.values()) {
            for (final State rpc : State.values()) {
                for (final long heartbeatDate : dateCombinations) {
                    for (final long rpcDate : dateCombinations) {
                        final MinionServiceStatus heartbeatStatus = new MinionServiceStatus(new Date(heartbeatDate), heartbeat);
                        final MinionServiceStatus rpcStatus = new MinionServiceStatus(new Date(rpcDate), rpc);

                        final String message = "heartbeat=" + heartbeat + ", heartbeatDate=" + new Date(heartbeatDate) + ", rpc=" + rpc + ", rpcDate=" + new Date(rpcDate);
                        final AggregateMinionStatus aggregateStatus = AggregateMinionStatus.create(heartbeatStatus, rpcStatus);

                        if (heartbeat == State.UP
                                && rpc == State.UP
                                && ( heartbeatDate == now || heartbeatDate == diff )
                                && ( rpcDate == now || rpcDate == diff) ) {
                            assertTrue(message, aggregateStatus.isUp(PERIOD));
                        } else {
                            assertFalse(message, aggregateStatus.isUp(PERIOD));
                        }
                    }
                }
            }
        }
    }
}
