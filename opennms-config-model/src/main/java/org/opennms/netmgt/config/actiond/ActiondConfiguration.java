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
package org.opennms.netmgt.config.actiond;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top-level element for the actiond-configuration.xml configuration file.
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "actiond-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class ActiondConfiguration implements Serializable {
    private static final long serialVersionUID = 1978139055248268440L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * The maximum number of simultaneous processes launched by actiond. If
     * the number of launched processes currently running reaches this number,
     * actiond waits for a process to complete or get terminated before it
     * launches the next process.
     */
    @XmlAttribute(name = "max-outstanding-actions")
    private Integer _maxOutstandingActions;

    /**
     * The maximum time that a launched process can take to complete. If
     * execution time exceeds this time, the launched process is terminated.
     */
    @XmlAttribute(name = "max-process-time")
    private Long _maxProcessTime;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public ActiondConfiguration() {
        super();
    }

    public ActiondConfiguration(int maxOutstandingActions, long maxProcessTime) {
        super();
        setMaxOutstandingActions(maxOutstandingActions);
        setMaxProcessTime(maxProcessTime);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * Overrides the java.lang.Object.equals method.
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
        ActiondConfiguration other = (ActiondConfiguration) obj;
        if (_maxOutstandingActions == null) {
            if (other._maxOutstandingActions != null)
                return false;
        } else if (!_maxOutstandingActions.equals(other._maxOutstandingActions))
            return false;
        if (_maxProcessTime == null) {
            if (other._maxProcessTime != null)
                return false;
        } else if (!_maxProcessTime.equals(other._maxProcessTime))
            return false;
        return true;
    }

    /**
     * Returns the value of field 'maxOutstandingActions'. The field
     * 'maxOutstandingActions' has the following description: The maximum
     * number of simultaneous processes launched by actiond. If the number of
     * launched processes currently running reaches this number, actiond waits
     * for a process to complete or get terminated before it launches the next
     * process.
     *
     * @return the value of field 'MaxOutstandingActions'.
     */
    public int getMaxOutstandingActions() {
        return _maxOutstandingActions == null ? 10 : _maxOutstandingActions;
    }

    /**
     * Returns the value of field 'maxProcessTime'. The field 'maxProcessTime'
     * has the following description: The maximum time that a launched process
     * can take to complete. If execution time exceeds this time, the launched
     * process is terminated.
     *
     * @return the value of field 'MaxProcessTime'.
     */
    public long getMaxProcessTime() {
        return _maxProcessTime == null ? 120000 : _maxProcessTime;
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
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
        result = prime
                * result
                + ((_maxOutstandingActions == null) ? 0
                                                   : _maxOutstandingActions.hashCode());
        result = prime
                * result
                + ((_maxProcessTime == null) ? 0 : _maxProcessTime.hashCode());
        return result;
    }

    /**
     * Sets the value of field 'maxOutstandingActions'. The field
     * 'maxOutstandingActions' has the following description: The maximum
     * number of simultaneous processes launched by actiond. If the number of
     * launched processes currently running reaches this number, actiond waits
     * for a process to complete or get terminated before it launches the next
     * process.
     *
     * @param maxOutstandingActions
     *            the value of field 'maxOutstandingActions'.
     */
    public void setMaxOutstandingActions(final int maxOutstandingActions) {
        this._maxOutstandingActions = maxOutstandingActions;
    }

    /**
     * Sets the value of field 'maxProcessTime'. The field 'maxProcessTime'
     * has the following description: The maximum time that a launched process
     * can take to complete. If execution time exceeds this time, the launched
     * process is terminated.
     *
     * @param maxProcessTime
     *            the value of field 'maxProcessTime'.
     */
    public void setMaxProcessTime(final long maxProcessTime) {
        this._maxProcessTime = maxProcessTime;
    }
}
