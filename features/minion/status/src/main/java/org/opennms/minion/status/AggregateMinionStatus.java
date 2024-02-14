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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

public class AggregateMinionStatus implements MinionStatus, Comparable<AggregateMinionStatus>, Serializable {
    private static final Comparator<AggregateMinionStatus> COMPARATOR = Comparator.comparing(AggregateMinionStatus::getHeartbeatStatus).thenComparing(AggregateMinionStatus::getRpcStatus);

    private static final long serialVersionUID = 1L;

    private MinionServiceStatus m_heartbeatStatus;
    private MinionServiceStatus m_rpcStatus;

    protected AggregateMinionStatus(final MinionServiceStatus heartbeat, final MinionServiceStatus rpc) {
        m_heartbeatStatus = heartbeat;
        m_rpcStatus = rpc;
    }

    /**
     * Create a new aggregate status, given existing heartbeat and RPC statuses.
     * @param heartbeat the heartbeat status
     * @param rpc the RPC status
     * @return an aggregate status
     */
    public static AggregateMinionStatus create(final MinionServiceStatus heartbeat, final MinionServiceStatus rpc) {
        return new AggregateMinionStatus(heartbeat, rpc);
    }

    /**
     * Create a new aggregate status without known state, assumed to be down.
     * @return a down aggregate status
     */
    public static AggregateMinionStatus down() {
        return new AggregateMinionStatus(MinionServiceStatus.down(), MinionServiceStatus.down());
    }

    /**
     * Create a new aggregate status assumed to be up.
     * @return an up aggregate status
     */
    public static AggregateMinionStatus up() {
        return new AggregateMinionStatus(MinionServiceStatus.up(), MinionServiceStatus.up());
    }

    public MinionServiceStatus getHeartbeatStatus() {
        return m_heartbeatStatus;
    }

    public MinionServiceStatus getRpcStatus() {
        return m_rpcStatus;
    }

    @Override
    public State getState() {
        return m_heartbeatStatus.getState() == UP && m_rpcStatus.getState() == UP? UP : DOWN;
    }

    @Override
    public boolean isUp() {
        return m_heartbeatStatus.isUp() && m_rpcStatus.isUp();
    }

    public AggregateMinionStatus heartbeatDown() {
        return new AggregateMinionStatus(MinionServiceStatus.down(), m_rpcStatus);
    }

    public AggregateMinionStatus heartbeatUp() {
        return new AggregateMinionStatus(MinionServiceStatus.up(), m_rpcStatus);
    }

    public AggregateMinionStatus rpcDown() {
        return new AggregateMinionStatus(m_heartbeatStatus, MinionServiceStatus.down());
    }

    public AggregateMinionStatus rpcUp() {
        return new AggregateMinionStatus(m_heartbeatStatus, MinionServiceStatus.up());
    }

    @Override
    public int compareTo(final AggregateMinionStatus o) {
        return COMPARATOR.compare(this, o);
    }

    @Override
    public boolean equals(final Object o) {
        if (o != null && o instanceof AggregateMinionStatus) {
            final AggregateMinionStatus status = (AggregateMinionStatus)o;
            return Objects.equals(m_heartbeatStatus, status.m_heartbeatStatus)
                    && Objects.equals(m_rpcStatus, status.m_rpcStatus);
        }
        return false;
    }

    @Override
    public String toString() {
        return "AggregateMinionStatus[Heartbeat=" + m_heartbeatStatus + ", RPC=" + m_rpcStatus + "]";
    }
    
}
