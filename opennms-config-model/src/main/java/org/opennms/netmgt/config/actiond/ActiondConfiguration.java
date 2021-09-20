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

package org.opennms.netmgt.config.actiond;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

/**
 * Top-level element for the actiond-configuration.xml configuration file.
 */
@XmlRootElement(name = "actiond-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("actiond-configuration.xsd")
public class ActiondConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

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

    public ActiondConfiguration() {
    }

    public ActiondConfiguration(final Integer maxOutstandingActions, final Long maxProcessTime) {
        setMaxOutstandingActions(maxOutstandingActions);
        setMaxProcessTime(maxProcessTime);
    }

    public Integer getMaxOutstandingActions() {
        return m_maxOutstandingActions == null ? 10 : m_maxOutstandingActions;
    }

    public void setMaxOutstandingActions(final Integer maxOutstandingActions) {
        m_maxOutstandingActions = maxOutstandingActions;
    }

    public Long getMaxProcessTime() {
        return m_maxProcessTime == null ? 120000 : m_maxProcessTime;
    }

    public void setMaxProcessTime(final Long maxProcessTime) {
        m_maxProcessTime = maxProcessTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_maxOutstandingActions, m_maxProcessTime);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj instanceof ActiondConfiguration) {
            final ActiondConfiguration that = (ActiondConfiguration) obj;
            return Objects.equals(this.m_maxOutstandingActions, that.m_maxOutstandingActions) &&
                    Objects.equals(this.m_maxProcessTime, that.m_maxProcessTime);
        }
        return false;
    }
}
