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
