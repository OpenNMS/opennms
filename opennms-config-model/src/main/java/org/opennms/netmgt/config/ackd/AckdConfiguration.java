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

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * Field _alarmSync.
     */
    @XmlAttribute(name = "alarm-sync")
    private Boolean _alarmSync = true;

    /**
     * Field _ackExpression.
     */
    @XmlAttribute(name = "ack-expression")
    private String _ackExpression = "~(?i)^ack$";

    /**
     * Field _unackExpression.
     */
    @XmlAttribute(name = "unack-expression")
    private String _unackExpression = "~(?i)^unack$";

    /**
     * Field _escalateExpression.
     */
    @XmlAttribute(name = "escalate-expression")
    private String _escalateExpression = "~(?i)^esc$";

    /**
     * Field _clearExpression.
     */
    @XmlAttribute(name = "clear-expression")
    private String _clearExpression = "~(?i)^(resolve|clear)$";

    /**
     * Field _notifyidMatchExpression.
     */
    @XmlAttribute(name = "notifyid-match-expression")
    private String _notifyidMatchExpression = "~(?i).*Re:.*Notice #([0-9]+).*";

    /**
     * Field _alarmidMatchExpression.
     */
    @XmlAttribute(name = "alarmid-match-expression")
    private String _alarmidMatchExpression = "~(?i).*alarmid:([0-9]+).*";

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
    @Override()
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof AckdConfiguration) {

            AckdConfiguration temp = (AckdConfiguration) obj;
            if (this._alarmSync != temp._alarmSync)
                return false;
            if (this._ackExpression != null) {
                if (temp._ackExpression == null)
                    return false;
                else if (!(this._ackExpression.equals(temp._ackExpression)))
                    return false;
            } else if (temp._ackExpression != null)
                return false;
            if (this._unackExpression != null) {
                if (temp._unackExpression == null)
                    return false;
                else if (!(this._unackExpression.equals(temp._unackExpression)))
                    return false;
            } else if (temp._unackExpression != null)
                return false;
            if (this._escalateExpression != null) {
                if (temp._escalateExpression == null)
                    return false;
                else if (!(this._escalateExpression.equals(temp._escalateExpression)))
                    return false;
            } else if (temp._escalateExpression != null)
                return false;
            if (this._clearExpression != null) {
                if (temp._clearExpression == null)
                    return false;
                else if (!(this._clearExpression.equals(temp._clearExpression)))
                    return false;
            } else if (temp._clearExpression != null)
                return false;
            if (this._notifyidMatchExpression != null) {
                if (temp._notifyidMatchExpression == null)
                    return false;
                else if (!(this._notifyidMatchExpression.equals(temp._notifyidMatchExpression)))
                    return false;
            } else if (temp._notifyidMatchExpression != null)
                return false;
            if (this._alarmidMatchExpression != null) {
                if (temp._alarmidMatchExpression == null)
                    return false;
                else if (!(this._alarmidMatchExpression.equals(temp._alarmidMatchExpression)))
                    return false;
            } else if (temp._alarmidMatchExpression != null)
                return false;
            if (this._readers != null) {
                if (temp._readers == null)
                    return false;
                else if (!(this._readers.equals(temp._readers)))
                    return false;
            } else if (temp._readers != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'ackExpression'.
     * 
     * @return the value of field 'AckExpression'.
     */
    public String getAckExpression() {
        return this._ackExpression;
    }

    /**
     * Returns the value of field 'alarmSync'.
     * 
     * @return the value of field 'AlarmSync'.
     */
    public boolean getAlarmSync() {
        return this._alarmSync;
    }

    /**
     * Returns the value of field 'alarmidMatchExpression'.
     * 
     * @return the value of field 'AlarmidMatchExpression'.
     */
    public String getAlarmidMatchExpression() {
        return this._alarmidMatchExpression;
    }

    /**
     * Returns the value of field 'clearExpression'.
     * 
     * @return the value of field 'ClearExpression'.
     */
    public String getClearExpression() {
        return this._clearExpression;
    }

    /**
     * Returns the value of field 'escalateExpression'.
     * 
     * @return the value of field 'EscalateExpression'.
     */
    public String getEscalateExpression() {
        return this._escalateExpression;
    }

    /**
     * Returns the value of field 'notifyidMatchExpression'.
     * 
     * @return the value of field 'NotifyidMatchExpression'.
     */
    public String getNotifyidMatchExpression() {
        return this._notifyidMatchExpression;
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
        return this._unackExpression;
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming Language
     * Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int result = 17;

        result = 37 * result + (_alarmSync ? 0 : 1);
        if (_ackExpression != null) {
            result = 37 * result + _ackExpression.hashCode();
        }
        if (_unackExpression != null) {
            result = 37 * result + _unackExpression.hashCode();
        }
        if (_escalateExpression != null) {
            result = 37 * result + _escalateExpression.hashCode();
        }
        if (_clearExpression != null) {
            result = 37 * result + _clearExpression.hashCode();
        }
        if (_notifyidMatchExpression != null) {
            result = 37 * result + _notifyidMatchExpression.hashCode();
        }
        if (_alarmidMatchExpression != null) {
            result = 37 * result + _alarmidMatchExpression.hashCode();
        }
        if (_readers != null) {
            result = 37 * result + _readers.hashCode();
        }

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
