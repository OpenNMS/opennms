/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import org.opennms.core.xml.ValidateUsing;

/**
 * Top-level element for the actiond-configuration.xml configuration file.
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "actiond-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("actiond-configuration.xsd")
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
    private Integer m_maxOutstandingActions;

    /**
     * The maximum time that a launched process can take to complete. If
     * execution time exceeds this time, the launched process is terminated.
     */
    @XmlAttribute(name = "max-process-time")
    private Long m_maxProcessTime;

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
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        final ActiondConfiguration other = (ActiondConfiguration) obj;
        if (m_maxOutstandingActions == null) {
            if (other.m_maxOutstandingActions != null) {
                return false;
            }
        } else if (!m_maxOutstandingActions.equals(other.m_maxOutstandingActions)) {
            return false;
        }
        if (m_maxProcessTime == null) {
            if (other.m_maxProcessTime != null) {
                return false;
            }
        } else if (!m_maxProcessTime.equals(other.m_maxProcessTime)) {
            return false;
        }
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
    public Integer getMaxOutstandingActions() {
        return m_maxOutstandingActions == null ? 10 : m_maxOutstandingActions;
    }

    /**
     * Returns the value of field 'maxProcessTime'. The field 'maxProcessTime'
     * has the following description: The maximum time that a launched process
     * can take to complete. If execution time exceeds this time, the launched
     * process is terminated.
     *
     * @return the value of field 'MaxProcessTime'.
     */
    public Long getMaxProcessTime() {
        return m_maxProcessTime == null ? 120000 : m_maxProcessTime;
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
        result = prime * result + ((m_maxOutstandingActions == null) ? 0 : m_maxOutstandingActions.hashCode());
        result = prime * result + ((m_maxProcessTime == null) ? 0 : m_maxProcessTime.hashCode());
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
    public void setMaxOutstandingActions(final Integer maxOutstandingActions) {
        m_maxOutstandingActions = maxOutstandingActions;
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
    public void setMaxProcessTime(final Long maxProcessTime) {
        m_maxProcessTime = maxProcessTime;
    }
}
