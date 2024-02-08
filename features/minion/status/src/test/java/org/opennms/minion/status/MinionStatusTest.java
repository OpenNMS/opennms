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
