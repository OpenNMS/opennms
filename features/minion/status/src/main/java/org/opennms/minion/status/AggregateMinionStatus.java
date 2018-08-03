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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
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
     * Create a new aggregate down status the given date as last seen.
     * @return a down aggregate status
     */
    public static AggregateMinionStatus down(final Date lastSeen) {
        return new AggregateMinionStatus(MinionServiceStatus.down(lastSeen), MinionServiceStatus.down(lastSeen));
    }

    /**
     * Create a new aggregate status assumed to be up.
     * @return an up aggregate status
     */
    public static AggregateMinionStatus up() {
        return new AggregateMinionStatus(MinionServiceStatus.up(), MinionServiceStatus.up());
    }

    /**
     * Create a new aggregate up status the given date as last seen.
     * @return a down aggregate status
     */
    public static AggregateMinionStatus up(final Date lastSeen) {
        return new AggregateMinionStatus(MinionServiceStatus.up(lastSeen), MinionServiceStatus.up(lastSeen));
    }

    public MinionServiceStatus getHeartbeatStatus() {
        return m_heartbeatStatus;
    }

    public MinionServiceStatus getRpcStatus() {
        return m_rpcStatus;
    }

    @Override
    public Date lastSeen() {
        final Date heartbeatSeen = m_heartbeatStatus.lastSeen();
        final Date rpcSeen = m_rpcStatus.lastSeen();
        return heartbeatSeen.compareTo(rpcSeen) < 1? heartbeatSeen : rpcSeen;
    }

    @Override
    public State getState() {
        return m_heartbeatStatus.getState() == UP && m_rpcStatus.getState() == UP? UP : DOWN;
    }

    @Override
    public boolean isUp(final long timeoutPeriod) {
        return m_heartbeatStatus.isUp(timeoutPeriod) && m_rpcStatus.isUp(timeoutPeriod);
    }

    public AggregateMinionStatus heartbeatDown(final Date lastSeen) {
        return new AggregateMinionStatus(MinionServiceStatus.down(lastSeen), m_rpcStatus);
    }

    public AggregateMinionStatus heartbeatUp(final Date lastSeen) {
        return new AggregateMinionStatus(MinionServiceStatus.up(lastSeen), m_rpcStatus);
    }

    public AggregateMinionStatus rpcDown(final Date lastSeen) {
        return new AggregateMinionStatus(m_heartbeatStatus, MinionServiceStatus.down(lastSeen));
    }

    public AggregateMinionStatus rpcUp(final Date lastSeen) {
        return new AggregateMinionStatus(m_heartbeatStatus, MinionServiceStatus.up(lastSeen));
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
