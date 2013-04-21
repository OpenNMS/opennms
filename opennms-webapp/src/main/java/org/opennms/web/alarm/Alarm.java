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

package org.opennms.web.alarm;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsMemo;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;

/**
 * A JavaBean implementation to hold information about a network alarm as
 * defined by OpenNMS.
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class Alarm {
    private final OnmsAlarm m_delegate;

    public Alarm(OnmsAlarm onmsAlarm) {
        m_delegate = onmsAlarm;
    }

    /**
     * <p>Getter for the field <code>id</code>.</p>
     *
     * @return a int.
     */
    public int getId() {
        return m_delegate.getId();
    }

    /**
     * <p>Getter for the field <code>uei</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUei() {
        return m_delegate.getUei();
    }

    /**
     * <p>Getter for the field <code>dpName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDpName() {
        return m_delegate.getDistPoller() == null ? null : m_delegate.getDistPoller().getName();
    }

    /**
     * <p>getLastEventTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getLastEventTime() {
        return m_delegate.getLastEventTime();
    }

    /**
     * <p>getFirstEventTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getFirstEventTime() {
        return m_delegate.getFirstEventTime();
    }

    /**
     * <p>Getter for the field <code>count</code>.</p>
     *
     * @return a int.
     */
    public int getCount() {
        return m_delegate.getCounter();
    }

    /**
     * <p>Getter for the field <code>severity</code>.</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     */
    public OnmsSeverity getSeverity() {
        return m_delegate.getSeverity();
    }
    
    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return m_delegate.getNodeId();
    }

    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddress() {
        return InetAddressUtils.str(m_delegate.getIpAddr());
    }

    /**
     * <p>getServiceId</p>
     *
     * @return a int.
     */
    public int getServiceId() {
        return m_delegate.getServiceType() == null ? null : m_delegate.getServiceType().getId();
    }

    /**
     * <p>Getter for the field <code>reductionKey</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReductionKey() {
        return m_delegate.getReductionKey();
    }

    /**
     * <p>Getter for the field <code>lastEventID</code>.</p>
     *
     * @return a int.
     */
    public int getLastEventID() {
        return m_delegate.getLastEvent() == null ? null : m_delegate.getLastEvent().getId();
    }

    /**
     * <p>Getter for the field <code>description</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return m_delegate.getDescription();
    }

    /**
     * <p>Getter for the field <code>logMessage</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLogMessage() {
        return m_delegate.getLogMsg();
    }

    /**
     * <p>Getter for the field <code>operatorInstruction</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOperatorInstruction() {
        return m_delegate.getOperInstruct();
    }

    /**
     * <p>Getter for the field <code>troubleTicket</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTroubleTicket() {
        return m_delegate.getTTicketId();
    }

    /**
     * <p>Getter for the field <code>troubleTicketState</code>.</p>
     *
     * @return a {@link org.opennms.netmgt.model.TroubleTicketState} object.
     */
    public TroubleTicketState getTroubleTicketState() {
        return m_delegate.getTTicketState();
    }

    /**
     * <p>Getter for the field <code>mouseOverText</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMouseOverText() {
        return m_delegate.getMouseOverText();
    }

    /**
     * <p>Getter for the field <code>suppressedUntil</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getSuppressedUntil() {
        return m_delegate.getSuppressedUntil();
    }

    /**
     * <p>Getter for the field <code>suppressedUser</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSuppressedUser() {
        return m_delegate.getSuppressedUser();
    }

    /**
     * <p>Getter for the field <code>suppressedTime</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getSuppressedTime() {
        return m_delegate.getSuppressedTime();
    }

    /**
     * <p>Getter for the field <code>acknowledgeUser</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAcknowledgeUser() {
        return m_delegate.getAckUser();
    }

    /**
     * <p>Getter for the field <code>acknowledgeTime</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getAcknowledgeTime() {
        return m_delegate.getAckTime();
    }

    /**
     * <p>isAcknowledged</p>
     *
     * @return a boolean.
     */
    public boolean isAcknowledged() {
        return m_delegate.getAckUser() != null;
    }

    /**
     * <p>Getter for the field <code>parms</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getParms() {
        return m_delegate.getEventParms();
    }

    /**
     * <p>Getter for the field <code>nodeLabel</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel() {
        return m_delegate.getNode() == null ? null : m_delegate.getNode().getLabel();
    }

    /**
     * <p>Getter for the field <code>serviceName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return m_delegate.getServiceType() == null ? null : m_delegate.getServiceType().getName();
    }

    public OnmsMemo getReductionKeyMemo() {
        return m_delegate.getReductionKeyMemo();
    }

    public OnmsMemo getStickyMemo() {
        return m_delegate.getStickyMemo();
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("UEI", getUei())
            .append("distPoller", getDpName())
            .append("nodeID", getNodeId())
            .append("nodeLabel", getNodeLabel())
            .append("ipAddress", getIpAddress())
            .append("serviceID", getServiceId())
            .append("reductionKey", getReductionKey())
            .append("count", getCount())
            .append("severity", getSeverity())
            .append("serviceName", getServiceName())
            .append("lastEventID", getLastEventID())
            .append("lastEventTime", getLastEventTime())
            .append("description", getDescription())
            .append("logMessage", getLogMessage())
            .append("operatorInstruction", getOperatorInstruction())
            .append("troubleTicket", getTroubleTicket())
            .append("troubleTicketState", getTroubleTicketState())
            .append("mouseOverText", getMouseOverText())
            .append("suppressedUntil", getSuppressedUntil())
            .append("suppressedUser", getSuppressedUser())
            .append("suppressedTime", getSuppressedTime())
            .append("acknowledgedUser", getAcknowledgeUser())
            .append("acknowledgedTime", getAcknowledgeTime())
            .append("stickyMemo", getStickyMemo())
            .append("reductionKeyMemo", getReductionKeyMemo())
            .toString();
    }
}
