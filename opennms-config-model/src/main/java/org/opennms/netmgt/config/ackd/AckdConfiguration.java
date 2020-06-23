/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.ackd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

/**
 * Behavior configuration for the Acknowledgment Daemon
 */
@XmlRootElement(name = "ackd-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("ackd-configuration.xsd")
public class AckdConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final boolean DEFAULT_ALARM_SYNC_FLAG = true;

    public static final String DEFAULT_ACK_EXPRESSION = "~(?i)^ack$";
    public static final String DEFAULT_UNACK_EXPRESSION = "~(?i)^unack$";
    public static final String DEFAULT_ESCALATE_EXPRESSION = "~(?i)^esc$";
    public static final String DEFAULT_CLEAR_EXPRESSION = "~(?i)^(resolve|clear)$";
    public static final String DEFAULT_NOTIFYID_MATCH_EXPRESSION = "~(?i).*Re:.*Notice #([0-9]+).*";
    public static final String DEFAULT_ALARMID_MATCH_EXPRESSION = "~(?i).*alarmid:([0-9]+).*";

    @XmlAttribute(name = "alarm-sync")
    private Boolean m_alarmSync;

    @XmlAttribute(name = "ack-expression")
    private String m_ackExpression;

    @XmlAttribute(name = "unack-expression")
    private String m_unackExpression;

    @XmlAttribute(name = "escalate-expression")
    private String m_escalateExpression;

    @XmlAttribute(name = "clear-expression")
    private String m_clearExpression;

    @XmlAttribute(name = "notifyid-match-expression")
    private String m_notifyidMatchExpression;

    @XmlAttribute(name = "alarmid-match-expression")
    private String m_alarmidMatchExpression;

    /**
     * Location for user to define readers and they're schedules.
     */
    @XmlElementWrapper(name = "readers")
    @XmlElement(name = "reader")
    private List<Reader> m_readers;

    public AckdConfiguration() {
    }

    public AckdConfiguration(final boolean alarmSync,
            final String ackExpression, final String unackExpression,
            final String clearExpression, final String escalateExpression,
            final String notifyidMatchExpression,
            final String alarmidMatchExpression, final List<Reader> readers) {
        setAlarmSync(alarmSync);
        setAckExpression(ackExpression);
        setUnackExpression(unackExpression);
        setClearExpression(clearExpression);
        setEscalateExpression(escalateExpression);
        setNotifyidMatchExpression(notifyidMatchExpression);
        setAlarmidMatchExpression(alarmidMatchExpression);
        setReaders(readers);
    }

    public boolean getAlarmSync() {
        return m_alarmSync == null ? DEFAULT_ALARM_SYNC_FLAG : m_alarmSync;
    }

    public void setAlarmSync(final Boolean alarmSync) {
        m_alarmSync = alarmSync;
    }

    public String getAckExpression() {
        return m_ackExpression == null ? DEFAULT_ACK_EXPRESSION : m_ackExpression;
    }

    public void setAckExpression(final String ackExpression) {
        m_ackExpression = ackExpression;
    }

    public String getUnackExpression() {
        return m_unackExpression == null ? DEFAULT_UNACK_EXPRESSION : m_unackExpression;
    }

    public void setUnackExpression(final String unackExpression) {
        m_unackExpression = unackExpression;
    }

    public String getEscalateExpression() {
        return m_escalateExpression == null ? DEFAULT_ESCALATE_EXPRESSION : m_escalateExpression;
    }

    public void setEscalateExpression(final String escalateExpression) {
        m_escalateExpression = escalateExpression;
    }

    public String getClearExpression() {
        return m_clearExpression == null ? DEFAULT_CLEAR_EXPRESSION : m_clearExpression;
    }

    public void setClearExpression(final String clearExpression) {
        m_clearExpression = clearExpression;
    }

    public String getNotifyidMatchExpression() {
        return m_notifyidMatchExpression == null ? DEFAULT_NOTIFYID_MATCH_EXPRESSION : m_notifyidMatchExpression;
    }

    public void setNotifyidMatchExpression(final String notifyidMatchExpression) {
        m_notifyidMatchExpression = notifyidMatchExpression;
    }

    public String getAlarmidMatchExpression() {
        return m_alarmidMatchExpression == null ? DEFAULT_ALARMID_MATCH_EXPRESSION : m_alarmidMatchExpression;
    }

    public void setAlarmidMatchExpression(final String alarmidMatchExpression) {
        m_alarmidMatchExpression = alarmidMatchExpression;
    }

    public List<Reader> getReaders() {
        return m_readers == null? Collections.emptyList() : m_readers;
    }

    public void setReaders(final List<Reader> readers) {
        if (readers == m_readers) {
            return;
        }
        if (m_readers != null) m_readers.clear();
        if (readers != null) {
            if (m_readers == null) m_readers = new ArrayList<>();
            m_readers.addAll(readers);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_alarmSync, m_ackExpression, m_unackExpression, m_escalateExpression, m_clearExpression, m_notifyidMatchExpression, m_alarmidMatchExpression, m_readers);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AckdConfiguration) {
            final AckdConfiguration that = (AckdConfiguration) obj;
            return Objects.equals(this.m_alarmSync, that.m_alarmSync) &&
                    Objects.equals(this.m_ackExpression, that.m_ackExpression) &&
                    Objects.equals(this.m_unackExpression, that.m_unackExpression) &&
                    Objects.equals(this.m_escalateExpression, that.m_escalateExpression) &&
                    Objects.equals(this.m_clearExpression, that.m_clearExpression) &&
                    Objects.equals(this.m_notifyidMatchExpression, that.m_notifyidMatchExpression) &&
                    Objects.equals(this.m_alarmidMatchExpression, that.m_alarmidMatchExpression) &&
                    Objects.equals(this.m_readers, that.m_readers);
        }
        return false;
    }
}
