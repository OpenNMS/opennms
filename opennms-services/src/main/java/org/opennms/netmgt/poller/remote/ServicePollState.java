/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.remote;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.opennms.netmgt.model.PollStatus;

/**
 * <p>ServicePollState class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class ServicePollState implements Comparable<ServicePollState>, Serializable {

    private static final long serialVersionUID = -8169533436306268574L;

    private PolledService m_polledService;
    private int m_index;
    private PollStatus m_lastPoll;
    private Date m_initialPollTime;

    /**
     * <p>Constructor for ServicePollState.</p>
     *
     * @param polledService a {@link org.opennms.netmgt.poller.remote.PolledService} object.
     * @param index a int.
     */
    public ServicePollState(final PolledService polledService, final int index) {
        m_polledService = polledService;
        m_index = index;
    }

    /**
     * <p>getLastPoll</p>
     *
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public PollStatus getLastPoll() {
        return m_lastPoll;
    }

    /**
     * <p>setLastPoll</p>
     *
     * @param lastPoll a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public void setLastPoll(final PollStatus lastPoll) {
        m_lastPoll = lastPoll;
    }
    
    /**
     * <p>getLastPollTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getLastPollTime() {
        return (m_lastPoll == null ? null : m_lastPoll.getTimestamp());
    }
    
    /**
     * <p>getNextPollTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getNextPollTime() {
        if (m_lastPoll == null) {
            return m_initialPollTime;
        }
        else {
            return m_polledService.getPollModel().getNextPollTime(getLastPollTime());
        }
    }

    /**
     * <p>getIndex</p>
     *
     * @return a int.
     */
    public int getIndex() {
        return m_index;
    }

    /**
     * <p>getPolledService</p>
     *
     * @return a {@link org.opennms.netmgt.poller.remote.PolledService} object.
     */
    public PolledService getPolledService() {
        return m_polledService;
    }

    /**
     * <p>setInitialPollTime</p>
     *
     * @param initialPollTime a {@link java.util.Date} object.
     */
    public void setInitialPollTime(final Date initialPollTime) {
        m_initialPollTime = initialPollTime;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 37)
            .append(this.getIndex())
            .append(this.getLastPoll())
            .append(this.getPolledService())
            .toHashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object o) {
        if (o == null) return false;
        if (!(o instanceof ServicePollState)) return false;
        final ServicePollState that = (ServicePollState)o;
        return new EqualsBuilder()
            .append(this.getIndex(), that.getIndex())
            .append(this.getPolledService(), that.getPolledService())
            .isEquals();
    }
    
    /**
     * <p>compareTo</p>
     *
     * @param that a {@link org.opennms.netmgt.poller.remote.ServicePollState} object.
     * @return a int.
     */
    @Override
    public int compareTo(final ServicePollState that) {
        if (that == null) return -1;
        final PolledService thisService = this.getPolledService();
        final PolledService thatService = that.getPolledService();
        return new CompareToBuilder()
            .append(thisService.getNodeLabel(), thatService.getNodeLabel())
            .append(thisService.getIpAddr(), thatService.getIpAddr())
            .append(this.getLastPoll().getStatusName(), that.getLastPoll().getStatusName())
            .append(thisService.getServiceId(), thatService.getServiceId())
            .append(thisService.getNodeId(), thatService.getNodeId())
            .toComparison();
    }

}
