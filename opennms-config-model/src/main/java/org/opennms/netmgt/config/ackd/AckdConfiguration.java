/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

/**
 * This class was original generated with Castor, but is no longer.
 */
package org.opennms.netmgt.config.ackd;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Behavior configuration for the Acknowledgment Daemon
 * 
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "ackd-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class AckdConfiguration implements Serializable {
    private static final long serialVersionUID = -8269227735655606782L;

    public static final boolean DEFAULT_ALARM_SYNC_FLAG = true;

    public static final String DEFAULT_ACK_EXPRESSION = "~(?i)^ack$";

    public static final String DEFAULT_UNACK_EXPRESSION = "~(?i)^unack$";

    public static final String DEFAULT_ESCALATE_EXPRESSION = "~(?i)^esc$";

    public static final String DEFAULT_CLEAR_EXPRESSION = "~(?i)^(resolve|clear)$";

    public static final String DEFAULT_NOTIFYID_MATCH_EXPRESSION = "~(?i).*Re:.*Notice #([0-9]+).*";

    public static final String DEFAULT_ALARMID_MATCH_EXPRESSION = "~(?i).*alarmid:([0-9]+).*";

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * Field _alarmSync.
     */
    @XmlAttribute(name = "alarm-sync")
    private Boolean _alarmSync;

    /**
     * Field _ackExpression.
     */
    @XmlAttribute(name = "ack-expression")
    private String _ackExpression;

    /**
     * Field _unackExpression.
     */
    @XmlAttribute(name = "unack-expression")
    private String _unackExpression;

    /**
     * Field _escalateExpression.
     */
    @XmlAttribute(name = "escalate-expression")
    private String _escalateExpression;

    /**
     * Field _clearExpression.
     */
    @XmlAttribute(name = "clear-expression")
    private String _clearExpression;

    /**
     * Field _notifyidMatchExpression.
     */
    @XmlAttribute(name = "notifyid-match-expression")
    private String _notifyidMatchExpression;

    /**
     * Field _alarmidMatchExpression.
     */
    @XmlAttribute(name = "alarmid-match-expression")
    private String _alarmidMatchExpression;

    /**
     * Location for user to define readers and they're schedules.
     * 
     */
    @XmlElement(name = "readers")
    private Readers _readers;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public AckdConfiguration() {
        super();
    }

    public AckdConfiguration(final boolean alarmSync,
            final String ackExpression, final String unackExpression,
            final String clearExpression, final String escalateExpression,
            final String notifyidMatchExpression,
            final String alarmidMatchExpression, final Readers readers) {
        super();
        setAlarmSync(alarmSync);
        setAckExpression(ackExpression);
        setUnackExpression(unackExpression);
        setClearExpression(clearExpression);
        setEscalateExpression(escalateExpression);
        setNotifyidMatchExpression(notifyidMatchExpression);
        setAlarmidMatchExpression(alarmidMatchExpression);
        setReaders(readers);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AckdConfiguration other = (AckdConfiguration) obj;
        if (_ackExpression == null) {
            if (other._ackExpression != null)
                return false;
        } else if (!_ackExpression.equals(other._ackExpression))
            return false;
        if (_alarmSync == null) {
            if (other._alarmSync != null)
                return false;
        } else if (!_alarmSync.equals(other._alarmSync))
            return false;
        if (_alarmidMatchExpression == null) {
            if (other._alarmidMatchExpression != null)
                return false;
        } else if (!_alarmidMatchExpression.equals(other._alarmidMatchExpression))
            return false;
        if (_clearExpression == null) {
            if (other._clearExpression != null)
                return false;
        } else if (!_clearExpression.equals(other._clearExpression))
            return false;
        if (_escalateExpression == null) {
            if (other._escalateExpression != null)
                return false;
        } else if (!_escalateExpression.equals(other._escalateExpression))
            return false;
        if (_notifyidMatchExpression == null) {
            if (other._notifyidMatchExpression != null)
                return false;
        } else if (!_notifyidMatchExpression.equals(other._notifyidMatchExpression))
            return false;
        if (_readers == null) {
            if (other._readers != null)
                return false;
        } else if (!_readers.equals(other._readers))
            return false;
        if (_unackExpression == null) {
            if (other._unackExpression != null)
                return false;
        } else if (!_unackExpression.equals(other._unackExpression))
            return false;
        return true;
    }

    /**
     * Returns the value of field 'ackExpression'.
     * 
     * @return the value of field 'AckExpression'.
     */
    public String getAckExpression() {
        return _ackExpression == null ? DEFAULT_ACK_EXPRESSION
                                     : _ackExpression;
    }

    /**
     * Returns the value of field 'alarmSync'.
     * 
     * @return the value of field 'AlarmSync'.
     */
    public boolean getAlarmSync() {
        return _alarmSync == null ? DEFAULT_ALARM_SYNC_FLAG : _alarmSync;
    }

    /**
     * Returns the value of field 'alarmidMatchExpression'.
     * 
     * @return the value of field 'AlarmidMatchExpression'.
     */
    public String getAlarmidMatchExpression() {
        return _alarmidMatchExpression == null ? DEFAULT_ALARMID_MATCH_EXPRESSION
                                              : _alarmidMatchExpression;
    }

    /**
     * Returns the value of field 'clearExpression'.
     * 
     * @return the value of field 'ClearExpression'.
     */
    public String getClearExpression() {
        return _clearExpression == null ? DEFAULT_CLEAR_EXPRESSION
                                       : _clearExpression;
    }

    /**
     * Returns the value of field 'escalateExpression'.
     * 
     * @return the value of field 'EscalateExpression'.
     */
    public String getEscalateExpression() {
        return _escalateExpression == null ? DEFAULT_ESCALATE_EXPRESSION
                                          : _escalateExpression;
    }

    /**
     * Returns the value of field 'notifyidMatchExpression'.
     * 
     * @return the value of field 'NotifyidMatchExpression'.
     */
    public String getNotifyidMatchExpression() {
        return _notifyidMatchExpression == null ? DEFAULT_NOTIFYID_MATCH_EXPRESSION
                                               : _notifyidMatchExpression;
    }

    /**
     * Returns the value of field 'readers'. The field 'readers' has the
     * following description: Location for user to define readers and they're
     * schedules.
     * 
     * 
     * @return the value of field 'Readers'.
     */
    public Readers getReaders() {
        return this._readers;
    }

    /**
     * Returns the value of field 'unackExpression'.
     * 
     * @return the value of field 'UnackExpression'.
     */
    public String getUnackExpression() {
        return _unackExpression == null ? DEFAULT_UNACK_EXPRESSION
                                       : _unackExpression;
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming Language
     * Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_ackExpression == null) ? 0 : _ackExpression.hashCode());
        result = prime * result
                + ((_alarmSync == null) ? 0 : _alarmSync.hashCode());
        result = prime
                * result
                + ((_alarmidMatchExpression == null) ? 0
                                                    : _alarmidMatchExpression.hashCode());
        result = prime
                * result
                + ((_clearExpression == null) ? 0
                                             : _clearExpression.hashCode());
        result = prime
                * result
                + ((_escalateExpression == null) ? 0
                                                : _escalateExpression.hashCode());
        result = prime
                * result
                + ((_notifyidMatchExpression == null) ? 0
                                                     : _notifyidMatchExpression.hashCode());
        result = prime * result
                + ((_readers == null) ? 0 : _readers.hashCode());
        result = prime
                * result
                + ((_unackExpression == null) ? 0
                                             : _unackExpression.hashCode());
        return result;
    }

    /**
     * Returns the value of field 'alarmSync'.
     * 
     * @return the value of field 'AlarmSync'.
     */
    public boolean isAlarmSync() {
        return this._alarmSync;
    }

    /**
     * Sets the value of field 'ackExpression'.
     * 
     * @param ackExpression
     *            the value of field 'ackExpression'.
     */
    public void setAckExpression(final String ackExpression) {
        this._ackExpression = ackExpression;
    }

    /**
     * Sets the value of field 'alarmSync'.
     * 
     * @param alarmSync
     *            the value of field 'alarmSync'.
     */
    public void setAlarmSync(final boolean alarmSync) {
        this._alarmSync = alarmSync;
    }

    /**
     * Sets the value of field 'alarmidMatchExpression'.
     * 
     * @param alarmidMatchExpression
     *            the value of field 'alarmidMatchExpression'.
     */
    public void setAlarmidMatchExpression(final String alarmidMatchExpression) {
        this._alarmidMatchExpression = alarmidMatchExpression;
    }

    /**
     * Sets the value of field 'clearExpression'.
     * 
     * @param clearExpression
     *            the value of field 'clearExpression'.
     */
    public void setClearExpression(final String clearExpression) {
        this._clearExpression = clearExpression;
    }

    /**
     * Sets the value of field 'escalateExpression'.
     * 
     * @param escalateExpression
     *            the value of field 'escalateExpression'.
     */
    public void setEscalateExpression(final String escalateExpression) {
        this._escalateExpression = escalateExpression;
    }

    /**
     * Sets the value of field 'notifyidMatchExpression'.
     * 
     * @param notifyidMatchExpression
     *            the value of field 'notifyidMatchExpression'.
     */
    public void setNotifyidMatchExpression(
            final String notifyidMatchExpression) {
        this._notifyidMatchExpression = notifyidMatchExpression;
    }

    /**
     * Sets the value of field 'readers'. The field 'readers' has the
     * following description: Location for user to define readers and they're
     * schedules.
     * 
     * 
     * @param readers
     *            the value of field 'readers'.
     */
    public void setReaders(final Readers readers) {
        this._readers = readers;
    }

    /**
     * Sets the value of field 'unackExpression'.
     * 
     * @param unackExpression
     *            the value of field 'unackExpression'.
     */
    public void setUnackExpression(final String unackExpression) {
        this._unackExpression = unackExpression;
    }
}
