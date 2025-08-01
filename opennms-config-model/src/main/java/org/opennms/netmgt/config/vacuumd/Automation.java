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
package org.opennms.netmgt.config.vacuumd;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "automation")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("vacuumd-configuration.xsd")
public class Automation implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final Boolean DEFAULT_ACTIVE_FLAG = Boolean.TRUE;

    /**
     * The name of this automation
     */
    @XmlAttribute(name = "name", required = true)
    private String m_name;

    /**
     * How often should this automation run
     */
    @XmlAttribute(name = "interval", required = true)
    private Integer m_interval;

    /**
     * reference a trigger from the collection of triggers
     */
    @XmlAttribute(name = "trigger-name")
    private String m_triggerName;

    /**
     * reference an action from the collection of actions
     */
    @XmlAttribute(name = "action-name", required = true)
    private String m_actionName;

    /**
     * specify an event UEI to send
     */
    @XmlAttribute(name = "auto-event-name")
    private String m_autoEventName;

    /**
     * Create an Event from Result of Trigger
     */
    @XmlAttribute(name = "action-event")
    private String m_actionEvent;

    /**
     * enable/disable this automation
     */
    @XmlAttribute(name = "active")
    private Boolean m_active;

    public Automation() {
    }

    public Automation(final String name, final Integer interval,
        final String triggerName, final String actionName,
        final String autoEventName, final String actionEvent,
        final Boolean active) {
        setName(name);
        setInterval(interval);
        setTriggerName(triggerName);
        setActionName(actionName);
        setAutoEventName(autoEventName);
        setActionEvent(actionEvent);
        setActive(active);
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public Integer getInterval() {
        return m_interval;
    }

    public void setInterval(final Integer interval) {
        m_interval = interval;
    }

    public Optional<String> getTriggerName() {
        return Optional.ofNullable(m_triggerName);
    }

    public void setTriggerName(final String triggerName) {
        m_triggerName = ConfigUtils.normalizeString(triggerName);
    }

    public String getActionName() {
        return m_actionName;
    }

    public void setActionName(final String actionName) {
        m_actionName = ConfigUtils.assertNotEmpty(actionName, "action-name");
    }

    public Optional<String> getAutoEventName() {
        return Optional.ofNullable(m_autoEventName);
    }

    public void setAutoEventName(final String autoEventName) {
        m_autoEventName = ConfigUtils.normalizeString(autoEventName);
    }

    public Optional<String> getActionEvent() {
        return Optional.ofNullable(m_actionEvent);
    }

    public void setActionEvent(final String actionEvent) {
        m_actionEvent = ConfigUtils.normalizeString(actionEvent);
    }

    public Boolean getActive() {
        return m_active == null ? DEFAULT_ACTIVE_FLAG : m_active;
    }

    public void setActive(final Boolean active) {
        m_active = active;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name,
                            m_interval,
                            m_triggerName,
                            m_actionName,
                            m_autoEventName,
                            m_actionEvent,
                            m_active);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Automation) {
            final Automation that = (Automation) obj;
            return Objects.equals(this.m_name, that.m_name) &&
                    Objects.equals(this.m_interval, that.m_interval) &&
                    Objects.equals(this.m_triggerName, that.m_triggerName) &&
                    Objects.equals(this.m_actionName, that.m_actionName) &&
                    Objects.equals(this.m_autoEventName, that.m_autoEventName) &&
                    Objects.equals(this.m_actionEvent, that.m_actionEvent) &&
                    Objects.equals(this.m_active, that.m_active);
        }
        return false;
    }
}
